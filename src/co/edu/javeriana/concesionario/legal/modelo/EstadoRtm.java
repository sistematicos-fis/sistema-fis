package co.edu.javeriana.concesionario.legal.modelo;

/** Situación de un vehículo frente a la revisión técnico-mecánica en un momento dado. */
public enum EstadoRtm {
    VIGENTE("Vigente"),
    POR_VENCER("Vigente, por vencer"),
    VENCE_DURANTE_ALQUILER("Vence durante el alquiler"),
    VENCIDA("Vencida"),
    RECHAZADA("Rechazada"),
    SIN_REVISION("Sin revisión registrada"),
    EXENTA_PRIMERA_REVISION("Aún no le corresponde");

    private final String etiqueta;

    EstadoRtm(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
