package co.edu.javeriana.concesionario.legal.modelo;

/** Situación del impuesto vehicular de un vehículo matriculado (CU-21). */
public enum EstadoImpuesto {
    AL_DIA("Al día"),
    PENDIENTE("Pendiente vigencia actual"),
    EN_MORA("En mora");

    private final String etiqueta;

    EstadoImpuesto(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
