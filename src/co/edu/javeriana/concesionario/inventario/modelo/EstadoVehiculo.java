package co.edu.javeriana.concesionario.inventario.modelo;

/**
 * Estados del ciclo de vida de una unidad del catálogo (CU-06).
 *
 * La columna vehiculos.estado se sigue guardando como texto por compatibilidad con
 * el código ya existente; esta enumeración centraliza el vocabulario para que el
 * CU-10 no tenga que comparar cadenas sueltas.
 *
 * Responsable: Juan Pablo León
 */
public enum EstadoVehiculo {

    DISPONIBLE,
    RESERVADO,
    VENDIDO,
    ALQUILADO,
    MANTENIMIENTO,
    /** Marcado por el CU-10 cuando el conteo físico no coincide con el sistema (flujo 4A). */
    EN_VERIFICACION;

    /** Devuelve null si el texto no corresponde a ningún estado conocido. */
    public static EstadoVehiculo desdeTexto(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /** Único estado desde el cual una unidad puede pasar a venta (RN-10-04). */
    public boolean esVendible() {
        return this == DISPONIBLE;
    }
}
