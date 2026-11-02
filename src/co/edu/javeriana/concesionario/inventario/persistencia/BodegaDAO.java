package co.edu.javeriana.concesionario.inventario.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.inventario.modelo.Bodega;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link BodegaRepositorio} sobre H2 (CU-07).
 * No valida ni toma decisiones de negocio: esas viven en InventarioServicio.
 *
 * Responsable: Juan Pablo León
 */
public class BodegaDAO implements BodegaRepositorio {

    @Override
    public Bodega guardar(Bodega bodega) throws SQLException {
        String sql = "INSERT INTO bodegas (codigo, nombre, ciudad, capacidad_vehiculos, activo) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, bodega.getCodigo());
            stmt.setString(2, bodega.getNombre());
            stmt.setString(3, bodega.getCiudad());
            stmt.setInt(4, bodega.getCapacidadVehiculos());
            stmt.setBoolean(5, bodega.isActivo());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    bodega.setId(rs.getLong(1));
                }
            }
        }
        return bodega;
    }

    @Override
    public void actualizar(Bodega bodega) throws SQLException {
        String sql = "UPDATE bodegas SET codigo = ?, nombre = ?, ciudad = ?, "
                   + "capacidad_vehiculos = ?, activo = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bodega.getCodigo());
            stmt.setString(2, bodega.getNombre());
            stmt.setString(3, bodega.getCiudad());
            stmt.setInt(4, bodega.getCapacidadVehiculos());
            stmt.setBoolean(5, bodega.isActivo());
            stmt.setLong(6, bodega.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM bodegas WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Bodega> listarTodas() throws SQLException {
        return listar("SELECT * FROM bodegas ORDER BY nombre");
    }

    @Override
    public List<Bodega> listarActivas() throws SQLException {
        return listar("SELECT * FROM bodegas WHERE activo = TRUE ORDER BY nombre");
    }

    @Override
    public Optional<Bodega> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM bodegas WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return buscarUno(stmt);
        }
    }

    @Override
    public Optional<Bodega> buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM bodegas WHERE codigo = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            return buscarUno(stmt);
        }
    }

    @Override
    public int contarVehiculos(Long bodegaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehiculos WHERE bodega_id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bodegaId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<Bodega> listar(String sql) throws SQLException {
        List<Bodega> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        }
        return lista;
    }

    private Optional<Bodega> buscarUno(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapearResultSet(rs));
            }
        }
        return Optional.empty();
    }

    private Bodega mapearResultSet(ResultSet rs) throws SQLException {
        Bodega bodega = new Bodega();
        bodega.setId(rs.getLong("id"));
        bodega.setCodigo(rs.getString("codigo"));
        bodega.setNombre(rs.getString("nombre"));
        bodega.setCiudad(rs.getString("ciudad"));
        bodega.setCapacidadVehiculos(rs.getInt("capacidad_vehiculos"));
        bodega.setActivo(rs.getBoolean("activo"));
        return bodega;
    }
}
