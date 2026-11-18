package co.edu.javeriana.concesionario.legal.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Póliza multirriesgo asociada a una unidad de la flota de alquiler (CU-23).
 * El tomador es el concesionario; los datos se toman de la póliza que expide la aseguradora.
 */
public class PolizaSeguro {
    private Long id;
    private String numeroPoliza;
    private Long aseguradoraId;
    private Long vehiculoId;
    private TipoPoliza tipo;
    private BigDecimal valorPrima;
    private BigDecimal valorCobertura;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    /** VIGENTE o CANCELADA; ver {@link #estadoEfectivo(LocalDate)}. */
    private EstadoPoliza estado;

    public PolizaSeguro() {
        this.estado = EstadoPoliza.VIGENTE;
    }

    public PolizaSeguro(Long id, String numeroPoliza, Long aseguradoraId, Long vehiculoId, TipoPoliza tipo,
                        BigDecimal valorPrima, BigDecimal valorCobertura, LocalDate fechaInicio,
                        LocalDate fechaVencimiento, EstadoPoliza estado) {
        this.id = id;
        this.numeroPoliza = numeroPoliza;
        this.aseguradoraId = aseguradoraId;
        this.vehiculoId = vehiculoId;
        this.tipo = tipo;
        this.valorPrima = valorPrima;
        this.valorCobertura = valorCobertura;
        this.fechaInicio = fechaInicio;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = estado;
    }

    public boolean estaCancelada() {
        return estado == EstadoPoliza.CANCELADA;
    }

    /** Estado que se muestra al usuario: una póliza no cancelada cuya vigencia terminó está vencida. */
    public EstadoPoliza estadoEfectivo(LocalDate hoy) {
        if (estaCancelada()) {
            return EstadoPoliza.CANCELADA;
        }
        return fechaVencimiento != null && fechaVencimiento.isBefore(hoy) ? EstadoPoliza.VENCIDA : EstadoPoliza.VIGENTE;
    }

    /** True si la póliza no está cancelada y la fecha dada cae dentro de su vigencia. */
    public boolean cubre(LocalDate fecha) {
        return !estaCancelada() && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaVencimiento);
    }

    /** True si la vigencia de esta póliza se cruza con el periodo dado (ambos extremos incluidos). */
    public boolean seCruzaCon(LocalDate inicio, LocalDate fin) {
        return !fechaInicio.isAfter(fin) && !fechaVencimiento.isBefore(inicio);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroPoliza() { return numeroPoliza; }
    public void setNumeroPoliza(String numeroPoliza) { this.numeroPoliza = numeroPoliza; }

    public Long getAseguradoraId() { return aseguradoraId; }
    public void setAseguradoraId(Long aseguradoraId) { this.aseguradoraId = aseguradoraId; }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public TipoPoliza getTipo() { return tipo; }
    public void setTipo(TipoPoliza tipo) { this.tipo = tipo; }

    public BigDecimal getValorPrima() { return valorPrima; }
    public void setValorPrima(BigDecimal valorPrima) { this.valorPrima = valorPrima; }

    public BigDecimal getValorCobertura() { return valorCobertura; }
    public void setValorCobertura(BigDecimal valorCobertura) { this.valorCobertura = valorCobertura; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public EstadoPoliza getEstado() { return estado; }
    public void setEstado(EstadoPoliza estado) { this.estado = estado; }
}
