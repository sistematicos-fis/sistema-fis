package co.edu.javeriana.concesionario.inventario.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.inventario.modelo.ConteoFisico;
import co.edu.javeriana.concesionario.inventario.modelo.EstadoIncidencia;
import co.edu.javeriana.concesionario.inventario.modelo.EstadoVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.IncidenciaInventario;
import co.edu.javeriana.concesionario.inventario.modelo.ResultadoVerificacion;
import co.edu.javeriana.concesionario.inventario.modelo.VerificacionInventario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC sobre H2 del repositorio de verificación de existencias (CU-10).
 *
 * Las operaciones que tocan más de una tabla (resultado de verificación + incidencia +
 * estado del vehículo) se ejecutan dentro de una única transacción, como exige RF-28.
 *
 * Responsable: Juan Pablo León
 */
public class VerificacionInventarioDAO implements VerificacionInventarioRepositorio {

    // ------------------------------------------------------------------
    // Conteos físicos
    // ------------------------------------------------------------------

    @Override
    public ConteoFisico registrarConteo(ConteoFisico conteo) throws SQLException {
        String sql = "INSERT INTO conteos_fisicos (vehiculo_id, bodega_id, estado_fisico, presente, "
                   + "responsable, observaciones) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, conteo.getVehiculoId());
            if (conteo.getBodegaId() != null) {
                stmt.setLong(2, conteo.getBodegaId());
            } else {
                stmt.setNull(2, java.sql.Types.BIGINT);
            }
            stmt.setString(3, conteo.getEstadoFisico() != null ? conteo.getEstadoFisico().name() : null);
            stmt.setBoolean(4, conteo.isPresente());
            stmt.setString(5, conteo.getResponsable());
            stmt.setString(6, conteo.getObservaciones());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    conteo.setId(rs.getLong(1));
                }
            }
        }
        return conteo;
    }

    @Override
    public Optional<ConteoFisico> ultimoConteo(Long vehiculoId) throws SQLException {
        // Se ordena también por id porque varios conteos del mismo segundo comparten timestamp.
        String sql = "SELECT * FROM conteos_fisicos WHERE vehiculo_id = ? "
                   + "ORDER BY fecha_conteo DESC, id DESC LIMIT 1";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vehiculoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearConteo(rs));
                }
            }
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------------
    // Incidencias
    // ------------------------------------------------------------------

    @Override
    public Optional<IncidenciaInventario> incidenciaAbierta(Long vehiculoId) throws SQLException {
        String sql = "SELECT * FROM incidencias_inventario WHERE vehiculo_id = ? AND estado = 'ABIERTA' "
                   + "ORDER BY id DESC LIMIT 1";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vehiculoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearIncidencia(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<IncidenciaInventario> listarIncidenciasAbiertas() throws SQLException {
        List<IncidenciaInventario> lista = new ArrayList<>();
        String sql = "SELECT * FROM incidencias_inventario WHERE estado = 'ABIERTA' ORDER BY fecha_deteccion DESC, id DESC";
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearIncidencia(rs));
            }
        }
        return lista;
    }

    @Override
    public Optional<IncidenciaInventario> buscarIncidencia(Long incidenciaId) throws SQLException {
        String sql = "SELECT * FROM incidencias_inventario WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, incidenciaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearIncidencia(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void resolverIncidencia(Long incidenciaId, EstadoVehiculo estadoConfirmado,
                                   String resueltaPor, String observacion) throws SQLException {
        String sqlIncidencia = "UPDATE incidencias_inventario SET estado = 'RESUELTA', resuelta_por = ?, "
                             + "observacion_resolucion = ?, fecha_resolucion = CURRENT_TIMESTAMP WHERE id = ?";
        String sqlVehiculo = "UPDATE vehiculos SET estado = ? WHERE id = "
                           + "(SELECT vehiculo_id FROM incidencias_inventario WHERE id = ?)";

        Connection conn = ConexionBD.obtenerConexion();
        boolean autoCommitOriginal = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlIncidencia)) {
                stmt.setString(1, resueltaPor);
                stmt.setString(2, observacion);
                stmt.setLong(3, incidenciaId);
                stmt.executeUpdate();
            }
            if (estadoConfirmado != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlVehiculo)) {
                    stmt.setString(1, estadoConfirmado.name());
                    stmt.setLong(2, incidenciaId);
                    stmt.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommitOriginal);
        }
    }

    // ------------------------------------------------------------------
    // Verificaciones
    // ------------------------------------------------------------------

    @Override
    public VerificacionInventario registrarResultado(VerificacionInventario verificacion,
                                                     EstadoVehiculo nuevoEstadoVehiculo) throws SQLException {
        String sqlVerificacion = "INSERT INTO verificaciones_inventario (vehiculo_id, resultado, motivo, "
                               + "estado_sistema, estado_fisico, solicitado_por, bloqueo_expira) "
                               + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlIncidencia = "INSERT INTO incidencias_inventario (vehiculo_id, estado_sistema, estado_fisico, "
                             + "descripcion, estado, detectada_por) VALUES (?, ?, ?, ?, 'ABIERTA', ?)";
        String sqlVehiculo = "UPDATE vehiculos SET estado = ? WHERE id = ?";

        Connection conn = ConexionBD.obtenerConexion();
        boolean autoCommitOriginal = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlVerificacion, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, verificacion.getVehiculoId());
                stmt.setString(2, verificacion.getResultado().name());
                stmt.setString(3, verificacion.getMotivo());
                stmt.setString(4, nombreEstado(verificacion.getEstadoSistema()));
                stmt.setString(5, nombreEstado(verificacion.getEstadoFisico()));
                stmt.setString(6, verificacion.getSolicitadoPor());
                if (verificacion.getBloqueoExpira() != null) {
                    stmt.setTimestamp(7, Timestamp.valueOf(verificacion.getBloqueoExpira()));
                } else {
                    stmt.setNull(7, java.sql.Types.TIMESTAMP);
                }
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        verificacion.setId(rs.getLong(1));
                    }
                }
            }

            IncidenciaInventario incidencia = verificacion.getIncidencia();
            if (incidencia != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlIncidencia, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setLong(1, incidencia.getVehiculoId());
                    stmt.setString(2, nombreEstado(incidencia.getEstadoSistema()));
                    stmt.setString(3, nombreEstado(incidencia.getEstadoFisico()));
                    stmt.setString(4, incidencia.getDescripcion());
                    stmt.setString(5, incidencia.getDetectadaPor());
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            incidencia.setId(rs.getLong(1));
                        }
                    }
                }
            }

            if (nuevoEstadoVehiculo != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlVehiculo)) {
                    stmt.setString(1, nuevoEstadoVehiculo.name());
                    stmt.setLong(2, verificacion.getVehiculoId());
                    stmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommitOriginal);
        }
        return verificacion;
    }

    @Override
    public List<VerificacionInventario> listarVerificaciones(Long vehiculoId) throws SQLException {
        List<VerificacionInventario> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM verificaciones_inventario");
        if (vehiculoId != null) {
            sql.append(" WHERE vehiculo_id = ?");
        }
        sql.append(" ORDER BY fecha_verificacion DESC, id DESC");

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            if (vehiculoId != null) {
                stmt.setLong(1, vehiculoId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearVerificacion(rs));
                }
            }
        }
        return lista;
    }

    // ------------------------------------------------------------------
    // Bloqueo temporal de venta
    // ------------------------------------------------------------------

    @Override
    public boolean liberarBloqueo(Long vehiculoId) throws SQLException {
        String sql = "UPDATE vehiculos SET estado = 'DISPONIBLE' WHERE id = ? AND estado = 'RESERVADO'";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vehiculoId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public int liberarBloqueosVencidos() throws SQLException {
        // Se mira solo la verificación más reciente que fijó un bloqueo: si esa ya venció,
        // la reserva perdió vigencia. La subconsulta se correlaciona por nombre de tabla
        // porque H2 no admite alias en el UPDATE.
        String sql = "UPDATE vehiculos SET estado = 'DISPONIBLE' "
                   + "WHERE estado = 'RESERVADO' AND EXISTS ("
                   + "  SELECT 1 FROM verificaciones_inventario v"
                   + "  WHERE v.vehiculo_id = vehiculos.id"
                   + "    AND v.bloqueo_expira IS NOT NULL"
                   + "    AND v.bloqueo_expira < CURRENT_TIMESTAMP"
                   + "    AND v.id = (SELECT MAX(u.id) FROM verificaciones_inventario u"
                   + "                WHERE u.vehiculo_id = vehiculos.id AND u.bloqueo_expira IS NOT NULL)"
                   + ")";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return stmt.executeUpdate();
        }
    }

    // ------------------------------------------------------------------
    // Consultas de apoyo
    // ------------------------------------------------------------------

    @Override
    public boolean tieneOperacionActiva(Long vehiculoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM contratos_alquiler WHERE vehiculo_id = ? "
                   + "AND estado IN ('RESERVADO', 'EN_CURSO')";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vehiculoId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ------------------------------------------------------------------
    // Mapeo
    // ------------------------------------------------------------------

    private String nombreEstado(EstadoVehiculo estado) {
        return estado != null ? estado.name() : null;
    }

    private LocalDateTime aFechaHora(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private ConteoFisico mapearConteo(ResultSet rs) throws SQLException {
        ConteoFisico c = new ConteoFisico();
        c.setId(rs.getLong("id"));
        c.setVehiculoId(rs.getLong("vehiculo_id"));
        long bodega = rs.getLong("bodega_id");
        if (!rs.wasNull()) {
            c.setBodegaId(bodega);
        }
        c.setEstadoFisico(EstadoVehiculo.desdeTexto(rs.getString("estado_fisico")));
        c.setPresente(rs.getBoolean("presente"));
        c.setResponsable(rs.getString("responsable"));
        c.setObservaciones(rs.getString("observaciones"));
        c.setFechaConteo(aFechaHora(rs.getTimestamp("fecha_conteo")));
        return c;
    }

    private IncidenciaInventario mapearIncidencia(ResultSet rs) throws SQLException {
        IncidenciaInventario i = new IncidenciaInventario();
        i.setId(rs.getLong("id"));
        i.setVehiculoId(rs.getLong("vehiculo_id"));
        i.setEstadoSistema(EstadoVehiculo.desdeTexto(rs.getString("estado_sistema")));
        i.setEstadoFisico(EstadoVehiculo.desdeTexto(rs.getString("estado_fisico")));
        i.setDescripcion(rs.getString("descripcion"));
        String estado = rs.getString("estado");
        i.setEstado(estado != null ? EstadoIncidencia.valueOf(estado) : EstadoIncidencia.ABIERTA);
        i.setDetectadaPor(rs.getString("detectada_por"));
        i.setFechaDeteccion(aFechaHora(rs.getTimestamp("fecha_deteccion")));
        i.setResueltaPor(rs.getString("resuelta_por"));
        i.setObservacionResolucion(rs.getString("observacion_resolucion"));
        i.setFechaResolucion(aFechaHora(rs.getTimestamp("fecha_resolucion")));
        return i;
    }

    private VerificacionInventario mapearVerificacion(ResultSet rs) throws SQLException {
        VerificacionInventario v = new VerificacionInventario();
        v.setId(rs.getLong("id"));
        v.setVehiculoId(rs.getLong("vehiculo_id"));
        v.setResultado(ResultadoVerificacion.valueOf(rs.getString("resultado")));
        v.setMotivo(rs.getString("motivo"));
        v.setEstadoSistema(EstadoVehiculo.desdeTexto(rs.getString("estado_sistema")));
        v.setEstadoFisico(EstadoVehiculo.desdeTexto(rs.getString("estado_fisico")));
        v.setSolicitadoPor(rs.getString("solicitado_por"));
        v.setFechaVerificacion(aFechaHora(rs.getTimestamp("fecha_verificacion")));
        v.setBloqueoExpira(aFechaHora(rs.getTimestamp("bloqueo_expira")));
        return v;
    }
}
