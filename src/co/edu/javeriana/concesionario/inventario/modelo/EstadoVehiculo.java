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
    EN_VERIFICACION,
    /** Retirado del catálogo (CU-06): conserva su historial, pero ya no se vende ni se alquila. */
    DADO_DE_BAJA;

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

    /**
     * Estados que el CU-06 puede asignar a mano (RN-06-04). RESERVADO y EN_VERIFICACION los
     * maneja el CU-10, ALQUILADO el módulo de alquiler y VENDIDO la venta.
     */
    public boolean esAsignableManualmente() {
        return this == DISPONIBLE || this == MANTENIMIENTO || this == DADO_DE_BAJA;
    }

    /** Estados en los que el catálogo permite editar o retirar la unidad (RN-06-03). */
    public boolean esEditableEnCatalogo() {
        return this == DISPONIBLE || this == MANTENIMIENTO;
    }

    /** Único estado desde el cual una unidad puede pasar a venta (RN-10-04). */
    public boolean esVendible() {
        return this == DISPONIBLE;
    }
}
