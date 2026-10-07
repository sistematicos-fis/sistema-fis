package co.edu.javeriana.concesionario.financiacion.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa la tabla completa de amortización calculada (CU-13).
 */
public class TablaAmortizacion {
    private BigDecimal montoFinanciado;
    private int plazoMeses;
    private BigDecimal tasaInteresEa;
    private BigDecimal tasaPeriodicaMensual;
    private BigDecimal cuotaMensualEstimada;
    private BigDecimal totalIntereses;
    private BigDecimal totalPagar;
    private List<CuotaAmortizacion> cuotas;

    public TablaAmortizacion() {
        this.cuotas = new ArrayList<>();
    }

    public BigDecimal getMontoFinanciado() { return montoFinanciado; }
    public void setMontoFinanciado(BigDecimal montoFinanciado) { this.montoFinanciado = montoFinanciado; }

    public int getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(int plazoMeses) { this.plazoMeses = plazoMeses; }

    public BigDecimal getTasaInteresEa() { return tasaInteresEa; }
    public void setTasaInteresEa(BigDecimal tasaInteresEa) { this.tasaInteresEa = tasaInteresEa; }

    public BigDecimal getTasaPeriodicaMensual() { return tasaPeriodicaMensual; }
    public void setTasaPeriodicaMensual(BigDecimal tasaPeriodicaMensual) { this.tasaPeriodicaMensual = tasaPeriodicaMensual; }

    public BigDecimal getCuotaMensualEstimada() { return cuotaMensualEstimada; }
    public void setCuotaMensualEstimada(BigDecimal cuotaMensualEstimada) { this.cuotaMensualEstimada = cuotaMensualEstimada; }

    public BigDecimal getTotalIntereses() { return totalIntereses; }
    public void setTotalIntereses(BigDecimal totalIntereses) { this.totalIntereses = totalIntereses; }

    public BigDecimal getTotalPagar() { return totalPagar; }
    public void setTotalPagar(BigDecimal totalPagar) { this.totalPagar = totalPagar; }

    public List<CuotaAmortizacion> getCuotas() { return cuotas; }
    public void setCuotas(List<CuotaAmortizacion> cuotas) { this.cuotas = cuotas; }
}
