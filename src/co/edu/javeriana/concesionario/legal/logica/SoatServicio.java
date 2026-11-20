package co.edu.javeriana.concesionario.legal.logica;

import co.edu.javeriana.concesionario.comun.simuladores.AseguradoraExterna;
import co.edu.javeriana.concesionario.comun.simuladores.AseguradoraExterna.ConsultaRestricciones;
import co.edu.javeriana.concesionario.comun.simuladores.AseguradoraExterna.RespuestaEmision;
import co.edu.javeriana.concesionario.comun.simuladores.AseguradoraExterna.SolicitudEmisionSoat;
import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.legal.modelo.Aseguradora;
import co.edu.javeriana.concesionario.legal.modelo.EstadoDocumental;
import co.edu.javeriana.concesionario.legal.modelo.Matricula;
import co.edu.javeriana.concesionario.legal.modelo.RegistroDocumental;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRenovacion;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRenovacionSoat;
import co.edu.javeriana.concesionario.legal.modelo.Soat;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;
import co.edu.javeriana.concesionario.legal.persistencia.AseguradoraRepositorio;
import co.edu.javeriana.concesionario.legal.persistencia.SoatRepositorio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * CU-24: validar y renovar el SOAT de un vehículo (no CRUD).
 *
 * Flujo básico (SRS, sección 9):
 * 2. El analista da la placa y la aseguradora.
 * 3. Se consulta el SOAT del vehículo y su vencimiento.
 * 4. Se valida ante la aseguradora (simulada) que no haya siniestros pendientes ni bloqueos.
 * 5. Se calcula el valor según el tipo de vehículo y el cilindraje ({@link TarifarioSoat}).
 * 6. Se solicita la emisión a la aseguradora (simulada).
 * 7. Se registra el nuevo SOAT y el vehículo queda con "Documentación al día".
 * 8. Se notifica el resultado ({@link ResultadoRenovacionSoat}).
 *
 * Flujos alternos: 3A SOAT aún vigente, 4A siniestros o bloqueos, 6A rechazo de la aseguradora.
 *
 * Supuestos:
 * - La renovación se habilita en los 30 días previos al vencimiento o cuando ya venció.
 * - El nuevo SOAT empieza el día siguiente al vencimiento del actual (o hoy, si ya venció),
 *   para que no queden días sin cobertura ni días pagados dos veces, y dura un año.
 * - Una matrícula suspendida o cancelada (CU-21) es un bloqueo administrativo.
 */
public class SoatServicio {
    static final int DIAS_ALERTA_RENOVACION = 30;
    private static final Pattern PATRON_NUMERO = Pattern.compile("^[A-Z0-9-]{4,40}$");

    private final SoatRepositorio repositorio;
    private final AseguradoraRepositorio aseguradoras;
    private final AseguradoraExterna aseguradoraExterna;
    private final Clock reloj;

    public SoatServicio(SoatRepositorio repositorio, AseguradoraRepositorio aseguradoras,
                        AseguradoraExterna aseguradoraExterna) {
        this(repositorio, aseguradoras, aseguradoraExterna, Clock.systemDefaultZone());
    }

    public SoatServicio(SoatRepositorio repositorio, AseguradoraRepositorio aseguradoras,
                        AseguradoraExterna aseguradoraExterna, Clock reloj) {
        this.repositorio = repositorio;
        this.aseguradoras = aseguradoras;
        this.aseguradoraExterna = aseguradoraExterna;
        this.reloj = reloj;
    }

    /** Aseguradoras activas cuyo convenio incluye SOAT (precondición del CU-24). */
    public List<Aseguradora> listarAseguradorasSoat() throws SQLException {
        return aseguradoras.listarActivas().stream().filter(Aseguradora::isCubreSoat).toList();
    }

    public List<Aseguradora> listarAseguradoras() throws SQLException {
        return aseguradoras.listarTodas();
    }

    /** Vehículo matriculado con esa placa; lanza ReglaNegocioException si no existe. */
    public VehiculoLegal buscarVehiculoPorPlaca(String placa) throws SQLException {
        return buscarMatriculado(placa).vehiculo();
    }

    public List<Soat> listarSoats(Long vehiculoId) throws SQLException {
        return repositorio.listarPorVehiculo(vehiculoId);
    }

