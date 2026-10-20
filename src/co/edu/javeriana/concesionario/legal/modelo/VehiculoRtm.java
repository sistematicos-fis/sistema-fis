package co.edu.javeriana.concesionario.legal.modelo;

/**
 * Datos mínimos de un vehículo que el módulo legal necesita para evaluar su revisión técnico-mecánica.
 * El año de matrícula se aproxima con el año del modelo, porque el inventario no registra la fecha de matrícula.
 */
public record VehiculoRtm(Long id, String vin, String placa, String tipo, String marca, String linea, int modeloAnio) {

    public boolean esMotocicleta() {
        return "MOTOCICLETA".equals(tipo);
    }

    @Override
    public String toString() {
        String identificador = placa != null && !placa.isBlank() ? placa : vin;
        return identificador + " - " + marca + " " + linea + " " + modeloAnio;
    }
}
