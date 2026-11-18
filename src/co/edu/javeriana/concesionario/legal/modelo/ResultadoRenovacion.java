package co.edu.javeriana.concesionario.legal.modelo;

/** Cómo terminó el proceso de validar y renovar el SOAT (CU-24). */
public enum ResultadoRenovacion {
    /** Flujo básico completo: la aseguradora emitió el nuevo SOAT. */
    RENOVADO,
    /** Flujo 3A: el SOAT actual no está dentro del rango de alerta de renovación. */
    NO_REQUIERE_RENOVACION,
    /** Flujo 4A: siniestros pendientes o bloqueos administrativos. */
    NO_CUMPLE_REQUISITOS,
    /** Flujo 6A: la aseguradora rechazó la emisión. */
    RECHAZADO_POR_ASEGURADORA
}
