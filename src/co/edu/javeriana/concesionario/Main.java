package co.edu.javeriana.concesionario;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.comun.seguridad.logica.AutenticacionServicio;
import co.edu.javeriana.concesionario.comun.seguridad.persistencia.UsuarioDAO;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.VentanaLogin;
import co.edu.javeriana.concesionario.comun.vista.VentanaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Iniciando Sistema Concesionario Javeriana...");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo establecer Look and Feel del sistema", e);
        }

        try {
            ConexionBD.inicializarBaseDatos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fallo al inicializar base de datos H2", e);
            SwingUtilities.invokeLater(() -> {
                MensajeUtil.mostrarError(null, "No se pudo iniciar el sistema: falló la creación de la base de datos H2. "
                        + "Verifique que la carpeta db/ esté junto a la aplicación y vuelva a abrirla.", "Error de Base de Datos");
                System.exit(1);
            });
            return;
        }

        SwingUtilities.invokeLater(Main::iniciarSesion);
    }

    private static void iniciarSesion() {
        AutenticacionServicio autenticacion = new AutenticacionServicio(new UsuarioDAO());
        new VentanaLogin(autenticacion).solicitarCredenciales().ifPresentOrElse(usuario -> {
            new VentanaPrincipal(usuario, Main::iniciarSesion).setVisible(true);
            LOGGER.info("Sesión iniciada por " + usuario.getUsername());
        }, () -> System.exit(0));
    }
}