    /**
     * Estado documental que se muestra al consultar un vehículo. Si el último registro dice
     * "al día" pero el SOAT ya venció, se informa como pendiente.
     */
    public RegistroDocumental consultarEstadoDocumental(Long vehiculoId) throws SQLException {
        LocalDate hoy = hoy();
        List<Soat> soats = repositorio.listarPorVehiculo(vehiculoId);
        boolean vigenteHoy = soats.stream().anyMatch(s -> s.estaVigente(hoy));
        Optional<RegistroDocumental> guardado = repositorio.buscarEstadoDocumental(vehiculoId);

        if (guardado.isPresent() && (guardado.get().estado() == EstadoDocumental.PENDIENTE || vigenteHoy)) {
            return guardado.get();
        }
        if (vigenteHoy) {
            return new RegistroDocumental(vehiculoId, EstadoDocumental.AL_DIA, "SOAT vigente.", null);
        }
        String motivo = soats.isEmpty()
                ? "El vehículo no tiene SOAT registrado."
                : "El SOAT venció el " + FormatoUtil.formatoFecha(soats.get(0).getFechaVencimiento()) + ".";
        return new RegistroDocumental(vehiculoId, EstadoDocumental.PENDIENTE, motivo, null);
    }

    /**
     * Registra un SOAT que el vehículo ya tenía, expedido por fuera del sistema (por ejemplo, al comprar
     * el vehículo). Es lo que permite cumplir la precondición del CU-24 para vehículos nuevos.
     */
    public Soat registrarSoatExistente(String placa, String numeroPoliza, Long aseguradoraId,
                                       LocalDate fechaInicio, BigDecimal valorPagado) throws SQLException {
        String prefijo = "No se pudo registrar el SOAT: ";
        VehiculoMatriculado matriculado = buscarMatriculado(placa);
        if (numeroPoliza == null || numeroPoliza.trim().isEmpty()) {
            throw new ReglaNegocioException(prefijo + "el N° de póliza es obligatorio.");
        }
        String numero = numeroPoliza.trim().toUpperCase();
        if (!PATRON_NUMERO.matcher(numero).matches()) {
            throw new ReglaNegocioException(prefijo + "el N° de póliza debe tener entre 4 y 40 letras, números o guiones.");
        }
        if (repositorio.buscarPorNumero(numero).isPresent()) {
            throw new ReglaNegocioException(prefijo + "ya existe un SOAT con el número " + numero + ".");
        }
        if (aseguradoraId == null) {
            throw new ReglaNegocioException(prefijo + "seleccione la aseguradora que lo expidió.");
        }
        Aseguradora aseguradora = aseguradoras.buscarPorId(aseguradoraId)
                .orElseThrow(() -> new ReglaNegocioException(prefijo + "la aseguradora seleccionada no existe."));
        if (!aseguradora.isCubreSoat()) {
            throw new ReglaNegocioException(prefijo + "el convenio con " + aseguradora.getNombre() + " no incluye SOAT.");
        }
        LocalDate hoy = hoy();
        if (fechaInicio == null) {
            throw new ReglaNegocioException(prefijo + "la Fecha de inicio de vigencia es obligatoria.");
        }
        if (fechaInicio.isAfter(hoy)) {
            throw new ReglaNegocioException(prefijo + "la Fecha de inicio no puede ser futura.");
        }
        if (valorPagado == null || valorPagado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException(prefijo + "el Valor pagado debe ser mayor que cero.");
        }

        Soat soat = new Soat(null, numero, matriculado.vehiculo().id(), aseguradoraId, fechaInicio,
                vencimientoDesde(fechaInicio), valorPagado, null);
        for (Soat otro : repositorio.listarPorVehiculo(soat.getVehiculoId())) {
            if (!otro.getFechaInicio().isAfter(soat.getFechaVencimiento()) && !otro.getFechaVencimiento().isBefore(fechaInicio)) {
                throw new ReglaNegocioException(prefijo + "su vigencia se cruza con el SOAT " + otro.getNumeroPoliza()
                        + " (" + FormatoUtil.formatoFecha(otro.getFechaInicio()) + " a "
                        + FormatoUtil.formatoFecha(otro.getFechaVencimiento()) + ").");
            }
        }
        boolean vigente = soat.estaVigente(hoy);
        return repositorio.guardarConEstadoDocumental(soat,
                vigente ? EstadoDocumental.AL_DIA : EstadoDocumental.PENDIENTE,
                vigente ? "SOAT " + numero + " registrado." : "SOAT " + numero + " registrado, pero ya está vencido.");
    }

