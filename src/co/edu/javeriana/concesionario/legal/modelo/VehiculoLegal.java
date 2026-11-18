package co.edu.javeriana.concesionario.legal.modelo;

/**
 * Datos del vehículo del inventario que usan los casos de uso de matrículas (CU-21),
 * pólizas (CU-23) y SOAT (CU-24). El módulo legal solo lee estas columnas de la tabla vehiculos.
 *
 * @param estado estado del vehículo en el inventario (DISPONIBLE, ALQUILADO, VENDIDO, ...)
 */
public record VehiculoLegal(Long id, String vin, String placa, String tipo, String marca, String linea,
                            int modeloAnio, String estado) {

    public boolean esMotocicleta() {
        return "MOTOCICLETA".equals(tipo);
    }

    public boolean estaAlquilado() {
        return "ALQUILADO".equals(estado);
    }

    /** Un vehículo vendido deja de pertenecer a la flota del concesionario. */
    public boolean perteneceAFlota() {
        return !"VENDIDO".equals(estado);
    }

    public boolean tienePlaca() {
        return placa != null && !placa.isBlank();
    }

    @Override
    public String toString() {
        String identificador = tienePlaca() ? placa : "VIN " + vin;
        return identificador + " - " + marca + " " + linea + " " + modeloAnio;
    }
}
