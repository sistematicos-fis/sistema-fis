package co.edu.javeriana.concesionario.legal;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.comun.simuladores.SimuladorAseguradora;
import co.edu.javeriana.concesionario.legal.logica.MatriculaServicio;
import co.edu.javeriana.concesionario.legal.logica.RevisionTecnicoMecanicaServicio;
import co.edu.javeriana.concesionario.legal.logica.SoatServicio;
import co.edu.javeriana.concesionario.legal.modelo.Aseguradora;
import co.edu.javeriana.concesionario.legal.modelo.EstadoMatricula;
import co.edu.javeriana.concesionario.legal.modelo.Matricula;
import co.edu.javeriana.concesionario.legal.modelo.OperacionComercial;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRenovacion;
import co.edu.javeriana.concesionario.legal.persistencia.AseguradoraDAO;
import co.edu.javeriana.concesionario.legal.persistencia.MatriculaDAO;
import co.edu.javeriana.concesionario.legal.persistencia.RevisionTecnicoMecanicaDAO;
import co.edu.javeriana.concesionario.legal.persistencia.SoatDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RNF-10 (rendimiento): las operaciones habituales deben completarse en máximo 1,5 segundos.
 * Se mide el tiempo de cada ejecución de las operaciones del CU-24 y el CU-25 contra H2 y se exige que
 * la más lenta no supere el límite. Cada resultado se imprime para dejarlo como evidencia en el PTP.
 */
public class RendimientoLegalTest {
    static final long LIMITE_MS = 1500;
    private static final int VEHICULOS = 10;
    private static final int REPETICIONES = 20;

    private static SoatServicio soats;
    private static RevisionTecnicoMecanicaServicio revisiones;
    private static Aseguradora sura;

    @BeforeAll
    public static void setUp() throws Exception {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        AseguradoraDAO aseguradoraDAO = new AseguradoraDAO();
        soats = new SoatServicio(new SoatDAO(), aseguradoraDAO, new SimuladorAseguradora());
        revisiones = new RevisionTecnicoMecanicaServicio(new RevisionTecnicoMecanicaDAO());
        sura = aseguradoraDAO.buscarPorCodigo("SURA").orElseThrow();

        // Vehículos matriculados con un SOAT que vence en 5 días, listos para renovar
        MatriculaServicio matriculas = new MatriculaServicio(new MatriculaDAO());
        LocalDate hoy = LocalDate.now();
        for (int i = 0; i < VEHICULOS; i++) {
            long id = 9301L + i;
            String placa = "RND" + (301 + i);
            try (Statement stmt = ConexionBD.obtenerConexion().createStatement()) {
                stmt.executeUpdate("MERGE INTO vehiculos (id, vin, placa, tipo, marca, modelo_anio, linea, color, precio_venta, estado, kilometraje) "
                        + "KEY(vin) VALUES (" + id + ", 'RENDTEST00000" + (301 + i) + "', NULL, 'AUTOMOVIL', 'Kia', 2025, 'Picanto', "
                        + "'Rojo', 60000000, 'DISPONIBLE', 0)");
            }
            matriculas.registrarMatricula(new Matricula(null, id, placa, "LT-REND-" + i, "Secretaría Distrital de Movilidad de Bogotá",
                    hoy.minusMonths(6), 1248, EstadoMatricula.ACTIVA, hoy.getYear(), null));
            soats.registrarSoatExistente(placa, "SOAT-REND-" + i, sura.getId(), hoy.minusYears(1).plusDays(6), new BigDecimal("470000"));
        }
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    /** Ejecuta la operación y devuelve cuánto tardó, en milisegundos. */
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
    public void testRenovacionCompletaDelSoat() throws Exception {
        List<Double> tiempos = new ArrayList<>();
        for (int i = 0; i < VEHICULOS; i++) {
            String placa = "RND" + (301 + i);
            ResultadoRenovacion[] resultado = new ResultadoRenovacion[1];
            tiempos.add(medir(() -> resultado[0] = soats.validarYRenovar(placa, sura.getId()).resultado()));
            assertEquals(ResultadoRenovacion.RENOVADO, resultado[0]);
        }
        verificarYReportar("CU-24 validar y renovar SOAT (flujo básico)", tiempos);
    }

    @Test
    public void testValidacionDeSoatVigente() throws Exception {
        List<Double> tiempos = new ArrayList<>();
        for (int i = 0; i < REPETICIONES; i++) {
            // KLU382 tiene SOAT semilla; se consulta repetidamente su estado y su historial
            tiempos.add(medir(() -> {
                Long id = soats.buscarVehiculoPorPlaca("KLU382").id();
                soats.listarSoats(id);
                soats.consultarEstadoDocumental(id);
            }));
        }
        verificarYReportar("CU-24 consultar vehículo, historial y estado documental", tiempos);
    }

    @Test
    public void testVerificacionDeRevisionTecnicoMecanica() throws Exception {
        Long vehiculo = soats.buscarVehiculoPorPlaca("KLU382").id();
        List<Double> tiempos = new ArrayList<>();
        for (int i = 0; i < REPETICIONES; i++) {
            OperacionComercial operacion = i % 2 == 0 ? OperacionComercial.VENTA : OperacionComercial.ALQUILER;
            tiempos.add(medir(() -> revisiones.verificarVigencia(vehiculo, operacion, LocalDate.now().plusDays(7))));
        }
        verificarYReportar("CU-25 verificar vigencia de la revisión técnico-mecánica", tiempos);
    }

    @FunctionalInterface
    private interface Operacion {
        void ejecutar() throws SQLException;
    }
}
