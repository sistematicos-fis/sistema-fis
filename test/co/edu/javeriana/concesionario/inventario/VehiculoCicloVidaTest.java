package co.edu.javeriana.concesionario.inventario;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.financiacion.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.inventario.logica.InventarioServicio;
import co.edu.javeriana.concesionario.inventario.logica.VerificacionInventarioServicio;
import co.edu.javeriana.concesionario.inventario.modelo.Bodega;
import co.edu.javeriana.concesionario.inventario.modelo.CategoriaVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.EstadoVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.Vehiculo;
import co.edu.javeriana.concesionario.inventario.persistencia.BodegaDAO;
import co.edu.javeriana.concesionario.inventario.persistencia.VehiculoDAO;
import co.edu.javeriana.concesionario.inventario.persistencia.VerificacionInventarioDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del ciclo de vida de los vehículos en el CU-06: actualizar datos, cambiar el
 * estado, dar de baja y eliminar (RN-06-01 a RN-06-05).
 * Usan la base en memoria para no tocar el archivo de datos real.
 *
 * Responsable: Juan Pablo León
 */
public class VehiculoCicloVidaTest {

    private static InventarioServicio servicio;
    private static VehiculoDAO vehiculoDAO;
    private static BodegaDAO bodegaDAO;
    private static VerificacionInventarioServicio verificacion;
    private static Long bodegaId;
    private static final AtomicInteger SECUENCIA = new AtomicInteger(1);

    private static final BigDecimal PRECIO = new BigDecimal("68000000.00");

    @BeforeAll
    public static void setUp() throws SQLException {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        vehiculoDAO = new VehiculoDAO();
        bodegaDAO = new BodegaDAO();
        servicio = new InventarioServicio(vehiculoDAO, bodegaDAO);
        verificacion = new VerificacionInventarioServicio(vehiculoDAO, new VerificacionInventarioDAO());
        bodegaId = bodegaDAO.guardar(new Bodega(null, "BOD-CV-01", "Bodega ciclo de vida", "Bogotá", 100, true)).getId();
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    /** VIN y placa únicos con un prefijo propio, para no chocar con otras clases de prueba. */
    private Vehiculo nuevo() throws SQLException {
        int n = SECUENCIA.getAndIncrement();
        return servicio.registrarVehiculo("VIN-CV-" + n, "CV" + n, CategoriaVehiculo.AUTOMOVIL,
                "Chevrolet", 2025, "Onix LT", "Blanco", PRECIO, bodegaId);
    }

    private Vehiculo leer(Long id) throws SQLException {
        return vehiculoDAO.buscarPorId(id).orElseThrow();
    }

    // ------------------------------------------------------------------
    // Actualizar datos (RN-06-01 a RN-06-03)
    // ------------------------------------------------------------------

    @Test
    public void testActualizarDatosQuedanGuardadosYConservaVinYBodega() throws Exception {
        Vehiculo v = nuevo();
        Vehiculo cambios = leer(v.getId());
        cambios.setColor("Rojo");
        cambios.setPrecioVenta(new BigDecimal("65500000.00"));
        cambios.setKilometraje(1200);
        cambios.setBodegaId(999_999L); // la bodega no se cambia desde el catálogo

        servicio.actualizarVehiculo(cambios);

        Vehiculo leido = leer(v.getId());
        assertEquals("Rojo", leido.getColor());
        assertEquals(0, new BigDecimal("65500000.00").compareTo(leido.getPrecioVenta()));
        assertEquals(1200, leido.getKilometraje());
        assertEquals(v.getVin(), leido.getVin());
        assertEquals(bodegaId, leido.getBodegaId());
    }

    @Test
    public void testActualizarConPrecioInvalidoOKilometrajeNegativoEsRechazado() throws Exception {
        Vehiculo v = nuevo();
        Vehiculo sinPrecio = leer(v.getId());
        sinPrecio.setPrecioVenta(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizarVehiculo(sinPrecio));

        Vehiculo kmNegativo = leer(v.getId());
        kmNegativo.setKilometraje(-5);
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizarVehiculo(kmNegativo));

        assertEquals(0, PRECIO.compareTo(leer(v.getId()).getPrecioVenta()), "Nada debe quedar guardado");
    }

