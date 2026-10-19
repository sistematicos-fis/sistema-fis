package co.edu.javeriana.concesionario.comun.util;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/**
 * Diálogos estándar de la aplicación (información, advertencia, error y confirmación).
 */
public final class MensajeUtil {

    private MensajeUtil() {
    }

    public static void mostrarInfo(Node origen, String mensaje, String titulo) {
        crear(Alert.AlertType.INFORMATION, origen, mensaje, titulo).showAndWait();
    }

    public static void mostrarAdvertencia(Node origen, String mensaje, String titulo) {
        crear(Alert.AlertType.WARNING, origen, mensaje, titulo).showAndWait();
    }

    public static void mostrarError(Node origen, String mensaje, String titulo) {
        crear(Alert.AlertType.ERROR, origen, mensaje, titulo).showAndWait();
    }

    public static boolean confirmar(Node origen, String mensaje, String titulo) {
        Alert alerta = crear(Alert.AlertType.CONFIRMATION, origen, mensaje, titulo);
        alerta.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return alerta.showAndWait().filter(ButtonType.YES::equals).isPresent();
    }

    private static Alert crear(Alert.AlertType tipo, Node origen, String mensaje, String titulo) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        Window ventana = origen != null && origen.getScene() != null ? origen.getScene().getWindow() : null;
        if (ventana != null) {
            alerta.initOwner(ventana);
        }
        return alerta;
    }
}
