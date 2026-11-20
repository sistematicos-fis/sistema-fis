package co.edu.javeriana.concesionario.legal;

import co.edu.javeriana.concesionario.comun.simuladores.AseguradoraExterna;
import co.edu.javeriana.concesionario.legal.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.legal.logica.SoatServicio;
import co.edu.javeriana.concesionario.legal.logica.TarifarioSoat;
import co.edu.javeriana.concesionario.legal.modelo.EstadoDocumental;
import co.edu.javeriana.concesionario.legal.modelo.EstadoMatricula;
import co.edu.javeriana.concesionario.legal.modelo.Matricula;
import co.edu.javeriana.concesionario.legal.modelo.RegistroDocumental;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRenovacion;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRenovacionSoat;
import co.edu.javeriana.concesionario.legal.modelo.Soat;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;
import co.edu.javeriana.concesionario.legal.persistencia.SoatRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Flujos del CU-24 con un repositorio en memoria, una aseguradora controlada y un reloj fijo:
 * hoy es 19/10/2026.
 */
public class SoatServicioTest {
    private static final LocalDate HOY = LocalDate.of(2026, 10, 19);
    private static final long AUTO = 1L;    // Corolla 1798 cc, placa KLU382
    private static final long MOTO = 2L;    // MT-09 890 cc, placa NKS81A
    private static final long SURA = 10L;
    private static final long SIN_SOAT = 11L;      // su convenio no incluye SOAT
    private static final long INACTIVA = 12L;

    private static final long BOLIVAR = 13L;       // su nombre termina en punto ("... S.A.")

    private RepositorioEnMemoria repositorio;
    private AseguradorasEnMemoria aseguradoras;
    private AseguradoraControlada aseguradora;
    private SoatServicio servicio;

    @BeforeEach
    public void setUp() {
        repositorio = new RepositorioEnMemoria();
        repositorio.agregarVehiculo(new VehiculoLegal(AUTO, "VIN1", "KLU382", "AUTOMOVIL", "Toyota", "Corolla", 2024, "DISPONIBLE"), 1798);
        repositorio.agregarVehiculo(new VehiculoLegal(MOTO, "VIN2", "NKS81A", "MOTOCICLETA", "Yamaha", "MT-09", 2025, "DISPONIBLE"), 890);
        aseguradoras = new AseguradorasEnMemoria();
        aseguradoras.agregar(SURA, "SURA", true, true, true);
        aseguradoras.agregar(SIN_SOAT, "SINSOAT", false, true, true);
        aseguradoras.agregar(INACTIVA, "INACTIVA", true, true, false);
        aseguradora = new AseguradoraControlada();
        servicio = new SoatServicio(repositorio, aseguradoras, aseguradora,
                Clock.fixed(Instant.parse("2026-10-19T12:00:00Z"), ZoneOffset.UTC));
    }

    /** SOAT anual que vence en la fecha indicada. */
    private void agregarSoat(long vehiculoId, String numero, LocalDate vencimiento) {
        repositorio.soats.add(new Soat((long) repositorio.soats.size() + 1, numero, vehiculoId, SURA,
                vencimiento.minusYears(1).plusDays(1), vencimiento, new BigDecimal("570000"), "AUT-0"));
    }

    // ---------------------------------------------------------------- flujo básico

    @Test
    public void testRenovacionExitosaEmpiezaAlDiaSiguienteDelVencimiento() throws Exception {
        agregarSoat(AUTO, "SOAT-1", HOY.plusDays(10));

        ResultadoRenovacionSoat r = servicio.validarYRenovar("klu 382", SURA);

        assertEquals(ResultadoRenovacion.RENOVADO, r.resultado());
        assertEquals(new BigDecimal("570000"), r.valorCalculado(), "Automóvil entre 1500 y 2500 cc");
        assertEquals(HOY.plusDays(11), r.soatNuevo().getFechaInicio());
        assertEquals(HOY.plusDays(10).plusYears(1), r.soatNuevo().getFechaVencimiento());
        assertEquals(2, repositorio.soats.size());
        assertEquals(EstadoDocumental.AL_DIA, repositorio.estados.get(AUTO).estado());
        assertEquals("KLU382", aseguradora.ultimaSolicitud.placa());
        assertEquals(1798, aseguradora.ultimaSolicitud.cilindrajeCc());
    }

