package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.ClienteServicio;
import co.edu.javeriana.concesionario.clientes.logica.PqrsServicio;
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

public class ClienteIntegracionTest {

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
             PreparedStatement pqrs = conn.prepareStatement(
                     "DELETE FROM pqrs WHERE cliente_id IN (SELECT id FROM clientes WHERE documento LIKE '77000001%')");
             PreparedStatement clientes = conn.prepareStatement(
                     "DELETE FROM clientes WHERE documento LIKE '77000001%'")) {
            pqrs.executeUpdate();
            clientes.executeUpdate();
        }
        ConexionBD.cerrarConexion();
    }

    @Test
    public void editarGuardaLosCambiosEnLaBaseDeDatos() throws Exception {
        Cliente c = servicio.registrarCliente("7700000101", "CC", "Prueba", "Editar", null, null,
                new BigDecimal("2000000"));

        servicio.editarCliente(c.getId(), "7700000102", "CC", "Prueba", "Editada", "editada@mail.com",
                "3001112233", new BigDecimal("20000000"));

        Cliente guardado = servicio.buscarPorId(c.getId()).orElseThrow();
        assertEquals("7700000102", guardado.getDocumento());
        assertEquals("Editada", guardado.getApellidos());
        assertEquals("editada@mail.com", guardado.getEmail());
        assertEquals(0, new BigDecimal("20000000").compareTo(guardado.getIngresosMensuales()));
        assertEquals(CategoriaComercial.CORPORATIVO, guardado.getCategoriaComercial());
    }

    @Test
    public void eliminarClienteSinRegistrosAsociados() throws Exception {
        Cliente c = servicio.registrarCliente("7700000103", "CC", "Prueba", "Eliminar", null, null,
                new BigDecimal("2000000"));

        servicio.eliminarCliente(c.getId());

        assertTrue(servicio.buscarPorId(c.getId()).isEmpty());
    }

    @Test
    public void eliminarClienteConPqrsEsRechazado() throws Exception {
        Cliente c = servicio.registrarCliente("7700000104", "CC", "Prueba", "Con PQRS", null, null,
                new BigDecimal("2000000"));
        new PqrsServicio().radicarPqrs(c.getId(), "PETICION", "Integración: cliente con PQRS");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarCliente(c.getId()));

        assertTrue(e.getMessage().contains("1 PQRS"));
        assertTrue(servicio.buscarPorId(c.getId()).isPresent());
    }

    @Test
    public void elClienteSemillaConEncuestasYPqrsNoSePuedeEliminar() throws Exception {
        Cliente maria = servicio.buscarPorDocumento("1020987654").orElseThrow();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarCliente(maria.getId()));

        assertTrue(e.getMessage().contains("encuesta(s) de satisfacción"));
    }
}
