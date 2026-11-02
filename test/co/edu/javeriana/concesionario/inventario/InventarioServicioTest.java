package co.edu.javeriana.concesionario.inventario;

import co.edu.javeriana.concesionario.inventario.logica.InventarioServicio;
import co.edu.javeriana.concesionario.inventario.modelo.CategoriaVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.Vehiculo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

public class InventarioServicioTest {

    private final InventarioServicio servicio = new InventarioServicio(null);

    @Test
    public void testVehiculoNuevoSinDepreciacion() {
        int anioActual = Year.now().getValue();
        Vehiculo v = new Vehiculo(1L, "VIN123", "ABC123", CategoriaVehiculo.AUTOMOVIL, "Toyota",
                anioActual, "Yaris", "Gris", new BigDecimal("80000000.00"), "DISPONIBLE", 0, 1L);

        BigDecimal depreciado = servicio.calcularValorDepreciado(v);
        assertEquals(new BigDecimal("80000000.00"), depreciado);
    }

    @Test
    public void testVehiculoUnAnioDepreciacion15Porciento() {
        int anioAnterior = Year.now().getValue() - 1;
        Vehiculo v = new Vehiculo(2L, "VIN456", "XYZ789", CategoriaVehiculo.CAMIONETA, "Mazda",
                anioAnterior, "CX-5", "Rojo", new BigDecimal("100000000.00"), "DISPONIBLE", 15000, 1L);

        BigDecimal depreciado = servicio.calcularValorDepreciado(v);
        assertEquals(new BigDecimal("85000000.00"), depreciado);
    }
}
