package co.edu.javeriana.concesionario.inventario;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.financiacion.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.inventario.logica.VerificacionInventarioServicio;
import co.edu.javeriana.concesionario.inventario.modelo.CategoriaVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.EstadoVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.IncidenciaInventario;
import co.edu.javeriana.concesionario.inventario.modelo.ResultadoVerificacion;
import co.edu.javeriana.concesionario.inventario.modelo.Vehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.VerificacionInventario;
import co.edu.javeriana.concesionario.inventario.persistencia.VehiculoDAO;
import co.edu.javeriana.concesionario.inventario.persistencia.VerificacionInventarioDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del CU-10 (controlar existencias en tiempo real para ventas).
 * Usan la base en memoria para no tocar el archivo de datos real.
 *
 * Responsable: Juan Pablo León
 */
public class VerificacionInventarioServicioTest {

    private static VerificacionInventarioServicio servicio;
    private static VehiculoDAO vehiculoDAO;
    private static final AtomicInteger SECUENCIA = new AtomicInteger(1);

    @BeforeAll
    public static void setUp() {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        vehiculoDAO = new VehiculoDAO();
        servicio = new VerificacionInventarioServicio(vehiculoDAO, new VerificacionInventarioDAO());
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    /**
     * Cada prueba trabaja sobre su propia unidad, con VIN y placa únicos, para que
     * los cambios de estado de una no alteren el resultado de las demás.
     */
    private Vehiculo nuevoVehiculo(String estado) throws SQLException {
        int n = SECUENCIA.getAndIncrement();
        Vehiculo v = new Vehiculo(null, "VINCU10TEST" + n, "CUX" + String.format("%03d", n),
                CategoriaVehiculo.AUTOMOVIL, "Renault", 2025, "Duster", "Gris",
                new BigDecimal("95000000"), estado, 0, null);
        return vehiculoDAO.guardar(v);
    }

    private String estadoActual(Long vehiculoId) throws SQLException {
        return vehiculoDAO.buscarPorId(vehiculoId).orElseThrow().getEstado();
    }

    // ------------------------------------------------------------------
    // Flujo básico
    // ------------------------------------------------------------------

    @Test
    public void testVerificacionFavorableConfirmaYBloquea() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.DISPONIBLE, true, "bodega", "conteo ok");

        VerificacionInventario resultado = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");

