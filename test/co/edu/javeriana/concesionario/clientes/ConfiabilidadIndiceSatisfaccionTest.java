package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.IndiceSatisfaccionServicio;
import co.edu.javeriana.concesionario.clientes.modelo.BaseCalculoIndice;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaComercial;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.modelo.ResultadoIndiceSatisfaccion;
import co.edu.javeriana.concesionario.clientes.persistencia.ClienteDAO;
import co.edu.javeriana.concesionario.clientes.persistencia.IndiceSatisfaccionDAO;
import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RNF-08 y RF-28 (confiabilidad): el CU-05 guarda el índice de cada cliente y sus alertas en una sola transacción.
 * Si algo falla a mitad del guardado, no debe quedar ningún índice ni ninguna alerta guardados.
 */
public class ConfiabilidadIndiceSatisfaccionTest {

    private static final long CLIENTE_INEXISTENTE = 999999L;

    private static IndiceSatisfaccionDAO dao;

    @BeforeAll
    public static void setUp() {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        dao = new IndiceSatisfaccionDAO();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement alertas = conn.prepareStatement("DELETE FROM alertas_satisfaccion WHERE cliente_id IN "
                     + "(SELECT id FROM clientes WHERE documento LIKE '77000004%')");
             PreparedStatement clientes = conn.prepareStatement("DELETE FROM clientes WHERE documento LIKE '77000004%'")) {
            alertas.executeUpdate();
            clientes.executeUpdate();
        }
        ConexionBD.cerrarConexion();
    }

    private static Long crearCliente(String documento) throws Exception {
        Cliente c = new Cliente(null, documento, "CC", "Prueba", "Confiabilidad", null, null,
                new BigDecimal("1000000"), CategoriaComercial.ESTANDAR);
        return new ClienteDAO().guardar(c).getId();
    }

    private static ResultadoIndiceSatisfaccion resultado(Long clienteId, String indice, CategoriaSatisfaccion categoria) {
        return new ResultadoIndiceSatisfaccion(clienteId, "doc", "Prueba", null, 1, 0,
                new BigDecimal(indice), null, new BigDecimal(indice), BaseCalculoIndice.SOLO_ENCUESTAS, categoria);
    }

    private static BigDecimal indiceGuardado(Long clienteId) throws Exception {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT indice_satisfaccion FROM clientes WHERE id = ?")) {
            stmt.setLong(1, clienteId);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                return rs.getBigDecimal(1);
            }
        }
    }

    private static int alertasDe(Long clienteId) throws Exception {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM alertas_satisfaccion WHERE cliente_id = ?")) {
            stmt.setLong(1, clienteId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Test
    public void siFallaLaBaseDeDatosNoQuedaGuardadoNingunIndice() throws Exception {
        Long enRiesgo = crearCliente("7700000401");
        List<ResultadoIndiceSatisfaccion> resultados = List.of(
                resultado(enRiesgo, "40.00", CategoriaSatisfaccion.EN_RIESGO),
                resultado(CLIENTE_INEXISTENTE, "80.00", CategoriaSatisfaccion.SATISFECHO));

        assertThrows(SQLException.class, () -> dao.guardarResultados(resultados,
                IndiceSatisfaccionServicio.MENSAJE_CLIENTE_EN_RIESGO, LocalDateTime.now()));

        assertNull(indiceGuardado(enRiesgo));
        assertEquals(0, alertasDe(enRiesgo));
    }

    @Test
    public void unErrorInesperadoTambienDeshaceLosCambios() throws Exception {
        Long enRiesgo = crearCliente("7700000402");
        Long sinCategoria = crearCliente("7700000403");
        List<ResultadoIndiceSatisfaccion> resultados = List.of(
                resultado(enRiesgo, "40.00", CategoriaSatisfaccion.EN_RIESGO),
                resultado(sinCategoria, "80.00", null));

        assertThrows(NullPointerException.class, () -> dao.guardarResultados(resultados,
                IndiceSatisfaccionServicio.MENSAJE_CLIENTE_EN_RIESGO, LocalDateTime.now()));

        assertNull(indiceGuardado(enRiesgo));
        assertEquals(0, alertasDe(enRiesgo));
    }

    @Test
    public void casoDeControlGuardaLosIndicesYLaAlertaJuntos() throws Exception {
        Long enRiesgo = crearCliente("7700000404");
        Long satisfecho = crearCliente("7700000405");
        List<ResultadoIndiceSatisfaccion> resultados = List.of(
                resultado(enRiesgo, "40.00", CategoriaSatisfaccion.EN_RIESGO),
                resultado(satisfecho, "80.00", CategoriaSatisfaccion.SATISFECHO));

        int alertas = dao.guardarResultados(resultados, IndiceSatisfaccionServicio.MENSAJE_CLIENTE_EN_RIESGO,
                LocalDateTime.now());

        assertEquals(1, alertas);
        assertEquals(0, new BigDecimal("40.00").compareTo(indiceGuardado(enRiesgo)));
        assertEquals(0, new BigDecimal("80.00").compareTo(indiceGuardado(satisfecho)));
        assertEquals(1, alertasDe(enRiesgo));
    }
}
