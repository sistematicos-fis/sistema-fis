package co.edu.javeriana.concesionario.legal.modelo;

/**
 * Pólizas voluntarias que se asocian a la flota de alquiler (CU-23). El SOAT, que es obligatorio,
 * se maneja aparte en el CU-24. Los nombres coinciden con los valores de polizas_seguro.tipo_poliza.
 */
public enum TipoPoliza {
    TODO_RIESGO_RESPONSABILIDAD("Todo riesgo (multirriesgo)"),
    EXTRACONTRACTUAL("Responsabilidad civil extracontractual");

    private final String etiqueta;

    TipoPoliza(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