    @Test
    public void testSoatVencidoSeRenuevaDesdeHoy() throws Exception {
        agregarSoat(MOTO, "SOAT-2", HOY.minusDays(15));

        ResultadoRenovacionSoat r = servicio.validarYRenovar("NKS81A", SURA);

        assertTrue(r.fueRenovado());
        assertEquals(HOY, r.soatNuevo().getFechaInicio());
        assertEquals(new BigDecimal("790000"), r.valorCalculado(), "Moto de más de 200 cc");
    }

    @Test
    public void testElDiaLimiteDelRangoDeAlertaSiSeRenueva() throws Exception {
        agregarSoat(AUTO, "SOAT-3", HOY.plusDays(30));
        assertTrue(servicio.validarYRenovar("KLU382", SURA).fueRenovado());
    }

    // ---------------------------------------------------------------- flujos alternos

    @Test
    public void test3ASoatVigenteNoRequiereRenovacion() throws Exception {
        agregarSoat(AUTO, "SOAT-4", HOY.plusDays(31));

        ResultadoRenovacionSoat r = servicio.validarYRenovar("KLU382", SURA);

        assertEquals(ResultadoRenovacion.NO_REQUIERE_RENOVACION, r.resultado());
        assertTrue(r.mensaje().startsWith("El SOAT del vehículo aún se encuentra vigente y no requiere renovación."));
        assertEquals(1, repositorio.soats.size());
        assertNull(aseguradora.ultimaSolicitud, "No se contacta a la aseguradora");
    }

    @Test
    public void test4ASiniestroPendienteBloqueaLaRenovacion() throws Exception {
        agregarSoat(AUTO, "SOAT-5", HOY.plusDays(5));
        aseguradora.restricciones = new AseguradoraExterna.ConsultaRestricciones(true, false, "siniestro SIN-1");

        ResultadoRenovacionSoat r = servicio.validarYRenovar("KLU382", SURA);

        assertEquals(ResultadoRenovacion.NO_CUMPLE_REQUISITOS, r.resultado());
        assertTrue(r.mensaje().startsWith("El vehículo no cumple los requisitos para la renovación del SOAT"));
        assertTrue(r.mensaje().contains("SIN-1"));
        assertEquals(1, repositorio.soats.size());
        assertNull(aseguradora.ultimaSolicitud, "No se solicita la emisión");
    }

    @Test
    public void test4AMatriculaCanceladaEsBloqueoAdministrativo() throws Exception {
        agregarSoat(AUTO, "SOAT-6", HOY.plusDays(5));
        repositorio.matriculas.get(AUTO).setEstadoRegistro(EstadoMatricula.CANCELADA);

        ResultadoRenovacionSoat r = servicio.validarYRenovar("KLU382", SURA);

        assertEquals(ResultadoRenovacion.NO_CUMPLE_REQUISITOS, r.resultado());
        assertTrue(r.mensaje().contains("cancelada"));
    }

    @Test
    public void test6ARechazoDeLaAseguradoraDejaDocumentacionPendiente() throws Exception {
        agregarSoat(AUTO, "SOAT-7", HOY.plusDays(5));
        aseguradora.rechazo = "datos inconsistentes con el RUNT";

        ResultadoRenovacionSoat r = servicio.validarYRenovar("KLU382", SURA);

        assertEquals(ResultadoRenovacion.RECHAZADO_POR_ASEGURADORA, r.resultado());
        assertEquals(EstadoDocumental.PENDIENTE, r.estadoDocumental());
        assertEquals(1, repositorio.soats.size(), "No se registra un SOAT nuevo");
        RegistroDocumental registro = repositorio.estados.get(AUTO);
        assertEquals(EstadoDocumental.PENDIENTE, registro.estado());
        assertTrue(registro.observacion().contains("datos inconsistentes con el RUNT"), "Queda registrado el motivo");
    }

    // ---------------------------------------------------------------- precondiciones

