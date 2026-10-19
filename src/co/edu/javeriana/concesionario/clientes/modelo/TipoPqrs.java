package co.edu.javeriana.concesionario.clientes.modelo;

public enum TipoPqrs {

    // (dias de SLA, puntaje base)
    PETICION(15, 100),
    QUEJA(10, 60),
    RECLAMO(10, 40),
    SUGERENCIA(15, 100);

    private final int diasSla;
    private final int puntajeBase;

    TipoPqrs(int diasSla, int puntajeBase) {
        this.diasSla = diasSla;
        this.puntajeBase = puntajeBase;
    }

    public int getDiasSla() {
        return diasSla;
    }

    public int getPuntajeBase() {
        return puntajeBase;
    }

    public static TipoPqrs desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("La PQRS no tiene tipo registrado.");
        }
        try {
            return TipoPqrs.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de PQRS no reconocido: " + texto);
        }
    }
}
