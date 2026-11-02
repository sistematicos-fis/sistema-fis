package co.edu.javeriana.concesionario.legal.logica;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.legal.modelo.EstadoRtm;
import co.edu.javeriana.concesionario.legal.modelo.OperacionComercial;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRevision;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoVerificacionRtm;
import co.edu.javeriana.concesionario.legal.modelo.RevisionTecnicoMecanica;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoRtm;
import co.edu.javeriana.concesionario.legal.persistencia.RevisionTecnicoMecanicaRepositorio;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * CU-25: verificar la vigencia de la revisión técnico-mecánica (RTM) antes de habilitar un vehículo
 * para una venta o un alquiler.
 *
 * Reglas (Código Nacional de Tránsito, art. 52, y Ley 2294 de 2023):
 * Una revisión aprobada vale un año desde su expedición; el día del vencimiento todavía es válida.
 * La primera revisión de un vehículo particular es a los 5 años de la matrícula y la de una motocicleta 
 * a los 2 años; antes de eso el vehículo no necesita revisión.
 *
 * En un alquiler, la revisión debe cubrir hasta la fecha de devolución, no solo el día de hoy.
 * El inventario no guarda la fecha de matrícula, así que se usa el año del modelo (aproximación prudente:
 * la matrícula nunca es anterior al modelo, por lo que la exigencia llega igual o antes).
 * Se asume servicio particular, porque el sistema no maneja vehículos de servicio público.
 *
 */
public class RevisionTecnicoMecanicaServicio {
    static final int ANIOS_PRIMERA_REVISION_PARTICULAR = 5;
    static final int ANIOS_PRIMERA_REVISION_MOTOCICLETA = 2;
    static final int DIAS_AVISO_POR_VENCER = 30;
    private static final int LONGITUD_MAX_CERTIFICADO = 30;
    private static final int LONGITUD_MAX_CDA = 80;

    private final RevisionTecnicoMecanicaRepositorio repositorio;
    private final Clock reloj;

    public RevisionTecnicoMecanicaServicio(RevisionTecnicoMecanicaRepositorio repositorio) {
        this(repositorio, Clock.systemDefaultZone());
    }

