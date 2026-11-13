package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.EncuestaServicio;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaComercial;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.modelo.EncuestaSatisfaccion;
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

public class EncuestaIntegracionTest {

    private static final String DOCUMENTO_PRUEBA = "7700000201";

    private static EncuestaServicio servicio;
    private static Long clienteId;

    @BeforeAll
    public static void setUp() throws Exception {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        Cliente cliente = new Cliente(null, DOCUMENTO_PRUEBA, "CC", "Prueba", "Encuestas", null, null,
                new BigDecimal("1000000"), CategoriaComercial.ESTANDAR);
        clienteId = new ClienteDAO().guardar(cliente).getId();
        servicio = new EncuestaServicio();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        ejecutar("DELETE FROM encuestas_satisfaccion WHERE cliente_id = ?");
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
    public void registrarGuardaLaEncuestaYEntraAlCalculoDelIndice() throws Exception {
        EncuestaSatisfaccion e = servicio.registrarEncuesta(clienteId, 4, 5, 3, "Integración: encuesta posventa");

        EncuestaSatisfaccion guardada = servicio.buscarEncuesta(e.getId()).orElseThrow();
        assertEquals(clienteId, guardada.getClienteId());
        assertEquals(4, guardada.getPuntuacionAtencion());
        assertEquals("Integración: encuesta posventa", guardada.getComentarios());
        assertNotNull(guardada.getFecha());

        List<EncuestaSatisfaccion> delPeriodo = new IndiceSatisfaccionDAO()
                .listarEncuestasPorCliente(clienteId, LocalDate.now().minusDays(1), LocalDate.now());
        assertTrue(delPeriodo.stream().anyMatch(x -> x.getId().equals(e.getId())));
    }

    @Test
    public void editarGuardaLosCambiosEnLaBaseDeDatos() throws Exception {
        EncuestaSatisfaccion e = servicio.registrarEncuesta(clienteId, 1, 1, 1, "Integración: texto inicial");

        servicio.editarEncuesta(e.getId(), 3, 4, 5, "Integración: texto corregido");

        EncuestaSatisfaccion editada = servicio.buscarEncuesta(e.getId()).orElseThrow();
        assertEquals(3, editada.getPuntuacionAtencion());
        assertEquals(4, editada.getPuntuacionProducto());
        assertEquals(5, editada.getPuntuacionTiempos());
        assertEquals("Integración: texto corregido", editada.getComentarios());
    }

    @Test
    public void eliminarBorraLaEncuestaDeLaBaseDeDatos() throws Exception {
        EncuestaSatisfaccion e = servicio.registrarEncuesta(clienteId, 2, 2, 2, null);

        servicio.eliminarEncuesta(e.getId());

        assertTrue(servicio.buscarEncuesta(e.getId()).isEmpty());
        assertTrue(servicio.listarPorCliente(clienteId).stream().noneMatch(x -> x.getId().equals(e.getId())));
    }
}
