package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.ClienteServicio;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaComercial;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;

public class CategoriaComercialIntegracionTest {

    private static ClienteServicio servicio;

    @BeforeAll
    public static void setUp() {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        servicio = new ClienteServicio();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM clientes WHERE documento LIKE '77000003%'")) {
            stmt.executeUpdate();
        }
        ConexionBD.cerrarConexion();
    }

    @Test
    public void laCategoriaManualSeGuardaYSeRespetaAlEditar() throws Exception {
        Cliente c = servicio.registrarCliente("7700000301", "CC", "Prueba", "Categoria", null, null,
                new BigDecimal("2000000"));
        assertFalse(servicio.buscarPorId(c.getId()).orElseThrow().isCategoriaManual());

        servicio.cambiarCategoria(c.getId(), CategoriaComercial.VIP);
        servicio.editarCliente(c.getId(), "7700000301", "CC", "Prueba", "Editada", null, null,
                new BigDecimal("3000000"));

        Cliente guardado = servicio.buscarPorId(c.getId()).orElseThrow();
        assertEquals(CategoriaComercial.VIP, guardado.getCategoriaComercial());
        assertTrue(guardado.isCategoriaManual());
        assertEquals("Editada", guardado.getApellidos());
    }

    @Test
    public void restablecerGuardaLaCategoriaAutomatica() throws Exception {
        Cliente c = servicio.registrarCliente("7700000302", "CC", "Prueba", "Restablecer", null, null,
                new BigDecimal("20000000"));
        servicio.cambiarCategoria(c.getId(), CategoriaComercial.ESTANDAR);

        servicio.restablecerCategoriaAutomatica(c.getId());

        Cliente guardado = servicio.buscarPorId(c.getId()).orElseThrow();
        assertEquals(CategoriaComercial.CORPORATIVO, guardado.getCategoriaComercial());
        assertFalse(guardado.isCategoriaManual());
    }

    @Test
    public void losClientesSemillaEmpiezanConCategoriaAutomatica() throws Exception {
        Cliente carlos = servicio.buscarPorDocumento("1018456789").orElseThrow();

        assertFalse(carlos.isCategoriaManual());
        assertEquals(CategoriaComercial.PREFERENCIAL, carlos.getCategoriaComercial());
    }
}
