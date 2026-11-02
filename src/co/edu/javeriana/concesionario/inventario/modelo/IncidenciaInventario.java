package co.edu.javeriana.concesionario.inventario.modelo;

import java.time.LocalDateTime;

/**
 * Discrepancia detectada entre el inventario físico y el registrado en el sistema.
 * La abre el CU-10 cuando el paso 4 falla y queda pendiente hasta que el Administrador
 * de Bodega la resuelve.
 *
 * Responsable: Juan Pablo León
 */
public class IncidenciaInventario {

    private Long id;
    private Long vehiculoId;
    private EstadoVehiculo estadoSistema;
    private EstadoVehiculo estadoFisico;
    private String descripcion;
    private EstadoIncidencia estado;
    private String detectadaPor;
    private LocalDateTime fechaDeteccion;
    private String resueltaPor;
    private String observacionResolucion;
    private LocalDateTime fechaResolucion;

    public IncidenciaInventario() {
        this.estado = EstadoIncidencia.ABIERTA;
    }

    public IncidenciaInventario(Long vehiculoId, EstadoVehiculo estadoSistema, EstadoVehiculo estadoFisico,
                                String descripcion, String detectadaPor) {
        this();
        this.vehiculoId = vehiculoId;
        this.estadoSistema = estadoSistema;
        this.estadoFisico = estadoFisico;
        this.descripcion = descripcion;
        this.detectadaPor = detectadaPor;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public EstadoVehiculo getEstadoSistema() { return estadoSistema; }
    public void setEstadoSistema(EstadoVehiculo estadoSistema) { this.estadoSistema = estadoSistema; }

    public EstadoVehiculo getEstadoFisico() { return estadoFisico; }
    public void setEstadoFisico(EstadoVehiculo estadoFisico) { this.estadoFisico = estadoFisico; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public EstadoIncidencia getEstado() { return estado; }
    public void setEstado(EstadoIncidencia estado) { this.estado = estado; }

    public String getDetectadaPor() { return detectadaPor; }
    public void setDetectadaPor(String detectadaPor) { this.detectadaPor = detectadaPor; }

    public LocalDateTime getFechaDeteccion() { return fechaDeteccion; }
    public void setFechaDeteccion(LocalDateTime fechaDeteccion) { this.fechaDeteccion = fechaDeteccion; }

    public String getResueltaPor() { return resueltaPor; }
    public void setResueltaPor(String resueltaPor) { this.resueltaPor = resueltaPor; }

    public String getObservacionResolucion() { return observacionResolucion; }
    public void setObservacionResolucion(String observacionResolucion) {
        this.observacionResolucion = observacionResolucion;
    }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public boolean estaAbierta() {
        return estado == EstadoIncidencia.ABIERTA;
    }

    @Override
    public String toString() {
        return "Incidencia " + id + " sobre vehículo " + vehiculoId + " (" + estado + ")";
    }
}
