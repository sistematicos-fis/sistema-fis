package co.edu.javeriana.concesionario.inventario.modelo;

import java.math.BigDecimal;

/**
 * Entidad que representa un vehículo o motocicleta (CU-06).
 * Responsable: Juan Pablo León
 */
public class Vehiculo {
    private Long id;
    private String vin;
    private String placa;
    private CategoriaVehiculo tipo;
    private String marca;
    private int modeloAnio;
    private String linea;
    private String color;
    private BigDecimal precioVenta;
    private String estado; // DISPONIBLE, RESERVADO, VENDIDO, ALQUILADO, MANTENIMIENTO
    private int kilometraje;
    private Long bodegaId;

    public Vehiculo() {
        this.estado = "DISPONIBLE";
        this.kilometraje = 0;
    }

    public Vehiculo(Long id, String vin, String placa, CategoriaVehiculo tipo, String marca,
                    int modeloAnio, String linea, String color, BigDecimal precioVenta,
                    String estado, int kilometraje, Long bodegaId) {
        this.id = id;
        this.vin = vin;
        this.placa = placa;
        this.tipo = tipo;
        this.marca = marca;
        this.modeloAnio = modeloAnio;
        this.linea = linea;
        this.color = color;
        this.precioVenta = precioVenta;
        this.estado = estado;
        this.kilometraje = kilometraje;
        this.bodegaId = bodegaId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public CategoriaVehiculo getTipo() { return tipo; }
    public void setTipo(CategoriaVehiculo tipo) { this.tipo = tipo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public int getModeloAnio() { return modeloAnio; }
    public void setModeloAnio(int modeloAnio) { this.modeloAnio = modeloAnio; }

    public String getLinea() { return linea; }
    public void setLinea(String linea) { this.linea = linea; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getKilometraje() { return kilometraje; }
    public void setKilometraje(int kilometraje) { this.kilometraje = kilometraje; }

    public Long getBodegaId() { return bodegaId; }
    public void setBodegaId(Long bodegaId) { this.bodegaId = bodegaId; }

    @Override
    public String toString() {
        return marca + " " + linea + " (" + modeloAnio + ") - " + (placa != null ? placa : vin);
    }
}
