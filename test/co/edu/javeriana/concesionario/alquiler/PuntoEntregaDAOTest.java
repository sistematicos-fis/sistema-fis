package co.edu.javeriana.concesionario.alquiler;

import co.edu.javeriana.concesionario.alquiler.logica.PuntoEntregaService;
import co.edu.javeriana.concesionario.alquiler.modelo.PuntoEntrega;
import co.edu.javeriana.concesionario.alquiler.persistencia.PuntoEntregaDAO;
import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CU-16 contra H2: lo que se registra o deshabilita queda en la tabla puntos_entrega.
 */
public class PuntoEntregaDAOTest {

    private static PuntoEntregaService service;

    @BeforeAll
    public static void setUp() {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        service = new PuntoEntregaService(new PuntoEntregaDAO());
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    private Boolean activoEnTabla(String id) throws Exception {
        try (PreparedStatement stmt = ConexionBD.obtenerConexion()
                .prepareStatement("SELECT activo FROM puntos_entrega WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBoolean(1) : null;
            }
        }
    }

    @Test
    public void testRegistrarGuardaEnLaTabla() throws Exception {
        service.registrarPunto("SEDE-T1", "Sede Prueba", "Calle 1 # 2-3", "Bogotá");

        assertEquals(Boolean.TRUE, activoEnTabla("SEDE-T1"));
        Optional<PuntoEntrega> leido = service.consultarPunto("SEDE-T1");
        assertTrue(leido.isPresent());
        assertEquals("Sede Prueba", leido.get().getNombre());
    }

    @Test
    public void testNoPermiteIdRepetidoDeLaSemilla() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrarPunto("SEDE-01", "Otra", "Calle 9", "Cali"));
    }

    @Test
    public void testDeshabilitarActualizaLaTabla() throws Exception {
        service.registrarPunto("SEDE-T2", "Sede Temporal", "Calle 4 # 5-6", "Medellín");

        assertTrue(service.deshabilitarPunto("SEDE-T2"));
        assertEquals(Boolean.FALSE, activoEnTabla("SEDE-T2"));
        assertTrue(service.listarPuntosActivos().stream().noneMatch(p -> p.getId().equals("SEDE-T2")));
        assertFalse(service.deshabilitarPunto("SEDE-T2"));
    }
}
