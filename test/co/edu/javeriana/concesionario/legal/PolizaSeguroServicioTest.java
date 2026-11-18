package co.edu.javeriana.concesionario.legal;

import co.edu.javeriana.concesionario.legal.logica.PolizaSeguroServicio;
import co.edu.javeriana.concesionario.legal.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.legal.modelo.EstadoPoliza;
import co.edu.javeriana.concesionario.legal.modelo.PolizaSeguro;
import co.edu.javeriana.concesionario.legal.modelo.TipoPoliza;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;
import co.edu.javeriana.concesionario.legal.persistencia.PolizaSeguroRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reglas del CU-23 con repositorios en memoria y un reloj fijo: hoy es 19/10/2026.
 */
public class PolizaSeguroServicioTest {
    private static final LocalDate HOY = LocalDate.of(2026, 10, 19);
    private static final Long DISPONIBLE = 1L;
    private static final Long ALQUILADO = 2L;
    private static final Long VENDIDO = 3L;
    private static final long SURA = 10L;          // activa, cubre daños a terceros
    private static final long SOLO_SOAT = 11L;     // activa, su convenio solo cubre SOAT
    private static final long INACTIVA = 12L;

    private RepositorioEnMemoria repositorio;
    private PolizaSeguroServicio servicio;

    @BeforeEach
    public void setUp() {
        repositorio = new RepositorioEnMemoria();
        repositorio.vehiculos.add(new VehiculoLegal(DISPONIBLE, "VIN1", "KLU382", "AUTOMOVIL", "Toyota", "Corolla", 2024, "DISPONIBLE"));
        repositorio.vehiculos.add(new VehiculoLegal(ALQUILADO, "VIN2", "LMP941", "CAMIONETA", "Mazda", "CX-30", 2024, "ALQUILADO"));
        repositorio.vehiculos.add(new VehiculoLegal(VENDIDO, "VIN3", "NKS817", "MOTOCICLETA", "Yamaha", "MT-09", 2025, "VENDIDO"));
        AseguradorasEnMemoria aseguradoras = new AseguradorasEnMemoria();
        aseguradoras.agregar(SURA, "SURA", true, true, true);
        aseguradoras.agregar(SOLO_SOAT, "SOLOSOAT", true, false, true);
        aseguradoras.agregar(INACTIVA, "INACTIVA", true, true, false);
        servicio = new PolizaSeguroServicio(repositorio, aseguradoras,
                Clock.fixed(Instant.parse("2026-10-19T12:00:00Z"), ZoneOffset.UTC));
    }

    private PolizaSeguro poliza(String numero, Long vehiculoId, LocalDate inicio) {
        return new PolizaSeguro(null, numero, SURA, vehiculoId, TipoPoliza.TODO_RIESGO_RESPONSABILIDAD,
                new BigDecimal("3500000"), new BigDecimal("115000000"), inicio, inicio.plusYears(1).minusDays(1),
                EstadoPoliza.VIGENTE);
    }

    // ---------------------------------------------------------------- registrar

    @Test
    public void testRegistrarPolizaDeUnidadDeLaFlota() throws Exception {
        PolizaSeguro p = servicio.registrarPoliza(poliza(" pol-tr-001 ", DISPONIBLE, HOY));

        assertNotNull(p.getId());
        assertEquals("POL-TR-001", p.getNumeroPoliza());
        assertEquals(EstadoPoliza.VIGENTE, servicio.estadoEfectivo(p));
    }

