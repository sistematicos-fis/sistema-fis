package co.edu.javeriana.concesionario.financiacion;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.financiacion.logica.FinanciacionServicio;
import co.edu.javeriana.concesionario.financiacion.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.financiacion.modelo.EntidadFinanciera;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

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

    private EntidadFinanciera nuevaEntidad(String codigo, String nit) {
        return new EntidadFinanciera(null, codigo, "Banco de Prueba " + codigo, nit,
                new BigDecimal("0.1500"), 650, true);
    }

    @Test
    public void testConsultarEntidadesSemilla() throws Exception {
        List<EntidadFinanciera> lista = servicio.listarEntidades();
        assertTrue(lista.stream().anyMatch(e -> "BANCOL".equals(e.getCodigo())));
        assertTrue(lista.stream().anyMatch(e -> "DAVIV".equals(e.getCodigo())));
        assertTrue(lista.stream().anyMatch(e -> "OCCID".equals(e.getCodigo())));
    }

    @Test
    public void testRegistrarEntidadValida() throws Exception {
        EntidadFinanciera e = servicio.registrarEntidad(nuevaEntidad("bbva", "860.003.020-1"));

        assertNotNull(e.getId());
        EntidadFinanciera guardada = servicio.buscarEntidad(e.getId()).orElseThrow();
        assertEquals("BBVA", guardada.getCodigo(), "El código se guarda en mayúsculas");
        assertEquals(0, new BigDecimal("0.1500").compareTo(guardada.getTasaBaseEa()));
        assertTrue(guardada.isActivo());
    }

    @Test
    public void testRegistrarSinCodigoEsRechazado() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarEntidad(nuevaEntidad("  ", "800.111.222-3")));
        assertTrue(ex.getMessage().contains("Código es obligatorio"));
    }

    @Test
    public void testRegistrarNitConFormatoInvalidoEsRechazado() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarEntidad(nuevaEntidad("MALNIT", "ABC123")));
        assertTrue(ex.getMessage().contains("NIT tiene un formato inválido"));
    }

    @Test
    public void testRegistrarTasaFueraDeRangoEsRechazada() {
        EntidadFinanciera e = nuevaEntidad("TASAMAL", "800.333.444-5");
        e.setTasaBaseEa(new BigDecimal("1.5"));
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarEntidad(e));
    }

    @Test
    public void testRegistrarScoringFueraDeRangoEsRechazado() {
        EntidadFinanciera e = nuevaEntidad("SCOREMAL", "800.555.666-7");
        e.setScoringMinimo(1000);
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarEntidad(e));
    }

    @Test
    public void testRegistrarCodigoDuplicadoEsRechazado() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarEntidad(nuevaEntidad("BANCOL", "800.777.888-9")));
        assertTrue(ex.getMessage().contains("ya existe otra entidad con el código"));
    }

    @Test
    public void testRegistrarNitDuplicadoEsRechazado() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarEntidad(nuevaEntidad("NITDUP", "890.903.938-8")));
        assertTrue(ex.getMessage().contains("ya existe otra entidad con el NIT"));
    }

    @Test
    public void testActualizarEntidad() throws Exception {
        EntidadFinanciera e = servicio.registrarEntidad(nuevaEntidad("EDITAR", "900.100.200-1"));

        e.setNombre("Banco Editado S.A.");
        e.setScoringMinimo(700);
        e.setActivo(false);
        servicio.actualizarEntidad(e);

        EntidadFinanciera actualizada = servicio.buscarEntidad(e.getId()).orElseThrow();
        assertEquals("Banco Editado S.A.", actualizada.getNombre());
        assertEquals(700, actualizada.getScoringMinimo());
        assertFalse(actualizada.isActivo());
        assertFalse(servicio.listarEntidadesActivas().stream().anyMatch(x -> x.getId().equals(e.getId())),
                "Una entidad inactiva no aparece en la lista de activas");
    }

    @Test
    public void testActualizarConservandoSuPropioCodigoYNit() throws Exception {
        EntidadFinanciera e = servicio.registrarEntidad(nuevaEntidad("PROPIO", "900.300.400-5"));
        e.setNombre("Mismo código y NIT, otro nombre");
        assertDoesNotThrow(() -> servicio.actualizarEntidad(e));
    }

    @Test
    public void testEliminarEntidad() throws Exception {
        EntidadFinanciera e = servicio.registrarEntidad(nuevaEntidad("BORRAR", "900.500.600-7"));

        servicio.eliminarEntidad(e.getId());

        assertTrue(servicio.buscarEntidad(e.getId()).isEmpty());
    }

    @Test
    public void testEliminarEntidadInexistenteEsRechazado() {
        assertThrows(ReglaNegocioException.class, () -> servicio.eliminarEntidad(999999L));
    }
}
