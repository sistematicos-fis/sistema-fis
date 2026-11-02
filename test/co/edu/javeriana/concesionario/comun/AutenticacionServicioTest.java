package co.edu.javeriana.concesionario.comun;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.comun.seguridad.logica.AutenticacionException;
import co.edu.javeriana.concesionario.comun.seguridad.logica.AutenticacionServicio;
import co.edu.javeriana.concesionario.comun.seguridad.modelo.Modulo;
import co.edu.javeriana.concesionario.comun.seguridad.modelo.Rol;
import co.edu.javeriana.concesionario.comun.seguridad.modelo.Usuario;
import co.edu.javeriana.concesionario.comun.seguridad.persistencia.UsuarioDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AutenticacionServicioTest {
    private static AutenticacionServicio servicio;

    @BeforeAll
    public static void setUp() {
        ConexionBD.configurarUrlMemoria();
        ConexionBD.inicializarBaseDatos();
        servicio = new AutenticacionServicio(new UsuarioDAO());
    }

    @AfterAll
    public static void tearDown() {
        ConexionBD.cerrarConexion();
    }

    @Test
    public void testIniciarSesionConCredencialesValidas() throws Exception {
        Usuario usuario = servicio.iniciarSesion("analista", "credito123".toCharArray());
        assertEquals(Rol.ANALISTA_CREDITO, usuario.getRol());
    }

    @Test
    public void testContrasenaIncorrectaEsRechazada() {
        assertThrows(AutenticacionException.class,
                () -> servicio.iniciarSesion("admin", "otra".toCharArray()));
    }

    @Test
    public void testUsuarioInexistenteEsRechazado() {
        assertThrows(AutenticacionException.class,
                () -> servicio.iniciarSesion("nadie", "admin123".toCharArray()));
    }

    @Test
    public void testCamposVaciosSonRechazados() {
        assertThrows(AutenticacionException.class,
                () -> servicio.iniciarSesion(" ", new char[0]));
    }

    @Test
    public void testUsuarioInactivoEsRechazado() {
        Usuario inactivo = new Usuario(99L, "inactivo",
                "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9", "Usuario Inactivo", Rol.ADMINISTRADOR, false);
        AutenticacionServicio conRepositorioFijo = new AutenticacionServicio(username -> Optional.of(inactivo));
        assertThrows(AutenticacionException.class,
                () -> conRepositorioFijo.iniciarSesion("inactivo", "admin123".toCharArray()));
    }

    @Test
    public void testPermisosPorRol() {
        assertTrue(Rol.ADMINISTRADOR.puedeAcceder(Modulo.LEGAL));
        assertTrue(Rol.ANALISTA_CREDITO.puedeAcceder(Modulo.FINANCIACION));
        assertFalse(Rol.ANALISTA_CREDITO.puedeAcceder(Modulo.INVENTARIO));
        assertTrue(Rol.ADMINISTRADOR_BODEGA.puedeAcceder(Modulo.INVENTARIO));
        assertFalse(Rol.ANALISTA_SERVICIO_CLIENTE.puedeAcceder(Modulo.FINANCIACION));
    }
}
