package co.edu.javeriana.concesionario.legal.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa una póliza de seguro automotriz emitida.
 */
public class PolizaSeguro {
    private Long id;
    private String numeroPoliza;
    private Long aseguradoraId;
    private Long vehiculoId;
    private Long clienteId;
    private String tipoPoliza; // SOAT, TODO_RIESGO_RESPONSABILIDAD, EXTRACONTRACTUAL
    private BigDecimal valorPrima;
    private BigDecimal valorCobertura;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String estado;
    private String codigoAprobacionSimulada;

    public PolizaSeguro() {
        this.estado = "VIGENTE";
        this.fechaEmision = LocalDate.now();
        this.fechaVencimiento = LocalDate.now().plusYears(1);
    }

    public PolizaSeguro(Long id, String numeroPoliza, Long aseguradoraId, Long vehiculoId, Long clienteId,
                        String tipoPoliza, BigDecimal valorPrima, BigDecimal valorCobertura,
                        String estado, String codigoAprobacionSimulada) {
        this.id = id;
        this.numeroPoliza = numeroPoliza;
        this.aseguradoraId = aseguradoraId;
        this.vehiculoId = vehiculoId;
        this.clienteId = clienteId;
        this.tipoPoliza = tipoPoliza;
        this.valorPrima = valorPrima;
        this.valorCobertura = valorCobertura;
        this.estado = estado;
        this.codigoAprobacionSimulada = codigoAprobacionSimulada;
        this.fechaEmision = LocalDate.now();
        this.fechaVencimiento = LocalDate.now().plusYears(1);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroPoliza() { return numeroPoliza; }
    public void setNumeroPoliza(String numeroPoliza) { this.numeroPoliza = numeroPoliza; }

    public Long getAseguradoraId() { return aseguradoraId; }
    public void setAseguradoraId(Long aseguradoraId) { this.aseguradoraId = aseguradoraId; }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getTipoPoliza() { return tipoPoliza; }
    public void setTipoPoliza(String tipoPoliza) { this.tipoPoliza = tipoPoliza; }

    public BigDecimal getValorPrima() { return valorPrima; }
    public void setValorPrima(BigDecimal valorPrima) { this.valorPrima = valorPrima; }

    public BigDecimal getValorCobertura() { return valorCobertura; }
    public void setValorCobertura(BigDecimal valorCobertura) { this.valorCobertura = valorCobertura; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCodigoAprobacionSimulada() { return codigoAprobacionSimulada; }
    public void setCodigoAprobacionSimulada(String codigoAprobacionSimulada) { this.codigoAprobacionSimulada = codigoAprobacionSimulada; }
}
