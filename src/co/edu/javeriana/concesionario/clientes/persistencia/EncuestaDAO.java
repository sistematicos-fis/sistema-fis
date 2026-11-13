package co.edu.javeriana.concesionario.clientes.persistencia;

import co.edu.javeriana.concesionario.clientes.modelo.EncuestaSatisfaccion;
import co.edu.javeriana.concesionario.comun.bd.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para gestión de encuestas de satisfacción en H2 (CU-04).
 * Responsable: Andrés Alquerque
 */
public class EncuestaDAO {

    public EncuestaSatisfaccion registrar(EncuestaSatisfaccion e) throws SQLException {
        String sql = "INSERT INTO encuestas_satisfaccion (cliente_id, puntuacion_atencion, puntuacion_producto, " +
                     "puntuacion_tiempos, comentarios, fecha) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, e.getClienteId());
            stmt.setInt(2, e.getPuntuacionAtencion());
            stmt.setInt(3, e.getPuntuacionProducto());
            stmt.setInt(4, e.getPuntuacionTiempos());
            stmt.setString(5, e.getComentarios());
            stmt.setTimestamp(6, Timestamp.valueOf(e.getFecha()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getLong(1));
                }
            }
        }
        return e;
    }

    public List<EncuestaSatisfaccion> listarTodas() throws SQLException {
        return listar("SELECT * FROM encuestas_satisfaccion ORDER BY fecha DESC, id DESC", null);
    }

    public List<EncuestaSatisfaccion> listarPorCliente(Long clienteId) throws SQLException {
        return listar("SELECT * FROM encuestas_satisfaccion WHERE cliente_id = ? ORDER BY fecha DESC, id DESC", clienteId);
    }

    public Optional<EncuestaSatisfaccion> buscarPorId(Long id) throws SQLException {
        List<EncuestaSatisfaccion> resultado = listar("SELECT * FROM encuestas_satisfaccion WHERE id = ?", id);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public boolean actualizar(EncuestaSatisfaccion e) throws SQLException {
        String sql = "UPDATE encuestas_satisfaccion SET puntuacion_atencion = ?, puntuacion_producto = ?, " +
                     "puntuacion_tiempos = ?, comentarios = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, e.getPuntuacionAtencion());
            stmt.setInt(2, e.getPuntuacionProducto());
            stmt.setInt(3, e.getPuntuacionTiempos());
            stmt.setString(4, e.getComentarios());
            stmt.setLong(5, e.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM encuestas_satisfaccion WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private List<EncuestaSatisfaccion> listar(String sql, Long parametro) throws SQLException {
        List<EncuestaSatisfaccion> lista = new ArrayList<>();
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

    private static EncuestaSatisfaccion mapear(ResultSet rs) throws SQLException {
        EncuestaSatisfaccion e = new EncuestaSatisfaccion();
        e.setId(rs.getLong("id"));
        e.setClienteId(rs.getLong("cliente_id"));
        e.setPuntuacionAtencion(rs.getInt("puntuacion_atencion"));
        e.setPuntuacionProducto(rs.getInt("puntuacion_producto"));
        e.setPuntuacionTiempos(rs.getInt("puntuacion_tiempos"));
        e.setComentarios(rs.getString("comentarios"));
        Timestamp fecha = rs.getTimestamp("fecha");
        e.setFecha(fecha != null ? fecha.toLocalDateTime() : null);
        return e;
    }
}
