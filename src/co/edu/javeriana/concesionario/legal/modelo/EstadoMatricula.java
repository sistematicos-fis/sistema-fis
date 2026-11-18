package co.edu.javeriana.concesionario.legal.modelo;

/** Estado del registro oficial (matrícula) de un vehículo ante el organismo de tránsito (CU-21). */
public enum EstadoMatricula {
    EN_TRAMITE("En trámite"),
    ACTIVA("Activa"),
    SUSPENDIDA("Suspendida"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoMatricula(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Un vehículo con la matrícula suspendida o cancelada tiene un bloqueo administrativo. */
    public boolean tieneBloqueoAdministrativo() {
        return this == SUSPENDIDA || this == CANCELADA;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
