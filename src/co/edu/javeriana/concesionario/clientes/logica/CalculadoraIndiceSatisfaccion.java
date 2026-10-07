package co.edu.javeriana.concesionario.clientes.logica;

import co.edu.javeriana.concesionario.clientes.modelo.CategoriaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.EncuestaSatisfaccion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Lógica no trivial para el cálculo y categorización del Índice de Satisfacción (CU-05 - No CRUD).
 * Calcula el promedio ponderado multidimensional (Atención 40%, Producto 35%, Tiempos 25%)
 * y asigna el nivel de satisfacción global del concesionario.
 * 
 * Responsable: Andrés Alquerque
 */
public class CalculadoraIndiceSatisfaccion {

    private static final BigDecimal PESO_ATENCION = new BigDecimal("0.40");
    private static final BigDecimal PESO_PRODUCTO = new BigDecimal("0.35");
    private static final BigDecimal PESO_TIEMPOS = new BigDecimal("0.25");

    public static BigDecimal calcularIndiceIndividual(EncuestaSatisfaccion enc) {
        if (enc == null) return BigDecimal.ZERO;

        BigDecimal ptAtencion = new BigDecimal(enc.getPuntuacionAtencion()).multiply(PESO_ATENCION);
        BigDecimal ptProducto = new BigDecimal(enc.getPuntuacionProducto()).multiply(PESO_PRODUCTO);
        BigDecimal ptTiempos = new BigDecimal(enc.getPuntuacionTiempos()).multiply(PESO_TIEMPOS);

        return ptAtencion.add(ptProducto).add(ptTiempos).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcularIndiceGlobal(List<EncuestaSatisfaccion> encuestas) {
        if (encuestas == null || encuestas.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal suma = BigDecimal.ZERO;
        for (EncuestaSatisfaccion e : encuestas) {
            suma = suma.add(calcularIndiceIndividual(e));
        }

        return suma.divide(new BigDecimal(encuestas.size()), 2, RoundingMode.HALF_UP);
    }

    public static CategoriaSatisfaccion clasificarNivel(BigDecimal puntaje) {
        if (puntaje == null) return CategoriaSatisfaccion.NEUTRO;
        double valor = puntaje.doubleValue();

        if (valor < 2.0) return CategoriaSatisfaccion.MUY_INSATISFECHO;
        if (valor < 3.0) return CategoriaSatisfaccion.INSATISFECHO;
        if (valor < 4.0) return CategoriaSatisfaccion.NEUTRO;
        if (valor < 4.6) return CategoriaSatisfaccion.SATISFECHO;
        return CategoriaSatisfaccion.MUY_SATISFECHO;
    }
}
