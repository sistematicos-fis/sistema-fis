package co.edu.javeriana.concesionario.financiacion.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.financiacion.modelo.SolicitudCredito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para radicación y evaluación de solicitudes de crédito (CU-14).
 */
public class SolicitudCreditoDAO {

    public SolicitudCredito guardar(SolicitudCredito s) throws SQLException {
        String sql = "INSERT INTO solicitudes_credito (numero_radicado, cliente_id, vehiculo_id, tipo_credito_id, " +
                     "monto_solicitado, plazo_meses, cuota_inicial, estado, ingresos_certificados, " +
                     "gastos_declarados, cuota_estimada, porcentaje_endeudamiento, score_crediticio_simulado, " +
                     "observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, s.getNumeroRadicado());
            stmt.setLong(2, s.getClienteId());
            stmt.setLong(3, s.getVehiculoId());
            stmt.setLong(4, s.getTipoCreditoId());
            stmt.setBigDecimal(5, s.getMontoSolicitado());
            stmt.setInt(6, s.getPlazoMeses());
            stmt.setBigDecimal(7, s.getCuotaInicial());
            stmt.setString(8, s.getEstado());
            stmt.setBigDecimal(9, s.getIngresosCertificados());
            stmt.setBigDecimal(10, s.getGastosDeclarados());
            stmt.setBigDecimal(11, s.getCuotaEstimada());
            stmt.setBigDecimal(12, s.getPorcentajeEndeudamiento());
            if (s.getScoreCrediticioSimulado() != null) {
                stmt.setInt(13, s.getScoreCrediticioSimulado());
            } else {
                stmt.setNull(13, java.sql.Types.INTEGER);
            }
            stmt.setString(14, s.getObservaciones());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    s.setId(rs.getLong(1));
                }
            }
        }
        return s;
    }

    public void actualizarEstado(Long id, String nuevoEstado, Integer score, String observaciones) throws SQLException {
        String sql = "UPDATE solicitudes_credito SET estado = ?, score_crediticio_simulado = ?, observaciones = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado);
            if (score != null) {
                stmt.setInt(2, score);
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setString(3, observaciones);
            stmt.setLong(4, id);
            stmt.executeUpdate();
        }
    }

    public Optional<SolicitudCredito> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM solicitudes_credito WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<SolicitudCredito> buscarPorRadicado(String radicado) throws SQLException {
        String sql = "SELECT * FROM solicitudes_credito WHERE numero_radicado = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, radicado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<SolicitudCredito> listarTodas() throws SQLException {
        List<SolicitudCredito> lista = new ArrayList<>();
        String sql = "SELECT * FROM solicitudes_credito ORDER BY id DESC";
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        }
        return lista;
    }

    private SolicitudCredito mapearResultSet(ResultSet rs) throws SQLException {
        SolicitudCredito s = new SolicitudCredito();
        s.setId(rs.getLong("id"));
        s.setNumeroRadicado(rs.getString("numero_radicado"));
        s.setClienteId(rs.getLong("cliente_id"));
        s.setVehiculoId(rs.getLong("vehiculo_id"));
        s.setTipoCreditoId(rs.getLong("tipo_credito_id"));
        s.setMontoSolicitado(rs.getBigDecimal("monto_solicitado"));
        s.setPlazoMeses(rs.getInt("plazo_meses"));
        s.setCuotaInicial(rs.getBigDecimal("cuota_inicial"));
        s.setEstado(rs.getString("estado"));
        s.setIngresosCertificados(rs.getBigDecimal("ingresos_certificados"));
        s.setGastosDeclarados(rs.getBigDecimal("gastos_declarados"));
        s.setCuotaEstimada(rs.getBigDecimal("cuota_estimada"));
        s.setPorcentajeEndeudamiento(rs.getBigDecimal("porcentaje_endeudamiento"));
        int score = rs.getInt("score_crediticio_simulado");
        if (!rs.wasNull()) {
            s.setScoreCrediticioSimulado(score);
        }
        s.setObservaciones(rs.getString("observaciones"));
        java.sql.Timestamp ts = rs.getTimestamp("fecha_solicitud");
        if (ts != null) {
            s.setFechaSolicitud(ts.toLocalDateTime());
        }
        return s;
    }
}
