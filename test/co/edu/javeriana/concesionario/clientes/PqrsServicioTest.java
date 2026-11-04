package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.PqrsServicio;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.modelo.Pqrs;
import co.edu.javeriana.concesionario.clientes.persistencia.ClienteDAO;
import co.edu.javeriana.concesionario.clientes.persistencia.PqrsDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PqrsServicioTest {

    private static final Long CLIENTE = 1L;

    private PqrsDaoFalso pqrsDao;
    private PqrsServicio servicio;

    @BeforeEach
    public void setUp() {
        pqrsDao = new PqrsDaoFalso();
        servicio = new PqrsServicio(pqrsDao, new ClienteDaoFalso());
    }

    @Test
    public void radicarPqrsValidaQuedaAbierta() throws Exception {
        Pqrs p = servicio.radicarPqrs(CLIENTE, "queja", "  El vehículo llegó sucio  ");

        assertNotNull(p.getId());
        assertEquals("QUEJA", p.getTipo());
        assertEquals("El vehículo llegó sucio", p.getDescripcion());
        assertEquals("ABIERTO", p.getEstado());
        assertNull(p.getFechaResolucion());
    }

    @Test
    public void radicarSinClienteEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.radicarPqrs(null, "QUEJA", "texto"));
    }

    @Test
    public void radicarConClienteInexistenteEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.radicarPqrs(99L, "QUEJA", "texto"));
    }

    @Test
    public void radicarConTipoInvalidoEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.radicarPqrs(CLIENTE, "FELICITACION", "texto"));
        assertThrows(IllegalArgumentException.class, () -> servicio.radicarPqrs(CLIENTE, "", "texto"));
    }

    @Test
    public void radicarSinDescripcionEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.radicarPqrs(CLIENTE, "PETICION", "   "));
    }

    @Test
    public void radicarConDescripcionMuyLargaEsRechazado() {
        String larga = "x".repeat(PqrsServicio.LONGITUD_MAX_DESCRIPCION + 1);
        assertThrows(IllegalArgumentException.class, () -> servicio.radicarPqrs(CLIENTE, "PETICION", larga));
    }

    @Test
    public void editarPqrsAbiertaActualizaTipoYDescripcion() throws Exception {
        Pqrs p = servicio.radicarPqrs(CLIENTE, "PETICION", "Pedir certificado");

        servicio.editarPqrs(p.getId(), "RECLAMO", "Cobro doble en la factura");

        Pqrs guardada = pqrsDao.buscarPorId(p.getId()).orElseThrow();
        assertEquals("RECLAMO", guardada.getTipo());
        assertEquals("Cobro doble en la factura", guardada.getDescripcion());
    }

    @Test
    public void iniciarGestionPasaDeAbiertoAEnProceso() throws Exception {
        Pqrs p = servicio.radicarPqrs(CLIENTE, "QUEJA", "Mala atención");

        servicio.iniciarGestion(p.getId());

        assertEquals("EN_PROCESO", pqrsDao.buscarPorId(p.getId()).orElseThrow().getEstado());
        assertThrows(IllegalArgumentException.class, () -> servicio.iniciarGestion(p.getId()));
    }

    @Test
    public void cerrarPqrsGuardaRespuestaYFechaDeResolucion() throws Exception {
        Pqrs p = servicio.radicarPqrs(CLIENTE, "RECLAMO", "Cobro de accesorios");
        servicio.iniciarGestion(p.getId());

        servicio.cerrarPqrs(p.getId(), " Se devolvió el valor cobrado ");

        Pqrs cerrada = pqrsDao.buscarPorId(p.getId()).orElseThrow();
        assertEquals("CERRADO", cerrada.getEstado());
        assertEquals("Se devolvió el valor cobrado", cerrada.getRespuesta());
        assertNotNull(cerrada.getFechaResolucion());
        assertTrue(cerrada.estaResuelta());
    }

    @Test
    public void cerrarDirectamenteDesdeAbiertoEstaPermitido() throws Exception {
        Pqrs p = servicio.radicarPqrs(CLIENTE, "SUGERENCIA", "Abrir los sábados");

        servicio.cerrarPqrs(p.getId(), "Gracias, se tendrá en cuenta");

        assertEquals("CERRADO", pqrsDao.buscarPorId(p.getId()).orElseThrow().getEstado());
    }

    @Test
    public void cerrarSinRespuestaEsRechazado() throws Exception {
        Pqrs p = servicio.radicarPqrs(CLIENTE, "QUEJA", "Demora en la entrega");

        assertThrows(IllegalArgumentException.class, () -> servicio.cerrarPqrs(p.getId(), ""));
        assertEquals("ABIERTO", pqrsDao.buscarPorId(p.getId()).orElseThrow().getEstado());
    }

    @Test
    public void cerrarPqrsYaCerradaEsRechazado() throws Exception {
        Pqrs p = servicio.radicarPqrs(CLIENTE, "QUEJA", "Demora en la entrega");
        servicio.cerrarPqrs(p.getId(), "Se entregó el vehículo");

        assertThrows(IllegalArgumentException.class, () -> servicio.cerrarPqrs(p.getId(), "Otra respuesta"));
    }

    @Test
    public void editarPqrsCerradaEsRechazado() throws Exception {
        Pqrs p = servicio.radicarPqrs(CLIENTE, "QUEJA", "Demora en la entrega");
        servicio.cerrarPqrs(p.getId(), "Se entregó el vehículo");

        assertThrows(IllegalArgumentException.class, () -> servicio.editarPqrs(p.getId(), "QUEJA", "Cambio"));
    }

    @Test
    public void eliminarPqrsAbiertaLaBorra() throws Exception {
        Pqrs p = servicio.radicarPqrs(CLIENTE, "PETICION", "Pedir cita");

        servicio.eliminarPqrs(p.getId());

        assertTrue(pqrsDao.buscarPorId(p.getId()).isEmpty());
    }

    @Test
    public void eliminarPqrsCerradaEsRechazado() throws Exception {
        Pqrs p = servicio.radicarPqrs(CLIENTE, "RECLAMO", "Cobro doble");
        servicio.cerrarPqrs(p.getId(), "Se reembolsó");

        assertThrows(IllegalArgumentException.class, () -> servicio.eliminarPqrs(p.getId()));
        assertTrue(pqrsDao.buscarPorId(p.getId()).isPresent());
    }

    @Test
    public void operacionSobrePqrsInexistenteEsRechazada() {
        assertThrows(IllegalArgumentException.class, () -> servicio.editarPqrs(500L, "QUEJA", "texto"));
        assertThrows(IllegalArgumentException.class, () -> servicio.iniciarGestion(500L));
        assertThrows(IllegalArgumentException.class, () -> servicio.cerrarPqrs(500L, "texto"));
        assertThrows(IllegalArgumentException.class, () -> servicio.eliminarPqrs(500L));
        assertThrows(IllegalArgumentException.class, () -> servicio.eliminarPqrs(null));
    }

    @Test
    public void listarPorClienteDevuelveSoloLasDelCliente() throws Exception {
        servicio.radicarPqrs(CLIENTE, "QUEJA", "Una");
        servicio.radicarPqrs(CLIENTE, "PETICION", "Dos");
        Pqrs otra = new Pqrs(null, 2L, "QUEJA", "De otro cliente", "ABIERTO");
        pqrsDao.radicar(otra);

        assertEquals(2, servicio.listarPorCliente(CLIENTE).size());
        assertEquals(3, servicio.listarPqrs().size());
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

    private static class PqrsDaoFalso extends PqrsDAO {
        private final Map<Long, Pqrs> datos = new LinkedHashMap<>();
        private long siguienteId = 1;

        @Override
        public Pqrs radicar(Pqrs pqrs) {
            pqrs.setId(siguienteId++);
            datos.put(pqrs.getId(), copiar(pqrs));
            return pqrs;
        }

        @Override
        public List<Pqrs> listarTodas() {
            List<Pqrs> lista = new ArrayList<>();
            for (Pqrs p : datos.values()) {
                lista.add(copiar(p));
            }
            return lista;
        }

        @Override
        public List<Pqrs> listarPorCliente(Long clienteId) {
            List<Pqrs> lista = new ArrayList<>();
            for (Pqrs p : datos.values()) {
                if (p.getClienteId().equals(clienteId)) {
                    lista.add(copiar(p));
                }
            }
            return lista;
        }

        @Override
        public Optional<Pqrs> buscarPorId(Long id) {
            Pqrs p = datos.get(id);
            return p == null ? Optional.empty() : Optional.of(copiar(p));
        }

        @Override
        public boolean actualizar(Pqrs pqrs) {
            Pqrs p = datos.get(pqrs.getId());
            if (p == null) {
                return false;
            }
            p.setTipo(pqrs.getTipo());
            p.setDescripcion(pqrs.getDescripcion());
            return true;
        }

        @Override
        public boolean actualizarEstado(Long id, String estado, LocalDateTime fechaResolucion, String respuesta) {
            Pqrs p = datos.get(id);
            if (p == null) {
                return false;
            }
            p.setEstado(estado);
            p.setFechaResolucion(fechaResolucion);
            p.setRespuesta(respuesta);
            return true;
        }

        @Override
        public boolean eliminar(Long id) {
            return datos.remove(id) != null;
        }

        private static Pqrs copiar(Pqrs o) {
            Pqrs p = new Pqrs(o.getId(), o.getClienteId(), o.getTipo(), o.getDescripcion(), o.getEstado());
            p.setFechaRadicacion(o.getFechaRadicacion());
            p.setFechaResolucion(o.getFechaResolucion());
            p.setRespuesta(o.getRespuesta());
            return p;
        }
    }
}
