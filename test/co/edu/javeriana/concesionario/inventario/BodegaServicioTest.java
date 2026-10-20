package co.edu.javeriana.concesionario.inventario;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.financiacion.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.inventario.logica.InventarioServicio;
import co.edu.javeriana.concesionario.inventario.modelo.Bodega;
import co.edu.javeriana.concesionario.inventario.modelo.CategoriaVehiculo;
import co.edu.javeriana.concesionario.inventario.persistencia.BodegaDAO;
import co.edu.javeriana.concesionario.inventario.persistencia.VehiculoDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del CU-07 (gestionar bodegas y patios de almacenamiento).
 * Usan la base en memoria para no tocar el archivo de datos real.
 *
 * Responsable: Juan Pablo León
 */
public class BodegaServicioTest {

    private static InventarioServicio servicio;
    private static final AtomicInteger SECUENCIA = new AtomicInteger(1);

    @BeforeAll
    public static void setUp() {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        servicio = new InventarioServicio(new VehiculoDAO(), new BodegaDAO());
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    /** Crea una sede con código único para que las pruebas no choquen entre sí. */
    private Bodega nuevaBodega(int capacidad) {
        int n = SECUENCIA.getAndIncrement();
        return new Bodega(null, "BOD-T" + n, "Bodega de Prueba " + n, "Bogotá", capacidad, true);
    }

    @Test
    public void testConsultarBodegasSemilla() throws Exception {
        List<Bodega> lista = servicio.listarBodegas();
        assertTrue(lista.stream().anyMatch(b -> "BOD-NORTE".equals(b.getCodigo())));
        assertTrue(lista.stream().anyMatch(b -> "BOD-CENTRO".equals(b.getCodigo())));
    }

    @Test
    public void testRegistrarBodegaValida() throws Exception {
        Bodega guardada = servicio.registrarBodega(nuevaBodega(30));
        assertNotNull(guardada.getId());
    }

    @Test
    public void testRegistrarSinCodigoEsRechazado() {
        Bodega b = nuevaBodega(10);
        b.setCodigo("   ");
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarBodega(b));
    }

    @Test
    public void testRegistrarSinNombreEsRechazado() {
        Bodega b = nuevaBodega(10);
        b.setNombre("");
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarBodega(b));
    }

    @Test
    public void testRegistrarCapacidadCeroEsRechazada() {
        Bodega b = nuevaBodega(0);
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarBodega(b));
    }

    @Test
    public void testRegistrarCodigoDuplicadoEsRechazado() throws Exception {
        Bodega primera = servicio.registrarBodega(nuevaBodega(20));
        Bodega duplicada = nuevaBodega(20);
        duplicada.setCodigo(primera.getCodigo());
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarBodega(duplicada));
    }

    @Test
    public void testActualizarBodega() throws Exception {
        Bodega b = servicio.registrarBodega(nuevaBodega(25));
        b.setNombre("Patio Renombrado");
        b.setCiudad("Medellín");
        b.setCapacidadVehiculos(45);
        servicio.actualizarBodega(b);

        Bodega leida = servicio.buscarBodega(b.getId()).orElseThrow();
        assertEquals("Patio Renombrado", leida.getNombre());
        assertEquals("Medellín", leida.getCiudad());
        assertEquals(45, leida.getCapacidadVehiculos());
    }

    @Test
    public void testActualizarConservandoSuPropioCodigo() throws Exception {
        Bodega b = servicio.registrarBodega(nuevaBodega(15));
        b.setNombre("Mismo código, otro nombre");
        assertDoesNotThrow(() -> servicio.actualizarBodega(b));
    }

    @Test
    public void testReducirCapacidadPorDebajoDeOcupacionEsRechazada() throws Exception {
        Bodega b = servicio.registrarBodega(nuevaBodega(5));
        registrarVehiculoEn(b.getId());
        registrarVehiculoEn(b.getId());

        // Capacidad 1 es válida por sí sola (RN-07-04), pero queda por debajo de
        // los 2 vehículos ya almacenados, así que debe fallar por RN-07-05.
        b.setCapacidadVehiculos(1);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.actualizarBodega(b));
        assertTrue(ex.getMessage().contains("2"), "El mensaje debe indicar la ocupación actual");
    }

    @Test
    public void testDesactivarBodegaNoBorraVehiculos() throws Exception {
        Bodega b = servicio.registrarBodega(nuevaBodega(5));
        registrarVehiculoEn(b.getId());

        servicio.desactivarBodega(b.getId());

        assertFalse(servicio.buscarBodega(b.getId()).orElseThrow().isActivo());
        assertEquals(1, servicio.ocupacionBodega(b.getId()));
    }

    @Test
    public void testListarBodegasActivasExcluyeInactivas() throws Exception {
        Bodega b = servicio.registrarBodega(nuevaBodega(8));
        servicio.desactivarBodega(b.getId());

        assertTrue(servicio.listarBodegasActivas().stream()
                .noneMatch(x -> x.getId().equals(b.getId())));
    }

    @Test
    public void testEliminarBodegaConVehiculosEsRechazada() throws Exception {
        Bodega b = servicio.registrarBodega(nuevaBodega(5));
        registrarVehiculoEn(b.getId());

        assertThrows(ReglaNegocioException.class, () -> servicio.eliminarBodega(b.getId()));
    }

    @Test
    public void testEliminarBodegaSinVehiculos() throws Exception {
        Bodega b = servicio.registrarBodega(nuevaBodega(5));
        servicio.eliminarBodega(b.getId());
        assertTrue(servicio.buscarBodega(b.getId()).isEmpty());
    }

    @Test
    public void testHayCupoEnBodegaLlena() throws Exception {
        Bodega b = servicio.registrarBodega(nuevaBodega(1));
        assertTrue(servicio.hayCupo(b.getId()));

        registrarVehiculoEn(b.getId());
        assertFalse(servicio.hayCupo(b.getId()));
    }

    private void registrarVehiculoEn(Long bodegaId) throws Exception {
        int n = SECUENCIA.getAndIncrement();
        servicio.registrarVehiculo("VIN-TEST-" + n, "TST" + n, CategoriaVehiculo.AUTOMOVIL,
                "Marca", 2024, "Linea", "Color", new BigDecimal("50000000.00"), bodegaId);
    }
}
