package co.edu.javeriana.concesionario.legal.vista;

import co.edu.javeriana.concesionario.legal.logica.ReglaNegocioException;

import java.math.BigDecimal;
import javafx.scene.control.TextField;

/**
 * Lectura de campos numéricos de los formularios del módulo legal. Un campo vacío se lee como null
 * (el servicio decide si es obligatorio); un texto que no es número se informa al usuario.
 */
final class CamposFX {

    private CamposFX() {
    }

    static Integer leerEntero(TextField campo, String nombreCampo) {
        String texto = limpiar(campo.getText());
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("El campo " + nombreCampo + " debe ser un número entero, sin puntos ni letras.");
        }
    }

    /** Acepta "1500000", "1.500.000" o "1500000,50" (coma decimal, como en Colombia). */
    static BigDecimal leerMonto(TextField campo, String nombreCampo) {
        String texto = limpiar(campo.getText()).replace("$", "");
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(texto.replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("El campo " + nombreCampo + " debe ser un valor numérico, por ejemplo 1500000.");
        }
    }

    /** Texto para cargar un monto en el formulario, en el mismo formato que lee {@link #leerMonto}. */
    static String mostrarMonto(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        BigDecimal sinCeros = valor.stripTrailingZeros();
        if (sinCeros.scale() < 0) {
            sinCeros = sinCeros.setScale(0);
        }
        return sinCeros.toPlainString().replace('.', ',');
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.trim().replace(" ", "");
    }
}
