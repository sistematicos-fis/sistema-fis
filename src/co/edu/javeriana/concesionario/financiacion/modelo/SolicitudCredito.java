package co.edu.javeriana.concesionario.financiacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa la radicación formal de un crédito (CU-14).
 */
public class SolicitudCredito {
    private Long id;
    private String numeroRadicado;
    private Long clienteId;
    private Long vehiculoId;
    private Long tipoCreditoId;
    private BigDecimal montoSolicitado;
    private int plazoMeses;
    private BigDecimal cuotaInicial;
    private String estado; // RADICADA, EN_EVALUACION, APROBADA, RECHAZADA, FORMALIZADA
    private BigDecimal ingresosCertificados;
    private BigDecimal gastosDeclarados;
    private BigDecimal cuotaEstimada;
    private BigDecimal porcentajeEndeudamiento;
    private Integer scoreCrediticioSimulado;
    private String observaciones;
    private LocalDateTime fechaSolicitud;

    public SolicitudCredito() {
        this.estado = "RADICADA";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroRadicado() { return numeroRadicado; }
    public void setNumeroRadicado(String numeroRadicado) { this.numeroRadicado = numeroRadicado; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public Long getTipoCreditoId() { return tipoCreditoId; }
    public void setTipoCreditoId(Long tipoCreditoId) { this.tipoCreditoId = tipoCreditoId; }

    public BigDecimal getMontoSolicitado() { return montoSolicitado; }
    public void setMontoSolicitado(BigDecimal montoSolicitado) { this.montoSolicitado = montoSolicitado; }

    public int getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(int plazoMeses) { this.plazoMeses = plazoMeses; }

    public BigDecimal getCuotaInicial() { return cuotaInicial; }
    public void setCuotaInicial(BigDecimal cuotaInicial) { this.cuotaInicial = cuotaInicial; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getIngresosCertificados() { return ingresosCertificados; }
    public void setIngresosCertificados(BigDecimal ingresosCertificados) { this.ingresosCertificados = ingresosCertificados; }

    public BigDecimal getGastosDeclarados() { return gastosDeclarados; }
    public void setGastosDeclarados(BigDecimal gastosDeclarados) { this.gastosDeclarados = gastosDeclarados; }

    public BigDecimal getCuotaEstimada() { return cuotaEstimada; }
    public void setCuotaEstimada(BigDecimal cuotaEstimada) { this.cuotaEstimada = cuotaEstimada; }

    public BigDecimal getPorcentajeEndeudamiento() { return porcentajeEndeudamiento; }
    public void setPorcentajeEndeudamiento(BigDecimal porcentajeEndeudamiento) { this.porcentajeEndeudamiento = porcentajeEndeudamiento; }

    public Integer getScoreCrediticioSimulado() { return scoreCrediticioSimulado; }
    public void setScoreCrediticioSimulado(Integer scoreCrediticioSimulado) { this.scoreCrediticioSimulado = scoreCrediticioSimulado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
}
