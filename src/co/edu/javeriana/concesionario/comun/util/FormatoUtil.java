package co.edu.javeriana.concesionario.comun.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilidades de formateo de moneda, porcentajes y fechas.
 */
public final class FormatoUtil {

    private static final Locale LOCALE_COLOMBIA = new Locale("es", "CO");
    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getCurrencyInstance(LOCALE_COLOMBIA);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FormatoUtil() {
    }

    public static String formatoMoneda(BigDecimal valor) {
        if (valor == null) return "$ 0";
        return FORMATO_MONEDA.format(valor);
    }

    public static String formatoPorcentaje(BigDecimal tasaEa) {
        if (tasaEa == null) return "0.00%";
        return tasaEa.multiply(new BigDecimal(100)).setScale(2, java.math.RoundingMode.HALF_UP) + "%";
    }

    public static String formatoFecha(LocalDate fecha) {
        if (fecha == null) return "-";
        return fecha.format(FORMATO_FECHA);
    }
}
