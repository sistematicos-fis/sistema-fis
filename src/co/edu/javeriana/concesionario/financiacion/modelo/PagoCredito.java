package co.edu.javeriana.concesionario.financiacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que registra el pago de cuotas de financiación.
 */
public class PagoCredito {
    private Long id;
    private Long creditoFormalizadoId;
    private int numeroCuota;
    private BigDecimal montoPagado;
    private LocalDateTime fechaPago;
    private String numeroRecibo;
    private String metodoPago;

    public PagoCredito() {
        this.fechaPago = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCreditoFormalizadoId() { return creditoFormalizadoId; }
    public void setCreditoFormalizadoId(Long creditoFormalizadoId) { this.creditoFormalizadoId = creditoFormalizadoId; }

    public int getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(int numeroCuota) { this.numeroCuota = numeroCuota; }

    public BigDecimal getMontoPagado() { return montoPagado; }
    public void setMontoPagado(BigDecimal montoPagado) { this.montoPagado = montoPagado; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public String getNumeroRecibo() { return numeroRecibo; }
    public void setNumeroRecibo(String numeroRecibo) { this.numeroRecibo = numeroRecibo; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
}
