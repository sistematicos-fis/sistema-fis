package co.edu.javeriana.concesionario.legal.modelo;

/** Operación para la que se quiere habilitar un vehículo. */
public enum OperacionComercial {
    VENTA("Venta"),
    ALQUILER("Alquiler");

    private final String etiqueta;

    OperacionComercial(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
