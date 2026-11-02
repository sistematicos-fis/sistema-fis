package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.IndiceSatisfaccionServicio;
import co.edu.javeriana.concesionario.clientes.logica.IndiceSatisfaccionServicio.ResumenCalculo;
import co.edu.javeriana.concesionario.clientes.modelo.AlertaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.BaseCalculoIndice;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.ResultadoIndiceSatisfaccion;
import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IndiceSatisfaccionIntegracionTest {

    private static IndiceSatisfaccionServicio servicio;

    @BeforeAll
    public static void setUp() {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        servicio = new IndiceSatisfaccionServicio();
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    private static LocalDate desde() {
        return LocalDate.now().minusDays(IndiceSatisfaccionServicio.DIAS_PERIODO_POR_DEFECTO);
    }

    @Test
    public void calculaLosTresCasosDeLosDatosSemilla() throws Exception {
        ResumenCalculo resumen = servicio.calcularIndices(desde(), LocalDate.now());

        ResultadoIndiceSatisfaccion carlos = buscar(resumen, "1018456789");
        assertEquals(new BigDecimal("92.50"), carlos.getIndice());
        assertEquals(CategoriaSatisfaccion.SATISFECHO, carlos.getCategoria());
        assertEquals(BaseCalculoIndice.SOLO_ENCUESTAS, carlos.getBaseCalculo());

        ResultadoIndiceSatisfaccion maria = buscar(resumen, "1020987654");
        assertEquals(new BigDecimal("36.50"), maria.getIndice());
        assertEquals(CategoriaSatisfaccion.EN_RIESGO, maria.getCategoria());
        assertEquals(BaseCalculoIndice.COMBINADO, maria.getBaseCalculo());

        ResultadoIndiceSatisfaccion transportes = buscar(resumen, "900123456");
        assertEquals(new BigDecimal("68.34"), transportes.getIndice());
        assertEquals(CategoriaSatisfaccion.NEUTRAL, transportes.getCategoria());
        assertEquals(BaseCalculoIndice.SOLO_PQRS, transportes.getBaseCalculo());
    }

    @Test
    public void actualizaElPerfilDelClienteEnLaBaseDeDatos() throws Exception {
        servicio.calcularIndices(desde(), LocalDate.now());

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT indice_satisfaccion, nivel_satisfaccion, fecha_indice_satisfaccion "
                     + "FROM clientes WHERE documento = ?")) {
            stmt.setString(1, "1020987654");
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, new BigDecimal("36.50").compareTo(rs.getBigDecimal(1)));
                assertEquals("EN_RIESGO", rs.getString(2));
                assertNotNull(rs.getTimestamp(3));
            }
        }
    }

    @Test
    public void generaUnaSolaAlertaPendientePorClienteEnRiesgo() throws Exception {
        servicio.calcularIndices(desde(), LocalDate.now());
        servicio.calcularIndices(desde(), LocalDate.now());

        List<AlertaSatisfaccion> pendientes = servicio.listarAlertasPendientes();
        long alertasMaria = pendientes.stream().filter(a -> a.getNombreCliente().startsWith("María")).count();
        assertEquals(1, alertasMaria);
        assertEquals(IndiceSatisfaccionServicio.MENSAJE_CLIENTE_EN_RIESGO, pendientes.get(0).getMensaje());
        assertEquals(2L, pendientes.get(0).getAsesorId());

        assertTrue(servicio.marcarAlertaAtendida(pendientes.get(0).getId()));
        assertEquals(pendientes.size() - 1, servicio.listarAlertasPendientes().size());
    }

    @Test
    public void periodoSinDatosDevuelveResultadoVacio() throws Exception {
        LocalDate haceDosAnios = LocalDate.now().minusYears(2);
        ResumenCalculo resumen = servicio.calcularIndices(haceDosAnios, haceDosAnios.plusDays(10));
        assertTrue(resumen.estaVacio());
    }

    private static ResultadoIndiceSatisfaccion buscar(ResumenCalculo resumen, String documento) {
        return resumen.getResultados().stream()
                .filter(r -> documento.equals(r.getDocumentoCliente()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el cliente " + documento));
    }
}
