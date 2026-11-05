package co.edu.javeriana.concesionario.inventario.modelo;

/**
 * Desenlaces posibles de una verificación de existencias (CU-10).
 * Solo el primero habilita la venta, los demás la bloquean por motivos distintos.
 *
 * Responsable: Juan Pablo León
 */
public enum ResultadoVerificacion {

    /** Físico y sistema coinciden, no hay operaciones activas: la unidad queda bloqueada para la venta. */
    DISPONIBLE_CONFIRMADO("Disponibilidad confirmada"),

    /** El vehículo tiene una reserva o un alquiler vigente (flujo alterno 3A). */
    OPERACION_ACTIVA("Vehículo con operación activa"),

    /** El estado registrado no permite vender (vendido, en mantenimiento, en verificación...). */
    ESTADO_NO_VENDIBLE("Estado del vehículo no apto para venta"),

    /** No hay ningún conteo físico contra el cual comparar (RN-10-02). */
    SIN_CONTEO_FISICO("Sin conteo físico registrado"),

    /** El físico no coincide con el sistema: se abre incidencia (flujo alterno 4A). */
    DISCREPANCIA_DETECTADA("Discrepancia entre inventario físico y sistema"),

    /** Ya existía una incidencia sin resolver sobre esta unidad (RN-10-06). */
    INCIDENCIA_PENDIENTE("Incidencia de inventario pendiente de resolución");

    private final String descripcion;

    ResultadoVerificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esFavorable() {
        return this == DISPONIBLE_CONFIRMADO;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
