package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.ClienteServicio;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaComercial;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.persistencia.ClienteDAO;
import co.edu.javeriana.concesionario.clientes.persistencia.PqrsDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CategoriaComercialTest {

    private ClienteDaoFalso dao;
    private ClienteServicio servicio;

    @BeforeEach
    public void setUp() {
        dao = new ClienteDaoFalso();
        servicio = new ClienteServicio(dao, new PqrsDAO());
    }

    private Cliente registrar(String documento, String ingresos) throws Exception {
        return servicio.registrarCliente(documento, "CC", "Laura", "Díaz", null, null, new BigDecimal(ingresos));
    }

    @Test
    public void clasificacionAutomaticaRespetaLosLimitesDeIngresos() {
        assertEquals(CategoriaComercial.ESTANDAR, servicio.clasificarCategoriaPorIngresos(new BigDecimal("0")));
        assertEquals(CategoriaComercial.ESTANDAR, servicio.clasificarCategoriaPorIngresos(new BigDecimal("4999999")));
        assertEquals(CategoriaComercial.PREFERENCIAL, servicio.clasificarCategoriaPorIngresos(new BigDecimal("5000000")));
        assertEquals(CategoriaComercial.PREFERENCIAL, servicio.clasificarCategoriaPorIngresos(new BigDecimal("14999999")));
        assertEquals(CategoriaComercial.CORPORATIVO, servicio.clasificarCategoriaPorIngresos(new BigDecimal("15000000")));
        assertEquals(CategoriaComercial.CORPORATIVO, servicio.clasificarCategoriaPorIngresos(new BigDecimal("29999999")));
        assertEquals(CategoriaComercial.VIP, servicio.clasificarCategoriaPorIngresos(new BigDecimal("30000000")));
    }

    @Test
    public void clienteNuevoQuedaConCategoriaAutomatica() throws Exception {
        Cliente c = registrar("1012345678", "8000000");

        Cliente guardado = dao.buscarPorId(c.getId()).orElseThrow();
        assertEquals(CategoriaComercial.PREFERENCIAL, guardado.getCategoriaComercial());
        assertFalse(guardado.isCategoriaManual());
    }

    @Test
    public void cambiarCategoriaLaGuardaComoManual() throws Exception {
        Cliente c = registrar("1012345678", "8000000");

        servicio.cambiarCategoria(c.getId(), CategoriaComercial.VIP);

        Cliente guardado = dao.buscarPorId(c.getId()).orElseThrow();
        assertEquals(CategoriaComercial.VIP, guardado.getCategoriaComercial());
        assertTrue(guardado.isCategoriaManual());
    }

    @Test
    public void editarIngresosNoCambiaUnaCategoriaManual() throws Exception {
        Cliente c = registrar("1012345678", "8000000");
        servicio.cambiarCategoria(c.getId(), CategoriaComercial.VIP);

        servicio.editarCliente(c.getId(), "1012345678", "CC", "Laura", "Díaz", null, null, new BigDecimal("1000000"));

        Cliente guardado = dao.buscarPorId(c.getId()).orElseThrow();
        assertEquals(CategoriaComercial.VIP, guardado.getCategoriaComercial());
        assertTrue(guardado.isCategoriaManual());
    }

    @Test
    public void editarIngresosRecalculaUnaCategoriaAutomatica() throws Exception {
        Cliente c = registrar("1012345678", "8000000");

        servicio.editarCliente(c.getId(), "1012345678", "CC", "Laura", "Díaz", null, null, new BigDecimal("20000000"));

        assertEquals(CategoriaComercial.CORPORATIVO, dao.buscarPorId(c.getId()).orElseThrow().getCategoriaComercial());
    }

    @Test
    public void restablecerVuelveALaCategoriaSegunIngresos() throws Exception {
        Cliente c = registrar("1012345678", "8000000");
        servicio.cambiarCategoria(c.getId(), CategoriaComercial.VIP);

        servicio.restablecerCategoriaAutomatica(c.getId());

        Cliente guardado = dao.buscarPorId(c.getId()).orElseThrow();
        assertEquals(CategoriaComercial.PREFERENCIAL, guardado.getCategoriaComercial());
        assertFalse(guardado.isCategoriaManual());
    }

    @Test
    public void restablecerUnaCategoriaQueYaEsAutomaticaEsRechazado() throws Exception {
        Cliente c = registrar("1012345678", "8000000");

        assertThrows(IllegalArgumentException.class, () -> servicio.restablecerCategoriaAutomatica(c.getId()));
    }

    @Test
    public void cambiarCategoriaSinCategoriaOClienteInexistenteEsRechazado() throws Exception {
        Cliente c = registrar("1012345678", "8000000");

        assertThrows(IllegalArgumentException.class, () -> servicio.cambiarCategoria(c.getId(), null));
        assertThrows(IllegalArgumentException.class, () -> servicio.cambiarCategoria(99L, CategoriaComercial.VIP));
        assertThrows(IllegalArgumentException.class, () -> servicio.cambiarCategoria(null, CategoriaComercial.VIP));
        assertFalse(dao.buscarPorId(c.getId()).orElseThrow().isCategoriaManual());
    }

    private static class ClienteDaoFalso extends ClienteDAO {
        private final Map<Long, Cliente> datos = new LinkedHashMap<>();
        private long siguienteId = 1;

        @Override
        public Cliente guardar(Cliente c) {
            c.setId(siguienteId++);
            datos.put(c.getId(), copiar(c));
            return c;
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
            Cliente actual = datos.get(c.getId());
            if (actual == null) {
                return false;
            }
            Cliente nuevo = copiar(c);
            nuevo.setCategoriaManual(actual.isCategoriaManual());
            datos.put(c.getId(), nuevo);
            return true;
        }

        @Override
        public boolean actualizarCategoria(Long id, CategoriaComercial categoria, boolean manual) {
            Cliente c = datos.get(id);
            if (c == null) {
                return false;
            }
            c.setCategoriaComercial(categoria);
            c.setCategoriaManual(manual);
            return true;
        }

        private static Cliente copiar(Cliente o) {
            Cliente c = new Cliente(o.getId(), o.getDocumento(), o.getTipoDocumento(), o.getNombres(), o.getApellidos(),
                    o.getEmail(), o.getTelefono(), o.getIngresosMensuales(), o.getCategoriaComercial());
            c.setCategoriaManual(o.isCategoriaManual());
            return c;
        }
    }
}
