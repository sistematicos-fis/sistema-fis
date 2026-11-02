package co.edu.javeriana.concesionario.clientes.modelo;

public enum BaseCalculoIndice {

    COMBINADO("Encuestas + PQRS"),
    SOLO_ENCUESTAS("Solo encuestas"),
    SOLO_PQRS("Solo PQRS");

    private final String etiqueta;

    BaseCalculoIndice(String etiqueta) {
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
