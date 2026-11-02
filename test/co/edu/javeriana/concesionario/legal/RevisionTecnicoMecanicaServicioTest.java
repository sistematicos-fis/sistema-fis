package co.edu.javeriana.concesionario.legal;

import co.edu.javeriana.concesionario.legal.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.legal.logica.RevisionNoVigenteException;
import co.edu.javeriana.concesionario.legal.logica.RevisionTecnicoMecanicaServicio;
import co.edu.javeriana.concesionario.legal.modelo.EstadoRtm;
import co.edu.javeriana.concesionario.legal.modelo.OperacionComercial;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRevision;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoVerificacionRtm;
import co.edu.javeriana.concesionario.legal.modelo.RevisionTecnicoMecanica;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoRtm;
import co.edu.javeriana.concesionario.legal.persistencia.RevisionTecnicoMecanicaRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reglas del CU-25 con un repositorio en memoria y un reloj fijo: hoy es 19/10/2026.
 */
public class RevisionTecnicoMecanicaServicioTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 19);
    private static final Long AUTO_VIEJO = 1L;     // automóvil modelo 2018: ya debe tener revisión
    private static final Long AUTO_NUEVO = 2L;     // automóvil modelo 2024: aún no le corresponde
    private static final Long AUTO_2021 = 3L;      // modelo 2021: cumple 5 años en 2026
    private static final Long AUTO_2022 = 4L;      // modelo 2022: cumple 5 años en 2027
    private static final Long MOTO_2025 = 5L;      // motocicleta 2025: aún no le corresponde
    private static final Long MOTO_2024 = 6L;      // motocicleta 2024: cumple 2 años en 2026

    private RepositorioEnMemoria repositorio;
    private RevisionTecnicoMecanicaServicio servicio;

    @BeforeEach
    public void setUp() {
        repositorio = new RepositorioEnMemoria();
        repositorio.vehiculos.add(new VehiculoRtm(AUTO_VIEJO, "VIN1", "AAA111", "AUTOMOVIL", "Mazda", "3", 2018));
        repositorio.vehiculos.add(new VehiculoRtm(AUTO_NUEVO, "VIN2", "AAA222", "AUTOMOVIL", "Toyota", "Corolla", 2024));
        repositorio.vehiculos.add(new VehiculoRtm(AUTO_2021, "VIN3", "AAA333", "CAMIONETA", "Kia", "Sportage", 2021));
        repositorio.vehiculos.add(new VehiculoRtm(AUTO_2022, "VIN4", "AAA444", "UTILITARIO", "Renault", "Duster", 2022));
        repositorio.vehiculos.add(new VehiculoRtm(MOTO_2025, "VIN5", "BBB55A", "MOTOCICLETA", "Yamaha", "MT-09", 2025));
        repositorio.vehiculos.add(new VehiculoRtm(MOTO_2024, "VIN6", "BBB66A", "MOTOCICLETA", "Honda", "CB500", 2024));
        servicio = new RevisionTecnicoMecanicaServicio(repositorio, Clock.fixed(Instant.parse("2026-10-19T12:00:00Z"), ZoneOffset.UTC));
    }

    private void agregarRevision(Long vehiculoId, String certificado, LocalDate expedicion, ResultadoRevision resultado) {
        LocalDate vencimiento = resultado == ResultadoRevision.APROBADA ? expedicion.plusYears(1) : null;
        repositorio.revisiones.add(new RevisionTecnicoMecanica((long) repositorio.revisiones.size() + 1, vehiculoId,
                certificado, "CDA Prueba", expedicion, vencimiento, resultado));
    }

    private ResultadoVerificacionRtm verificarVenta(Long vehiculoId) throws Exception {
        return servicio.verificarVigencia(vehiculoId, OperacionComercial.VENTA, null);
    }

    // ---------------------------------------------------------------- vigencia

    @Test
    public void testRevisionVigenteHabilitaLaVenta() throws Exception {
        agregarRevision(AUTO_VIEJO, "C-1", LocalDate.of(2026, 3, 1), ResultadoRevision.APROBADA);

        ResultadoVerificacionRtm r = verificarVenta(AUTO_VIEJO);

        assertTrue(r.habilitado());
        assertEquals(EstadoRtm.VIGENTE, r.estado());
        assertEquals(LocalDate.of(2027, 3, 1), r.fechaVencimiento());
        assertEquals(133L, r.diasRestantes());
    }

    @Test
    public void testRevisionPorVencerHabilitaPeroAvisa() throws Exception {
        agregarRevision(AUTO_VIEJO, "C-1", LocalDate.of(2025, 10, 29), ResultadoRevision.APROBADA);

        ResultadoVerificacionRtm r = verificarVenta(AUTO_VIEJO);

        assertTrue(r.habilitado());
        assertEquals(EstadoRtm.POR_VENCER, r.estado());
        assertEquals(10L, r.diasRestantes());
    }

    @Test
    public void testRevisionQueVenceHoySigueVigente() throws Exception {
        agregarRevision(AUTO_VIEJO, "C-1", LocalDate.of(2025, 10, 19), ResultadoRevision.APROBADA);

        ResultadoVerificacionRtm r = verificarVenta(AUTO_VIEJO);

        assertTrue(r.habilitado(), "El día del vencimiento el certificado todavía es válido");
        assertEquals(0L, r.diasRestantes());
    }

    @Test
    public void testRevisionVencidaAyerNoHabilita() throws Exception {
        agregarRevision(AUTO_VIEJO, "C-1", LocalDate.of(2025, 10, 18), ResultadoRevision.APROBADA);

        ResultadoVerificacionRtm r = verificarVenta(AUTO_VIEJO);

        assertFalse(r.habilitado());
        assertEquals(EstadoRtm.VENCIDA, r.estado());
        assertEquals(LocalDate.of(2026, 10, 18), r.fechaVencimiento());
    }

    @Test
    public void testSinRevisionNoHabilitaSiYaLeCorresponde() throws Exception {
        ResultadoVerificacionRtm r = verificarVenta(AUTO_VIEJO);

        assertFalse(r.habilitado());
        assertEquals(EstadoRtm.SIN_REVISION, r.estado());
        assertNull(r.fechaVencimiento());
    }

    @Test
    public void testUsaLaRevisionAprobadaMasReciente() throws Exception {
        agregarRevision(AUTO_VIEJO, "C-VIEJO", LocalDate.of(2024, 5, 1), ResultadoRevision.APROBADA);
        agregarRevision(AUTO_VIEJO, "C-NUEVO", LocalDate.of(2026, 5, 1), ResultadoRevision.APROBADA);

        ResultadoVerificacionRtm r = verificarVenta(AUTO_VIEJO);

        assertTrue(r.habilitado());
        assertEquals(LocalDate.of(2027, 5, 1), r.fechaVencimiento());
        assertTrue(r.motivo().contains("C-NUEVO"));
    }

    // ---------------------------------------------------------------- revisión rechazada

    @Test
    public void testUltimaRevisionRechazadaNoHabilita() throws Exception {
        agregarRevision(AUTO_VIEJO, "C-1", LocalDate.of(2026, 9, 1), ResultadoRevision.RECHAZADA);

        ResultadoVerificacionRtm r = verificarVenta(AUTO_VIEJO);

        assertFalse(r.habilitado());
        assertEquals(EstadoRtm.RECHAZADA, r.estado());
    }

    @Test
    public void testRechazadaPosteriorAUnaAprobadaVencidaSeReportaComoRechazada() throws Exception {
        agregarRevision(AUTO_VIEJO, "C-1", LocalDate.of(2025, 1, 10), ResultadoRevision.APROBADA);
        agregarRevision(AUTO_VIEJO, "C-2", LocalDate.of(2026, 9, 1), ResultadoRevision.RECHAZADA);

        assertEquals(EstadoRtm.RECHAZADA, verificarVenta(AUTO_VIEJO).estado());
    }

    @Test
    public void testAprobadaPosteriorALaRechazadaHabilita() throws Exception {
        agregarRevision(AUTO_VIEJO, "C-1", LocalDate.of(2026, 9, 1), ResultadoRevision.RECHAZADA);
        agregarRevision(AUTO_VIEJO, "C-2", LocalDate.of(2026, 9, 20), ResultadoRevision.APROBADA);

        ResultadoVerificacionRtm r = verificarVenta(AUTO_VIEJO);

        assertTrue(r.habilitado());
        assertEquals(EstadoRtm.VIGENTE, r.estado());
    }

    // ---------------------------------------------------------------- primera revisión

    @Test
    public void testAutomovilNuevoNoRequiereRevision() throws Exception {
        ResultadoVerificacionRtm r = verificarVenta(AUTO_NUEVO);

        assertTrue(r.habilitado());
        assertEquals(EstadoRtm.EXENTA_PRIMERA_REVISION, r.estado());
    }

    @Test
    public void testParticularDebeRevisarseAlCumplirCincoAnios() throws Exception {
        assertEquals(EstadoRtm.EXENTA_PRIMERA_REVISION, verificarVenta(AUTO_2022).estado(), "2026 - 2022 = 4 años");
        assertEquals(EstadoRtm.SIN_REVISION, verificarVenta(AUTO_2021).estado(), "2026 - 2021 = 5 años");
    }

    @Test
    public void testMotocicletaDebeRevisarseAlCumplirDosAnios() throws Exception {
        assertEquals(EstadoRtm.EXENTA_PRIMERA_REVISION, verificarVenta(MOTO_2025).estado(), "2026 - 2025 = 1 año");
        assertEquals(EstadoRtm.SIN_REVISION, verificarVenta(MOTO_2024).estado(), "2026 - 2024 = 2 años");
    }

    @Test
    public void testRevisionVigenteTienePrioridadSobreLaExencion() throws Exception {
        agregarRevision(AUTO_NUEVO, "C-1", LocalDate.of(2026, 6, 1), ResultadoRevision.APROBADA);

        assertEquals(EstadoRtm.VIGENTE, verificarVenta(AUTO_NUEVO).estado());
    }

    // ---------------------------------------------------------------- alquiler

    @Test
    public void testAlquilerConRevisionQueCubreHastaLaDevolucionHabilita() throws Exception {
        agregarRevision(AUTO_VIEJO, "C-1", LocalDate.of(2026, 3, 1), ResultadoRevision.APROBADA); // vence 01/03/2027

        ResultadoVerificacionRtm r = servicio.verificarVigencia(AUTO_VIEJO, OperacionComercial.ALQUILER, LocalDate.of(2027, 3, 1));

        assertTrue(r.habilitado());
    }

    @Test
    public void testAlquilerQueTerminaDespuesDelVencimientoNoHabilita() throws Exception {
        agregarRevision(AUTO_VIEJO, "C-1", LocalDate.of(2026, 3, 1), ResultadoRevision.APROBADA); // vence 01/03/2027

        ResultadoVerificacionRtm r = servicio.verificarVigencia(AUTO_VIEJO, OperacionComercial.ALQUILER, LocalDate.of(2027, 3, 2));

        assertFalse(r.habilitado());
        assertEquals(EstadoRtm.VENCE_DURANTE_ALQUILER, r.estado());
        assertEquals(LocalDate.of(2027, 3, 1), r.fechaVencimiento());
        assertTrue(r.motivo().contains("01/03/2027") && r.motivo().contains("02/03/2027"));
    }

    @Test
    public void testLaVentaIgnoraLaFechaDeFinDeOperacion() throws Exception {
        agregarRevision(AUTO_VIEJO, "C-1", LocalDate.of(2026, 3, 1), ResultadoRevision.APROBADA);

        ResultadoVerificacionRtm r = servicio.verificarVigencia(AUTO_VIEJO, OperacionComercial.VENTA, LocalDate.of(2028, 1, 1));

        assertTrue(r.habilitado());
    }

    @Test
    public void testAlquilerSinFechaDeFinSoloExigeVigenciaHoy() throws Exception {
        agregarRevision(AUTO_VIEJO, "C-1", LocalDate.of(2026, 3, 1), ResultadoRevision.APROBADA);

        assertTrue(servicio.verificarVigencia(AUTO_VIEJO, OperacionComercial.ALQUILER, null).habilitado());
    }

    @Test
    public void testLaExencionSeEvaluaALaFechaDeDevolucionDelAlquiler() throws Exception {
        // Modelo 2022: hoy (2026) está exento, pero si la devolución es en 2027 ya cumple 5 años
        assertTrue(servicio.verificarVigencia(AUTO_2022, OperacionComercial.ALQUILER, LocalDate.of(2026, 12, 31)).habilitado());

        ResultadoVerificacionRtm r = servicio.verificarVigencia(AUTO_2022, OperacionComercial.ALQUILER, LocalDate.of(2027, 1, 5));
        assertFalse(r.habilitado());
        assertEquals(EstadoRtm.SIN_REVISION, r.estado());
    }

    @Test
    public void testAlquilerConFechaDeFinPasadaEsRechazado() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.verificarVigencia(AUTO_VIEJO, OperacionComercial.ALQUILER, HOY.minusDays(1)));
    }

    // ---------------------------------------------------------------- datos de entrada

    @Test
    public void testVehiculoInexistenteEsRechazado() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> verificarVenta(999L));
        assertTrue(ex.getMessage().contains("no existe"));
    }

    @Test
    public void testVehiculoYOperacionSonObligatorios() {
        assertThrows(ReglaNegocioException.class, () -> servicio.verificarVigencia(null, OperacionComercial.VENTA, null));
        assertThrows(ReglaNegocioException.class, () -> servicio.verificarVigencia(AUTO_VIEJO, null, null));
    }

    // ---------------------------------------------------------------- punto de integración

    @Test
    public void testExigirRevisionVigenteBloqueaLaOperacionSiNoEstaVigente() {
        RevisionNoVigenteException ex = assertThrows(RevisionNoVigenteException.class,
                () -> servicio.exigirRevisionVigente(AUTO_VIEJO, OperacionComercial.VENTA, null));
        assertEquals(EstadoRtm.SIN_REVISION, ex.getVerificacion().estado());
        assertTrue(ex.getMessage().startsWith("Operación no permitida"));
    }

    @Test
    public void testExigirRevisionVigenteDejaPasarSiEstaHabilitado() {
        agregarRevision(AUTO_VIEJO, "C-1", LocalDate.of(2026, 3, 1), ResultadoRevision.APROBADA);
        assertDoesNotThrow(() -> servicio.exigirRevisionVigente(AUTO_VIEJO, OperacionComercial.VENTA, null));
    }

    // ---------------------------------------------------------------- registrar revisión

    private RevisionTecnicoMecanica nuevaRevision(Long vehiculoId, String certificado, LocalDate expedicion, ResultadoRevision resultado) {
        RevisionTecnicoMecanica r = new RevisionTecnicoMecanica();
        r.setVehiculoId(vehiculoId);
        r.setNumeroCertificado(certificado);
        r.setCentroDiagnostico("CDA Bogotá Norte");
        r.setFechaExpedicion(expedicion);
        r.setResultado(resultado);
        return r;
    }

    @Test
    public void testRegistrarRevisionAprobadaCalculaVencimientoAUnAnio() throws Exception {
        RevisionTecnicoMecanica r = servicio.registrarRevision(
                nuevaRevision(AUTO_VIEJO, "  c-100 ", LocalDate.of(2026, 5, 10), ResultadoRevision.APROBADA));

        assertNotNull(r.getId());
        assertEquals("C-100", r.getNumeroCertificado());
        assertEquals(LocalDate.of(2027, 5, 10), r.getFechaVencimiento());
        assertTrue(verificarVenta(AUTO_VIEJO).habilitado(), "Registrar la revisión habilita el vehículo");
    }

    @Test
    public void testRegistrarRevisionRechazadaNoTieneVencimiento() throws Exception {
        RevisionTecnicoMecanica r = servicio.registrarRevision(
                nuevaRevision(AUTO_VIEJO, "C-101", LocalDate.of(2026, 5, 10), ResultadoRevision.RECHAZADA));

        assertNull(r.getFechaVencimiento());
    }

    @Test
    public void testRegistrarCertificadoDuplicadoEsRechazado() throws Exception {
        servicio.registrarRevision(nuevaRevision(AUTO_VIEJO, "C-102", LocalDate.of(2026, 5, 10), ResultadoRevision.APROBADA));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.registrarRevision(
                nuevaRevision(AUTO_2021, "c-102", LocalDate.of(2026, 6, 10), ResultadoRevision.APROBADA)));
        assertTrue(ex.getMessage().contains("ya existe el certificado"));
    }

    @Test
    public void testRegistrarConFechaFuturaEsRechazado() {
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarRevision(
                nuevaRevision(AUTO_VIEJO, "C-103", HOY.plusDays(1), ResultadoRevision.APROBADA)));
    }

    @Test
    public void testRegistrarConCamposObligatoriosVaciosEsRechazado() {
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarRevision(
                nuevaRevision(null, "C-104", HOY, ResultadoRevision.APROBADA)));
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarRevision(
                nuevaRevision(999L, "C-104", HOY, ResultadoRevision.APROBADA)));
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarRevision(
                nuevaRevision(AUTO_VIEJO, "  ", HOY, ResultadoRevision.APROBADA)));
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarRevision(
                nuevaRevision(AUTO_VIEJO, "C-104", null, ResultadoRevision.APROBADA)));
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarRevision(
                nuevaRevision(AUTO_VIEJO, "C-104", HOY, null)));
        RevisionTecnicoMecanica sinCda = nuevaRevision(AUTO_VIEJO, "C-104", HOY, ResultadoRevision.APROBADA);
        sinCda.setCentroDiagnostico("");
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarRevision(sinCda));
    }

    // ---------------------------------------------------------------- repositorio falso

    private static class RepositorioEnMemoria implements RevisionTecnicoMecanicaRepositorio {
        final List<VehiculoRtm> vehiculos = new ArrayList<>();
        final List<RevisionTecnicoMecanica> revisiones = new ArrayList<>();

        @Override
        public RevisionTecnicoMecanica guardar(RevisionTecnicoMecanica r) {
            r.setId((long) revisiones.size() + 1);
            revisiones.add(r);
            return r;
        }

        @Override
        public List<RevisionTecnicoMecanica> listarPorVehiculo(Long vehiculoId) {
            return revisiones.stream()
                    .filter(r -> r.getVehiculoId().equals(vehiculoId))
                    .sorted(Comparator.comparing(RevisionTecnicoMecanica::getFechaExpedicion)
                            .thenComparing(RevisionTecnicoMecanica::getId).reversed())
                    .toList();
        }

        @Override
        public Optional<RevisionTecnicoMecanica> buscarPorCertificado(String numeroCertificado) {
            return revisiones.stream().filter(r -> r.getNumeroCertificado().equals(numeroCertificado)).findFirst();
        }

        @Override
        public Optional<VehiculoRtm> buscarVehiculo(Long vehiculoId) {
            return vehiculos.stream().filter(v -> v.id().equals(vehiculoId)).findFirst();
        }

        @Override
        public List<VehiculoRtm> listarVehiculos() {
            return vehiculos;
        }
    }
}
