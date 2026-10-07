package co.edu.javeriana.concesionario.rrhh;

import co.edu.javeriana.concesionario.rrhh.logica.RRHHServicio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class RRHHServicioTest {

    private final RRHHServicio servicio = new RRHHServicio(null);

    @Test
    public void testLiquidarComisionEscalonadaTramo1() {
        // Ventas: 80.000.000 -> 1.0% = 800.000
        BigDecimal comision = servicio.liquidarComisionMensual(new BigDecimal("80000000.00"));
        assertEquals(new BigDecimal("800000.00"), comision);
    }

    @Test
    public void testLiquidarComisionEscalonadaTramo3ConBono() {
        // Ventas: 300.000.000 -> 2.2% = 6.600.000 + 1.000.000 bono = 7.600.000
        BigDecimal comision = servicio.liquidarComisionMensual(new BigDecimal("300000000.00"));
        assertEquals(new BigDecimal("7600000.00"), comision);
    }
}
