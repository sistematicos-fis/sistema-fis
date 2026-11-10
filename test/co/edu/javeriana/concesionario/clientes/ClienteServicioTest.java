package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.ClienteServicio;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaComercial;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.persistencia.ClienteDAO;
import co.edu.javeriana.concesionario.clientes.persistencia.PqrsDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ClienteServicioTest {

    private static final BigDecimal INGRESOS = new BigDecimal("4000000");

    private ClienteDaoFalso dao;
    private ClienteServicio servicio;

    @BeforeEach
    public void setUp() {
        dao = new ClienteDaoFalso();
        servicio = new ClienteServicio(dao, new PqrsDAO());
    }

    private Cliente registrar(String documento) throws Exception {
        return servicio.registrarCliente(documento, "CC", "Laura", "Díaz", "laura@mail.com", "3001234567", INGRESOS);
    }

    @Test
    public void registrarClienteValidoLoGuardaConSuCategoria() throws Exception {
        Cliente c = servicio.registrarCliente(" 1012345678 ", "cc", " Laura ", " Díaz ", "", "", new BigDecimal("16000000"));

        assertNotNull(c.getId());
        assertEquals("1012345678", c.getDocumento());
        assertEquals("CC", c.getTipoDocumento());
        assertEquals("Laura", c.getNombres());
        assertNull(c.getEmail());
        assertNull(c.getTelefono());
        assertEquals(CategoriaComercial.CORPORATIVO, c.getCategoriaComercial());
    }

    @Test
    public void registrarConDocumentoRepetidoEsRechazado() throws Exception {
        registrar("1012345678");
        assertThrows(IllegalArgumentException.class, () -> registrar("1012345678"));
    }

    @Test
    public void registrarSinCamposObligatoriosEsRechazado() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCliente("", "CC", "Laura", "Díaz", null, null, INGRESOS));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCliente("1012345678", "CC", " ", "Díaz", null, null, INGRESOS));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCliente("1012345678", "CC", "Laura", "", null, null, INGRESOS));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCliente("1012345678", "CC", "Laura", "Díaz", null, null, null));
    }

    @Test
    public void registrarConFormatosInvalidosEsRechazado() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCliente("10123A", "CC", "Laura", "Díaz", null, null, INGRESOS));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCliente("1012345678", "TI", "Laura", "Díaz", null, null, INGRESOS));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCliente("1012345678", "CC", "Laura", "Díaz", "laura.mail.com", null, INGRESOS));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCliente("1012345678", "CC", "Laura", "Díaz", null, "300-123", INGRESOS));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarCliente("1012345678", "CC", "Laura", "Díaz", null, null, new BigDecimal("-1")));
        assertTrue(dao.datos.isEmpty());
    }

    @Test
    public void registrarNitYPasaporteConSuFormato() throws Exception {
        Cliente empresa = servicio.registrarCliente("900555444-3", "NIT", "Autos SAS", "Empresa", null, null, INGRESOS);
        Cliente extranjero = servicio.registrarCliente("AB123456", "PASAPORTE", "John", "Smith", null, null, INGRESOS);

        assertEquals("900555444-3", empresa.getDocumento());
        assertEquals("AB123456", extranjero.getDocumento());
    }

    @Test
    public void editarClienteActualizaDatosYRecalculaCategoria() throws Exception {
        Cliente c = registrar("1012345678");

        servicio.editarCliente(c.getId(), "1012345678", "CC", "Laura Marcela", "Díaz Rojas",
                "laura.diaz@mail.com", "6015551234", new BigDecimal("31000000"));

        Cliente editado = dao.buscarPorId(c.getId()).orElseThrow();
        assertEquals("Laura Marcela", editado.getNombres());
        assertEquals("Díaz Rojas", editado.getApellidos());
        assertEquals("laura.diaz@mail.com", editado.getEmail());
        assertEquals("6015551234", editado.getTelefono());
        assertEquals(CategoriaComercial.VIP, editado.getCategoriaComercial());
    }

    @Test
    public void editarConElDocumentoDeOtroClienteEsRechazado() throws Exception {
        registrar("1012345678");
        Cliente otro = registrar("1098765432");

        assertThrows(IllegalArgumentException.class, () -> servicio.editarCliente(otro.getId(), "1012345678", "CC",
                "Laura", "Díaz", null, null, INGRESOS));
        assertEquals("1098765432", dao.buscarPorId(otro.getId()).orElseThrow().getDocumento());
    }

    @Test
    public void editarConDatosInvalidosNoCambiaNada() throws Exception {
        Cliente c = registrar("1012345678");

        assertThrows(IllegalArgumentException.class, () -> servicio.editarCliente(c.getId(), "1012345678", "CC",
                "", "Díaz", null, null, INGRESOS));
        assertEquals("Laura", dao.buscarPorId(c.getId()).orElseThrow().getNombres());
    }

    @Test
    public void editarClienteInexistenteEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.editarCliente(99L, "1012345678", "CC",
                "Laura", "Díaz", null, null, INGRESOS));
        assertThrows(IllegalArgumentException.class, () -> servicio.editarCliente(null, "1012345678", "CC",
                "Laura", "Díaz", null, null, INGRESOS));
    }

    @Test
    public void eliminarClienteSinRegistrosAsociadosLoBorra() throws Exception {
        Cliente c = registrar("1012345678");

        servicio.eliminarCliente(c.getId());

        assertTrue(dao.buscarPorId(c.getId()).isEmpty());
    }

    @Test
    public void eliminarClienteConRegistrosAsociadosEsRechazadoEIndicaCuales() throws Exception {
        Cliente c = registrar("1012345678");
        dao.asociados.put("PQRS", 2);
        dao.asociados.put("contrato(s) de alquiler", 1);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarCliente(c.getId()));

        assertTrue(e.getMessage().contains("2 PQRS"));
        assertTrue(e.getMessage().contains("1 contrato(s) de alquiler"));
        assertTrue(dao.buscarPorId(c.getId()).isPresent());
    }

    @Test
    public void eliminarClienteReferenciadoEnOtraTablaEsRechazado() throws Exception {
        Cliente c = registrar("1012345678");
        dao.fallarPorLlaveForanea = true;

        assertThrows(IllegalArgumentException.class, () -> servicio.eliminarCliente(c.getId()));
        assertTrue(dao.buscarPorId(c.getId()).isPresent());
    }

    @Test
    public void eliminarClienteInexistenteEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.eliminarCliente(99L));
        assertThrows(IllegalArgumentException.class, () -> servicio.eliminarCliente(null));
    }

    private static class ClienteDaoFalso extends ClienteDAO {
        private final Map<Long, Cliente> datos = new LinkedHashMap<>();
        private final Map<String, Integer> asociados = new LinkedHashMap<>();
        private boolean fallarPorLlaveForanea;
        private long siguienteId = 1;

        @Override
        public Cliente guardar(Cliente c) {
            c.setId(siguienteId++);
            datos.put(c.getId(), copiar(c));
            return c;
        }

        @Override
        public List<Cliente> listarTodos() {
            List<Cliente> lista = new ArrayList<>();
            for (Cliente c : datos.values()) {
                lista.add(copiar(c));
            }
            return lista;
        }

        @Override
        public Optional<Cliente> buscarPorDocumento(String documento) {
            for (Cliente c : datos.values()) {
                if (c.getDocumento().equals(documento)) {
                    return Optional.of(copiar(c));
                }
            }
            return Optional.empty();
        }

        @Override
        public Optional<Cliente> buscarPorId(Long id) {
            Cliente c = datos.get(id);
            return c == null ? Optional.empty() : Optional.of(copiar(c));
        }

        @Override
        public boolean actualizar(Cliente c) {
            if (!datos.containsKey(c.getId())) {
                return false;
            }
            datos.put(c.getId(), copiar(c));
            return true;
        }

        @Override
        public boolean eliminar(Long id) throws SQLException {
            if (fallarPorLlaveForanea) {
                throw new SQLException("Referential integrity constraint violation", "23503");
            }
            return datos.remove(id) != null;
        }

        @Override
        public Map<String, Integer> contarRegistrosAsociados(Long id) {
            return new HashMap<>(asociados);
        }

        private static Cliente copiar(Cliente o) {
            return new Cliente(o.getId(), o.getDocumento(), o.getTipoDocumento(), o.getNombres(), o.getApellidos(),
                    o.getEmail(), o.getTelefono(), o.getIngresosMensuales(), o.getCategoriaComercial());
        }
    }
}