    /** Ejecuta el CU-24 completo para la placa y la aseguradora elegidas por el analista. */
    public ResultadoRenovacionSoat validarYRenovar(String placa, Long aseguradoraId) throws SQLException {
        // Paso 2: datos del formulario y precondiciones
        VehiculoMatriculado matriculado = buscarMatriculado(placa);
        VehiculoLegal vehiculo = matriculado.vehiculo();
        Matricula matricula = matriculado.matricula();
        if (aseguradoraId == null) {
            throw new ReglaNegocioException("No se pudo validar el SOAT: seleccione la aseguradora.");
        }
        Aseguradora aseguradora = aseguradoras.buscarPorId(aseguradoraId)
                .orElseThrow(() -> new ReglaNegocioException("No se pudo validar el SOAT: la aseguradora seleccionada no existe."));
        if (!aseguradora.isActivo()) {
            throw new ReglaNegocioException("No se pudo validar el SOAT: la aseguradora " + aseguradora.getNombre() + " está inactiva.");
        }
        if (!aseguradora.isCubreSoat()) {
            throw new ReglaNegocioException("No se pudo validar el SOAT: el convenio con " + aseguradora.getNombre()
                    + " no incluye SOAT. Seleccione otra aseguradora.");
        }

        // Paso 3: SOAT actual y su vencimiento
        List<Soat> soats = repositorio.listarPorVehiculo(vehiculo.id());
        if (soats.isEmpty()) {
            throw new ReglaNegocioException("No se pudo validar el SOAT: el vehículo " + matricula.getPlaca()
                    + " no tiene ningún SOAT registrado. Registre primero el SOAT con el que llegó el vehículo.");
        }
        Soat actual = soats.get(0);
        LocalDate hoy = hoy();
        long diasParaVencer = ChronoUnit.DAYS.between(hoy, actual.getFechaVencimiento());
        if (diasParaVencer > DIAS_ALERTA_RENOVACION) {
            // Flujo 3A
            return new ResultadoRenovacionSoat(ResultadoRenovacion.NO_REQUIERE_RENOVACION,
                    "El SOAT del vehículo aún se encuentra vigente y no requiere renovación. "
                            + "El SOAT " + actual.getNumeroPoliza() + " vence el " + FormatoUtil.formatoFecha(actual.getFechaVencimiento())
                            + " (en " + diasParaVencer + " días); la renovación se habilita " + DIAS_ALERTA_RENOVACION
                            + " días antes del vencimiento.",
                    null, null, null);
        }

        // Paso 4: siniestros pendientes y bloqueos administrativos
        if (matricula.getEstadoRegistro().tieneBloqueoAdministrativo()) {
            return noCumpleRequisitos("la matrícula del vehículo está " + matricula.getEstadoRegistro().toString().toLowerCase() + ".");
        }
        ConsultaRestricciones restricciones = aseguradoraExterna.consultarRestricciones(aseguradora.getCodigo(), matricula.getPlaca());
        if (restricciones.tieneRestricciones()) {
            String tipo = restricciones.siniestrosPendientes() ? "siniestros pendientes" : "un bloqueo administrativo";
            String detalle = restricciones.detalle() == null ? "" : " (" + restricciones.detalle() + ")";
            return noCumpleRequisitos("la aseguradora reporta " + tipo + detalle + ".");
        }

        // Paso 5: valor según tipo de vehículo y cilindraje
        BigDecimal valor = TarifarioSoat.calcularValor(vehiculo.tipo(), matricula.getCilindrajeCc());

        // Paso 6: solicitud de emisión
        LocalDate inicio = actual.getFechaVencimiento().isBefore(hoy) ? hoy : actual.getFechaVencimiento().plusDays(1);
        LocalDate vencimiento = vencimientoDesde(inicio);
        RespuestaEmision respuesta = aseguradoraExterna.emitirSoat(new SolicitudEmisionSoat(aseguradora.getCodigo(),
                matricula.getPlaca(), vehiculo.tipo(), matricula.getCilindrajeCc(), inicio, vencimiento, valor));
        if (!respuesta.aprobada()) {
            // Flujo 6A
            String motivo = "Rechazo de " + aseguradora.getNombre() + ": " + respuesta.motivoRechazo();
            repositorio.actualizarEstadoDocumental(vehiculo.id(), EstadoDocumental.PENDIENTE, motivo);
            return new ResultadoRenovacionSoat(ResultadoRenovacion.RECHAZADO_POR_ASEGURADORA,
                    "La aseguradora " + aseguradora.getNombre() + " rechazó la emisión del SOAT: " + respuesta.motivoRechazo()
                            + ". El vehículo queda con " + EstadoDocumental.PENDIENTE.toString().toLowerCase()
                            + ". Puede intentar con otra aseguradora.",
                    null, valor, EstadoDocumental.PENDIENTE);
        }

        // Paso 7: registro de la renovación (SOAT + estado documental en una transacción)
        Soat nuevo = new Soat(null, respuesta.numeroPoliza(), vehiculo.id(), aseguradora.getId(), inicio, vencimiento,
                valor, respuesta.codigoAutorizacion());
        repositorio.guardarConEstadoDocumental(nuevo, EstadoDocumental.AL_DIA,
                "SOAT renovado con " + aseguradora.getNombre() + ", póliza " + nuevo.getNumeroPoliza() + ".");

        // Paso 8: notificación
        return new ResultadoRenovacionSoat(ResultadoRenovacion.RENOVADO,
                "SOAT renovado para el vehículo " + matricula.getPlaca() + " con " + conPuntoFinal(aseguradora.getNombre()) + "\n"
                        + "Póliza N° " + nuevo.getNumeroPoliza() + " (autorización " + nuevo.getCodigoAutorizacion() + ").\n"
                        + "Vigencia: " + FormatoUtil.formatoFecha(inicio) + " a " + FormatoUtil.formatoFecha(vencimiento)
                        + ". Valor: " + FormatoUtil.formatoMoneda(valor) + ".\n"
                        + "Estado documental: " + EstadoDocumental.AL_DIA + ".",
                nuevo, valor, EstadoDocumental.AL_DIA);
    }

