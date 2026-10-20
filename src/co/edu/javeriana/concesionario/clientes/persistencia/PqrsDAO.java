package co.edu.javeriana.concesionario.clientes.persistencia;

import co.edu.javeriana.concesionario.clientes.modelo.Pqrs;
import co.edu.javeriana.concesionario.comun.bd.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestión de PQRS en H2 (CU-03).
 */
public class PqrsDAO {

    public Pqrs radicar(Pqrs pqrs) throws SQLException {
        String sql = "INSERT INTO pqrs (cliente_id, tipo, descripcion, estado) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, pqrs.getClienteId());
            stmt.setString(2, pqrs.getTipo());
            stmt.setString(3, pqrs.getDescripcion());
            stmt.setString(4, pqrs.getEstado());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pqrs.setId(rs.getLong(1));
                }
            }
        }
        return pqrs;
    }

    public List<Pqrs> listarTodas() throws SQLException {
        List<Pqrs> lista = new ArrayList<>();
        String sql = "SELECT * FROM pqrs ORDER BY fecha_radicacion DESC";
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Pqrs p = new Pqrs();
                p.setId(rs.getLong("id"));
                p.setClienteId(rs.getLong("cliente_id"));
                p.setTipo(rs.getString("tipo"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setEstado(rs.getString("estado"));
                java.sql.Timestamp ts = rs.getTimestamp("fecha_radicacion");
                if (ts != null) p.setFechaRadicacion(ts.toLocalDateTime());
                java.sql.Timestamp tsResolucion = rs.getTimestamp("fecha_resolucion");
                if (tsResolucion != null) p.setFechaResolucion(tsResolucion.toLocalDateTime());
                lista.add(p);
            }
        }
        return lista;
    }
}
