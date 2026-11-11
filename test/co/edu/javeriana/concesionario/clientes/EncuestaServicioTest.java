package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.EncuestaServicio;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.modelo.EncuestaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.persistencia.ClienteDAO;
import co.edu.javeriana.concesionario.clientes.persistencia.EncuestaDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class EncuestaServicioTest {

    private static final Long CLIENTE = 1L;

    private EncuestaDaoFalso encuestaDao;
    private EncuestaServicio servicio;

    @BeforeEach
    public void setUp() {
        encuestaDao = new EncuestaDaoFalso();
        servicio = new EncuestaServicio(encuestaDao, new ClienteDaoFalso());
    }

    @Test
    public void registrarEncuestaValidaLaGuardaConFecha() throws Exception {
        EncuestaSatisfaccion e = servicio.registrarEncuesta(CLIENTE, 5, 4, 3, "  Buena atención  ");

        assertNotNull(e.getId());
        assertEquals(CLIENTE, e.getClienteId());
        assertEquals(5, e.getPuntuacionAtencion());
        assertEquals(4, e.getPuntuacionProducto());
        assertEquals(3, e.getPuntuacionTiempos());
        assertEquals("Buena atención", e.getComentarios());
        assertNotNull(e.getFecha());
    }

    @Test
    public void registrarSinComentariosLosDejaVacios() throws Exception {
        EncuestaSatisfaccion e = servicio.registrarEncuesta(CLIENTE, 3, 3, 3, "   ");

        assertNull(e.getComentarios());
    }

    @Test
    public void registrarSinClienteOConClienteInexistenteEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarEncuesta(null, 5, 5, 5, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarEncuesta(99L, 5, 5, 5, null));
        assertTrue(encuestaDao.datos.isEmpty());
    }

    @Test
    public void registrarConPuntuacionFueraDeRangoEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarEncuesta(CLIENTE, 0, 5, 5, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarEncuesta(CLIENTE, 5, 6, 5, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarEncuesta(CLIENTE, 5, 5, -1, null));
        assertTrue(encuestaDao.datos.isEmpty());
    }

    @Test
    public void registrarConComentariosMuyLargosEsRechazado() {
        String largo = "x".repeat(EncuestaServicio.LONGITUD_MAX_COMENTARIOS + 1);
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarEncuesta(CLIENTE, 4, 4, 4, largo));
    }

    @Test
    public void editarEncuestaCambiaPuntuacionesYComentariosPeroNoElCliente() throws Exception {
        EncuestaSatisfaccion e = servicio.registrarEncuesta(CLIENTE, 2, 2, 2, "Regular");

        servicio.editarEncuesta(e.getId(), 4, 5, 3, "Mejoró después de la llamada");

        EncuestaSatisfaccion editada = encuestaDao.buscarPorId(e.getId()).orElseThrow();
        assertEquals(4, editada.getPuntuacionAtencion());
        assertEquals(5, editada.getPuntuacionProducto());
        assertEquals(3, editada.getPuntuacionTiempos());
        assertEquals("Mejoró después de la llamada", editada.getComentarios());
        assertEquals(CLIENTE, editada.getClienteId());
    }

    @Test
    public void editarConPuntuacionInvalidaNoCambiaNada() throws Exception {
        EncuestaSatisfaccion e = servicio.registrarEncuesta(CLIENTE, 2, 2, 2, "Regular");

        assertThrows(IllegalArgumentException.class, () -> servicio.editarEncuesta(e.getId(), 7, 2, 2, "Regular"));
        assertEquals(2, encuestaDao.buscarPorId(e.getId()).orElseThrow().getPuntuacionAtencion());
    }

    @Test
    public void eliminarEncuestaLaBorra() throws Exception {
        EncuestaSatisfaccion e = servicio.registrarEncuesta(CLIENTE, 5, 5, 5, null);

        servicio.eliminarEncuesta(e.getId());

        assertTrue(encuestaDao.buscarPorId(e.getId()).isEmpty());
    }

    @Test
    public void operacionSobreEncuestaInexistenteEsRechazada() {
        assertThrows(IllegalArgumentException.class, () -> servicio.editarEncuesta(500L, 3, 3, 3, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.eliminarEncuesta(500L));
        assertThrows(IllegalArgumentException.class, () -> servicio.eliminarEncuesta(null));
    }

    @Test
    public void listarPorClienteDevuelveSoloLasDelCliente() throws Exception {
        servicio.registrarEncuesta(CLIENTE, 5, 5, 5, "Una");
        servicio.registrarEncuesta(CLIENTE, 4, 4, 4, "Dos");
        encuestaDao.registrar(new EncuestaSatisfaccion(null, 2L, 1, 1, 1, "De otro cliente"));

        assertEquals(2, servicio.listarPorCliente(CLIENTE).size());
        assertEquals(3, servicio.listarEncuestas().size());
    }

    private static class ClienteDaoFalso extends ClienteDAO {
        @Override
        public Optional<Cliente> buscarPorId(Long id) {
            if (CLIENTE.equals(id)) {
                Cliente c = new Cliente();
                c.setId(CLIENTE);
                return Optional.of(c);
            }
            return Optional.empty();
        }
    }

    private static class EncuestaDaoFalso extends EncuestaDAO {
        private final Map<Long, EncuestaSatisfaccion> datos = new LinkedHashMap<>();
        private long siguienteId = 1;

        @Override
        public EncuestaSatisfaccion registrar(EncuestaSatisfaccion e) {
            e.setId(siguienteId++);
            datos.put(e.getId(), copiar(e));
            return e;
        }

        @Override
        public List<EncuestaSatisfaccion> listarTodas() {
            List<EncuestaSatisfaccion> lista = new ArrayList<>();
            for (EncuestaSatisfaccion e : datos.values()) {
                lista.add(copiar(e));
            }
            return lista;
        }

        @Override
        public List<EncuestaSatisfaccion> listarPorCliente(Long clienteId) {
            List<EncuestaSatisfaccion> lista = new ArrayList<>();
            for (EncuestaSatisfaccion e : datos.values()) {
                if (e.getClienteId().equals(clienteId)) {
                    lista.add(copiar(e));
                }
            }
            return lista;
        }

        @Override
        public Optional<EncuestaSatisfaccion> buscarPorId(Long id) {
            EncuestaSatisfaccion e = datos.get(id);
            return e == null ? Optional.empty() : Optional.of(copiar(e));
        }

        @Override
        public boolean actualizar(EncuestaSatisfaccion e) {
            if (!datos.containsKey(e.getId())) {
                return false;
            }
            datos.put(e.getId(), copiar(e));
            return true;
        }

        @Override
        public boolean eliminar(Long id) {
            return datos.remove(id) != null;
        }

        private static EncuestaSatisfaccion copiar(EncuestaSatisfaccion o) {
            EncuestaSatisfaccion e = new EncuestaSatisfaccion(o.getId(), o.getClienteId(), o.getPuntuacionAtencion(),
                    o.getPuntuacionProducto(), o.getPuntuacionTiempos(), o.getComentarios());
            e.setFecha(o.getFecha());
            return e;
        }
    }
}