    @Test
    public void testActualizarCambiandoElVinEsRechazado() throws Exception {
        Vehiculo v = nuevo();
        Vehiculo cambios = leer(v.getId());
        cambios.setVin("OTRO-VIN-123");

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.actualizarVehiculo(cambios));
        assertTrue(ex.getMessage().contains("VIN"));
    }

    @Test
    public void testActualizarVehiculoInexistenteEsRechazado() {
        Vehiculo fantasma = new Vehiculo(999_999L, "VIN-X", "X1", CategoriaVehiculo.AUTOMOVIL, "Kia", 2024,
                "Rio", "Gris", PRECIO, "DISPONIBLE", 0, bodegaId);
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizarVehiculo(fantasma));
    }

    @Test
    public void testActualizarVehiculoReservadoEsRechazado() throws Exception {
        Vehiculo v = nuevo();
        vehiculoDAO.actualizarEstado(v.getId(), EstadoVehiculo.RESERVADO.name());
        Vehiculo cambios = leer(v.getId());
        cambios.setColor("Negro");

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.actualizarVehiculo(cambios));
        assertTrue(ex.getMessage().contains("RESERVADO"));
    }

    // ------------------------------------------------------------------
    // Cambiar estado (RN-06-04)
    // ------------------------------------------------------------------

    @Test
    public void testMantenimientoSaleDeLosDisponiblesYPuedeVolver() throws Exception {
        Vehiculo v = nuevo();

        servicio.cambiarEstadoVehiculo(v.getId(), EstadoVehiculo.MANTENIMIENTO);
        assertEquals("MANTENIMIENTO", leer(v.getId()).getEstado());
        assertFalse(servicio.listarDisponibles().stream().anyMatch(x -> x.getId().equals(v.getId())));

        servicio.cambiarEstadoVehiculo(v.getId(), EstadoVehiculo.DISPONIBLE);
        assertEquals("DISPONIBLE", leer(v.getId()).getEstado());
        assertTrue(servicio.listarDisponibles().stream().anyMatch(x -> x.getId().equals(v.getId())));
    }

    @Test
    public void testEstadosDeOtrosCasosDeUsoNoSeAsignanAMano() throws Exception {
        Vehiculo v = nuevo();
        for (EstadoVehiculo e : new EstadoVehiculo[]{EstadoVehiculo.RESERVADO, EstadoVehiculo.EN_VERIFICACION,
                EstadoVehiculo.ALQUILADO, EstadoVehiculo.VENDIDO}) {
            assertThrows(ReglaNegocioException.class, () -> servicio.cambiarEstadoVehiculo(v.getId(), e),
                    "El estado " + e + " no debe asignarse desde el catálogo");
        }
        assertEquals("DISPONIBLE", leer(v.getId()).getEstado());
    }

    @Test
    public void testVehiculoVendidoNoCambiaDeEstado() throws Exception {
        Vehiculo v = nuevo();
        vehiculoDAO.actualizarEstado(v.getId(), EstadoVehiculo.VENDIDO.name());

        assertThrows(ReglaNegocioException.class,
                () -> servicio.cambiarEstadoVehiculo(v.getId(), EstadoVehiculo.DISPONIBLE));
        assertEquals("VENDIDO", leer(v.getId()).getEstado());
    }

    // ------------------------------------------------------------------
    // Dar de baja y eliminar (RN-06-05)
    // ------------------------------------------------------------------

    @Test
    public void testDarDeBajaRetiraLaUnidadYEsDefinitivo() throws Exception {
        Vehiculo v = nuevo();

        servicio.darDeBajaVehiculo(v.getId());

        assertEquals("DADO_DE_BAJA", leer(v.getId()).getEstado());
        assertFalse(servicio.listarDisponibles().stream().anyMatch(x -> x.getId().equals(v.getId())));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.cambiarEstadoVehiculo(v.getId(), EstadoVehiculo.DISPONIBLE));
        Vehiculo cambios = leer(v.getId());
        cambios.setColor("Azul");
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizarVehiculo(cambios));
    }

    @Test
    public void testEliminarVehiculoSinHistorial() throws Exception {
        Vehiculo v = nuevo();

        servicio.eliminarVehiculo(v.getId());

        assertTrue(vehiculoDAO.buscarPorId(v.getId()).isEmpty());
    }

    @Test
    public void testEliminarVehiculoConHistorialEsRechazadoYSugiereBaja() throws Exception {
        Vehiculo v = nuevo();
        verificacion.registrarConteoFisico(v.getId(), EstadoVehiculo.DISPONIBLE, true, "bodega", null);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.eliminarVehiculo(v.getId()));
        assertTrue(ex.getMessage().contains("dele de baja"));
        assertTrue(vehiculoDAO.buscarPorId(v.getId()).isPresent(), "El vehículo y su historial se conservan");
    }

    @Test
    public void testEliminarVehiculoInexistenteEsRechazado() {
        assertThrows(ReglaNegocioException.class, () -> servicio.eliminarVehiculo(999_999L));
    }

    @Test
    public void testListarVehiculosIncluyeTodosLosEstados() throws Exception {
        Vehiculo disponible = nuevo();
        Vehiculo deBaja = nuevo();
        servicio.darDeBajaVehiculo(deBaja.getId());

        assertTrue(servicio.listarVehiculos().stream().anyMatch(x -> x.getId().equals(disponible.getId())));
        assertTrue(servicio.listarVehiculos().stream().anyMatch(x -> x.getId().equals(deBaja.getId())));
    }
}