    @Test
    public void testVehiculoVendidoNoHacePartedeLaFlota() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarPoliza(poliza("POL-002", VENDIDO, HOY)));
        assertTrue(ex.getMessage().contains("vendido"));
    }

    @Test
    public void testAseguradoraSinConvenioDeDanosATercerosEsRechazada() {
        PolizaSeguro p = poliza("POL-003", DISPONIBLE, HOY);
        p.setAseguradoraId(SOLO_SOAT);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.registrarPoliza(p));
        assertTrue(ex.getMessage().contains("no cubre"));
    }

    @Test
    public void testAseguradoraInactivaEsRechazada() {
        PolizaSeguro p = poliza("POL-004", DISPONIBLE, HOY);
        p.setAseguradoraId(INACTIVA);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.registrarPoliza(p));
        assertTrue(ex.getMessage().contains("inactiva"));
    }

    @Test
    public void testValorAseguradoDebeSuperarLaPrima() {
        PolizaSeguro p = poliza("POL-005", DISPONIBLE, HOY);
        p.setValorCobertura(new BigDecimal("3000000"));
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarPoliza(p));
    }

    @Test
    public void testVigenciaMayorAUnAnioEsRechazada() {
        PolizaSeguro p = poliza("POL-006", DISPONIBLE, HOY);
        p.setFechaVencimiento(HOY.plusYears(1).plusDays(1));
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.registrarPoliza(p));
        assertTrue(ex.getMessage().contains("un año"));
    }

    @Test
    public void testNumeroDePolizaRepetidoEsRechazado() throws Exception {
        servicio.registrarPoliza(poliza("POL-007", DISPONIBLE, HOY));
        PolizaSeguro otra = poliza("POL-007", ALQUILADO, HOY);
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarPoliza(otra));
    }

    @Test
    public void testNoSeCruzanDosPolizasDelMismoTipo() throws Exception {
        servicio.registrarPoliza(poliza("POL-008", DISPONIBLE, HOY));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarPoliza(poliza("POL-009", DISPONIBLE, HOY.plusMonths(6))));
        assertTrue(ex.getMessage().contains("POL-008"));
    }

    @Test
    public void testPolizaDeOtroTipoOConsecutivaSiSePermite() throws Exception {
        servicio.registrarPoliza(poliza("POL-010", DISPONIBLE, HOY));

        PolizaSeguro rc = poliza("POL-011", DISPONIBLE, HOY);
        rc.setTipo(TipoPoliza.EXTRACONTRACTUAL);
        servicio.registrarPoliza(rc);
        servicio.registrarPoliza(poliza("POL-012", DISPONIBLE, HOY.plusYears(1)));

        assertEquals(3, servicio.listarPolizas().size());
    }

    // ---------------------------------------------------------------- actualizar, cancelar y eliminar

    @Test
    public void testActualizarPrimaYCobertura() throws Exception {
        PolizaSeguro p = servicio.registrarPoliza(poliza("POL-013", DISPONIBLE, HOY));
        PolizaSeguro cambios = poliza("POL-013", DISPONIBLE, HOY);
        cambios.setId(p.getId());
        cambios.setValorPrima(new BigDecimal("4000000"));

        servicio.actualizarPoliza(cambios);

        assertEquals(new BigDecimal("4000000"), servicio.buscarPoliza(p.getId()).orElseThrow().getValorPrima());
    }

    @Test
    public void testNoSeCancelaLaPolizaDeUnVehiculoAlquilado() throws Exception {
        PolizaSeguro p = servicio.registrarPoliza(poliza("POL-014", ALQUILADO, HOY.minusMonths(2)));
        PolizaSeguro cambios = poliza("POL-014", ALQUILADO, HOY.minusMonths(2));
        cambios.setId(p.getId());
        cambios.setEstado(EstadoPoliza.CANCELADA);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.actualizarPoliza(cambios));
        assertTrue(ex.getMessage().contains("alquilado"));
        assertThrows(ReglaNegocioException.class, () -> servicio.eliminarPoliza(p.getId()));
    }

    @Test
    public void testCancelarYEliminarPolizaDeVehiculoDisponible() throws Exception {
        PolizaSeguro p = servicio.registrarPoliza(poliza("POL-015", DISPONIBLE, HOY));
        PolizaSeguro cambios = poliza("POL-015", DISPONIBLE, HOY);
        cambios.setId(p.getId());
        cambios.setEstado(EstadoPoliza.CANCELADA);

        servicio.actualizarPoliza(cambios);
        assertEquals(EstadoPoliza.CANCELADA, servicio.estadoEfectivo(servicio.buscarPoliza(p.getId()).orElseThrow()));

        // Ya cancelada no bloquea una póliza nueva del mismo tipo en las mismas fechas
        servicio.registrarPoliza(poliza("POL-016", DISPONIBLE, HOY));
        servicio.eliminarPoliza(p.getId());
        assertEquals(1, servicio.listarPolizas().size());
    }

    @Test
    public void testPolizaQueYaTerminoSeMuestraVencida() throws Exception {
        PolizaSeguro p = servicio.registrarPoliza(poliza("POL-017", DISPONIBLE, HOY.minusYears(1).minusDays(5)));
        assertEquals(EstadoPoliza.VENCIDA, servicio.estadoEfectivo(p));
    }

    @Test
    public void testAseguradorasDisponiblesSoloActivasConCoberturaATerceros() throws Exception {
        assertEquals(List.of("SURA"), servicio.listarAseguradorasDisponibles().stream().map(a -> a.getCodigo()).toList());
    }

    // ---------------------------------------------------------------- repositorio en memoria

    private static class RepositorioEnMemoria implements PolizaSeguroRepositorio {
        final List<VehiculoLegal> vehiculos = new ArrayList<>();
        final List<PolizaSeguro> polizas = new ArrayList<>();
        private long secuencia = 1;

        @Override public PolizaSeguro guardar(PolizaSeguro p) { p.setId(secuencia++); polizas.add(p); return p; }
        @Override public void actualizar(PolizaSeguro p) { polizas.replaceAll(a -> a.getId().equals(p.getId()) ? p : a); }
        @Override public void eliminar(Long id) { polizas.removeIf(p -> p.getId().equals(id)); }
        @Override public List<PolizaSeguro> listarTodas() { return new ArrayList<>(polizas); }
        @Override public Optional<PolizaSeguro> buscarPorId(Long id) { return polizas.stream().filter(p -> p.getId().equals(id)).findFirst(); }
        @Override public Optional<PolizaSeguro> buscarPorNumero(String n) { return polizas.stream().filter(p -> p.getNumeroPoliza().equals(n)).findFirst(); }
        @Override public List<PolizaSeguro> listarPorVehiculo(Long id) { return polizas.stream().filter(p -> p.getVehiculoId().equals(id)).toList(); }
        @Override public List<VehiculoLegal> listarVehiculosFlota() { return vehiculos.stream().filter(VehiculoLegal::perteneceAFlota).toList(); }
        @Override public List<VehiculoLegal> listarVehiculos() { return new ArrayList<>(vehiculos); }
        @Override public Optional<VehiculoLegal> buscarVehiculo(Long id) { return vehiculos.stream().filter(v -> v.id().equals(id)).findFirst(); }
    }
}
