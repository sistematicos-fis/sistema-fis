package co.edu.javeriana.concesionario.legal.modelo;

import java.time.LocalDate;

/**
 * Resultado de verificar la revisión técnico-mecánica de un vehículo antes de una venta o un alquiler.
 *
 * @param estado           situación del vehículo frente a la revisión
 * @param habilitado       true si el vehículo puede continuar con la operación
 * @param motivo           explicación lista para mostrar al usuario
 * @param fechaVencimiento vencimiento de la última revisión aprobada (null si no hay)
 * @param diasRestantes    días entre hoy y el vencimiento (null si no hay revisión vigente)
 */
public record ResultadoVerificacionRtm(EstadoRtm estado, boolean habilitado, String motivo,
                                       LocalDate fechaVencimiento, Long diasRestantes) {
}
