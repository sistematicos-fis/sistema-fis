package co.edu.javeriana.concesionario.inventario.logica;

import co.edu.javeriana.concesionario.comun.util.ValidadorUtil;
import co.edu.javeriana.concesionario.financiacion.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.inventario.modelo.ConteoFisico;
import co.edu.javeriana.concesionario.inventario.modelo.EstadoVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.IncidenciaInventario;
import co.edu.javeriana.concesionario.inventario.modelo.ResultadoVerificacion;
import co.edu.javeriana.concesionario.inventario.modelo.VerificacionInventario;
import co.edu.javeriana.concesionario.inventario.modelo.Vehiculo;
import co.edu.javeriana.concesionario.inventario.persistencia.VehiculoDAO;
import co.edu.javeriana.concesionario.inventario.persistencia.VerificacionInventarioDAO;
import co.edu.javeriana.concesionario.inventario.persistencia.VerificacionInventarioRepositorio;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CU-10 — Controlar existencias en tiempo real para ventas (No CRUD, no trivial).
 *
 * Antes de confirmar una venta, contrasta tres fuentes que pueden contradecirse:
 * el estado registrado del vehículo, las operaciones vigentes de otros módulos
 * (alquiler) y el último conteo físico de la bodega. Si las tres concuerdan,
 * bloquea temporalmente la unidad para la venta en curso. Si no, deja constancia
 * del motivo y, cuando el desacuerdo es entre el físico y el sistema, abre una
 * incidencia que solo el Administrador de Bodega puede cerrar.
 *
 * Reglas de negocio:
 * <ul>
 *   <li>RN-10-01: el vehículo debe existir en el catálogo.</li>
 *   <li>RN-10-02: sin conteo físico previo no se puede confirmar disponibilidad.</li>
 *   <li>RN-10-03: un alquiler RESERVADO o EN_CURSO impide la venta (flujo alterno 3A).</li>
 *   <li>RN-10-04: solo un vehículo DISPONIBLE puede pasar a venta.</li>
 *   <li>RN-10-05: si el físico no coincide con el sistema se abre incidencia y la
 *       unidad pasa a EN_VERIFICACION (flujo alterno 4A).</li>
 *   <li>RN-10-06: con una incidencia abierta no se confirma nada hasta resolverla.</li>
 *   <li>RN-10-07: la confirmación deja la unidad RESERVADA con un bloqueo vigente
 *       durante {@value #MINUTOS_BLOQUEO_VENTA} minutos.</li>
 *   <li>RN-10-08: toda verificación queda registrada, sea cual sea su resultado.</li>
 *   <li>RN-10-09: el resultado, la incidencia y el cambio de estado se escriben en
 *       una sola transacción (RF-28).</li>
 * </ul>
 *
 * Responsable: Juan Pablo León
 */
public class VerificacionInventarioServicio {

    /** Duración del bloqueo temporal que deja una verificación favorable (RN-10-07). */
    public static final int MINUTOS_BLOQUEO_VENTA = 30;

    private final VehiculoDAO vehiculoDAO;
    private final VerificacionInventarioRepositorio repositorio;

    public VerificacionInventarioServicio() {
        this(new VehiculoDAO(), new VerificacionInventarioDAO());
    }

    public VerificacionInventarioServicio(VehiculoDAO vehiculoDAO,
                                          VerificacionInventarioRepositorio repositorio) {
        this.vehiculoDAO = vehiculoDAO;
        this.repositorio = repositorio;
    }

    // ------------------------------------------------------------------
    // Conteo físico (alimenta la precondición del caso de uso)
    // ------------------------------------------------------------------

    /**
     * Deja constancia de lo que el personal de bodega vio en el piso. Es lo que
     * después se compara contra el sistema en el paso 4 del flujo básico.
     *
     * @param presente false cuando la unidad no apareció en la bodega.
     */
    public ConteoFisico registrarConteoFisico(Long vehiculoId, EstadoVehiculo estadoFisico, boolean presente,
                                              String responsable, String observaciones) throws SQLException {
        ValidadorUtil.validarRequerido(responsable, "Responsable del conteo");
        Vehiculo vehiculo = obtenerVehiculo(vehiculoId);

        if (estadoFisico == null) {
            throw new ReglaNegocioException("No se pudo registrar el conteo: falta el estado físico observado.");
        }

        ConteoFisico conteo = new ConteoFisico();
        conteo.setVehiculoId(vehiculo.getId());
        conteo.setBodegaId(vehiculo.getBodegaId());
        conteo.setEstadoFisico(estadoFisico);
        conteo.setPresente(presente);
        conteo.setResponsable(responsable.trim());
        conteo.setObservaciones(observaciones != null ? observaciones.trim() : null);
        return repositorio.registrarConteo(conteo);
    }

    public Optional<ConteoFisico> ultimoConteo(Long vehiculoId) throws SQLException {
        return vehiculoId == null ? Optional.empty() : repositorio.ultimoConteo(vehiculoId);
    }

    // ------------------------------------------------------------------
    // Flujo principal del CU-10
    // ------------------------------------------------------------------

    /**
     * Ejecuta la verificación de existencias solicitada por el asesor comercial.
     * Nunca lanza excepción por un resultado desfavorable: devuelve el veredicto con
     * su motivo, porque el caso de uso exige informar al asesor en todos los casos.
     * Solo lanza {@link ReglaNegocioException} si los datos de entrada no son válidos.
     */
    public VerificacionInventario verificarDisponibilidadParaVenta(Long vehiculoId,
                                                                   String solicitadoPor) throws SQLException {
        ValidadorUtil.validarRequerido(solicitadoPor, "Solicitante de la verificación");
        Vehiculo vehiculo = obtenerVehiculo(vehiculoId);
        String solicitante = solicitadoPor.trim();

        EstadoVehiculo estadoSistema = EstadoVehiculo.desdeTexto(vehiculo.getEstado());

        // RN-10-06: una incidencia sin resolver congela cualquier verificación posterior.
        Optional<IncidenciaInventario> pendiente = repositorio.incidenciaAbierta(vehiculo.getId());
        if (pendiente.isPresent()) {
            return registrar(vehiculo, ResultadoVerificacion.INCIDENCIA_PENDIENTE,
                    "El vehículo tiene la incidencia " + pendiente.get().getId()
                            + " sin resolver; no puede confirmarse para venta.",
                    estadoSistema, pendiente.get().getEstadoFisico(), solicitante, null, null);
        }

        // RN-10-03 / flujo alterno 3A: reservas o alquileres vigentes.
        if (repositorio.tieneOperacionActiva(vehiculo.getId())) {
            return registrar(vehiculo, ResultadoVerificacion.OPERACION_ACTIVA,
                    "El vehículo tiene un contrato de alquiler vigente y no está disponible para venta.",
                    estadoSistema, null, solicitante, null, null);
        }

        // RN-10-04: el estado registrado debe permitir la venta.
        if (estadoSistema == null || !estadoSistema.esVendible()) {
            return registrar(vehiculo, ResultadoVerificacion.ESTADO_NO_VENDIBLE,
                    "El estado registrado del vehículo es " + vehiculo.getEstado()
                            + ", que no permite iniciar una venta.",
                    estadoSistema, null, solicitante, null, null);
        }

        // RN-10-02: sin conteo físico no hay nada contra qué comparar.
        Optional<ConteoFisico> conteo = repositorio.ultimoConteo(vehiculo.getId());
        if (conteo.isEmpty()) {
            return registrar(vehiculo, ResultadoVerificacion.SIN_CONTEO_FISICO,
                    "No hay conteo físico registrado para el vehículo; la bodega debe verificarlo primero.",
                    estadoSistema, null, solicitante, null, null);
        }

        ConteoFisico ultimo = conteo.get();
        EstadoVehiculo estadoFisico = ultimo.getEstadoFisico();

        // RN-10-05 / flujo alterno 4A: el físico contradice al sistema.
        String discrepancia = describirDiscrepancia(ultimo, estadoSistema);
        if (discrepancia != null) {
            IncidenciaInventario incidencia = new IncidenciaInventario(
                    vehiculo.getId(), estadoSistema, estadoFisico, discrepancia, solicitante);
            return registrar(vehiculo, ResultadoVerificacion.DISCREPANCIA_DETECTADA, discrepancia,
                    estadoSistema, estadoFisico, solicitante, EstadoVehiculo.EN_VERIFICACION, incidencia);
        }

        // RN-10-07: todo concuerda, se bloquea la unidad para la venta en curso.
        LocalDateTime expira = LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO_VENTA);
        return registrar(vehiculo, ResultadoVerificacion.DISPONIBLE_CONFIRMADO,
                "Inventario físico y sistema concuerdan; unidad bloqueada por "
                        + MINUTOS_BLOQUEO_VENTA + " minutos para la venta en curso.",
                estadoSistema, estadoFisico, solicitante, EstadoVehiculo.RESERVADO, null, expira);
    }

    /**
     * Compara el conteo físico con el estado del sistema.
     *
     * @return la descripción de la diferencia, o null si concuerdan.
     */
    String describirDiscrepancia(ConteoFisico conteo, EstadoVehiculo estadoSistema) {
        if (!conteo.isPresente()) {
            return "El vehículo figura como " + estadoSistema
                    + " en el sistema pero no fue encontrado físicamente en la bodega.";
        }
        if (conteo.getEstadoFisico() != estadoSistema) {
            return "El sistema registra el vehículo como " + estadoSistema
                    + " y el último conteo físico lo reporta como " + conteo.getEstadoFisico() + ".";
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Gestión de incidencias
    // ------------------------------------------------------------------

    public List<IncidenciaInventario> listarIncidenciasAbiertas() throws SQLException {
        return repositorio.listarIncidenciasAbiertas();
    }

    /**
     * Cierra una incidencia dejando el vehículo en el estado que el Administrador de
     * Bodega confirma como cierto. Mientras no se haga esto, la unidad sigue
     * EN_VERIFICACION y ninguna verificación posterior puede confirmarla (RN-10-06).
     */
    public void resolverIncidencia(Long incidenciaId, EstadoVehiculo estadoConfirmado,
                                   String resueltaPor, String observacion) throws SQLException {
        ValidadorUtil.validarRequerido(resueltaPor, "Responsable de la resolución");

        if (incidenciaId == null) {
            throw new ReglaNegocioException("No se pudo resolver: no se indicó la incidencia.");
        }
        if (estadoConfirmado == null) {
            throw new ReglaNegocioException(
                    "No se pudo resolver: debe confirmarse el estado real del vehículo.");
        }

        IncidenciaInventario incidencia = repositorio.buscarIncidencia(incidenciaId)
                .orElseThrow(() -> new ReglaNegocioException("No se pudo resolver: la incidencia no existe."));
        if (!incidencia.estaAbierta()) {
            throw new ReglaNegocioException("La incidencia " + incidenciaId + " ya fue resuelta.");
        }

        repositorio.resolverIncidencia(incidenciaId, estadoConfirmado, resueltaPor.trim(), observacion);
    }

    // ------------------------------------------------------------------
    // Bloqueo temporal de venta (RN-10-07)
    // ------------------------------------------------------------------

    /**
     * Devuelve la unidad a DISPONIBLE cuando la venta no se concretó y el asesor
     * libera la reserva antes de que venza.
     */
    public void liberarBloqueo(Long vehiculoId) throws SQLException {
        Vehiculo vehiculo = obtenerVehiculo(vehiculoId);
        if (!repositorio.liberarBloqueo(vehiculo.getId())) {
            throw new ReglaNegocioException("No se pudo liberar: el vehículo está en estado "
                    + vehiculo.getEstado() + " y solo se puede liberar una unidad RESERVADA.");
        }
    }

    /**
     * Libera las unidades cuyo bloqueo ya venció. Es lo que hace temporal al bloqueo
     * de RN-10-07: sin esto la unidad quedaría reservada indefinidamente. La vista lo
     * llama al abrir la pestaña y al refrescar la flota.
     *
     * @return cuántas unidades volvieron a DISPONIBLE.
     */
    public int liberarBloqueosVencidos() throws SQLException {
        return repositorio.liberarBloqueosVencidos();
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /** Historial de verificaciones; con vehiculoId en null devuelve todas. */
    public List<VerificacionInventario> historial(Long vehiculoId) throws SQLException {
        return repositorio.listarVerificaciones(vehiculoId);
    }

    // ------------------------------------------------------------------
    // Apoyo interno
    // ------------------------------------------------------------------

    /** RN-10-01. */
    private Vehiculo obtenerVehiculo(Long vehiculoId) throws SQLException {
        if (vehiculoId == null) {
            throw new ReglaNegocioException("No se indicó el vehículo a verificar.");
        }
        return vehiculoDAO.buscarPorId(vehiculoId)
                .orElseThrow(() -> new ReglaNegocioException(
                        "El vehículo " + vehiculoId + " no existe en el catálogo de inventario."));
    }

    private VerificacionInventario registrar(Vehiculo vehiculo, ResultadoVerificacion resultado, String motivo,
                                             EstadoVehiculo estadoSistema, EstadoVehiculo estadoFisico,
                                             String solicitante, EstadoVehiculo nuevoEstado,
                                             IncidenciaInventario incidencia) throws SQLException {
        return registrar(vehiculo, resultado, motivo, estadoSistema, estadoFisico,
                solicitante, nuevoEstado, incidencia, null);
    }

    private VerificacionInventario registrar(Vehiculo vehiculo, ResultadoVerificacion resultado, String motivo,
                                             EstadoVehiculo estadoSistema, EstadoVehiculo estadoFisico,
                                             String solicitante, EstadoVehiculo nuevoEstado,
                                             IncidenciaInventario incidencia,
                                             LocalDateTime bloqueoExpira) throws SQLException {
        VerificacionInventario verificacion = new VerificacionInventario(
                vehiculo.getId(), resultado, motivo, estadoSistema, estadoFisico, solicitante);
        verificacion.setBloqueoExpira(bloqueoExpira);
        verificacion.setIncidencia(incidencia);

        // RN-10-08 y RN-10-09: el registro, la incidencia y el cambio de estado van juntos.
        return repositorio.registrarResultado(verificacion, nuevoEstado);
    }
}
