package co.edu.javeriana.concesionario.alquiler.persistencia;

import co.edu.javeriana.concesionario.alquiler.modelo.PuntoEntrega;
import co.edu.javeriana.concesionario.comun.bd.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de puntos de entrega sobre la tabla puntos_entrega de H2.
 * Responsable: Samuel Borras (CU-16)
 */
public class PuntoEntregaDAO implements PuntoEntregaRepository {

    @Override
    public PuntoEntrega guardar(PuntoEntrega punto) {
        if (punto == null || punto.getId() == null) {
            throw new IllegalArgumentException("El punto de entrega y su ID no pueden ser nulos.");
        }
        String sql = "MERGE INTO puntos_entrega (id, nombre, direccion, ciudad, activo) KEY(id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, punto.getId());
            stmt.setString(2, punto.getNombre());
            stmt.setString(3, punto.getDireccion());
            stmt.setString(4, punto.getCiudad());
            stmt.setBoolean(5, punto.isActivo());
            stmt.executeUpdate();
            return punto;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar el punto de entrega en la base de datos: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<PuntoEntrega> buscarPorId(String id) {
        String sql = "SELECT * FROM puntos_entrega WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el punto de entrega: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PuntoEntrega> listarTodos() {
        return listar("SELECT * FROM puntos_entrega ORDER BY id");
    }

    @Override
    public List<PuntoEntrega> listarActivos() {
        return listar("SELECT * FROM puntos_entrega WHERE activo = TRUE ORDER BY id");
    }

    @Override
    public boolean eliminarLogico(String id) {
        String sql = "UPDATE puntos_entrega SET activo = FALSE WHERE id = ? AND activo = TRUE";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo deshabilitar el punto de entrega: " + e.getMessage(), e);
        }
    }

    private List<PuntoEntrega> listar(String sql) {
        List<PuntoEntrega> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron consultar los puntos de entrega: " + e.getMessage(), e);
        }
        return lista;
    }

    private PuntoEntrega mapear(ResultSet rs) throws SQLException {
        return new PuntoEntrega(
                rs.getString("id"),
                rs.getString("nombre"),
                rs.getString("direccion"),
                rs.getString("ciudad"),
                rs.getBoolean("activo"));
    }
}
