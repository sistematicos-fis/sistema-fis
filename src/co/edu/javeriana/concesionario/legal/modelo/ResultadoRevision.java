package co.edu.javeriana.concesionario.legal.modelo;

/** Resultado de una revisión técnico-mecánica realizada en un centro de diagnóstico automotor (CDA). */
public enum ResultadoRevision {
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada");

    private final String etiqueta;

    ResultadoRevision(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
