package co.edu.javeriana.concesionario.legal.modelo;

import java.time.LocalDateTime;

/**
 * Último estado documental guardado para un vehículo (tabla documentacion_vehiculos).
 *
 * @param observacion motivo del último cambio, por ejemplo el motivo de rechazo de la aseguradora
 */
public record RegistroDocumental(Long vehiculoId, EstadoDocumental estado, String observacion,
                                 LocalDateTime fechaActualizacion) {
}
