package co.edu.javeriana.concesionario.legal.modelo;

import java.math.BigDecimal;

/**
 * Resultado de validar y renovar el SOAT de un vehículo (CU-24), listo para notificar al analista (paso 8).
 *
 * @param soatNuevo        SOAT emitido; solo existe si el resultado es RENOVADO
 * @param valorCalculado   valor del SOAT según la tarifa (paso 5); null si el proceso terminó antes
 * @param estadoDocumental estado documental del vehículo al terminar; null si el proceso no lo cambió
 */
public record ResultadoRenovacionSoat(ResultadoRenovacion resultado, String mensaje, Soat soatNuevo,
                                      BigDecimal valorCalculado, EstadoDocumental estadoDocumental) {

    public boolean fueRenovado() {
        return resultado == ResultadoRenovacion.RENOVADO;
    }
}
