package co.edu.javeriana.concesionario.legal;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.legal.logica.AseguradoraServicio;
import co.edu.javeriana.concesionario.legal.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.legal.modelo.Aseguradora;
import co.edu.javeriana.concesionario.legal.persistencia.AseguradoraDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AseguradoraServicioTest {
    private static AseguradoraServicio servicio;

    @BeforeAll
    public static void setUp() {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        servicio = new AseguradoraServicio(new AseguradoraDAO());
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    private Aseguradora nuevaAseguradora(String codigo, String nit) {
        return new Aseguradora(null, codigo, "Aseguradora de Prueba " + codigo, nit, "6015551234", true, true, true);
    }

    @Test
    public void testConsultarAseguradorasSemilla() throws Exception {
        List<Aseguradora> lista = servicio.listarAseguradoras();
        assertTrue(lista.stream().anyMatch(a -> "SURA".equals(a.getCodigo())));
        assertTrue(lista.stream().anyMatch(a -> "ALLIANZ".equals(a.getCodigo())));
        assertTrue(lista.stream().anyMatch(a -> "BOLIVAR".equals(a.getCodigo())));
    }

    @Test
    public void testAseguradorasSemillaCubrenAmbasPolizasPorDefecto() throws Exception {
        Aseguradora sura = servicio.listarAseguradoras().stream()
                .filter(a -> "SURA".equals(a.getCodigo())).findFirst().orElseThrow();
        assertTrue(sura.isCubreSoat());
        assertTrue(sura.isCubreResponsabilidadCivil());
    }

    @Test
    public void testRegistrarAseguradoraValida() throws Exception {
        Aseguradora a = servicio.registrarAseguradora(nuevaAseguradora("mapfre", "891.700.037-9"));

        assertNotNull(a.getId());
        Aseguradora guardada = servicio.buscarAseguradora(a.getId()).orElseThrow();
        assertEquals("MAPFRE", guardada.getCodigo(), "El código se guarda en mayúsculas");
        assertTrue(guardada.isCubreSoat());
        assertTrue(guardada.isCubreResponsabilidadCivil());
        assertTrue(guardada.isActivo());
    }

    @Test
    public void testRegistrarConvenioSoloSoat() throws Exception {
        Aseguradora a = nuevaAseguradora("SOLOSOAT", "800.100.200-1");
        a.setCubreResponsabilidadCivil(false);
        servicio.registrarAseguradora(a);

        Aseguradora guardada = servicio.buscarAseguradora(a.getId()).orElseThrow();
        assertTrue(guardada.isCubreSoat());
        assertFalse(guardada.isCubreResponsabilidadCivil());
    }

    @Test
    public void testRegistrarSinNingunaCoberturaEsRechazado() {
        Aseguradora a = nuevaAseguradora("SINCOBER", "800.100.300-2");
        a.setCubreSoat(false);
        a.setCubreResponsabilidadCivil(false);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.registrarAseguradora(a));
        assertTrue(ex.getMessage().contains("al menos un tipo de póliza"));
    }

    @Test
    public void testRegistrarSinCodigoEsRechazado() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarAseguradora(nuevaAseguradora("  ", "800.111.222-3")));
        assertTrue(ex.getMessage().contains("Código es obligatorio"));
    }

    @Test
    public void testRegistrarSinNombreEsRechazado() {
        Aseguradora a = nuevaAseguradora("SINNOM", "800.111.333-4");
        a.setNombre("");
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.registrarAseguradora(a));
        assertTrue(ex.getMessage().contains("Nombre es obligatorio"));
    }

    @Test
    public void testRegistrarNitConFormatoInvalidoEsRechazado() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarAseguradora(nuevaAseguradora("MALNIT", "ABC123")));
        assertTrue(ex.getMessage().contains("NIT tiene un formato inválido"));
    }

    @Test
    public void testRegistrarTelefonoInvalidoEsRechazado() {
        Aseguradora a = nuevaAseguradora("MALTEL", "800.222.444-5");
        a.setTelefonoContacto("llamar luego");
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarAseguradora(a));
    }

    @Test
    public void testRegistrarSinTelefonoEsValido() throws Exception {
        Aseguradora a = nuevaAseguradora("SINTEL", "800.222.555-6");
        a.setTelefonoContacto("   ");
        servicio.registrarAseguradora(a);
        assertNull(servicio.buscarAseguradora(a.getId()).orElseThrow().getTelefonoContacto());
    }

    @Test
    public void testRegistrarCodigoDuplicadoEsRechazado() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarAseguradora(nuevaAseguradora("sura", "800.333.444-7")));
        assertTrue(ex.getMessage().contains("ya existe otra aseguradora con el código"));
    }

    @Test
    public void testRegistrarNitDuplicadoEsRechazado() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarAseguradora(nuevaAseguradora("NITDUP", "890.903.407-9")));
        assertTrue(ex.getMessage().contains("ya existe otra aseguradora con el NIT"));
    }

    @Test
    public void testActualizarAseguradora() throws Exception {
        Aseguradora a = servicio.registrarAseguradora(nuevaAseguradora("EDITAR", "900.100.200-1"));

        a.setNombre("Aseguradora Editada S.A.");
        a.setCubreResponsabilidadCivil(false);
        a.setActivo(false);
        servicio.actualizarAseguradora(a);

        Aseguradora actualizada = servicio.buscarAseguradora(a.getId()).orElseThrow();
        assertEquals("Aseguradora Editada S.A.", actualizada.getNombre());
        assertTrue(actualizada.isCubreSoat());
        assertFalse(actualizada.isCubreResponsabilidadCivil());
        assertFalse(actualizada.isActivo());
        assertFalse(servicio.listarAseguradorasActivas().stream().anyMatch(x -> x.getId().equals(a.getId())),
                "Una aseguradora inactiva no aparece en la lista de activas");
    }

    @Test
    public void testActualizarConservandoSuPropioCodigoYNit() throws Exception {
        Aseguradora a = servicio.registrarAseguradora(nuevaAseguradora("PROPIO", "900.300.400-5"));
        a.setNombre("Mismo código y NIT, otro nombre");
        assertDoesNotThrow(() -> servicio.actualizarAseguradora(a));
    }

    @Test
    public void testActualizarInexistenteEsRechazado() {
        Aseguradora a = nuevaAseguradora("FANTASMA", "900.700.800-9");
        a.setId(999999L);
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizarAseguradora(a));
    }

    @Test
    public void testEliminarAseguradora() throws Exception {
        Aseguradora a = servicio.registrarAseguradora(nuevaAseguradora("BORRAR", "900.500.600-7"));

        servicio.eliminarAseguradora(a.getId());

        assertTrue(servicio.buscarAseguradora(a.getId()).isEmpty());
    }

    @Test
    public void testEliminarAseguradoraInexistenteEsRechazado() {
        assertThrows(ReglaNegocioException.class, () -> servicio.eliminarAseguradora(999999L));
    }

    @Test
    public void testEliminarAseguradoraConPolizasEmitidasEsRechazado() throws Exception {
        Aseguradora a = servicio.registrarAseguradora(nuevaAseguradora("CONPOLIZA", "900.900.100-3"));
        insertarPolizaDe(a.getId());

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.eliminarAseguradora(a.getId()));
        assertTrue(ex.getMessage().contains("pólizas emitidas"));
        assertTrue(servicio.buscarAseguradora(a.getId()).isPresent(), "La aseguradora sigue existiendo");
    }

    /** Inserta una póliza que referencia a la aseguradora (vehículo 1 y cliente 1 vienen de la semilla). */
    private void insertarPolizaDe(Long aseguradoraId) throws Exception {
        String sql = "INSERT INTO polizas_seguro (numero_poliza, aseguradora_id, vehiculo_id, cliente_id, tipo_poliza, "
                   + "valor_prima, valor_cobertura, fecha_emision, fecha_vencimiento, estado, codigo_aprobacion_simulada) "
                   + "VALUES (?, ?, 1, 1, 'SOAT', 1000, 2000, CURRENT_DATE, DATEADD(YEAR, 1, CURRENT_DATE), 'VIGENTE', 'AUT-TEST')";
        Connection conn = ConexionBD.obtenerConexion();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "POL-TEST-" + aseguradoraId);
            stmt.setLong(2, aseguradoraId);
            stmt.executeUpdate();
        }
    }
}
