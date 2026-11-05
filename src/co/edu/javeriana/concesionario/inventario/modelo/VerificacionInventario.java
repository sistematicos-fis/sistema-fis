package co.edu.javeriana.concesionario.inventario.modelo;

import java.time.LocalDateTime;

/**
 * Registro de una verificación de existencias en tiempo real (CU-10).
 * Es la entidad principal del caso de uso: queda guardada siempre, confirme o no
 * la disponibilidad, para dejar traza de por qué se permitió o se negó una venta.
 *
 * Responsable: Juan Pablo León
 */
public class VerificacionInventario {

    private Long id;
    private Long vehiculoId;
    private ResultadoVerificacion resultado;
    private String motivo;
    private EstadoVehiculo estadoSistema;
    private EstadoVehiculo estadoFisico;
    private String solicitadoPor;
    private LocalDateTime fechaVerificacion;
    private LocalDateTime bloqueoExpira;

    /** Incidencia abierta por esta verificación, si hubo discrepancia. No se persiste aquí. */
    private IncidenciaInventario incidencia;

    public VerificacionInventario() {
    }

    public VerificacionInventario(Long vehiculoId, ResultadoVerificacion resultado, String motivo,
                                  EstadoVehiculo estadoSistema, EstadoVehiculo estadoFisico,
                                  String solicitadoPor) {
        this.vehiculoId = vehiculoId;
        this.resultado = resultado;
        this.motivo = motivo;
        this.estadoSistema = estadoSistema;
        this.estadoFisico = estadoFisico;
        this.solicitadoPor = solicitadoPor;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public ResultadoVerificacion getResultado() { return resultado; }
    public void setResultado(ResultadoVerificacion resultado) { this.resultado = resultado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public EstadoVehiculo getEstadoSistema() { return estadoSistema; }
    public void setEstadoSistema(EstadoVehiculo estadoSistema) { this.estadoSistema = estadoSistema; }

    public EstadoVehiculo getEstadoFisico() { return estadoFisico; }
    public void setEstadoFisico(EstadoVehiculo estadoFisico) { this.estadoFisico = estadoFisico; }

    public String getSolicitadoPor() { return solicitadoPor; }
    public void setSolicitadoPor(String solicitadoPor) { this.solicitadoPor = solicitadoPor; }

    public LocalDateTime getFechaVerificacion() { return fechaVerificacion; }
    public void setFechaVerificacion(LocalDateTime fechaVerificacion) {
        this.fechaVerificacion = fechaVerificacion;
    }

    public LocalDateTime getBloqueoExpira() { return bloqueoExpira; }
    public void setBloqueoExpira(LocalDateTime bloqueoExpira) { this.bloqueoExpira = bloqueoExpira; }

    public IncidenciaInventario getIncidencia() { return incidencia; }
    public void setIncidencia(IncidenciaInventario incidencia) { this.incidencia = incidencia; }

    public boolean confirmoDisponibilidad() {
        return resultado != null && resultado.esFavorable();
    }

    @Override
    public String toString() {
        return "Verificación de vehículo " + vehiculoId + ": " + resultado;
    }
}
