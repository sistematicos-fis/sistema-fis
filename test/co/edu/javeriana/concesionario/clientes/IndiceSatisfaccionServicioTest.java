package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.IndiceSatisfaccionServicio;
import co.edu.javeriana.concesionario.clientes.logica.IndiceSatisfaccionServicio.ResumenCalculo;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.modelo.EncuestaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.Pqrs;
import co.edu.javeriana.concesionario.clientes.modelo.ResultadoIndiceSatisfaccion;
import co.edu.javeriana.concesionario.clientes.persistencia.IndiceSatisfaccionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class IndiceSatisfaccionServicioTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 7, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 9, 30);

    private DaoFalso dao;
    private IndiceSatisfaccionServicio servicio;

    @BeforeEach
    public void setUp() {
        dao = new DaoFalso();
        servicio = new IndiceSatisfaccionServicio(dao);
    }

    @Test
    public void calculaYGuardaElIndiceDeCadaClienteEvaluable() throws Exception {
        Cliente feliz = dao.agregarCliente(1L, "Carlos");
        dao.encuestas.put(1L, List.of(new EncuestaSatisfaccion(1L, 1L, 5, 5, 5, "ok")));

        Cliente molesto = dao.agregarCliente(2L, "María");
        dao.encuestas.put(2L, List.of(new EncuestaSatisfaccion(2L, 2L, 1, 2, 1, "mal")));
        dao.pqrs.put(2L, List.of(pqrs("RECLAMO", 25)));

        ResumenCalculo resumen = servicio.calcularIndices(DESDE, HASTA);

        assertEquals(2, resumen.getResultados().size());
        assertEquals(CategoriaSatisfaccion.SATISFECHO, buscar(resumen, feliz.getId()).getCategoria());
        assertEquals(CategoriaSatisfaccion.EN_RIESGO, buscar(resumen, molesto.getId()).getCategoria());
        assertEquals(1, resumen.contarEnRiesgo());

        assertEquals(2, dao.guardados.size());
        assertEquals(IndiceSatisfaccionServicio.MENSAJE_CLIENTE_EN_RIESGO, dao.mensajeRecibido);
        assertEquals(1, resumen.getAlertasGeneradas());
    }

    @Test
    public void sinClientesEnElPeriodoDevuelveVacioYNoGuarda() throws Exception {
        ResumenCalculo resumen = servicio.calcularIndices(DESDE, HASTA);
        assertTrue(resumen.estaVacio());
        assertTrue(dao.guardados.isEmpty());
    }

    @Test
    public void periodoInvalidoEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.calcularIndices(HASTA, DESDE));
        assertThrows(IllegalArgumentException.class, () -> servicio.calcularIndices(null, HASTA));
    }

    @Test
    public void clienteInexistenteEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.calcularIndiceCliente(99L, DESDE, HASTA));
    }

    @Test
    public void clienteSinDatosNoSeGuarda() {
        dao.agregarCliente(3L, "Sin datos");
        assertThrows(IllegalStateException.class, () -> servicio.calcularIndiceCliente(3L, DESDE, HASTA));
        assertTrue(dao.guardados.isEmpty());
    }

    @Test
    public void calculoDeUnSoloClienteConSoloPqrs() throws Exception {
        dao.agregarCliente(4L, "Empresa");
        dao.pqrs.put(4L, List.of(pqrs("PETICION", 3)));

        ResumenCalculo resumen = servicio.calcularIndiceCliente(4L, DESDE, HASTA);

        ResultadoIndiceSatisfaccion r = resumen.getResultados().get(0);
        assertEquals(new BigDecimal("100.00"), r.getIndice());
        assertNull(r.getComponenteEncuestas());
        assertEquals(1, dao.guardados.size());
    }

    private static Pqrs pqrs(String tipo, int dias) {
        Pqrs p = new Pqrs(null, null, tipo, "prueba", "CERRADO");
        p.setFechaRadicacion(LocalDateTime.of(2026, 8, 1, 9, 0));
        p.setFechaResolucion(LocalDateTime.of(2026, 8, 1, 9, 0).plusDays(dias));
        return p;
    }

    private static ResultadoIndiceSatisfaccion buscar(ResumenCalculo resumen, Long clienteId) {
        return resumen.getResultados().stream()
                .filter(r -> r.getClienteId().equals(clienteId))
                .findFirst()
                .orElseThrow();
    }

    private static class DaoFalso extends IndiceSatisfaccionDAO {
        final Map<Long, Cliente> clientes = new HashMap<>();
        final Map<Long, List<EncuestaSatisfaccion>> encuestas = new HashMap<>();
        final Map<Long, List<Pqrs>> pqrs = new HashMap<>();
        final List<ResultadoIndiceSatisfaccion> guardados = new ArrayList<>();
        String mensajeRecibido;

        Cliente agregarCliente(Long id, String nombre) {
            Cliente c = new Cliente();
            c.setId(id);
            c.setDocumento("DOC-" + id);
            c.setNombres(nombre);
            c.setApellidos("Prueba");
            c.setAsesorId(2L);
            clientes.put(id, c);
            return c;
        }

        @Override
        public List<Cliente> listarClientesEvaluables(LocalDate desde, LocalDate hasta) {
            List<Cliente> lista = new ArrayList<>();
            for (Cliente c : clientes.values()) {
                if (encuestas.containsKey(c.getId()) || pqrs.containsKey(c.getId())) {
                    lista.add(c);
                }
            }
            return lista;
        }

        @Override
        public Optional<Cliente> buscarClientePorId(Long clienteId) {
            return Optional.ofNullable(clientes.get(clienteId));
        }

        @Override
        public List<EncuestaSatisfaccion> listarEncuestasPorCliente(Long clienteId, LocalDate desde, LocalDate hasta) {
            return encuestas.getOrDefault(clienteId, Collections.emptyList());
        }

        @Override
        public List<Pqrs> listarPqrsResueltasPorCliente(Long clienteId, LocalDate desde, LocalDate hasta) {
            return pqrs.getOrDefault(clienteId, Collections.emptyList());
        }

        @Override
        public int guardarResultados(List<ResultadoIndiceSatisfaccion> resultados, String mensajeAlerta,
                                     LocalDateTime fechaCalculo) {
            guardados.addAll(resultados);
            mensajeRecibido = mensajeAlerta;
            return (int) resultados.stream().filter(ResultadoIndiceSatisfaccion::requiereAlerta).count();
        }
    }
}
