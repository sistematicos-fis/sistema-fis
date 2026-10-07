package co.edu.javeriana.concesionario.mantenimiento;

import co.edu.javeriana.concesionario.mantenimiento.logica.MantenimientoServicio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class MantenimientoServicioTest {

    private final MantenimientoServicio servicio = new MantenimientoServicio(null);

    @Test
    public void testCalcularCostoReparacionConIva() {
        // Horas: 2 * 85.000 = 170.000
        // Repuestos: 300.000
        // Subtotal = 470.000
        // IVA 19% = 89.300
        // Total = 559.300
        BigDecimal total = servicio.calcularCostoReparacion(2, new BigDecimal("300000.00"));
        assertEquals(new BigDecimal("559300.00"), total);
    }
}
