package co.edu.javeriana.concesionario.mantenimiento.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de orden de servicio y mantenimiento técnico (CU-21).
 * Responsable: Jaime Andrés Molina
 */
public class OrdenMantenimiento {
    private Long id;
    private String numeroOrden;
    private Long vehiculoId;
    private Long tallerId;
    private String tipo; // PREVENTIVO, CORRECTIVO, ALISTAMIENTO
    private LocalDateTime fechaIngreso;
    private BigDecimal costoTotal;
    private String estado; // EN_DIAGNOSTICO, EN_REPARACION, COMPLETADO, ENTREGADO
    private String diagnostico;

    public OrdenMantenimiento() {
        this.estado = "EN_DIAGNOSTICO";
        this.costoTotal = BigDecimal.ZERO;
        this.fechaIngreso = LocalDateTime.now();
    }

    public OrdenMantenimiento(Long id, String numeroOrden, Long vehiculoId, Long tallerId, String tipo,
                              BigDecimal costoTotal, String estado, String diagnostico) {
        this.id = id;
        this.numeroOrden = numeroOrden;
        this.vehiculoId = vehiculoId;
        this.tallerId = tallerId;
        this.tipo = tipo;
        this.costoTotal = costoTotal;
        this.estado = estado;
        this.diagnostico = diagnostico;
        this.fechaIngreso = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public Long getTallerId() { return tallerId; }
    public void setTallerId(Long tallerId) { this.tallerId = tallerId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
}
