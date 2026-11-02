package co.edu.javeriana.concesionario.comun.seguridad.persistencia;

import co.edu.javeriana.concesionario.comun.seguridad.modelo.Usuario;

import java.sql.SQLException;
import java.util.Optional;

public interface UsuarioRepositorio {
    Optional<Usuario> buscarPorUsername(String username) throws SQLException;
}
