package co.edu.javeriana.concesionario;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.comun.vista.VentanaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Punto de entrada principal del Sistema Concesionario Integrado.
 * Pontificia Universidad Javeriana - FIS.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Iniciando Sistema Concesionario Javeriana...");

        // Configuración de Look and Feel del sistema operativo para Swing
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo establecer Look and Feel del sistema", e);
        }

        // Inicialización de la base de datos relacional H2 embebida
        try {
            ConexionBD.inicializarBaseDatos();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fallo al inicializar base de datos H2", e);
            System.err.println("ERROR CRÍTICO: No fue posible conectar con H2. Verifique la configuración.");
        }

        // Lanzamiento de la interfaz gráfica Swing en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
            LOGGER.info("Aplicación iniciada exitosamente.");
        });
    }
}
