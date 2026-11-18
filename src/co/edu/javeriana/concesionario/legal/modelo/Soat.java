package co.edu.javeriana.concesionario.legal.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seguro obligatorio de accidentes de tránsito (SOAT) de un vehículo (CU-24).
 * Cada renovación crea un SOAT nuevo; los anteriores quedan como historial.
 */
public class Soat {
    private Long id;
    private String numeroPoliza;
    private Long vehiculoId;
    private Long aseguradoraId;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private BigDecimal valorPagado;
    private String codigoAutorizacion;

    public Soat() {
    }

    public Soat(Long id, String numeroPoliza, Long vehiculoId, Long aseguradoraId, LocalDate fechaInicio,
                LocalDate fechaVencimiento, BigDecimal valorPagado, String codigoAutorizacion) {
        this.id = id;
        this.numeroPoliza = numeroPoliza;
        this.vehiculoId = vehiculoId;
        this.aseguradoraId = aseguradoraId;
        this.fechaInicio = fechaInicio;
        this.fechaVencimiento = fechaVencimiento;
        this.valorPagado = valorPagado;
        this.codigoAutorizacion = codigoAutorizacion;
    }

    /** El SOAT cubre desde el día de inicio hasta el día de vencimiento, ambos incluidos. */
    public boolean estaVigente(LocalDate fecha) {
        return !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaVencimiento);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroPoliza() { return numeroPoliza; }
    public void setNumeroPoliza(String numeroPoliza) { this.numeroPoliza = numeroPoliza; }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public Long getAseguradoraId() { return aseguradoraId; }
    public void setAseguradoraId(Long aseguradoraId) { this.aseguradoraId = aseguradoraId; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public BigDecimal getValorPagado() { return valorPagado; }
    public void setValorPagado(BigDecimal valorPagado) { this.valorPagado = valorPagado; }

    public String getCodigoAutorizacion() { return codigoAutorizacion; }
    public void setCodigoAutorizacion(String codigoAutorizacion) { this.codigoAutorizacion = codigoAutorizacion; }
}
