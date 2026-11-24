package co.edu.javeriana.concesionario.inventario;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.financiacion.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.inventario.logica.InventarioServicio;
import co.edu.javeriana.concesionario.inventario.modelo.Bodega;
import co.edu.javeriana.concesionario.inventario.modelo.CategoriaVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.Vehiculo;
import co.edu.javeriana.concesionario.inventario.persistencia.BodegaDAO;
import co.edu.javeriana.concesionario.inventario.persistencia.VehiculoDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del CU-06 (gestionar catálogo de vehículos y motocicletas).
 * Cubren el registro de vehículos con sus validaciones, las reglas de bodega
 * que aplica el registro (RN-07-07 y RN-07-08) y las consultas del catálogo.
 * Usan la base en memoria para no tocar el archivo de datos real.
 *
 * Responsable: Juan Pablo León
 */
public class VehiculoCatalogoTest {

    private static InventarioServicio servicio;
    private static VehiculoDAO vehiculoDAO;
    private static BodegaDAO bodegaDAO;
    private static final AtomicInteger SECUENCIA = new AtomicInteger(1);

    private static final BigDecimal PRECIO = new BigDecimal("75000000.00");

    @BeforeAll
    public static void setUp() {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        vehiculoDAO = new VehiculoDAO();
        bodegaDAO = new BodegaDAO();
        servicio = new InventarioServicio(vehiculoDAO, bodegaDAO);
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    /** VIN único por prueba; el prefijo evita choques con los VIN de otras clases de prueba. */
    private String nuevoVin() {
        return "VIN-CU06-" + SECUENCIA.getAndIncrement();
    }

    /** Placa única de máximo 10 caracteres, como exige la tabla vehiculos. */
    private String nuevaPlaca() {
        return "C06" + SECUENCIA.getAndIncrement();
    }

    private Bodega nuevaBodega(int capacidad, boolean activa) throws SQLException {
        int n = SECUENCIA.getAndIncrement();
        return bodegaDAO.guardar(new Bodega(null, "BOD-C06-" + n, "Bodega CU-06 " + n,
                "Bogotá", capacidad, activa));
    }

    private Vehiculo registrarEn(Long bodegaId) throws SQLException {
        return servicio.registrarVehiculo(nuevoVin(), nuevaPlaca(), CategoriaVehiculo.AUTOMOVIL,
                "Renault", 2025, "Duster Intens", "Gris", PRECIO, bodegaId);
    }

    // ------------------------------------------------------------------
    // Registro exitoso
    // ------------------------------------------------------------------

    @Test
    public void testRegistrarVehiculoValidoQuedaDisponibleSinKilometraje() throws Exception {
        Bodega b = nuevaBodega(10, true);
        Vehiculo v = registrarEn(b.getId());

        assertNotNull(v.getId(), "El vehículo registrado debe recibir un id");
        assertEquals("DISPONIBLE", v.getEstado());
        assertEquals(0, v.getKilometraje());
        assertEquals(b.getId(), v.getBodegaId());
    }

    @Test
    public void testRegistrarMotocicletaPersisteTodosLosCampos() throws Exception {
        Bodega b = nuevaBodega(10, true);
        String vin = nuevoVin();
        String placa = nuevaPlaca();
        Vehiculo v = servicio.registrarVehiculo(vin, placa, CategoriaVehiculo.MOTOCICLETA, "Yamaha",
                2026, "MT-07", "Azul", new BigDecimal("42000000.00"), b.getId());

        Vehiculo leido = vehiculoDAO.buscarPorId(v.getId()).orElseThrow();
        assertEquals(vin, leido.getVin());
        assertEquals(placa, leido.getPlaca());
        assertEquals(CategoriaVehiculo.MOTOCICLETA, leido.getTipo());
        assertEquals("Yamaha", leido.getMarca());
        assertEquals(2026, leido.getModeloAnio());
        assertEquals("MT-07", leido.getLinea());
        assertEquals("Azul", leido.getColor());
        assertEquals(0, new BigDecimal("42000000.00").compareTo(leido.getPrecioVenta()));
        assertEquals(b.getId(), leido.getBodegaId());
    }

    // ------------------------------------------------------------------
    // Validaciones de campos obligatorios y precio
    // ------------------------------------------------------------------

    @Test
    public void testRegistrarSinVinEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarVehiculo("   ", nuevaPlaca(),
                CategoriaVehiculo.AUTOMOVIL, "Renault", 2025, "Duster", "Gris", PRECIO, 1L));
    }

    @Test
    public void testRegistrarSinMarcaEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarVehiculo(nuevoVin(), nuevaPlaca(),
                CategoriaVehiculo.AUTOMOVIL, null, 2025, "Duster", "Gris", PRECIO, 1L));
    }

    @Test
    public void testRegistrarSinLineaEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarVehiculo(nuevoVin(), nuevaPlaca(),
                CategoriaVehiculo.AUTOMOVIL, "Renault", 2025, "", "Gris", PRECIO, 1L));
    }

    @Test
    public void testRegistrarConPrecioNegativoEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarVehiculo(nuevoVin(), nuevaPlaca(),
                CategoriaVehiculo.AUTOMOVIL, "Renault", 2025, "Duster", "Gris",
                new BigDecimal("-1000.00"), 1L));
    }

    // ------------------------------------------------------------------
    // Reglas de bodega aplicadas al registrar (RN-07-07 y RN-07-08)
    // ------------------------------------------------------------------

    @Test
    public void testRegistrarEnBodegaInexistenteEsRechazadoYNoGuardaNada() throws Exception {
        int antes = vehiculoDAO.listarTodos().size();

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> registrarEn(999_999L));
        assertTrue(ex.getMessage().contains("no existe"));
        assertEquals(antes, vehiculoDAO.listarTodos().size(), "Un registro rechazado no debe quedar guardado");
    }

    @Test
    public void testRegistrarEnBodegaInactivaEsRechazado() throws Exception {
        Bodega inactiva = nuevaBodega(10, false);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> registrarEn(inactiva.getId()));
        assertTrue(ex.getMessage().contains("inactiva"));
    }

    @Test
    public void testRegistrarEnBodegaLlenaEsRechazado() throws Exception {
        Bodega b = nuevaBodega(1, true);
        registrarEn(b.getId());

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> registrarEn(b.getId()));
        assertTrue(ex.getMessage().contains("capacidad"));
        assertEquals(1, servicio.ocupacionBodega(b.getId()));
    }

    @Test
    public void testRegistrarConVinDuplicadoEsRechazadoPorLaBase() throws Exception {
        Bodega b = nuevaBodega(10, true);
        String vin = nuevoVin();
        servicio.registrarVehiculo(vin, nuevaPlaca(), CategoriaVehiculo.AUTOMOVIL, "Kia", 2025,
                "Picanto", "Blanco", PRECIO, b.getId());

        assertThrows(SQLException.class, () -> servicio.registrarVehiculo(vin, nuevaPlaca(),
                CategoriaVehiculo.AUTOMOVIL, "Kia", 2025, "Picanto", "Negro", PRECIO, b.getId()));
    }

    // ------------------------------------------------------------------
    // Consultas del catálogo
    // ------------------------------------------------------------------

    @Test
    public void testListarDisponiblesExcluyeVehiculosNoDisponibles() throws Exception {
        Bodega b = nuevaBodega(10, true);
        Vehiculo disponible = registrarEn(b.getId());
        Vehiculo vendido = vehiculoDAO.guardar(new Vehiculo(null, nuevoVin(), nuevaPlaca(),
                CategoriaVehiculo.CAMIONETA, "Mazda", 2024, "CX-5", "Rojo", PRECIO, "VENDIDO", 8000, b.getId()));

        List<Vehiculo> disponibles = servicio.listarDisponibles();
        assertTrue(disponibles.stream().anyMatch(v -> v.getId().equals(disponible.getId())));
        assertFalse(disponibles.stream().anyMatch(v -> v.getId().equals(vendido.getId())));
        assertTrue(disponibles.stream().allMatch(v -> "DISPONIBLE".equals(v.getEstado())));

        // listarTodos sí incluye la unidad vendida.
        assertTrue(vehiculoDAO.listarTodos().stream().anyMatch(v -> v.getId().equals(vendido.getId())));
    }

    @Test
    public void testBuscarPorIdInexistenteDevuelveVacio() throws Exception {
        Optional<Vehiculo> resultado = vehiculoDAO.buscarPorId(999_999L);
        assertTrue(resultado.isEmpty());
    }
}
