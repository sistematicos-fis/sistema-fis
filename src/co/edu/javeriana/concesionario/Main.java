package co.edu.javeriana.concesionario;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.comun.seguridad.logica.AutenticacionServicio;
import co.edu.javeriana.concesionario.comun.seguridad.persistencia.UsuarioDAO;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.VentanaLogin;
import co.edu.javeriana.concesionario.comun.vista.VentanaPrincipal;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Iniciando Sistema Concesionario Javeriana...");
        Application.launch(Aplicacion.class, args);
    }

    // Main no extiende Application para que el programa arranque también desde el classpath (IDE o java -cp).
    public static class Aplicacion extends Application {

        @Override
        public void start(Stage escenarioInicial) {
            Platform.setImplicitExit(false);
            try {
                ConexionBD.inicializarBaseDatos();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Fallo al inicializar base de datos H2", e);
                MensajeUtil.mostrarError(null, "No se pudo iniciar el sistema: falló la creación de la base de datos H2. "
                        + "Verifique que la carpeta db/ esté junto a la aplicación y vuelva a abrirla.", "Error de Base de Datos");
                System.exit(1);
                return;
            }
            iniciarSesion();
        }
    }

    private static void iniciarSesion() {
        AutenticacionServicio autenticacion = new AutenticacionServicio(new UsuarioDAO());
        new VentanaLogin(autenticacion).solicitarCredenciales().ifPresentOrElse(usuario -> {
            new VentanaPrincipal(usuario, Main::iniciarSesion).show();
            LOGGER.info("Sesión iniciada por " + usuario.getUsername());
        }, () -> {
            Platform.exit();
            System.exit(0);
        });
    }
}
