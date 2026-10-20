package co.edu.javeriana.concesionario.clientes.modelo;

import java.time.LocalDateTime;

/**
 * Entidad de radicación de PQRS (CU-03).
 */
public class Pqrs {
    private Long id;
    private Long clienteId;
    private String tipo; // PETICION, QUEJA, RECLAMO, SUGERENCIA
    private String descripcion;
    private String estado;
    private LocalDateTime fechaRadicacion;
    private LocalDateTime fechaResolucion;

    public Pqrs() {
        this.estado = "ABIERTO";
        this.fechaRadicacion = LocalDateTime.now();
    }

    public Pqrs(Long id, Long clienteId, String tipo, String descripcion, String estado) {
        this.id = id;
        this.clienteId = clienteId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.estado = estado;
        this.fechaRadicacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaRadicacion() { return fechaRadicacion; }
    public void setFechaRadicacion(LocalDateTime fechaRadicacion) { this.fechaRadicacion = fechaRadicacion; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public boolean estaResuelta() {
        return fechaResolucion != null;
    }
}
