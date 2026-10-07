package co.edu.javeriana.concesionario.financiacion;

import co.edu.javeriana.concesionario.financiacion.logica.CalculadoraAmortizacion;
import co.edu.javeriana.concesionario.financiacion.modelo.CuotaAmortizacion;
import co.edu.javeriana.concesionario.financiacion.modelo.TablaAmortizacion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CalculadoraAmortizacionTest {

    @Test
    public void testConvertirEaAMensualVencida() {
        BigDecimal tasaEa = new BigDecimal("0.1650"); // 16.50% EA
        BigDecimal mensual = CalculadoraAmortizacion.convertirEaAMensualVencida(tasaEa);
        assertNotNull(mensual);
        assertTrue(mensual.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(mensual.compareTo(new BigDecimal("0.02")) < 0);
    }

    @Test
    public void testCalcularCuotaFija() {
        BigDecimal monto = new BigDecimal("50000000.00");
        BigDecimal tasaMensual = new BigDecimal("0.0127");
        int plazo = 48;

        BigDecimal cuota = CalculadoraAmortizacion.calcularCuotaFija(monto, tasaMensual, plazo);
        assertNotNull(cuota);
        assertTrue(cuota.compareTo(BigDecimal.ZERO) > 0);
        // Cuota debe ser mayor que monto/plazo
        assertTrue(cuota.compareTo(monto.divide(new BigDecimal(plazo), 2, java.math.RoundingMode.HALF_UP)) > 0);
    }

    @Test
    public void testTablaAmortizacionSaldoFinalCero() {
        BigDecimal monto = new BigDecimal("60000000.00");
        BigDecimal tasaEa = new BigDecimal("0.1650");
        int plazo = 36;

        TablaAmortizacion tabla = CalculadoraAmortizacion.generarTablaAmortizacion(monto, tasaEa, plazo, LocalDate.now());
        assertNotNull(tabla);
        assertEquals(36, tabla.getCuotas().size());

        // La última cuota debe tener saldo restante igual a 0.00
        CuotaAmortizacion ultimaCuota = tabla.getCuotas().get(35);
        assertEquals(new BigDecimal("0.00"), ultimaCuota.getSaldoRestante());

        // La suma de los abonos a capital debe ser exactamente igual al monto financiado
        BigDecimal sumaCapital = BigDecimal.ZERO;
        for (CuotaAmortizacion c : tabla.getCuotas()) {
            sumaCapital = sumaCapital.add(c.getAbonoCapital());
        }
        assertEquals(monto, sumaCapital);
    }
}
