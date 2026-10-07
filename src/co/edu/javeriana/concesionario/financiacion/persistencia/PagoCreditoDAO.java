package co.edu.javeriana.concesionario.financiacion.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.financiacion.modelo.PagoCredito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para registro de pagos de cuotas y actualización de saldos de financiación.
 */
public class PagoCreditoDAO {

    public PagoCredito registrarPago(PagoCredito pago) throws SQLException {
        String sqlPago = "INSERT INTO pagos_credito (credito_formalizado_id, numero_cuota, monto_pagado, " +
                         "numero_recibo, metodo_pago) VALUES (?, ?, ?, ?, ?)";

        String sqlActualizarCuota = "UPDATE cuotas_amortizacion SET estado = 'PAGADA' " +
                                    "WHERE credito_formalizado_id = ? AND numero_cuota = ?";

        String sqlActualizarSaldo = "UPDATE creditos_formalizados SET saldo_capital_pendiente = " +
                                    "GREATEST(0, saldo_capital_pendiente - (SELECT abono_capital FROM cuotas_amortizacion WHERE credito_formalizado_id = ? AND numero_cuota = ?)) " +
                                    "WHERE id = ?";

        try (Connection conn = ConexionBD.obtenerConexion()) {
            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // 1. Insertar el pago
                try (PreparedStatement stmt = conn.prepareStatement(sqlPago, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setLong(1, pago.getCreditoFormalizadoId());
                    stmt.setInt(2, pago.getNumeroCuota());
                    stmt.setBigDecimal(3, pago.getMontoPagado());
                    stmt.setString(4, pago.getNumeroRecibo());
                    stmt.setString(5, pago.getMetodoPago());
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            pago.setId(rs.getLong(1));
                        }
                    }
                }

                // 2. Marcar cuota como pagada
                try (PreparedStatement stmt = conn.prepareStatement(sqlActualizarCuota)) {
                    stmt.setLong(1, pago.getCreditoFormalizadoId());
                    stmt.setInt(2, pago.getNumeroCuota());
                    stmt.executeUpdate();
                }

                // 3. Descontar abono a capital del saldo pendiente
                try (PreparedStatement stmt = conn.prepareStatement(sqlActualizarSaldo)) {
                    stmt.setLong(1, pago.getCreditoFormalizadoId());
                    stmt.setInt(2, pago.getNumeroCuota());
                    stmt.setLong(3, pago.getCreditoFormalizadoId());
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        }
        return pago;
    }

    public List<PagoCredito> listarPorCredito(Long creditoId) throws SQLException {
        List<PagoCredito> pagos = new ArrayList<>();
        String sql = "SELECT * FROM pagos_credito WHERE credito_formalizado_id = ? ORDER BY numero_cuota";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, creditoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PagoCredito p = new PagoCredito();
                    p.setId(rs.getLong("id"));
                    p.setCreditoFormalizadoId(rs.getLong("credito_formalizado_id"));
                    p.setNumeroCuota(rs.getInt("numero_cuota"));
                    p.setMontoPagado(rs.getBigDecimal("monto_pagado"));
                    p.setNumeroRecibo(rs.getString("numero_recibo"));
                    p.setMetodoPago(rs.getString("metodo_pago"));
                    java.sql.Timestamp ts = rs.getTimestamp("fecha_pago");
                    if (ts != null) p.setFechaPago(ts.toLocalDateTime());
                    pagos.add(p);
                }
            }
        }
        return pagos;
    }
}
