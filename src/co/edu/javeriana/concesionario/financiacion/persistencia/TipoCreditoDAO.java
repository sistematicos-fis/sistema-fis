package co.edu.javeriana.concesionario.financiacion.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.financiacion.modelo.TipoCredito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para configuración y persistencia de Tipos de Crédito en H2 (CU-12).
 */
public class TipoCreditoDAO {

    public TipoCredito guardar(TipoCredito tipo) throws SQLException {
        String sql = "INSERT INTO tipos_credito (codigo, nombre, descripcion, entidad_financiera_id, " +
                     "tasa_interes_ea, plazo_minimo_meses, plazo_maximo_meses, porcentaje_financiacion_max, " +
                     "requiere_garantia_prenda, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tipo.getCodigo());
            stmt.setString(2, tipo.getNombre());
            stmt.setString(3, tipo.getDescripcion());
            stmt.setLong(4, tipo.getEntidadFinancieraId());
            stmt.setBigDecimal(5, tipo.getTasaInteresEa());
            stmt.setInt(6, tipo.getPlazoMinimoMeses());
            stmt.setInt(7, tipo.getPlazoMaximoMeses());
            stmt.setBigDecimal(8, tipo.getPorcentajeFinanciacionMax());
            stmt.setBoolean(9, tipo.isRequiereGarantiaPrenda());
            stmt.setBoolean(10, tipo.isActivo());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    tipo.setId(rs.getLong(1));
                }
            }
        }
        return tipo;
    }

    public List<TipoCredito> listarActivos() throws SQLException {
        List<TipoCredito> lista = new ArrayList<>();
        String sql = "SELECT * FROM tipos_credito WHERE activo = TRUE ORDER BY nombre";
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        }
        return lista;
    }

    public Optional<TipoCredito> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM tipos_credito WHERE id = ?";
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

    private TipoCredito mapearResultSet(ResultSet rs) throws SQLException {
        TipoCredito tc = new TipoCredito();
        tc.setId(rs.getLong("id"));
        tc.setCodigo(rs.getString("codigo"));
        tc.setNombre(rs.getString("nombre"));
        tc.setDescripcion(rs.getString("descripcion"));
        tc.setEntidadFinancieraId(rs.getLong("entidad_financiera_id"));
        tc.setTasaInteresEa(rs.getBigDecimal("tasa_interes_ea"));
        tc.setPlazoMinimoMeses(rs.getInt("plazo_minimo_meses"));
        tc.setPlazoMaximoMeses(rs.getInt("plazo_maximo_meses"));
        tc.setPorcentajeFinanciacionMax(rs.getBigDecimal("porcentaje_financiacion_max"));
        tc.setRequiereGarantiaPrenda(rs.getBoolean("requiere_garantia_prenda"));
        tc.setActivo(rs.getBoolean("activo"));
        return tc;
    }
}
