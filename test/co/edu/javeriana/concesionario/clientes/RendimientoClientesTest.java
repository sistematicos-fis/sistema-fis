package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.EncuestaServicio;
import co.edu.javeriana.concesionario.clientes.logica.IndiceSatisfaccionServicio;
import co.edu.javeriana.concesionario.clientes.logica.PqrsServicio;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaComercial;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.modelo.Pqrs;
import co.edu.javeriana.concesionario.clientes.persistencia.ClienteDAO;
import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RNF-10 (rendimiento): las operaciones habituales deben completarse en máximo 1,5 segundos.
 * Se mide cada ejecución de las operaciones del CU-05 contra H2 y se exige que la más lenta no supere el límite.
 */
public class RendimientoClientesTest {

    static final long LIMITE_MS = 1500;
    private static final int CLIENTES = 10;
    private static final int REPETICIONES = 20;

    private static final List<Long> clientes = new ArrayList<>();
    private static IndiceSatisfaccionServicio indices;
    private static EncuestaServicio encuestas;
    private static LocalDate desde;
    private static LocalDate hasta;

    @BeforeAll
    public static void setUp() throws Exception {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        indices = new IndiceSatisfaccionServicio();
        encuestas = new EncuestaServicio();
        PqrsServicio pqrs = new PqrsServicio();
        hasta = LocalDate.now();
        desde = hasta.minusDays(IndiceSatisfaccionServicio.DIAS_PERIODO_POR_DEFECTO);

        for (int i = 0; i < CLIENTES; i++) {
            Cliente c = new Cliente(null, "77000005" + String.format("%02d", i), "CC", "Prueba", "Rendimiento " + i,
                    null, null, new BigDecimal("3000000"), CategoriaComercial.ESTANDAR);
            Long id = new ClienteDAO().guardar(c).getId();
            clientes.add(id);
            encuestas.registrarEncuesta(id, 5, 4, 4, "Rendimiento: encuesta 1");
            encuestas.registrarEncuesta(id, 3, 3, 2, "Rendimiento: encuesta 2");
            Pqrs p = pqrs.radicarPqrs(id, "QUEJA", "Rendimiento: queja de prueba");
            pqrs.cerrarPqrs(p.getId(), "Rendimiento: respuesta");
        }
    }

    @AfterAll
    public static void tearDown() throws Exception {
        String clientesPrueba = "(SELECT id FROM clientes WHERE documento LIKE '77000005%')";
        try (Connection conn = ConexionBD.obtenerConexion()) {
            for (String tabla : List.of("alertas_satisfaccion", "encuestas_satisfaccion", "pqrs")) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM " + tabla + " WHERE cliente_id IN " + clientesPrueba)) {
                    stmt.executeUpdate();
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM clientes WHERE documento LIKE '77000005%'")) {
                stmt.executeUpdate();
            }
        }
        ConexionBD.cerrarConexion();
    }

    private interface Operacion {
        void ejecutar() throws Exception;
    }

    private static double medir(Operacion operacion) throws Exception {
        long inicio = System.nanoTime();
        operacion.ejecutar();
        return (System.nanoTime() - inicio) / 1_000_000.0;
    }

    private static void verificarYReportar(String nombre, List<Double> tiempos) {
        double maximo = tiempos.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double promedio = tiempos.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        System.out.printf("RNF-10 | %-56s | %2d ejecuciones | promedio %7.2f ms | máximo %7.2f ms | límite %d ms%n",
                nombre, tiempos.size(), promedio, maximo, LIMITE_MS);
        assertTrue(maximo <= LIMITE_MS, nombre + " tardó " + maximo + " ms y el límite es " + LIMITE_MS + " ms");
    }

    @Test
    public void calculoDelIndiceDeTodosLosClientes() throws Exception {
        List<Double> tiempos = new ArrayList<>();
        int[] evaluados = new int[1];
        for (int i = 0; i < CLIENTES; i++) {
            tiempos.add(medir(() -> evaluados[0] = indices.calcularIndices(desde, hasta).getResultados().size()));
        }
        assertTrue(evaluados[0] >= CLIENTES);
        verificarYReportar("CU-05: calcular el índice de todos los clientes", tiempos);
    }

    @Test
    public void calculoDelIndiceDeUnCliente() throws Exception {
        List<Double> tiempos = new ArrayList<>();
        for (int i = 0; i < REPETICIONES; i++) {
            Long id = clientes.get(i % CLIENTES);
            tiempos.add(medir(() -> indices.calcularIndiceCliente(id, desde, hasta)));
        }
        verificarYReportar("CU-05: calcular el índice de un cliente", tiempos);
    }

    @Test
    public void registroDeEncuestaConRecalculoDelIndice() throws Exception {
        List<Double> tiempos = new ArrayList<>();
        for (int i = 0; i < CLIENTES; i++) {
            Long id = clientes.get(i);
            tiempos.add(medir(() -> {
                encuestas.registrarEncuesta(id, 4, 4, 4, "Rendimiento: encuesta medida");
                indices.calcularIndiceCliente(id, desde, hasta);
            }));
        }
        verificarYReportar("CU-04 y CU-05: registrar encuesta y recalcular índice", tiempos);
    }
}
