package co.edu.javeriana.concesionario.inventario.logica;

import co.edu.javeriana.concesionario.comun.util.ValidadorUtil;
import co.edu.javeriana.concesionario.inventario.modelo.CategoriaVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.Vehiculo;
import co.edu.javeriana.concesionario.inventario.persistencia.VehiculoDAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;

/**
 * Servicio de lógica de negocio para Inventario (CU-06 a CU-10).
 * Incluye valoración de stock y cálculo de depreciación anual comercial (CU-10 - No CRUD).
 * 
 * Responsable: Juan Pablo León
 */
public class InventarioServicio {

    private final VehiculoDAO vehiculoDAO;

    public InventarioServicio() {
        this.vehiculoDAO = new VehiculoDAO();
    }

    public InventarioServicio(VehiculoDAO vehiculoDAO) {
        this.vehiculoDAO = vehiculoDAO;
    }

    public Vehiculo registrarVehiculo(String vin, String placa, CategoriaVehiculo tipo, String marca,
                                      int modeloAnio, String linea, String color, BigDecimal precioVenta) throws SQLException {
        ValidadorUtil.validarRequerido(vin, "VIN");
        ValidadorUtil.validarRequerido(marca, "Marca");
        ValidadorUtil.validarRequerido(linea, "Línea");
        ValidadorUtil.validarMontoPositivo(precioVenta, "Precio de Venta");

        Vehiculo v = new Vehiculo(null, vin, placa, tipo, marca, modeloAnio, linea, color, precioVenta, "DISPONIBLE", 0, 1L);
        return vehiculoDAO.guardar(v);
    }

    public List<Vehiculo> listarDisponibles() throws SQLException {
        return vehiculoDAO.listarDisponibles();
    }

    /**
     * Calcula la depreciación estimada del vehículo según años de antigüedad (CU-10 - No CRUD).
     * Regla: 15% el primer año, 10% por año subsecuente, con valor de rescate mínimo del 30%.
     */
    public BigDecimal calcularValorDepreciado(Vehiculo v) {
        if (v == null || v.getPrecioVenta() == null) return BigDecimal.ZERO;

        int anioActual = Year.now().getValue();
        int antiguedad = Math.max(0, anioActual - v.getModeloAnio());

        if (antiguedad == 0) {
            return v.getPrecioVenta();
        }

        double tasaDepreciacion = 0.15 + (antiguedad - 1) * 0.10;
        tasaDepreciacion = Math.min(tasaDepreciacion, 0.70); // Máximo 70% de depreciación

        BigDecimal factor = BigDecimal.valueOf(1.0 - tasaDepreciacion);
        return v.getPrecioVenta().multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
