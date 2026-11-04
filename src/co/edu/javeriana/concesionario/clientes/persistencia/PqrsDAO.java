package co.edu.javeriana.concesionario.clientes.persistencia;

import co.edu.javeriana.concesionario.clientes.modelo.Pqrs;
import co.edu.javeriana.concesionario.comun.bd.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return listar("SELECT * FROM pqrs ORDER BY fecha_radicacion DESC, id DESC", null);
    }

    public List<Pqrs> listarPorCliente(Long clienteId) throws SQLException {
        return listar("SELECT * FROM pqrs WHERE cliente_id = ? ORDER BY fecha_radicacion DESC, id DESC", clienteId);
    }

    public Optional<Pqrs> buscarPorId(Long id) throws SQLException {
        List<Pqrs> lista = listar("SELECT * FROM pqrs WHERE id = ?", id);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    public boolean actualizar(Pqrs pqrs) throws SQLException {
        String sql = "UPDATE pqrs SET tipo = ?, descripcion = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pqrs.getTipo());
            stmt.setString(2, pqrs.getDescripcion());
            stmt.setLong(3, pqrs.getId());
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean actualizarEstado(Long id, String estado, LocalDateTime fechaResolucion, String respuesta)
            throws SQLException {
        String sql = "UPDATE pqrs SET estado = ?, fecha_resolucion = ?, respuesta = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            if (fechaResolucion != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(fechaResolucion));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }
            stmt.setString(3, respuesta);
            stmt.setLong(4, id);
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM pqrs WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() == 1;
        }
    }

    private List<Pqrs> listar(String sql, Long parametro) throws SQLException {
        List<Pqrs> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (parametro != null) {
                stmt.setLong(1, parametro);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private static Pqrs mapear(ResultSet rs) throws SQLException {
        Pqrs p = new Pqrs();
        p.setId(rs.getLong("id"));
        p.setClienteId(rs.getLong("cliente_id"));
        p.setTipo(rs.getString("tipo"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setEstado(rs.getString("estado"));
        Timestamp radicacion = rs.getTimestamp("fecha_radicacion");
        p.setFechaRadicacion(radicacion != null ? radicacion.toLocalDateTime() : null);
        Timestamp resolucion = rs.getTimestamp("fecha_resolucion");
        p.setFechaResolucion(resolucion != null ? resolucion.toLocalDateTime() : null);
        p.setRespuesta(rs.getString("respuesta"));
        return p;
    }
}