        assertEquals(ResultadoVerificacion.DISPONIBLE_CONFIRMADO, resultado.getResultado());
        assertTrue(resultado.confirmoDisponibilidad());
        assertNotNull(resultado.getId(), "la verificación debe quedar persistida");
        assertNotNull(resultado.getBloqueoExpira(), "RN-10-07: debe fijarse la vigencia del bloqueo");
        assertEquals("RESERVADO", estadoActual(v.getId()),
                "RN-10-07: la unidad queda bloqueada para la venta en curso");
    }

    @Test
    public void testBloqueoVenceEnLaVentanaConfigurada() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.DISPONIBLE, true, "bodega", null);

        LocalDateTime antes = LocalDateTime.now();
        VerificacionInventario resultado = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");
        LocalDateTime despues = LocalDateTime.now();

        int minutos = VerificacionInventarioServicio.MINUTOS_BLOQUEO_VENTA;
        assertFalse(resultado.getBloqueoExpira().isBefore(antes.plusMinutes(minutos)),
                "el bloqueo debe cubrir la ventana completa");
        assertFalse(resultado.getBloqueoExpira().isAfter(despues.plusMinutes(minutos)),
                "el bloqueo no debe extenderse más allá de la ventana");
    }

    // ------------------------------------------------------------------
    // Flujo alterno 4A: discrepancia entre físico y sistema
    // ------------------------------------------------------------------

    @Test
    public void testEstadoFisicoDistintoAbreIncidencia() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.MANTENIMIENTO, true, "bodega",
                "está en el taller");

        VerificacionInventario resultado = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");

        assertEquals(ResultadoVerificacion.DISCREPANCIA_DETECTADA, resultado.getResultado());
        assertNotNull(resultado.getIncidencia());
        assertNotNull(resultado.getIncidencia().getId(), "la incidencia debe quedar persistida");
        assertEquals("EN_VERIFICACION", estadoActual(v.getId()),
                "RN-10-05: la unidad pasa a EN_VERIFICACION");
    }

    @Test
    public void testVehiculoAusenteEnBodegaAbreIncidencia() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.DISPONIBLE, false, "bodega",
                "no apareció en el patio");

        VerificacionInventario resultado = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");

        assertEquals(ResultadoVerificacion.DISCREPANCIA_DETECTADA, resultado.getResultado());
        assertTrue(resultado.getMotivo().contains("no fue encontrado"));
        assertEquals("EN_VERIFICACION", estadoActual(v.getId()));
    }

    @Test
    public void testIncidenciaAbiertaCongelaVerificacionesPosteriores() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.VENDIDO, true, "bodega", null);
        servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");

        // Aunque la bodega corrija el conteo, la incidencia sigue abierta.
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.DISPONIBLE, true, "bodega", "corregido");
        VerificacionInventario segunda = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");

        assertEquals(ResultadoVerificacion.INCIDENCIA_PENDIENTE, segunda.getResultado(),
                "RN-10-06: no se confirma nada mientras haya incidencia sin resolver");
    }

    // ------------------------------------------------------------------
    // Resolución de incidencias
    // ------------------------------------------------------------------

    @Test
    public void testResolverIncidenciaDejaElEstadoConfirmado() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.MANTENIMIENTO, true, "bodega", null);
        VerificacionInventario conDiscrepancia = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");
        Long incidenciaId = conDiscrepancia.getIncidencia().getId();

        servicio.resolverIncidencia(incidenciaId, EstadoVehiculo.DISPONIBLE, "bodega",
                "era otra unidad la que estaba en el taller");

        assertEquals("DISPONIBLE", estadoActual(v.getId()));
        assertTrue(servicio.listarIncidenciasAbiertas().stream()
                        .noneMatch(i -> i.getId().equals(incidenciaId)),
                "la incidencia ya no debe figurar como abierta");
    }

    @Test
    public void testTrasResolverSePuedeVolverAConfirmar() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.MANTENIMIENTO, true, "bodega", null);
        VerificacionInventario primera = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");

        servicio.resolverIncidencia(primera.getIncidencia().getId(), EstadoVehiculo.DISPONIBLE,
                "bodega", "corregido");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.DISPONIBLE, true, "bodega", null);

        VerificacionInventario segunda = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");
        assertEquals(ResultadoVerificacion.DISPONIBLE_CONFIRMADO, segunda.getResultado());
    }

    @Test
    public void testResolverIncidenciaInexistenteEsRechazado() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.resolverIncidencia(999999L, EstadoVehiculo.DISPONIBLE, "bodega", "x"));
    }

    @Test
    public void testResolverDosVecesLaMismaIncidenciaEsRechazado() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.ALQUILADO, true, "bodega", null);
        VerificacionInventario r = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");
        Long incidenciaId = r.getIncidencia().getId();

        servicio.resolverIncidencia(incidenciaId, EstadoVehiculo.DISPONIBLE, "bodega", "ok");

        assertThrows(ReglaNegocioException.class,
                () -> servicio.resolverIncidencia(incidenciaId, EstadoVehiculo.DISPONIBLE, "bodega", "ok"));
    }

    @Test
    public void testResolverSinEstadoConfirmadoEsRechazado() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.VENDIDO, true, "bodega", null);
        VerificacionInventario r = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");

        assertThrows(ReglaNegocioException.class,
                () -> servicio.resolverIncidencia(r.getIncidencia().getId(), null, "bodega", "x"));
    }

    // ------------------------------------------------------------------
    // Reglas que niegan la venta sin abrir incidencia
    // ------------------------------------------------------------------

    @Test
    public void testSinConteoFisicoNoSeConfirma() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");

        VerificacionInventario resultado = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");

        assertEquals(ResultadoVerificacion.SIN_CONTEO_FISICO, resultado.getResultado());
        assertEquals("DISPONIBLE", estadoActual(v.getId()), "sin conteo no se debe tocar el estado");
    }

    @Test
    public void testEstadoNoVendibleEsRechazado() throws Exception {
        Vehiculo v = nuevoVehiculo("MANTENIMIENTO");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.MANTENIMIENTO, true, "bodega", null);

        VerificacionInventario resultado = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");

        assertEquals(ResultadoVerificacion.ESTADO_NO_VENDIBLE, resultado.getResultado(),
                "RN-10-04: solo una unidad DISPONIBLE puede pasar a venta");
    }

    @Test
    public void testAlquilerVigenteBloqueaLaVenta() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.DISPONIBLE, true, "bodega", null);
        crearContratoAlquilerVigente(v.getId());

        VerificacionInventario resultado = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");

        assertEquals(ResultadoVerificacion.OPERACION_ACTIVA, resultado.getResultado(),
                "flujo alterno 3A: un alquiler vigente impide la venta");
        assertEquals("DISPONIBLE", estadoActual(v.getId()), "no debe bloquearse la unidad");
    }

    // ------------------------------------------------------------------
    // Validación de entradas y trazabilidad
    // ------------------------------------------------------------------

    @Test
    public void testVehiculoInexistenteEsRechazado() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.verificarDisponibilidadParaVenta(999999L, "asesor"));
    }

    @Test
    public void testVehiculoNuloEsRechazado() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.verificarDisponibilidadParaVenta(null, "asesor"));
    }

    @Test
    public void testSolicitanteVacioEsRechazado() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        assertThrows(IllegalArgumentException.class,
                () -> servicio.verificarDisponibilidadParaVenta(v.getId(), "   "));
    }

    @Test
    public void testConteoSinEstadoFisicoEsRechazado() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarConteoFisico(v.getId(), null, true, "bodega", null));
    }

    @Test
    public void testTodaVerificacionQuedaRegistrada() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");   // sin conteo
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.DISPONIBLE, true, "bodega", null);
        servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");   // favorable

        List<VerificacionInventario> historial = servicio.historial(v.getId());
        assertEquals(2, historial.size(),
                "RN-10-08: se registran también las verificaciones que niegan la venta");
    }

    @Test
    public void testUltimoConteoEsElQueMandaEnLaComparacion() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.VENDIDO, true, "bodega", "primero");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.DISPONIBLE, true, "bodega", "segundo");

        assertEquals(EstadoVehiculo.DISPONIBLE, servicio.ultimoConteo(v.getId()).orElseThrow().getEstadoFisico());

        VerificacionInventario resultado = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");
        assertEquals(ResultadoVerificacion.DISPONIBLE_CONFIRMADO, resultado.getResultado());
    }

    @Test
    public void testIncidenciaGuardaAmbosEstadosEnConflicto() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.VENDIDO, true, "bodega", null);
        servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");

        IncidenciaInventario incidencia = servicio.listarIncidenciasAbiertas().stream()
                .filter(i -> i.getVehiculoId().equals(v.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(EstadoVehiculo.DISPONIBLE, incidencia.getEstadoSistema());
        assertEquals(EstadoVehiculo.VENDIDO, incidencia.getEstadoFisico());
        assertEquals("asesor", incidencia.getDetectadaPor());
    }

    // ------------------------------------------------------------------
    // Bloqueo temporal de venta (RN-10-07)
    // ------------------------------------------------------------------

    @Test
    public void testLiberarBloqueoDevuelveLaUnidadADisponible() throws Exception {
        Vehiculo v = confirmarDisponibilidad();
        assertEquals("RESERVADO", estadoActual(v.getId()));

        servicio.liberarBloqueo(v.getId());

        assertEquals("DISPONIBLE", estadoActual(v.getId()));
    }

    @Test
    public void testTrasLiberarSePuedeVolverAVerificar() throws Exception {
        Vehiculo v = confirmarDisponibilidad();
        servicio.liberarBloqueo(v.getId());

        VerificacionInventario segunda = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");
        assertEquals(ResultadoVerificacion.DISPONIBLE_CONFIRMADO, segunda.getResultado());
    }

    @Test
    public void testLiberarUnaUnidadNoReservadaEsRechazado() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        assertThrows(ReglaNegocioException.class, () -> servicio.liberarBloqueo(v.getId()));
    }

    @Test
    public void testLiberarVehiculoInexistenteEsRechazado() {
        assertThrows(ReglaNegocioException.class, () -> servicio.liberarBloqueo(999999L));
    }

    @Test
    public void testBloqueoVencidoSeLiberaSolo() throws Exception {
        Vehiculo v = confirmarDisponibilidad();
        vencerBloqueo(v.getId());

        int liberadas = servicio.liberarBloqueosVencidos();

        assertTrue(liberadas >= 1, "debe liberar al menos la unidad con el bloqueo vencido");
        assertEquals("DISPONIBLE", estadoActual(v.getId()),
                "RN-10-07: el bloqueo es temporal, así que al vencer la unidad vuelve a DISPONIBLE");
    }

    @Test
    public void testBloqueoVigenteNoSeLibera() throws Exception {
        Vehiculo v = confirmarDisponibilidad();

        servicio.liberarBloqueosVencidos();

        assertEquals("RESERVADO", estadoActual(v.getId()),
                "un bloqueo todavía vigente no debe liberarse");
    }

    @Test
    public void testLiberarVencidosNoTocaUnidadesEnVerificacion() throws Exception {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.MANTENIMIENTO, true, "bodega", null);
        servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");
        assertEquals("EN_VERIFICACION", estadoActual(v.getId()));

        servicio.liberarBloqueosVencidos();

        assertEquals("EN_VERIFICACION", estadoActual(v.getId()),
                "una discrepancia sin resolver no se libera por vencimiento");
    }

    /** Deja una unidad RESERVADA por una verificación favorable. */
    private Vehiculo confirmarDisponibilidad() throws SQLException {
        Vehiculo v = nuevoVehiculo("DISPONIBLE");
        servicio.registrarConteoFisico(v.getId(), EstadoVehiculo.DISPONIBLE, true, "bodega", null);
        VerificacionInventario r = servicio.verificarDisponibilidadParaVenta(v.getId(), "asesor");
        assertEquals(ResultadoVerificacion.DISPONIBLE_CONFIRMADO, r.getResultado());
        return v;
    }

    /** Antedata el bloqueo de la unidad para simular que ya pasaron los 30 minutos. */
    private void vencerBloqueo(Long vehiculoId) throws SQLException {
        String sql = "UPDATE verificaciones_inventario "
                   + "SET bloqueo_expira = DATEADD('MINUTE', -5, CURRENT_TIMESTAMP) "
                   + "WHERE vehiculo_id = ? AND bloqueo_expira IS NOT NULL";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vehiculoId);
            stmt.executeUpdate();
        }
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------

    /**
     * Inserta un contrato de alquiler EN_CURSO usando la semilla de clientes y puntos
     * de entrega, para probar el flujo alterno 3A sin depender del servicio de alquiler.
     */
    private void crearContratoAlquilerVigente(Long vehiculoId) throws SQLException {
        String sql = "INSERT INTO contratos_alquiler (numero_contrato, cliente_id, vehiculo_id, "
                   + "punto_entrega_origen_id, punto_entrega_destino_id, fecha_inicio, fecha_fin_estimada, "
                   + "valor_total, deposito_pagado, estado) "
                   + "VALUES (?, (SELECT id FROM clientes WHERE documento = '1018456789'), ?, "
                   + "'SEDE-01', 'SEDE-01', CURRENT_TIMESTAMP, DATEADD('DAY', 5, CURRENT_TIMESTAMP), "
                   + "1500000.00, 500000.00, 'EN_CURSO')";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "ALQ-CU10-" + vehiculoId);
            stmt.setLong(2, vehiculoId);
            stmt.executeUpdate();
        }
    }
}
