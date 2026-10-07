package co.edu.javeriana.concesionario.financiacion.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.financiacion.modelo.CreditoFormalizado;
import co.edu.javeriana.concesionario.financiacion.modelo.CuotaAmortizacion;
import co.edu.javeriana.concesionario.financiacion.modelo.TablaAmortizacion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para créditos formalizados y persistencia atómica de tablas de cuotas (CU-15).
 */
public class CreditoFormalizadoDAO {

    public CreditoFormalizado formalizarCreditoConCuotas(CreditoFormalizado cf, TablaAmortizacion tabla) throws SQLException {
        String sqlCredito = "INSERT INTO creditos_formalizados (solicitud_id, codigo_aprobacion_banco, " +
                            "entidad_id, monto_aprobado, tasa_mensual_aplicada, plazo_meses, " +
                            "saldo_capital_pendiente, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlCuota = "INSERT INTO cuotas_amortizacion (credito_formalizado_id, numero_cuota, " +
                          "fecha_vencimiento, cuota_fija, abono_capital, interes, saldo_restante, estado) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlActualizarSol = "UPDATE solicitudes_credito SET estado = 'FORMALIZADA' WHERE id = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // 1. Insertar el crédito formalizado
                try (PreparedStatement stmt = conn.prepareStatement(sqlCredito, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setLong(1, cf.getSolicitudId());
                    stmt.setString(2, cf.getCodigoAprobacionBanco());
                    stmt.setLong(3, cf.getEntidadId());
                    stmt.setBigDecimal(4, cf.getMontoAprobado());
                    stmt.setBigDecimal(5, cf.getTasaMensualAplicada());
                    stmt.setInt(6, cf.getPlazoMeses());
                    stmt.setBigDecimal(7, cf.getSaldoCapitalPendiente());
                    stmt.setString(8, cf.getEstado());
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            cf.setId(rs.getLong(1));
                        }
                    }
                }

                // 2. Insertar las cuotas de la tabla de amortización
                try (PreparedStatement stmtCuota = conn.prepareStatement(sqlCuota)) {
                    for (CuotaAmortizacion c : tabla.getCuotas()) {
                        stmtCuota.setLong(1, cf.getId());
                        stmtCuota.setInt(2, c.getNumeroCuota());
                        stmtCuota.setDate(3, Date.valueOf(c.getFechaVencimiento()));
                        stmtCuota.setBigDecimal(4, c.getCuotaFija());
                        stmtCuota.setBigDecimal(5, c.getAbonoCapital());
                        stmtCuota.setBigDecimal(6, c.getInteres());
                        stmtCuota.setBigDecimal(7, c.getSaldoRestante());
                        stmtCuota.setString(8, c.getEstado());
                        stmtCuota.addBatch();
                    }
                    stmtCuota.executeBatch();
                }

                // 3. Actualizar estado de la solicitud
                try (PreparedStatement stmtSol = conn.prepareStatement(sqlActualizarSol)) {
                    stmtSol.setLong(1, cf.getSolicitudId());
                    stmtSol.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        }
        return cf;
    }

    public Optional<CreditoFormalizado> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM creditos_formalizados WHERE id = ?";
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

    public List<CuotaAmortizacion> listarCuotasPorCredito(Long creditoId) throws SQLException {
        List<CuotaAmortizacion> cuotas = new ArrayList<>();
        String sql = "SELECT * FROM cuotas_amortizacion WHERE credito_formalizado_id = ? ORDER BY numero_cuota";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, creditoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CuotaAmortizacion c = new CuotaAmortizacion();
                    c.setNumeroCuota(rs.getInt("numero_cuota"));
                    Date d = rs.getDate("fecha_vencimiento");
                    if (d != null) c.setFechaVencimiento(d.toLocalDate());
                    c.setCuotaFija(rs.getBigDecimal("cuota_fija"));
                    c.setAbonoCapital(rs.getBigDecimal("abono_capital"));
                    c.setInteres(rs.getBigDecimal("interes"));
                    c.setSaldoRestante(rs.getBigDecimal("saldo_restante"));
                    c.setEstado(rs.getString("estado"));
                    cuotas.add(c);
                }
            }
        }
        return cuotas;
    }

    private CreditoFormalizado mapearResultSet(ResultSet rs) throws SQLException {
        CreditoFormalizado cf = new CreditoFormalizado();
        cf.setId(rs.getLong("id"));
        cf.setSolicitudId(rs.getLong("solicitud_id"));
        cf.setCodigoAprobacionBanco(rs.getString("codigo_aprobacion_banco"));
        cf.setEntidadId(rs.getLong("entidad_id"));
        cf.setMontoAprobado(rs.getBigDecimal("monto_aprobado"));
        cf.setTasaMensualAplicada(rs.getBigDecimal("tasa_mensual_aplicada"));
        cf.setPlazoMeses(rs.getInt("plazo_meses"));
        cf.setEstado(rs.getString("estado"));
        cf.setSaldoCapitalPendiente(rs.getBigDecimal("saldo_capital_pendiente"));
        java.sql.Timestamp ts = rs.getTimestamp("fecha_formalizacion");
        if (ts != null) {
            cf.setFechaFormalizacion(ts.toLocalDateTime());
        }
        return cf;
    }
}
