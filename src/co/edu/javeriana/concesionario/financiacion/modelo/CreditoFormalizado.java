package co.edu.javeriana.concesionario.financiacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa la formalización y desembolso del crédito aprobado (CU-15).
 */
public class CreditoFormalizado {
    private Long id;
    private Long solicitudId;
    private String codigoAprobacionBanco;
    private Long entidadId;
    private BigDecimal montoAprobado;
    private BigDecimal tasaMensualAplicada;
    private int plazoMeses;
    private LocalDateTime fechaFormalizacion;
    private String estado; // ACTIVO, FINALIZADO, CANCELADO, MORA
    private BigDecimal saldoCapitalPendiente;

    public CreditoFormalizado() {
        this.estado = "ACTIVO";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSolicitudId() { return solicitudId; }
    public void setSolicitudId(Long solicitudId) { this.solicitudId = solicitudId; }

    public String getCodigoAprobacionBanco() { return codigoAprobacionBanco; }
    public void setCodigoAprobacionBanco(String codigoAprobacionBanco) { this.codigoAprobacionBanco = codigoAprobacionBanco; }

    public Long getEntidadId() { return entidadId; }
    public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }

    public BigDecimal getMontoAprobado() { return montoAprobado; }
    public void setMontoAprobado(BigDecimal montoAprobado) { this.montoAprobado = montoAprobado; }

    public BigDecimal getTasaMensualAplicada() { return tasaMensualAplicada; }
    public void setTasaMensualAplicada(BigDecimal tasaMensualAplicada) { this.tasaMensualAplicada = tasaMensualAplicada; }

    public int getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(int plazoMeses) { this.plazoMeses = plazoMeses; }

    public LocalDateTime getFechaFormalizacion() { return fechaFormalizacion; }
    public void setFechaFormalizacion(LocalDateTime fechaFormalizacion) { this.fechaFormalizacion = fechaFormalizacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getSaldoCapitalPendiente() { return saldoCapitalPendiente; }
    public void setSaldoCapitalPendiente(BigDecimal saldoCapitalPendiente) { this.saldoCapitalPendiente = saldoCapitalPendiente; }
}
