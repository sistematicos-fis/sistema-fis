package co.edu.javeriana.concesionario.clientes.modelo;

public enum EstadoPqrs {

    ABIERTO("Abierta"),
    EN_PROCESO("En proceso"),
    CERRADO("Cerrada");

    private final String etiqueta;

    EstadoPqrs(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static EstadoPqrs desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("La PQRS no tiene estado registrado.");
        }
        try {
            return EstadoPqrs.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de PQRS no reconocido: " + texto);
        }
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