    @Test
    public void testPlacaSinMatriculaEsRechazada() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.validarYRenovar("ZZZ999", SURA));
        assertTrue(ex.getMessage().contains("CU-21"));
    }

    @Test
    public void testVehiculoSinSoatRegistradoEsRechazado() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.validarYRenovar("KLU382", SURA));
        assertTrue(ex.getMessage().contains("no tiene ningún SOAT"));
    }

    @Test
    public void testAseguradoraInactivaOSinConvenioSoatEsRechazada() {
        agregarSoat(AUTO, "SOAT-8", HOY.plusDays(5));
        assertThrows(ReglaNegocioException.class, () -> servicio.validarYRenovar("KLU382", INACTIVA));
        assertThrows(ReglaNegocioException.class, () -> servicio.validarYRenovar("KLU382", SIN_SOAT));
    }

    // ---------------------------------------------------------------- SOAT existente y estado documental

    @Test
    public void testRegistrarSoatExistenteVigente() throws Exception {
        Soat s = servicio.registrarSoatExistente("KLU382", "soat-ext-1", SURA, HOY.minusMonths(2), new BigDecimal("570000"));

        assertEquals("SOAT-EXT-1", s.getNumeroPoliza());
        assertEquals(HOY.minusMonths(2).plusYears(1).minusDays(1), s.getFechaVencimiento());
        assertEquals(EstadoDocumental.AL_DIA, servicio.consultarEstadoDocumental(AUTO).estado());
    }

    @Test
    public void testRegistrarSoatQueSeCruzaConOtroEsRechazado() throws Exception {
        servicio.registrarSoatExistente("KLU382", "SOAT-EXT-2", SURA, HOY.minusMonths(2), new BigDecimal("570000"));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarSoatExistente("KLU382", "SOAT-EXT-3", SURA, HOY.minusDays(1), new BigDecimal("570000")));
    }

    @Test
    public void testEstadoDocumentalPendienteSiElSoatYaVencio() throws Exception {
        agregarSoat(AUTO, "SOAT-9", HOY.minusDays(1));
        repositorio.estados.put(AUTO, new RegistroDocumental(AUTO, EstadoDocumental.AL_DIA, "registro viejo", LocalDateTime.now()));

        RegistroDocumental estado = servicio.consultarEstadoDocumental(AUTO);

        assertEquals(EstadoDocumental.PENDIENTE, estado.estado());
        assertTrue(estado.observacion().contains("venció"));
    }

    @Test
    public void testTarifarioSegunTipoYCilindraje() {
        assertEquals(new BigDecimal("260000"), TarifarioSoat.calcularValor("MOTOCICLETA", 99));
        assertEquals(new BigDecimal("350000"), TarifarioSoat.calcularValor("MOTOCICLETA", 200));
        assertEquals(new BigDecimal("470000"), TarifarioSoat.calcularValor("AUTOMOVIL", 1400));
        assertEquals(new BigDecimal("670000"), TarifarioSoat.calcularValor("AUTOMOVIL", 3000));
        assertEquals(new BigDecimal("980000"), TarifarioSoat.calcularValor("CAMIONETA", 2488));
        assertThrows(ReglaNegocioException.class, () -> TarifarioSoat.calcularValor("BUS", 6000));
    }

    // ---------------------------------------------------------------- más casos del flujo 4A

    @Test
    public void test4ABloqueoAdministrativoReportadoPorLaAseguradora() throws Exception {
        agregarSoat(AUTO, "SOAT-10", HOY.plusDays(5));
        aseguradora.restricciones = new AseguradoraExterna.ConsultaRestricciones(false, true, "embargo judicial");

        ResultadoRenovacionSoat r = servicio.validarYRenovar("KLU382", SURA);

        assertEquals(ResultadoRenovacion.NO_CUMPLE_REQUISITOS, r.resultado());
        assertTrue(r.mensaje().contains("bloqueo administrativo"));
        assertTrue(r.mensaje().contains("embargo judicial"));
        assertNull(aseguradora.ultimaSolicitud, "No se solicita la emisión");
    }

    @Test
    public void test4AMatriculaSuspendidaEsBloqueoAdministrativo() throws Exception {
        agregarSoat(AUTO, "SOAT-11", HOY.plusDays(5));
        repositorio.matriculas.get(AUTO).setEstadoRegistro(EstadoMatricula.SUSPENDIDA);

        ResultadoRenovacionSoat r = servicio.validarYRenovar("KLU382", SURA);

        assertEquals(ResultadoRenovacion.NO_CUMPLE_REQUISITOS, r.resultado());
        assertTrue(r.mensaje().contains("suspendida"));
        assertEquals(1, repositorio.soats.size());
    }

    @Test
    public void testMatriculaEnTramiteSiPermiteRenovar() throws Exception {
        agregarSoat(AUTO, "SOAT-12", HOY.plusDays(5));
        repositorio.matriculas.get(AUTO).setEstadoRegistro(EstadoMatricula.EN_TRAMITE);

        assertTrue(servicio.validarYRenovar("KLU382", SURA).fueRenovado());
    }

    // ---------------------------------------------------------------- datos enviados y mensaje (pasos 6 y 8)

    @Test
    public void testLaSolicitudALaAseguradoraLlevaElValorYLaVigencia() throws Exception {
        agregarSoat(MOTO, "SOAT-13", HOY.plusDays(20));

        servicio.validarYRenovar("NKS81A", SURA);

        AseguradoraExterna.SolicitudEmisionSoat solicitud = aseguradora.ultimaSolicitud;
        assertEquals("SURA", solicitud.codigoAseguradora());
        assertEquals("MOTOCICLETA", solicitud.tipoVehiculo());
        assertEquals(890, solicitud.cilindrajeCc());
        assertEquals(new BigDecimal("790000"), solicitud.valor());
        assertEquals(HOY.plusDays(21), solicitud.fechaInicio());
        assertEquals(HOY.plusDays(20).plusYears(1), solicitud.fechaVencimiento());
    }

    @Test
    public void testElMensajeNoRepiteElPuntoDelNombreDeLaAseguradora() throws Exception {
        aseguradoras.guardar(new co.edu.javeriana.concesionario.legal.modelo.Aseguradora(
                BOLIVAR, "BOLIVAR", "Seguros Bolívar S.A.", "860.002.503-2", null, true, true, true));
        agregarSoat(AUTO, "SOAT-14", HOY.plusDays(5));

        ResultadoRenovacionSoat r = servicio.validarYRenovar("KLU382", BOLIVAR);

        assertTrue(r.mensaje().contains("Seguros Bolívar S.A.\n"));
        assertFalse(r.mensaje().contains("S.A.."));
    }

    // ---------------------------------------------------------------- tarifario: valores límite

    @ParameterizedTest(name = "{0} de {1} cc cuesta {2}")
    @CsvSource({
        "MOTOCICLETA, 99, 260000",
        "MOTOCICLETA, 100, 350000",
        "MOTOCICLETA, 200, 350000",
        "MOTOCICLETA, 201, 790000",
        "AUTOMOVIL, 1499, 470000",
        "AUTOMOVIL, 1500, 570000",
        "AUTOMOVIL, 2500, 570000",
        "AUTOMOVIL, 2501, 670000",
        "CAMIONETA, 1499, 820000",
        "CAMIONETA, 1500, 980000",
        "CAMIONETA, 2501, 1150000",
        "UTILITARIO, 1499, 900000",
        "UTILITARIO, 2500, 1050000",
        "UTILITARIO, 2501, 1200000"
    })
    public void testTarifarioValoresLimite(String tipo, int cilindraje, long valorEsperado) {
        assertEquals(BigDecimal.valueOf(valorEsperado), TarifarioSoat.calcularValor(tipo, cilindraje));
    }

    @Test
    public void testTarifarioRechazaCilindrajeOTipoInvalido() {
        assertThrows(ReglaNegocioException.class, () -> TarifarioSoat.calcularValor("AUTOMOVIL", 0));
        assertThrows(ReglaNegocioException.class, () -> TarifarioSoat.calcularValor(null, 1600));
    }

    // ---------------------------------------------------------------- precondiciones y datos de entrada

    @Test
    public void testPlacaYAseguradoraSonObligatorias() {
        agregarSoat(AUTO, "SOAT-15", HOY.plusDays(5));
        assertThrows(ReglaNegocioException.class, () -> servicio.validarYRenovar("  ", SURA));
        assertThrows(ReglaNegocioException.class, () -> servicio.validarYRenovar("KLU382", null));
        assertThrows(ReglaNegocioException.class, () -> servicio.validarYRenovar("KLU382", 99L));
    }

    @Test
    public void testRegistrarSoatExistenteValidaLosDatos() {
        BigDecimal valor = new BigDecimal("570000");
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarSoatExistente("KLU382", "SOAT-EXT-9", SURA, HOY.plusDays(1), valor), "Fecha futura");
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarSoatExistente("KLU382", "SOAT-EXT-9", SURA, HOY, BigDecimal.ZERO), "Valor cero");
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarSoatExistente("KLU382", "AB", SURA, HOY, valor), "Número muy corto");
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarSoatExistente("KLU382", "SOAT-EXT-9", SIN_SOAT, HOY, valor), "Convenio sin SOAT");
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarSoatExistente("KLU382", "SOAT-EXT-9", null, HOY, valor), "Sin aseguradora");
        assertTrue(repositorio.soats.isEmpty());
    }

    @Test
    public void testRegistrarSoatExistenteYaVencidoDejaDocumentacionPendiente() throws Exception {
        servicio.registrarSoatExistente("KLU382", "SOAT-EXT-10", SURA, HOY.minusYears(1).minusDays(3), new BigDecimal("570000"));

        assertEquals(EstadoDocumental.PENDIENTE, repositorio.estados.get(AUTO).estado());
        assertEquals(EstadoDocumental.PENDIENTE, servicio.consultarEstadoDocumental(AUTO).estado());
    }

    // ---------------------------------------------------------------- estado documental

    @Test
    public void testSinSoatElEstadoDocumentalEsPendiente() throws Exception {
        RegistroDocumental estado = servicio.consultarEstadoDocumental(AUTO);
        assertEquals(EstadoDocumental.PENDIENTE, estado.estado());
        assertTrue(estado.observacion().contains("no tiene SOAT"));
    }

    @Test
    public void test6AElRechazoPrevaleceAunqueElSoatSigaVigente() throws Exception {
        agregarSoat(AUTO, "SOAT-16", HOY.plusDays(5));
        aseguradora.rechazo = "datos inconsistentes con el RUNT";

        servicio.validarYRenovar("KLU382", SURA);

        assertEquals(EstadoDocumental.PENDIENTE, servicio.consultarEstadoDocumental(AUTO).estado(),
                "El SOAT vence en 5 días, pero el vehículo queda pendiente por el rechazo");
    }

    // ---------------------------------------------------------------- dobles de prueba

    /** Aseguradora cuya respuesta se controla desde cada prueba. */
    private static class AseguradoraControlada implements AseguradoraExterna {
        ConsultaRestricciones restricciones = new ConsultaRestricciones(false, false, null);
        String rechazo;
        SolicitudEmisionSoat ultimaSolicitud;
        private int emitidos;

        @Override
        public ConsultaRestricciones consultarRestricciones(String codigoAseguradora, String placa) {
            return restricciones;
        }

        @Override
        public RespuestaEmision emitirSoat(SolicitudEmisionSoat solicitud) {
            ultimaSolicitud = solicitud;
            return rechazo != null
                    ? RespuestaEmision.rechazada(rechazo)
                    : RespuestaEmision.aprobada("SOAT-NUEVO-" + (++emitidos), "AUT-" + emitidos);
        }
    }

    private static class RepositorioEnMemoria implements SoatRepositorio {
        final Map<Long, VehiculoLegal> vehiculos = new HashMap<>();
        final Map<Long, Matricula> matriculas = new HashMap<>();
        final List<Soat> soats = new ArrayList<>();
        final Map<Long, RegistroDocumental> estados = new HashMap<>();

        void agregarVehiculo(VehiculoLegal v, int cilindraje) {
            vehiculos.put(v.id(), v);
            matriculas.put(v.id(), new Matricula(v.id(), v.id(), v.placa(), "LT-" + v.placa(), "Movilidad Bogotá",
                    LocalDate.of(v.modeloAnio(), 1, 15), cilindraje, EstadoMatricula.ACTIVA, 2026, null));
        }

        @Override
        public List<Soat> listarPorVehiculo(Long vehiculoId) {
            return soats.stream().filter(s -> s.getVehiculoId().equals(vehiculoId))
                    .sorted(Comparator.comparing(Soat::getFechaVencimiento).reversed()).toList();
        }

        @Override
        public Optional<Soat> buscarPorNumero(String numero) {
            return soats.stream().filter(s -> s.getNumeroPoliza().equals(numero)).findFirst();
        }

        @Override
        public Soat guardarConEstadoDocumental(Soat soat, EstadoDocumental estado, String observacion) {
            soat.setId((long) soats.size() + 1);
            soats.add(soat);
            actualizarEstadoDocumental(soat.getVehiculoId(), estado, observacion);
            return soat;
        }

        @Override
        public void actualizarEstadoDocumental(Long vehiculoId, EstadoDocumental estado, String observacion) {
            estados.put(vehiculoId, new RegistroDocumental(vehiculoId, estado, observacion, LocalDateTime.now()));
        }

        @Override public Optional<RegistroDocumental> buscarEstadoDocumental(Long id) { return Optional.ofNullable(estados.get(id)); }

        @Override
        public Optional<Matricula> buscarMatriculaPorPlaca(String placa) {
            return matriculas.values().stream().filter(m -> m.getPlaca().equals(placa)).findFirst();
        }

        @Override public Optional<VehiculoLegal> buscarVehiculo(Long id) { return Optional.ofNullable(vehiculos.get(id)); }
    }
}
