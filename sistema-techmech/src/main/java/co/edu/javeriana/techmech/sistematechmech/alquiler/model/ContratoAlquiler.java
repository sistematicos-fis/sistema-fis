package co.edu.javeriana.techmech.sistematechmech.alquiler.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContratoAlquiler {
    private Long idContrato;
    private Long idCliente;
    private Long idVehiculo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private long diasAlquiler;
    private BigDecimal valorTotal;
    private BigDecimal depositoGarantia;
    private String estado; // "ACTIVO", "FINALIZADO", "CANCELADO"
    private boolean activo;

    public ContratoAlquiler(Long idContrato, Long idCliente, Long idVehiculo, 
                            LocalDate fechaInicio, LocalDate fechaFin, long diasAlquiler, 
                            BigDecimal valorTotal, BigDecimal depositoGarantia, 
                            String estado, boolean activo) {
        this.idContrato = idContrato;
        this.idCliente = idCliente;
        this.idVehiculo = idVehiculo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.diasAlquiler = diasAlquiler;
        this.valorTotal = valorTotal;
        this.depositoGarantia = depositoGarantia;
        this.estado = estado;
        this.activo = activo;
    }

    public Long getIdContrato() { return idContrato; }
    public void setIdContrato(Long idContrato) { this.idContrato = idContrato; }

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public Long getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(Long idVehiculo) { this.idVehiculo = idVehiculo; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public long getDiasAlquiler() { return diasAlquiler; }
    public void setDiasAlquiler(long diasAlquiler) { this.diasAlquiler = diasAlquiler; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public BigDecimal getDepositoGarantia() { return depositoGarantia; }
    public void setDepositoGarantia(BigDecimal depositoGarantia) { this.depositoGarantia = depositoGarantia; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}