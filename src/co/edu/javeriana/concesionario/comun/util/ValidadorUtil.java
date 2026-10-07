package co.edu.javeriana.concesionario.comun.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Utilidades de validación transversal para todos los módulos.
 */
public final class ValidadorUtil {

    private static final Pattern PATTERN_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private ValidadorUtil() {
    }

    public static boolean esTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    public static boolean esEmailValido(String email) {
        if (!esTextoValido(email)) return false;
        return PATTERN_EMAIL.matcher(email.trim()).matches();
    }

    public static boolean esMontoPositivo(BigDecimal monto) {
        return monto != null && monto.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean esMontoNoNegativo(BigDecimal monto) {
        return monto != null && monto.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static void validarRequerido(String texto, String nombreCampo) {
        if (!esTextoValido(texto)) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' es obligatorio.");
        }
    }

    public static void validarMontoPositivo(BigDecimal monto, String nombreCampo) {
        if (!esMontoPositivo(monto)) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' debe ser un valor mayor a cero.");
        }
    }
}
