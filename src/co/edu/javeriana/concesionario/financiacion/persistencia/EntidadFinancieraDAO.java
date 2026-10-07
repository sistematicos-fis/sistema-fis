package co.edu.javeriana.concesionario.financiacion.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.financiacion.modelo.EntidadFinanciera;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para gestión y persistencia de Entidades Financieras en H2 vía JDBC (CU-11).
 */
public class EntidadFinancieraDAO {

    public EntidadFinanciera guardar(EntidadFinanciera entidad) throws SQLException {
        String sql = "INSERT INTO entidades_financieras (codigo, nombre, nit, tasa_base_ea, scoring_minimo, activo) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entidad.getCodigo());
            stmt.setString(2, entidad.getNombre());
            stmt.setString(3, entidad.getNit());
            stmt.setBigDecimal(4, entidad.getTasaBaseEa());
            stmt.setInt(5, entidad.getScoringMinimo());
            stmt.setBoolean(6, entidad.isActivo());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    entidad.setId(rs.getLong(1));
                }
            }
        }
        return entidad;
    }

    public List<EntidadFinanciera> listarActivas() throws SQLException {
        List<EntidadFinanciera> lista = new ArrayList<>();
        String sql = "SELECT * FROM entidades_financieras WHERE activo = TRUE ORDER BY nombre";
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        }
        return lista;
    }

    public Optional<EntidadFinanciera> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM entidades_financieras WHERE id = ?";
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

    public Optional<EntidadFinanciera> buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM entidades_financieras WHERE codigo = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    private EntidadFinanciera mapearResultSet(ResultSet rs) throws SQLException {
        EntidadFinanciera e = new EntidadFinanciera();
        e.setId(rs.getLong("id"));
        e.setCodigo(rs.getString("codigo"));
        e.setNombre(rs.getString("nombre"));
        e.setNit(rs.getString("nit"));
        e.setTasaBaseEa(rs.getBigDecimal("tasa_base_ea"));
        e.setScoringMinimo(rs.getInt("scoring_minimo"));
        e.setActivo(rs.getBoolean("activo"));
        java.sql.Timestamp ts = rs.getTimestamp("fecha_creacion");
        if (ts != null) {
            e.setFechaCreacion(ts.toLocalDateTime());
        }
        return e;
    }
}
