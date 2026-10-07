package co.edu.javeriana.concesionario.comun;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class ConexionBDTest {

    @BeforeAll
    public static void setUp() {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    @Test
    public void testConexionActiva() throws Exception {
        Connection conn = ConexionBD.obtenerConexion();
        assertNotNull(conn);
        assertFalse(conn.isClosed());
    }

    @Test
    public void testTablasPrincipalesCreadas() throws Exception {
        Connection conn = ConexionBD.obtenerConexion();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT count(*) FROM usuarios");
        assertTrue(rs.next());
        assertTrue(rs.getInt(1) > 0, "Debe haber usuarios iniciales");
    }
}
