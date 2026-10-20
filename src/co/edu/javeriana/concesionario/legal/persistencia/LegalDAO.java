package co.edu.javeriana.concesionario.legal.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.legal.modelo.PolizaSeguro;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para persistencia de documentación legal y pólizas de seguro en H2 (CU-31, CU-34).
 * Responsable: Samuel Montaña
 */
public class LegalDAO {

    public PolizaSeguro guardarPoliza(PolizaSeguro p) throws SQLException {
        String sql = "INSERT INTO polizas_seguro (numero_poliza, aseguradora_id, vehiculo_id, cliente_id, " +
                "tipo_poliza, valor_prima, valor_cobertura, fecha_emision, fecha_vencimiento, estado, " +
                "codigo_aprobacion_simulada) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getNumeroPoliza());
            stmt.setLong(2, p.getAseguradoraId());
            stmt.setLong(3, p.getVehiculoId());
            stmt.setLong(4, p.getClienteId());
            stmt.setString(5, p.getTipoPoliza());
            stmt.setBigDecimal(6, p.getValorPrima());
            stmt.setBigDecimal(7, p.getValorCobertura());
            stmt.setDate(8, Date.valueOf(p.getFechaEmision()));
            stmt.setDate(9, Date.valueOf(p.getFechaVencimiento()));
            stmt.setString(10, p.getEstado());
            stmt.setString(11, p.getCodigoAprobacionSimulada());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getLong(1));
                }
            }
        }
        return p;
    }

    public List<PolizaSeguro> listarPolizas() throws SQLException {
        List<PolizaSeguro> lista = new ArrayList<>();
        String sql = "SELECT * FROM polizas_seguro ORDER BY id DESC";
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                PolizaSeguro p = new PolizaSeguro();
                p.setId(rs.getLong("id"));
                p.setNumeroPoliza(rs.getString("numero_poliza"));
                p.setAseguradoraId(rs.getLong("aseguradora_id"));
                p.setVehiculoId(rs.getLong("vehiculo_id"));
                p.setClienteId(rs.getLong("cliente_id"));
                p.setTipoPoliza(rs.getString("tipo_poliza"));
                p.setValorPrima(rs.getBigDecimal("valor_prima"));
                p.setValorCobertura(rs.getBigDecimal("valor_cobertura"));
                Date em = rs.getDate("fecha_emision");
                if (em != null) p.setFechaEmision(em.toLocalDate());
                Date ven = rs.getDate("fecha_vencimiento");
                if (ven != null) p.setFechaVencimiento(ven.toLocalDate());
                p.setEstado(rs.getString("estado"));
                p.setCodigoAprobacionSimulada(rs.getString("codigo_aprobacion_simulada"));
                lista.add(p);
            }
        }
        return lista;
    }
}
