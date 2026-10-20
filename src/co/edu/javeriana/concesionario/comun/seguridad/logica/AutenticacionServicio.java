package co.edu.javeriana.concesionario.comun.seguridad.logica;

import co.edu.javeriana.concesionario.comun.seguridad.modelo.Usuario;
import co.edu.javeriana.concesionario.comun.seguridad.persistencia.UsuarioRepositorio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.Optional;

public class AutenticacionServicio {
    private static final String CREDENCIALES_INVALIDAS =
            "No se pudo iniciar sesión: el usuario o la contraseña no son correctos. Verifique los datos e intente de nuevo.";

    private final UsuarioRepositorio usuarioRepositorio;

    public AutenticacionServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Usuario iniciarSesion(String username, char[] password) throws SQLException {
        if (username == null || username.trim().isEmpty() || password == null || password.length == 0) {
            throw new AutenticacionException("No se pudo iniciar sesión: ingrese el usuario y la contraseña.");
        }
        Optional<Usuario> encontrado = usuarioRepositorio.buscarPorUsername(username.trim());
        if (encontrado.isEmpty() || !encontrado.get().getPasswordHash().equalsIgnoreCase(calcularHash(password))) {
            throw new AutenticacionException(CREDENCIALES_INVALIDAS);
        }
        Usuario usuario = encontrado.get();
        if (!usuario.isActivo()) {
            throw new AutenticacionException("No se pudo iniciar sesión: el usuario está inactivo. Comuníquese con el administrador.");
        }
        if (usuario.getRol() == null) {
            throw new AutenticacionException("No se pudo iniciar sesión: el usuario no tiene un rol válido. Comuníquese con el administrador.");
        }
        return usuario;
    }

    static String calcularHash(char[] password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(new String(password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible en esta JVM", e);
        }
    }
}
