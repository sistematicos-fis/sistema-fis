package co.edu.javeriana.concesionario.comun.util;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Utilidades para diálogos estándar Swing (mensajes y confirmaciones).
 */
public final class MensajeUtil {

    private MensajeUtil() {
    }

    public static void mostrarInfo(Component parent, String mensaje, String titulo) {
        JOptionPane.showMessageDialog(parent, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void mostrarAdvertencia(Component parent, String mensaje, String titulo) {
        JOptionPane.showMessageDialog(parent, mensaje, titulo, JOptionPane.WARNING_MESSAGE);
    }

    public static void mostrarError(Component parent, String mensaje, String titulo) {
        JOptionPane.showMessageDialog(parent, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component parent, String mensaje, String titulo) {
        int resp = JOptionPane.showConfirmDialog(parent, mensaje, titulo, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return resp == JOptionPane.YES_OPTION;
    }
}
