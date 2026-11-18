package co.edu.javeriana.concesionario.legal.modelo;

/**
 * Estado de una póliza (CU-23). En la base solo se guarda VIGENTE o CANCELADA;
 * VENCIDA se deduce cuando la fecha de vencimiento ya pasó.
 */
public enum EstadoPoliza {
    VIGENTE("Vigente"),
    VENCIDA("Vencida"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoPoliza(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
