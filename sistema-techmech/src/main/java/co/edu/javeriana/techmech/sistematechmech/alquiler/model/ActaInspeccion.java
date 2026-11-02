package co.edu.javeriana.techmech.sistematechmech.alquiler.model;

import java.time.LocalDateTime;

public class ActaInspeccion {
    private Long idActa;
    private Long idVehiculo;
    private String tipoInspeccion; // "ENTREGA" o "DEVOLUCION"
    private int kilometraje;
    private int nivelCombustible; // Porcentaje de 0 a 100
    private String estadoCarroceria; // "BUENO", "RAYONES", "GOLPES"
    private String observaciones;
    private LocalDateTime fechaRegistro;
    private boolean activo;

    public ActaInspeccion(Long idActa, Long idVehiculo, String tipoInspeccion, 
                          int kilometraje, int nivelCombustible, String estadoCarroceria, 
                          String observaciones, LocalDateTime fechaRegistro, boolean activo) {
        this.idActa = idActa;
        this.idVehiculo = idVehiculo;
        this.tipoInspeccion = tipoInspeccion;
        this.kilometraje = kilometraje;
        this.nivelCombustible = nivelCombustible;
        this.estadoCarroceria = estadoCarroceria;
        this.observaciones = observaciones;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    public Long getIdActa() { return idActa; }
    public void setIdActa(Long idActa) { this.idActa = idActa; }

    public Long getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(Long idVehiculo) { this.idVehiculo = idVehiculo; }

    public String getTipoInspeccion() { return tipoInspeccion; }
    public void setTipoInspeccion(String tipoInspeccion) { this.tipoInspeccion = tipoInspeccion; }

    public int getKilometraje() { return kilometraje; }
    public void setKilometraje(int kilometraje) { this.kilometraje = kilometraje; }

    public int getNivelCombustible() { return nivelCombustible; }
    public void setNivelCombustible(int nivelCombustible) { this.nivelCombustible = nivelCombustible; }

    public String getEstadoCarroceria() { return estadoCarroceria; }
    public void setEstadoCarroceria(String estadoCarroceria) { this.estadoCarroceria = estadoCarroceria; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}