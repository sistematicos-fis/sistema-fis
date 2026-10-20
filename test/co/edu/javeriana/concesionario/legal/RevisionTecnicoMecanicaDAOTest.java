package co.edu.javeriana.concesionario.legal;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.legal.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.legal.logica.RevisionTecnicoMecanicaServicio;
import co.edu.javeriana.concesionario.legal.modelo.EstadoRtm;
import co.edu.javeriana.concesionario.legal.modelo.OperacionComercial;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRevision;
import co.edu.javeriana.concesionario.legal.modelo.RevisionTecnicoMecanica;
import co.edu.javeriana.concesionario.legal.persistencia.RevisionTecnicoMecanicaDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CU-25 contra H2: el DAO y el servicio juntos, con el reloj real (las fechas son relativas a hoy).
 */
public class RevisionTecnicoMecanicaDAOTest {
    /** Id explícito y alto: no depende del autoincremento de la tabla de vehículos. */
    private static final long VEHICULO_VIEJO = 9101L;
    private static final long VEHICULO_OTRO = 9102L;

    private static RevisionTecnicoMecanicaServicio servicio;

    @BeforeAll
    public static void setUp() throws Exception {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        try (Statement stmt = ConexionBD.obtenerConexion().createStatement()) {
            stmt.executeUpdate("MERGE INTO vehiculos (id, vin, placa, tipo, marca, modelo_anio, linea, color, precio_venta, estado, kilometraje) "
                    + "KEY(vin) VALUES (" + VEHICULO_VIEJO + ", 'RTMTEST0000000001', 'RTM001', 'AUTOMOVIL', 'Chevrolet', 2015, 'Spark', 'Gris', 25000000, 'DISPONIBLE', 90000)");
            stmt.executeUpdate("MERGE INTO vehiculos (id, vin, placa, tipo, marca, modelo_anio, linea, color, precio_venta, estado, kilometraje) "
                    + "KEY(vin) VALUES (" + VEHICULO_OTRO + ", 'RTMTEST0000000002', 'RTM002', 'AUTOMOVIL', 'Renault', 2014, 'Sandero', 'Negro', 22000000, 'DISPONIBLE', 120000)");
        }
        servicio = new RevisionTecnicoMecanicaServicio(new RevisionTecnicoMecanicaDAO());
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    private RevisionTecnicoMecanica revision(long vehiculoId, String certificado, LocalDate expedicion, ResultadoRevision resultado) {
        RevisionTecnicoMecanica r = new RevisionTecnicoMecanica();
        r.setVehiculoId(vehiculoId);
        r.setNumeroCertificado(certificado);
        r.setCentroDiagnostico("CDA Integración");
        r.setFechaExpedicion(expedicion);
        r.setResultado(resultado);
        return r;
    }

    @Test
    public void testListarVehiculosIncluyeLosDelInventario() throws Exception {
        assertTrue(servicio.listarVehiculos().stream().anyMatch(v -> v.id() == VEHICULO_VIEJO));
        assertTrue(servicio.listarVehiculos().stream().anyMatch(v -> "KLU382".equals(v.placa())), "Vehículo semilla del inventario");
    }

    @Test
    public void testVehiculoSinRevisionNoSePuedeVenderHastaRegistrarla() throws Exception {
        assertEquals(EstadoRtm.SIN_REVISION,
                servicio.verificarVigencia(VEHICULO_OTRO, OperacionComercial.VENTA, null).estado());

        servicio.registrarRevision(revision(VEHICULO_OTRO, "RTM-DAO-0001", LocalDate.now().minusMonths(2), ResultadoRevision.APROBADA));

        assertTrue(servicio.verificarVigencia(VEHICULO_OTRO, OperacionComercial.VENTA, null).habilitado());
    }

    @Test
    public void testRegistrarYListarRevisionesDelVehiculo() throws Exception {
        LocalDate expedicion = LocalDate.now().minusMonths(3);
        RevisionTecnicoMecanica guardada = servicio.registrarRevision(
                revision(VEHICULO_VIEJO, "RTM-DAO-0002", expedicion, ResultadoRevision.APROBADA));
        servicio.registrarRevision(revision(VEHICULO_VIEJO, "RTM-DAO-0003", expedicion.minusYears(1), ResultadoRevision.APROBADA));

        List<RevisionTecnicoMecanica> lista = servicio.listarRevisiones(VEHICULO_VIEJO);

        assertEquals("RTM-DAO-0002", lista.get(0).getNumeroCertificado(), "La más reciente va primero");
        assertEquals(guardada.getId(), lista.get(0).getId());
        assertEquals(expedicion.plusYears(1), lista.get(0).getFechaVencimiento());
        assertEquals(ResultadoRevision.APROBADA, lista.get(0).getResultado());
        assertTrue(lista.size() >= 2);
    }

    @Test
    public void testRevisionRechazadaSeGuardaSinVencimiento() throws Exception {
        servicio.registrarRevision(revision(VEHICULO_VIEJO, "RTM-DAO-0004", LocalDate.now().minusDays(1), ResultadoRevision.RECHAZADA));

        RevisionTecnicoMecanica r = servicio.listarRevisiones(VEHICULO_VIEJO).stream()
                .filter(x -> "RTM-DAO-0004".equals(x.getNumeroCertificado())).findFirst().orElseThrow();
        assertNull(r.getFechaVencimiento());
        assertEquals(ResultadoRevision.RECHAZADA, r.getResultado());
    }

    @Test
    public void testCertificadoDuplicadoEsRechazado() throws Exception {
        servicio.registrarRevision(revision(VEHICULO_VIEJO, "RTM-DAO-0005", LocalDate.now().minusDays(10), ResultadoRevision.APROBADA));

        assertThrows(ReglaNegocioException.class, () -> servicio.registrarRevision(
                revision(VEHICULO_OTRO, "rtm-dao-0005", LocalDate.now().minusDays(5), ResultadoRevision.APROBADA)));
    }

    @Test
    public void testVehiculoSemillaNuevoNoRequiereRevision() throws Exception {
        Long idSemilla = servicio.listarVehiculos().stream()
                .filter(v -> "KLU382".equals(v.placa())).findFirst().orElseThrow().id();

        assertEquals(EstadoRtm.EXENTA_PRIMERA_REVISION,
                servicio.verificarVigencia(idSemilla, OperacionComercial.VENTA, null).estado());
    }
}
