package co.edu.javeriana.concesionario.financiacion;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.financiacion.logica.FinanciacionServicio;
import co.edu.javeriana.concesionario.financiacion.modelo.CreditoFormalizado;
import co.edu.javeriana.concesionario.financiacion.modelo.SolicitudCredito;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class FinanciacionServicioTest {

    private static FinanciacionServicio servicio;

    @BeforeAll
    public static void setUp() {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        servicio = new FinanciacionServicio();
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    @Test
    public void testRadicarSolicitudAprobadaCapacidadEndeudamiento() throws Exception {
        // Ingresos: 10 millones, Monto: 40 millones a 48 meses -> Cuota ~ 1.1M (~11% < 30%)
        SolicitudCredito sol = servicio.radicarSolicitud(
                1L, 1L, 1L,
                new BigDecimal("40000000.00"), 48, BigDecimal.ZERO,
                new BigDecimal("10000000.00"), BigDecimal.ZERO, "1018456789"
        );

        assertNotNull(sol);
        assertEquals("EN_EVALUACION", sol.getEstado());
        assertTrue(sol.getPorcentajeEndeudamiento().compareTo(new BigDecimal("30.00")) <= 0);
    }

    @Test
    public void testRadicarSolicitudRechazadaRegla30Porciento() throws Exception {
        // Ingresos: 2 millones, Monto: 80 millones a 24 meses -> Cuota ~ 3.8M (> 100% > 30%)
        SolicitudCredito sol = servicio.radicarSolicitud(
                1L, 1L, 1L,
                new BigDecimal("80000000.00"), 24, BigDecimal.ZERO,
                new BigDecimal("2000000.00"), BigDecimal.ZERO, "1018456789"
        );

        assertNotNull(sol);
        assertEquals("RECHAZADA", sol.getEstado());
        assertTrue(sol.getPorcentajeEndeudamiento().compareTo(new BigDecimal("30.00")) > 0);
        assertTrue(sol.getObservaciones().contains("Capacidad de endeudamiento excedida"));
    }

    @Test
    public void testFormalizarCreditoConSimuladorBancario() throws Exception {
        // Radicar solicitud viable
        SolicitudCredito sol = servicio.radicarSolicitud(
                1L, 1L, 1L,
                new BigDecimal("30000000.00"), 36, BigDecimal.ZERO,
                new BigDecimal("12000000.00"), BigDecimal.ZERO, "1018456789"
        );

        // Formalizarla llamando al simulador bancario
        CreditoFormalizado cf = servicio.formalizarSolicitudConBanco(sol.getId(), "1018456789");
        assertNotNull(cf);
        assertNotNull(cf.getCodigoAprobacionBanco());
        assertTrue(cf.getCodigoAprobacionBanco().startsWith("BANC-APROB-"));
        assertEquals("ACTIVO", cf.getEstado());
    }
}
