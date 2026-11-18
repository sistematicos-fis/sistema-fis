package co.edu.javeriana.concesionario.legal;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.comun.simuladores.SimuladorAseguradora;
import co.edu.javeriana.concesionario.legal.logica.MatriculaServicio;
import co.edu.javeriana.concesionario.legal.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.legal.logica.SoatServicio;
import co.edu.javeriana.concesionario.legal.modelo.Aseguradora;
import co.edu.javeriana.concesionario.legal.modelo.EstadoDocumental;
import co.edu.javeriana.concesionario.legal.modelo.EstadoMatricula;
import co.edu.javeriana.concesionario.legal.modelo.Matricula;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRenovacion;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRenovacionSoat;
import co.edu.javeriana.concesionario.legal.modelo.Soat;
import co.edu.javeriana.concesionario.legal.persistencia.AseguradoraDAO;
import co.edu.javeriana.concesionario.legal.persistencia.MatriculaDAO;
import co.edu.javeriana.concesionario.legal.persistencia.SoatDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CU-24 contra H2: el DAO de SOAT, la transacción SOAT + estado documental (RF-28),
 * la semilla y el simulador de aseguradora.
 * Cada prueba usa sus propios vehículos (ids altos) para no depender de las demás.
 */
public class SoatDAOTest {
    private static MatriculaServicio matriculas;
    private static SoatServicio soats;
    private static Aseguradora sura;
    private static Aseguradora allianz;

    @BeforeAll
    public static void setUp() throws Exception {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        AseguradoraDAO aseguradoraDAO = new AseguradoraDAO();
        matriculas = new MatriculaServicio(new MatriculaDAO());
        soats = new SoatServicio(new SoatDAO(), aseguradoraDAO, new SimuladorAseguradora());
        sura = aseguradoraDAO.buscarPorCodigo("SURA").orElseThrow();
        allianz = aseguradoraDAO.buscarPorCodigo("ALLIANZ").orElseThrow();
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    private static void insertarVehiculo(long id, String vin, String placa, String tipo, String estado) throws SQLException {
        try (Statement stmt = ConexionBD.obtenerConexion().createStatement()) {
            stmt.executeUpdate("MERGE INTO vehiculos (id, vin, placa, tipo, marca, modelo_anio, linea, color, precio_venta, estado, kilometraje) "
                    + "KEY(vin) VALUES (" + id + ", '" + vin + "', " + (placa == null ? "NULL" : "'" + placa + "'") + ", '" + tipo
                    + "', 'Renault', 2025, 'Duster', 'Gris', 90000000, '" + estado + "', 0)");
        }
    }

    private static Matricula matricula(long vehiculoId, String placa, String licencia) {
        return new Matricula(null, vehiculoId, placa, licencia, "Secretaría Distrital de Movilidad de Bogotá",
                LocalDate.now().minusMonths(3), 1598, EstadoMatricula.ACTIVA, LocalDate.now().getYear(), new BigDecimal("1300000"));
    }

    @Test
    public void testSemillaDaSoatALosVehiculosMatriculados() throws Exception {
        Long corolla = soats.buscarVehiculoPorPlaca("KLU382").id();
        List<Soat> historial = soats.listarSoats(corolla);
        assertFalse(historial.isEmpty());
        assertEquals(LocalDate.now().plusDays(10), historial.get(0).getFechaVencimiento());
    }

    @Test
    public void testRenovarSoatGuardaElSoatYElEstadoDocumental() throws Exception {
        insertarVehiculo(9205L, "LEGALTEST00000005", null, "AUTOMOVIL", "DISPONIBLE");
        matriculas.registrarMatricula(matricula(9205L, "QWE127", "LT-9205"));
        soats.registrarSoatExistente("QWE127", "SOAT-9205-A", sura.getId(),
                LocalDate.now().minusYears(1).plusDays(6), new BigDecimal("570000"));

        ResultadoRenovacionSoat r = soats.validarYRenovar("QWE127", sura.getId());

        assertEquals(ResultadoRenovacion.RENOVADO, r.resultado(), r.mensaje());
        assertEquals(2, soats.listarSoats(9205L).size());
        assertEquals(EstadoDocumental.AL_DIA, soats.consultarEstadoDocumental(9205L).estado());

        // Ya renovado, el nuevo SOAT vence en más de 30 días: flujo 3A
        assertEquals(ResultadoRenovacion.NO_REQUIERE_RENOVACION, soats.validarYRenovar("QWE127", sura.getId()).resultado());
    }

    @Test
    public void testSimuladorRechazaMotoConAllianzYDejaPendiente() throws Exception {
        insertarVehiculo(9206L, "LEGALTEST00000006", null, "MOTOCICLETA", "DISPONIBLE");
        Matricula m = matricula(9206L, "QWE12A", "LT-9206");
        m.setCilindrajeCc(150);
        matriculas.registrarMatricula(m);
        soats.registrarSoatExistente("QWE12A", "SOAT-9206-A", sura.getId(),
                LocalDate.now().minusYears(1).minusDays(3), new BigDecimal("350000"));

        ResultadoRenovacionSoat r = soats.validarYRenovar("QWE12A", allianz.getId());

        assertEquals(ResultadoRenovacion.RECHAZADO_POR_ASEGURADORA, r.resultado());
        assertEquals(EstadoDocumental.PENDIENTE, soats.consultarEstadoDocumental(9206L).estado());
        assertTrue(soats.consultarEstadoDocumental(9206L).observacion().contains("Allianz"));

        // Con otra aseguradora sí se renueva y vuelve a quedar al día
        assertTrue(soats.validarYRenovar("QWE12A", sura.getId()).fueRenovado());
        assertEquals(EstadoDocumental.AL_DIA, soats.consultarEstadoDocumental(9206L).estado());
    }

    @Test
    public void testSemillaLMP941TieneSiniestroPendiente() throws Exception {
        ResultadoRenovacionSoat r = soats.validarYRenovar("LMP941", sura.getId());
        assertEquals(ResultadoRenovacion.NO_CUMPLE_REQUISITOS, r.resultado());
        assertTrue(r.mensaje().contains("siniestro"));
    }

    @Test
    public void testNumeroDeSoatRepetidoEsRechazado() throws Exception {
        insertarVehiculo(9207L, "LEGALTEST00000007", null, "AUTOMOVIL", "DISPONIBLE");
        matriculas.registrarMatricula(matricula(9207L, "QWE128", "LT-9207"));
        soats.registrarSoatExistente("QWE128", "SOAT-9207-A", sura.getId(), LocalDate.now().minusDays(20), new BigDecimal("570000"));

        assertThrows(ReglaNegocioException.class, () -> soats.registrarSoatExistente("QWE128", "SOAT-9207-A", sura.getId(),
                LocalDate.now().minusYears(2), new BigDecimal("570000")));
    }
}
