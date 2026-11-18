package co.edu.javeriana.concesionario.legal;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.legal.logica.MatriculaServicio;
import co.edu.javeriana.concesionario.legal.modelo.EstadoMatricula;
import co.edu.javeriana.concesionario.legal.modelo.Matricula;
import co.edu.javeriana.concesionario.legal.persistencia.MatriculaDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CU-21 contra H2: el DAO, la transacción que asigna la placa (RF-28) y la semilla.
 * Cada prueba usa sus propios vehículos (ids altos) para no depender de las demás.
 */
public class MatriculaDAOTest {
    private static MatriculaServicio servicio;

    @BeforeAll
    public static void setUp() {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        servicio = new MatriculaServicio(new MatriculaDAO());
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    private static void insertarVehiculo(long id, String vin, String placa, String tipo, String estado) throws SQLException {
        try (Statement stmt = ConexionBD.obtenerConexion().createStatement()) {
            stmt.executeUpdate("MERGE INTO vehiculos (id, vin, placa, tipo, marca, modelo_anio, linea, color, precio_venta, estado, kilometraje) "
                    + "KEY(vin) VALUES (" + id + ", '" + vin + "', " + (placa == null ? "NULL" : "'" + placa + "'") + ", '" + tipo
                    + "', 'Renault', 2025, 'Duster', 'Gris', 90000000, '" + estado + "', 0)");
        }
    }

    private static String placaDelVehiculo(long id) throws SQLException {
        try (Statement stmt = ConexionBD.obtenerConexion().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT placa FROM vehiculos WHERE id = " + id)) {
            rs.next();
            return rs.getString(1);
        }
    }

    private static Matricula matricula(long vehiculoId, String placa, String licencia) {
        return new Matricula(null, vehiculoId, placa, licencia, "Secretaría Distrital de Movilidad de Bogotá",
                LocalDate.now().minusMonths(3), 1598, EstadoMatricula.ACTIVA, LocalDate.now().getYear(), new BigDecimal("1300000"));
    }

    @Test
    public void testSemillaMatriculaLosVehiculosDelInventario() throws Exception {
        List<Matricula> lista = servicio.listarMatriculas();
        assertTrue(lista.stream().anyMatch(m -> m.getPlaca().equals("KLU382") && m.getCilindrajeCc() == 1798));
        assertTrue(lista.stream().anyMatch(m -> m.getPlaca().equals("NKS817")));
    }

    @Test
    public void testMatricularAsignaLaPlacaAlVehiculoEnLaMismaTransaccion() throws Exception {
        insertarVehiculo(9201L, "LEGALTEST00000001", null, "AUTOMOVIL", "DISPONIBLE");

        Matricula m = servicio.registrarMatricula(matricula(9201L, "QWE123", "LT-9201"));

        assertNotNull(m.getId());
        assertEquals("QWE123", placaDelVehiculo(9201L));
        assertEquals(EstadoMatricula.ACTIVA, servicio.buscarMatricula(m.getId()).orElseThrow().getEstadoRegistro());
        assertTrue(servicio.listarVehiculosSinMatricula().stream().noneMatch(v -> v.id() == 9201L));
    }

    @Test
    public void testActualizarYEliminarMatricula() throws Exception {
        insertarVehiculo(9202L, "LEGALTEST00000002", null, "AUTOMOVIL", "DISPONIBLE");
        Matricula m = servicio.registrarMatricula(matricula(9202L, "QWE124", "LT-9202"));

        m.setEstadoRegistro(EstadoMatricula.CANCELADA);
        m.setUltimoAnioImpuestoPagado(null);
        servicio.actualizarMatricula(m);
        assertNull(servicio.buscarMatricula(m.getId()).orElseThrow().getUltimoAnioImpuestoPagado());

        servicio.eliminarMatricula(m.getId());
        assertTrue(servicio.buscarMatricula(m.getId()).isEmpty());
        assertEquals("QWE124", placaDelVehiculo(9202L), "La placa queda en el inventario");
    }

    @Test
    public void testSiFallaLaAsignacionDePlacaSeDeshaceLaMatricula() throws Exception {
        insertarVehiculo(9203L, "LEGALTEST00000003", null, "AUTOMOVIL", "DISPONIBLE");
        insertarVehiculo(9210L, "LEGALTEST00000010", "QWE130", "AUTOMOVIL", "DISPONIBLE");

        // Se llama al DAO sin pasar por el servicio: el INSERT de la matrícula funciona, pero el UPDATE
        // de la placa falla porque QWE130 ya es de otro vehículo (placa única en el inventario)
        Matricula conPlacaAjena = matricula(9203L, "QWE130", "LT-9203");
        assertThrows(SQLException.class, () -> new MatriculaDAO().guardar(conPlacaAjena));

        assertTrue(new MatriculaDAO().buscarPorVehiculo(9203L).isEmpty(), "El rollback deshace el INSERT de la matrícula");
        assertNull(placaDelVehiculo(9203L));
    }
}