    public RevisionTecnicoMecanicaServicio(RevisionTecnicoMecanicaRepositorio repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public List<VehiculoRtm> listarVehiculos() throws SQLException {
        return repositorio.listarVehiculos();
    }

    public List<RevisionTecnicoMecanica> listarRevisiones(Long vehiculoId) throws SQLException {
        return repositorio.listarPorVehiculo(vehiculoId);
    }

    /** Registra el certificado emitido por un CDA. La fecha de vencimiento se calcula, no se digita. */
    public RevisionTecnicoMecanica registrarRevision(RevisionTecnicoMecanica revision) throws SQLException {
        if (revision.getVehiculoId() == null || repositorio.buscarVehiculo(revision.getVehiculoId()).isEmpty()) {
            throw new ReglaNegocioException("No se pudo registrar la revisión: seleccione un vehículo existente.");
        }
        if (estaVacio(revision.getNumeroCertificado())) {
            throw new ReglaNegocioException("No se pudo registrar la revisión: el Número de certificado es obligatorio.");
        }
        if (revision.getNumeroCertificado().trim().length() > LONGITUD_MAX_CERTIFICADO) {
            throw new ReglaNegocioException("No se pudo registrar la revisión: el Número de certificado no puede superar "
                    + LONGITUD_MAX_CERTIFICADO + " caracteres.");
        }
        if (estaVacio(revision.getCentroDiagnostico())) {
            throw new ReglaNegocioException("No se pudo registrar la revisión: el Centro de diagnóstico (CDA) es obligatorio.");
        }
        if (revision.getCentroDiagnostico().trim().length() > LONGITUD_MAX_CDA) {
            throw new ReglaNegocioException("No se pudo registrar la revisión: el Centro de diagnóstico no puede superar "
                    + LONGITUD_MAX_CDA + " caracteres.");
        }
        if (revision.getFechaExpedicion() == null) {
            throw new ReglaNegocioException("No se pudo registrar la revisión: la Fecha de expedición es obligatoria.");
        }
        if (revision.getFechaExpedicion().isAfter(hoy())) {
            throw new ReglaNegocioException("No se pudo registrar la revisión: la Fecha de expedición no puede ser futura.");
        }
        if (revision.getResultado() == null) {
            throw new ReglaNegocioException("No se pudo registrar la revisión: seleccione el Resultado (aprobada o rechazada).");
        }

        revision.setNumeroCertificado(revision.getNumeroCertificado().trim().toUpperCase());
        revision.setCentroDiagnostico(revision.getCentroDiagnostico().trim());
        if (repositorio.buscarPorCertificado(revision.getNumeroCertificado()).isPresent()) {
            throw new ReglaNegocioException("No se pudo registrar la revisión: ya existe el certificado "
                    + revision.getNumeroCertificado() + ".");
        }

        revision.setFechaVencimiento(revision.getResultado() == ResultadoRevision.APROBADA
                ? revision.getFechaExpedicion().plusYears(1)
                : null);
        return repositorio.guardar(revision);
    }

    /**
     * Verifica si el vehículo puede venderse o alquilarse según su revisión técnico-mecánica.
     *
     * @param fechaFinOperacion fecha de devolución del alquiler; solo se considera si la operación es
     *                          ALQUILER (en una venta basta con que la revisión esté vigente hoy). Puede ser null.
     */
    public ResultadoVerificacionRtm verificarVigencia(Long vehiculoId, OperacionComercial operacion,
                                                      LocalDate fechaFinOperacion) throws SQLException {
        if (vehiculoId == null) {
            throw new ReglaNegocioException("No se pudo verificar: seleccione un vehículo.");
        }
        if (operacion == null) {
            throw new ReglaNegocioException("No se pudo verificar: seleccione la operación (venta o alquiler).");
        }
        LocalDate hoy = hoy();
        LocalDate fechaRequerida = hoy;
        if (operacion == OperacionComercial.ALQUILER && fechaFinOperacion != null) {
            if (fechaFinOperacion.isBefore(hoy)) {
                throw new ReglaNegocioException("No se pudo verificar: la fecha de fin del alquiler no puede ser anterior a hoy.");
            }
            fechaRequerida = fechaFinOperacion;
        }

        VehiculoRtm vehiculo = repositorio.buscarVehiculo(vehiculoId)
                .orElseThrow(() -> new ReglaNegocioException("No se pudo verificar: el vehículo seleccionado no existe."));
        List<RevisionTecnicoMecanica> revisiones = repositorio.listarPorVehiculo(vehiculoId);

        Optional<RevisionTecnicoMecanica> ultimaAprobada = revisiones.stream()
                .filter(r -> r.getResultado() == ResultadoRevision.APROBADA)
                .findFirst();
        LocalDate vencimiento = ultimaAprobada.map(RevisionTecnicoMecanica::getFechaVencimiento).orElse(null);

        // 1. Revisión aprobada que cubre toda la operación
        if (vencimiento != null && !vencimiento.isBefore(fechaRequerida)) {
            long dias = ChronoUnit.DAYS.between(hoy, vencimiento);
            String certificado = ultimaAprobada.get().getNumeroCertificado();
            if (dias <= DIAS_AVISO_POR_VENCER) {
                return new ResultadoVerificacionRtm(EstadoRtm.POR_VENCER, true,
                        "La revisión técnico-mecánica (certificado " + certificado + ") está vigente, pero vence el "
                                + FormatoUtil.formatoFecha(vencimiento) + " (en " + dias + " días). Conviene renovarla pronto.",
                        vencimiento, dias);
            }
            return new ResultadoVerificacionRtm(EstadoRtm.VIGENTE, true,
                    "Revisión técnico-mecánica vigente hasta el " + FormatoUtil.formatoFecha(vencimiento)
                            + " (certificado " + certificado + ").",
                    vencimiento, dias);
        }

        // 2. Vehículo al que todavía no le corresponde la primera revisión
        int aniosPrimeraRevision = vehiculo.esMotocicleta()
                ? ANIOS_PRIMERA_REVISION_MOTOCICLETA : ANIOS_PRIMERA_REVISION_PARTICULAR;
        if (fechaRequerida.getYear() - vehiculo.modeloAnio() < aniosPrimeraRevision) {
            return new ResultadoVerificacionRtm(EstadoRtm.EXENTA_PRIMERA_REVISION, true,
                    "No requiere revisión técnico-mecánica todavía: es modelo " + vehiculo.modeloAnio()
                            + " y la primera revisión se exige a los " + aniosPrimeraRevision + " años de la matrícula.",
                    vencimiento, null);
        }

        // 3. Debe tener revisión y no la tiene vigente: se explica por qué
        if (vencimiento != null && !vencimiento.isBefore(hoy)) {
            return new ResultadoVerificacionRtm(EstadoRtm.VENCE_DURANTE_ALQUILER, false,
                    "La revisión técnico-mecánica vence el " + FormatoUtil.formatoFecha(vencimiento)
                            + ", antes de la devolución del alquiler (" + FormatoUtil.formatoFecha(fechaRequerida)
                            + "). Debe renovarse antes de habilitar este alquiler.",
                    vencimiento, ChronoUnit.DAYS.between(hoy, vencimiento));
        }
        if (!revisiones.isEmpty() && revisiones.get(0).getResultado() == ResultadoRevision.RECHAZADA) {
            RevisionTecnicoMecanica rechazada = revisiones.get(0);
            return new ResultadoVerificacionRtm(EstadoRtm.RECHAZADA, false,
                    "La última revisión (certificado " + rechazada.getNumeroCertificado() + ", "
                            + FormatoUtil.formatoFecha(rechazada.getFechaExpedicion())
                            + ") fue rechazada y no hay una aprobada posterior vigente. Debe repetirse la revisión.",
                    vencimiento, null);
        }
        if (vencimiento != null) {
            return new ResultadoVerificacionRtm(EstadoRtm.VENCIDA, false,
                    "La revisión técnico-mecánica venció el " + FormatoUtil.formatoFecha(vencimiento)
                            + ". Debe renovarse antes de vender o alquilar el vehículo.",
                    vencimiento, null);
        }
        return new ResultadoVerificacionRtm(EstadoRtm.SIN_REVISION, false,
                "El vehículo no tiene ninguna revisión técnico-mecánica registrada y ya le corresponde "
                        + "(modelo " + vehiculo.modeloAnio() + ", primera revisión a los " + aniosPrimeraRevision + " años).",
                null, null);
    }

    /**
     * Punto de integración para los módulos de venta y alquiler: no hace nada si el vehículo está
     * habilitado y lanza {@link RevisionNoVigenteException} si no lo está.
     */
    public void exigirRevisionVigente(Long vehiculoId, OperacionComercial operacion,
                                      LocalDate fechaFinOperacion) throws SQLException {
        ResultadoVerificacionRtm verificacion = verificarVigencia(vehiculoId, operacion, fechaFinOperacion);
        if (!verificacion.habilitado()) {
            throw new RevisionNoVigenteException(verificacion);
        }
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
