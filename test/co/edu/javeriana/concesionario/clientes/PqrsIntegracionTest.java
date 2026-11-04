package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.PqrsServicio;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaComercial;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.modelo.Pqrs;
import co.edu.javeriana.concesionario.clientes.persistencia.ClienteDAO;
import co.edu.javeriana.concesionario.clientes.persistencia.IndiceSatisfaccionDAO;
import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PqrsIntegracionTest {

    private static final String DOCUMENTO_PRUEBA = "7700000003";

    private static PqrsServicio servicio;
    private static Long clienteId;

    @BeforeAll
    public static void setUp() throws Exception {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        Cliente cliente = new Cliente(null, DOCUMENTO_PRUEBA, "CC", "Prueba", "PQRS", null, null,
                new BigDecimal("1000000"), CategoriaComercial.ESTANDAR);
        clienteId = new ClienteDAO().guardar(cliente).getId();
        servicio = new PqrsServicio();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        ejecutar("DELETE FROM pqrs WHERE cliente_id = ?");
        ejecutar("DELETE FROM clientes WHERE id = ?");
        ConexionBD.cerrarConexion();
    }

    private static void ejecutar(String sql) throws Exception {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, clienteId);
            stmt.executeUpdate();
        }
    }

    @Test
    public void radicarGuardaLaPqrsAbiertaEnLaBaseDeDatos() throws Exception {
        Pqrs p = servicio.radicarPqrs(clienteId, "PETICION", "Integración: solicitar certificado");

        Pqrs guardada = servicio.buscarPqrs(p.getId()).orElseThrow();
        assertEquals("PETICION", guardada.getTipo());
        assertEquals("ABIERTO", guardada.getEstado());
        assertNotNull(guardada.getFechaRadicacion());
        assertNull(guardada.getFechaResolucion());
        assertTrue(servicio.listarPorCliente(clienteId).stream().anyMatch(x -> x.getId().equals(p.getId())));
    }

    @Test
    public void cerrarGuardaLaResolucionYLaPqrsEntraAlCalculoDelIndice() throws Exception {
        Pqrs p = servicio.radicarPqrs(clienteId, "QUEJA", "Integración: demora en la entrega");
        servicio.iniciarGestion(p.getId());
        servicio.cerrarPqrs(p.getId(), "Se entregó el vehículo y se ofreció un descuento");

        Pqrs cerrada = servicio.buscarPqrs(p.getId()).orElseThrow();
        assertEquals("CERRADO", cerrada.getEstado());
        assertEquals("Se entregó el vehículo y se ofreció un descuento", cerrada.getRespuesta());
        assertNotNull(cerrada.getFechaResolucion());

        List<Pqrs> resueltas = new IndiceSatisfaccionDAO()
                .listarPqrsResueltasPorCliente(clienteId, LocalDate.now().minusDays(1), LocalDate.now());
        assertTrue(resueltas.stream().anyMatch(x -> x.getId().equals(p.getId())));
    }

    @Test
    public void editarYEliminarUnaPqrsAbierta() throws Exception {
        Pqrs p = servicio.radicarPqrs(clienteId, "SUGERENCIA", "Integración: texto inicial");

        servicio.editarPqrs(p.getId(), "RECLAMO", "Integración: texto corregido");
        Pqrs editada = servicio.buscarPqrs(p.getId()).orElseThrow();
        assertEquals("RECLAMO", editada.getTipo());
        assertEquals("Integración: texto corregido", editada.getDescripcion());

        servicio.eliminarPqrs(p.getId());
        assertTrue(servicio.buscarPqrs(p.getId()).isEmpty());
    }
}
