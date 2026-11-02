package co.edu.javeriana.concesionario.inventario.modelo;

import java.time.LocalDateTime;

/**
 * Lo que el personal de bodega observó físicamente de una unidad en un momento dado.
 * Es la referencia contra la que el CU-10 contrasta el estado registrado en el sistema
 * (paso 4 del flujo básico).
 *
 * Responsable: Juan Pablo León
 */
public class ConteoFisico {

    private Long id;
    private Long vehiculoId;
    private Long bodegaId;
    private EstadoVehiculo estadoFisico;
    private boolean presente;
    private String responsable;
    private String observaciones;
    private LocalDateTime fechaConteo;

    public ConteoFisico() {
        this.presente = true;
    }

    public ConteoFisico(Long id, Long vehiculoId, Long bodegaId, EstadoVehiculo estadoFisico,
                        boolean presente, String responsable, String observaciones,
                        LocalDateTime fechaConteo) {
        this.id = id;
        this.vehiculoId = vehiculoId;
        this.bodegaId = bodegaId;
        this.estadoFisico = estadoFisico;
        this.presente = presente;
        this.responsable = responsable;
        this.observaciones = observaciones;
        this.fechaConteo = fechaConteo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public Long getBodegaId() { return bodegaId; }
    public void setBodegaId(Long bodegaId) { this.bodegaId = bodegaId; }

    public EstadoVehiculo getEstadoFisico() { return estadoFisico; }
    public void setEstadoFisico(EstadoVehiculo estadoFisico) { this.estadoFisico = estadoFisico; }

    public boolean isPresente() { return presente; }
    public void setPresente(boolean presente) { this.presente = presente; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaConteo() { return fechaConteo; }
    public void setFechaConteo(LocalDateTime fechaConteo) { this.fechaConteo = fechaConteo; }

    @Override
    public String toString() {
        return "Conteo de vehículo " + vehiculoId + ": " + estadoFisico
                + (presente ? "" : " (ausente en bodega)");
    }
}
