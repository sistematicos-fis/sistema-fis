package co.edu.javeriana.concesionario.financiacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Detalle de cada cuota individual en la tabla de amortización (CU-13).
 */
public class CuotaAmortizacion {
    private int numeroCuota;
    private LocalDate fechaVencimiento;
    private BigDecimal cuotaFija;
    private BigDecimal abonoCapital;
    private BigDecimal interes;
    private BigDecimal saldoRestante;
    private String estado; // PENDIENTE, PAGADA, MORA

    public CuotaAmortizacion() {
        this.estado = "PENDIENTE";
    }

    public CuotaAmortizacion(int numeroCuota, LocalDate fechaVencimiento, BigDecimal cuotaFija,
                             BigDecimal abonoCapital, BigDecimal interes, BigDecimal saldoRestante) {
        this.numeroCuota = numeroCuota;
        this.fechaVencimiento = fechaVencimiento;
        this.cuotaFija = cuotaFija;
        this.abonoCapital = abonoCapital;
        this.interes = interes;
        this.saldoRestante = saldoRestante;
        this.estado = "PENDIENTE";
    }

    public int getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(int numeroCuota) { this.numeroCuota = numeroCuota; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public BigDecimal getCuotaFija() { return cuotaFija; }
    public void setCuotaFija(BigDecimal cuotaFija) { this.cuotaFija = cuotaFija; }

    public BigDecimal getAbonoCapital() { return abonoCapital; }
    public void setAbonoCapital(BigDecimal abonoCapital) { this.abonoCapital = abonoCapital; }

    public BigDecimal getInteres() { return interes; }
    public void setInteres(BigDecimal interes) { this.interes = interes; }

    public BigDecimal getSaldoRestante() { return saldoRestante; }
    public void setSaldoRestante(BigDecimal saldoRestante) { this.saldoRestante = saldoRestante; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