    private ResultadoRenovacionSoat noCumpleRequisitos(String razon) {
        // Flujo 4A
        return new ResultadoRenovacionSoat(ResultadoRenovacion.NO_CUMPLE_REQUISITOS,
                "El vehículo no cumple los requisitos para la renovación del SOAT: " + razon, null, null, null);
    }

    private VehiculoMatriculado buscarMatriculado(String placa) throws SQLException {
        String placaNormalizada = MatriculaServicio.normalizarPlaca(placa);
        if (placaNormalizada == null || placaNormalizada.isEmpty()) {
            throw new ReglaNegocioException("No se pudo consultar: ingrese la placa del vehículo.");
        }
        Matricula matricula = repositorio.buscarMatriculaPorPlaca(placaNormalizada)
                .orElseThrow(() -> new ReglaNegocioException("No se encontró un vehículo matriculado con la placa " + placaNormalizada
                        + ". Verifique la placa o registre la matrícula en Matrículas (CU-21)."));
        VehiculoLegal vehiculo = repositorio.buscarVehiculo(matricula.getVehiculoId())
                .orElseThrow(() -> new ReglaNegocioException("No se encontró en el inventario el vehículo de la placa " + placaNormalizada + "."));
        return new VehiculoMatriculado(vehiculo, matricula);
    }

    /** Agrega el punto final, salvo que el texto ya termine en punto (por ejemplo, "Seguros Bolívar S.A."). */
    static String conPuntoFinal(String texto) {
        return texto.endsWith(".") ? texto : texto + ".";
    }

    /** El SOAT dura un año: si empieza el 10/03/2026 cubre hasta el 09/03/2027. */
    private static LocalDate vencimientoDesde(LocalDate inicio) {
        return inicio.plusYears(1).minusDays(1);
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    private record VehiculoMatriculado(VehiculoLegal vehiculo, Matricula matricula) {
    }
}
