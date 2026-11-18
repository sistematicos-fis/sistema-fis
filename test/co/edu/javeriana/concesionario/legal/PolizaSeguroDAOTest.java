package co.edu.javeriana.concesionario.legal;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.legal.logica.PolizaSeguroServicio;
import co.edu.javeriana.concesionario.legal.modelo.Aseguradora;
import co.edu.javeriana.concesionario.legal.modelo.EstadoPoliza;
import co.edu.javeriana.concesionario.legal.modelo.PolizaSeguro;
import co.edu.javeriana.concesionario.legal.modelo.TipoPoliza;
import co.edu.javeriana.concesionario.legal.persistencia.AseguradoraDAO;
import co.edu.javeriana.concesionario.legal.persistencia.PolizaSeguroDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CU-23 contra H2: el DAO de pólizas y el servicio juntos.
 */
public class PolizaSeguroDAOTest {
    private static PolizaSeguroServicio polizas;
    private static Aseguradora sura;

    @BeforeAll
    public static void setUp() throws Exception {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        AseguradoraDAO aseguradoraDAO = new AseguradoraDAO();
        polizas = new PolizaSeguroServicio(new PolizaSeguroDAO(), aseguradoraDAO);
        sura = aseguradoraDAO.buscarPorCodigo("SURA").orElseThrow();
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

    @Test
    public void testCrudDePolizaYFiltradoDePolizasAntiguasDeSoat() throws Exception {
        insertarVehiculo(9204L, "LEGALTEST00000004", "QWE126", "CAMIONETA", "DISPONIBLE");
        LocalDate hoy = LocalDate.now();
        PolizaSeguro p = polizas.registrarPoliza(new PolizaSeguro(null, "TR-9204", sura.getId(), 9204L,
                TipoPoliza.TODO_RIESGO_RESPONSABILIDAD, new BigDecimal("4200000"), new BigDecimal("128000000"),
                hoy, hoy.plusYears(1).minusDays(1), EstadoPoliza.VIGENTE));

        PolizaSeguro leida = polizas.buscarPoliza(p.getId()).orElseThrow();
        assertEquals(TipoPoliza.TODO_RIESGO_RESPONSABILIDAD, leida.getTipo());
        assertEquals(0, new BigDecimal("4200000").compareTo(leida.getValorPrima()));

        leida.setEstado(EstadoPoliza.CANCELADA);
        polizas.actualizarPoliza(leida);
        assertEquals(EstadoPoliza.CANCELADA, polizas.buscarPoliza(p.getId()).orElseThrow().getEstado());

        assertTrue(polizas.listarPolizas().stream().allMatch(x -> x.getTipo() != null), "No aparecen filas de tipo SOAT");
        polizas.eliminarPoliza(p.getId());
        assertTrue(polizas.buscarPoliza(p.getId()).isEmpty());
    }
}
