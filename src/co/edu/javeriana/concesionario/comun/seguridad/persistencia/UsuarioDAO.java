package co.edu.javeriana.concesionario.comun.seguridad.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.comun.seguridad.modelo.Rol;
import co.edu.javeriana.concesionario.comun.seguridad.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UsuarioDAO implements UsuarioRepositorio {
    @Override
    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE username = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Usuario mapearResultSet(ResultSet rs) throws SQLException {
        Rol rol;
        try {
            rol = Rol.valueOf(rs.getString("rol"));
        } catch (IllegalArgumentException e) {
            rol = null;
        }
        return new Usuario(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("nombre_completo"),
                rol,
                rs.getBoolean("activo"));
    }
}
