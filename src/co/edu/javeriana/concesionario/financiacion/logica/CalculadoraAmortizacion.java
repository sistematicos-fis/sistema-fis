package co.edu.javeriana.concesionario.financiacion.logica;

import co.edu.javeriana.concesionario.financiacion.modelo.CuotaAmortizacion;
import co.edu.javeriana.concesionario.financiacion.modelo.TablaAmortizacion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Motor de Cálculo Financiero y Amortización (Caso de Uso NO TRIVIAL: CU-13).
 * Implementa el sistema de cuota fija con sistema Francés, conversión exacta de tasas
 * y ajuste al centavo en la última cuota para garantizar saldo cero exacto.
 * 
 * Responsable: Jerónimo Martin
 */
public class CalculadoraAmortizacion {

    private static final MathContext MC_CALC = new MathContext(16, RoundingMode.HALF_UP);

    /**
     * Convierte una tasa Efectiva Anual (EA) a tasa periódica Mes Vencido (MV).
     * Fórmula: i_m = (1 + EA)^(1/12) - 1
     */
    public static BigDecimal convertirEaAMensualVencida(BigDecimal tasaEa) {
        if (tasaEa == null || tasaEa.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tasa Efectiva Anual no puede ser nula ni negativa.");
        }
        double ea = tasaEa.doubleValue();
        double mensual = Math.pow(1.0 + ea, 1.0 / 12.0) - 1.0;
        return new BigDecimal(mensual, MC_CALC).setScale(8, RoundingMode.HALF_UP);
    }

    /**
     * Calcula la cuota mensual fija (Fórmula Francesa).
     * Cuota = P * [ i / (1 - (1 + i)^(-n)) ]
     */
    public static BigDecimal calcularCuotaFija(BigDecimal monto, BigDecimal tasaMensual, int plazoMeses) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a financiar debe ser mayor a cero.");
        }
        if (plazoMeses <= 0) {
            throw new IllegalArgumentException("El plazo en meses debe ser mayor a cero.");
        }
        if (tasaMensual == null || tasaMensual.compareTo(BigDecimal.ZERO) <= 0) {
            // Tasa cero: amortización lineal simple
            return monto.divide(new BigDecimal(plazoMeses), 2, RoundingMode.HALF_UP);
        }

        double p = monto.doubleValue();
        double i = tasaMensual.doubleValue();
        double factor = Math.pow(1.0 + i, -plazoMeses);
        double cuota = p * (i / (1.0 - factor));

        return new BigDecimal(cuota).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Genera la tabla completa de amortización con detalle cuota a cuota.
     */
    public static TablaAmortizacion generarTablaAmortizacion(BigDecimal monto, BigDecimal tasaEa, int plazoMeses, LocalDate fechaPrimerVencimiento) {
        if (fechaPrimerVencimiento == null) {
            fechaPrimerVencimiento = LocalDate.now().plusMonths(1);
        }

        BigDecimal tasaMensual = convertirEaAMensualVencida(tasaEa);
        BigDecimal cuotaFija = calcularCuotaFija(monto, tasaMensual, plazoMeses);

        TablaAmortizacion tabla = new TablaAmortizacion();
        tabla.setMontoFinanciado(monto.setScale(2, RoundingMode.HALF_UP));
        tabla.setPlazoMeses(plazoMeses);
        tabla.setTasaInteresEa(tasaEa);
        tabla.setTasaPeriodicaMensual(tasaMensual);
        tabla.setCuotaMensualEstimada(cuotaFija);

        List<CuotaAmortizacion> cuotas = new ArrayList<>();
        BigDecimal saldoActual = monto.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalIntereses = BigDecimal.ZERO;
        BigDecimal totalPagar = BigDecimal.ZERO;

        for (int k = 1; k <= plazoMeses; k++) {
            LocalDate fechaVenc = fechaPrimerVencimiento.plusMonths(k - 1);
            BigDecimal interesCuota = saldoActual.multiply(tasaMensual).setScale(2, RoundingMode.HALF_UP);
            BigDecimal abonoCapital;
            BigDecimal cuotaAplicada;
            BigDecimal nuevoSaldo;

            if (k == plazoMeses) {
                // Última cuota: Ajuste de centavos para garantizar saldo cero
                abonoCapital = saldoActual;
                cuotaAplicada = abonoCapital.add(interesCuota).setScale(2, RoundingMode.HALF_UP);
                nuevoSaldo = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            } else {
                abonoCapital = cuotaFija.subtract(interesCuota).setScale(2, RoundingMode.HALF_UP);
                if (abonoCapital.compareTo(saldoActual) > 0) {
                    abonoCapital = saldoActual;
                }
                cuotaAplicada = cuotaFija;
                nuevoSaldo = saldoActual.subtract(abonoCapital).setScale(2, RoundingMode.HALF_UP);
            }

            totalIntereses = totalIntereses.add(interesCuota);
            totalPagar = totalPagar.add(cuotaAplicada);

            CuotaAmortizacion fila = new CuotaAmortizacion(k, fechaVenc, cuotaAplicada, abonoCapital, interesCuota, nuevoSaldo);
            cuotas.add(fila);

            saldoActual = nuevoSaldo;
        }

        tabla.setCuotas(cuotas);
        tabla.setTotalIntereses(totalIntereses.setScale(2, RoundingMode.HALF_UP));
        tabla.setTotalPagar(totalPagar.setScale(2, RoundingMode.HALF_UP));

        return tabla;
    }
}
