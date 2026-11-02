package co.edu.javeriana.concesionario.comun.seguridad.modelo;

public enum Modulo {
    CLIENTES("Clientes y PQRS"),
    INVENTARIO("Inventario y Bodega"),
    FINANCIACION("Financiación"),
    ALQUILER("Alquiler"),
    LEGAL("Documentación Legal y Seguros");

    private final String titulo;

    Modulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }
}
