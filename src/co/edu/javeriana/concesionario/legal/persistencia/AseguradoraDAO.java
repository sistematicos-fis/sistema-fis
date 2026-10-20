package co.edu.javeriana.concesionario.legal.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.legal.modelo.Aseguradora;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso JDBC a la tabla (CU-22).
 */
public class AseguradoraDAO implements AseguradoraRepositorio {

    @Override
    public Aseguradora guardar(Aseguradora a) throws SQLException {
        String sql = "INSERT INTO aseguradoras (codigo, nombre, nit, telefono_contacto, cubre_soat, "
                   + "cubre_responsabilidad_civil, activo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, a.getCodigo());
            stmt.setString(2, a.getNombre());
            stmt.setString(3, a.getNit());
            stmt.setString(4, a.getTelefonoContacto());
            stmt.setBoolean(5, a.isCubreSoat());
            stmt.setBoolean(6, a.isCubreResponsabilidadCivil());
            stmt.setBoolean(7, a.isActivo());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    a.setId(rs.getLong(1));
                }
            }
        }
        return a;
    }

    @Override
    public void actualizar(Aseguradora a) throws SQLException {
        String sql = "UPDATE aseguradoras SET codigo = ?, nombre = ?, nit = ?, telefono_contacto = ?, "
                   + "cubre_soat = ?, cubre_responsabilidad_civil = ?, activo = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.getCodigo());
            stmt.setString(2, a.getNombre());
            stmt.setString(3, a.getNit());
            stmt.setString(4, a.getTelefonoContacto());
            stmt.setBoolean(5, a.isCubreSoat());
            stmt.setBoolean(6, a.isCubreResponsabilidadCivil());
            stmt.setBoolean(7, a.isActivo());
            stmt.setLong(8, a.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM aseguradoras WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Aseguradora> listarTodas() throws SQLException {
        return listar("SELECT * FROM aseguradoras ORDER BY nombre");
    }

    @Override
    public List<Aseguradora> listarActivas() throws SQLException {
        return listar("SELECT * FROM aseguradoras WHERE activo = TRUE ORDER BY nombre");
    }

    @Override
    public Optional<Aseguradora> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM aseguradoras WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return buscarUno(stmt);
        }
    }

    @Override
    public Optional<Aseguradora> buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM aseguradoras WHERE codigo = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            return buscarUno(stmt);
        }
    }

    @Override
    public Optional<Aseguradora> buscarPorNit(String nit) throws SQLException {
        String sql = "SELECT * FROM aseguradoras WHERE nit = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nit);
            return buscarUno(stmt);
        }
    }

    private List<Aseguradora> listar(String sql) throws SQLException {
        List<Aseguradora> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        }
        return lista;
    }

    private Optional<Aseguradora> buscarUno(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapearResultSet(rs));
            }
        }
        return Optional.empty();
    }

    private Aseguradora mapearResultSet(ResultSet rs) throws SQLException {
        Aseguradora a = new Aseguradora();
        a.setId(rs.getLong("id"));
        a.setCodigo(rs.getString("codigo"));
        a.setNombre(rs.getString("nombre"));
        a.setNit(rs.getString("nit"));
        a.setTelefonoContacto(rs.getString("telefono_contacto"));
        a.setCubreSoat(rs.getBoolean("cubre_soat"));
        a.setCubreResponsabilidadCivil(rs.getBoolean("cubre_responsabilidad_civil"));
        a.setActivo(rs.getBoolean("activo"));
        return a;
    }
}
