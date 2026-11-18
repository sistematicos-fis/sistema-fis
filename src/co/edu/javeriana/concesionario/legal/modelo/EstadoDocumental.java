package co.edu.javeriana.concesionario.legal.modelo;

/** Estado documental de un vehículo frente al SOAT (CU-24, paso 7 y flujo 6A). */
public enum EstadoDocumental {
    AL_DIA("Documentación al día"),
    PENDIENTE("Documentación pendiente");

    private final String etiqueta;

    EstadoDocumental(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
