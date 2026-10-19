package co.edu.javeriana.concesionario.clientes.modelo;

public enum CategoriaSatisfaccion {

    SATISFECHO("Satisfecho"),
    NEUTRAL("Neutral"),
    EN_RIESGO("En riesgo");

    private final String etiqueta;

    CategoriaSatisfaccion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
