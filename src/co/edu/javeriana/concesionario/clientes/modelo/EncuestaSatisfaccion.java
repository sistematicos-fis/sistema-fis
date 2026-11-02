package co.edu.javeriana.concesionario.clientes.modelo;

import java.time.LocalDateTime;

/**
 * Entidad de Encuesta de Satisfacción (CU-04).
 */
public class EncuestaSatisfaccion {
    private Long id;
    private Long clienteId;
    private int puntuacionAtencion; // 1 a 5
    private int puntuacionProducto; // 1 a 5
    private int puntuacionTiempos;  // 1 a 5
    private String comentarios;
    private LocalDateTime fecha;

    public EncuestaSatisfaccion() {
        this.fecha = LocalDateTime.now();
    }

    public EncuestaSatisfaccion(Long id, Long clienteId, int puntuacionAtencion, int puntuacionProducto, int puntuacionTiempos, String comentarios) {
        this.id = id;
        this.clienteId = clienteId;
        this.puntuacionAtencion = puntuacionAtencion;
        this.puntuacionProducto = puntuacionProducto;
        this.puntuacionTiempos = puntuacionTiempos;
        this.comentarios = comentarios;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public int getPuntuacionAtencion() { return puntuacionAtencion; }
    public void setPuntuacionAtencion(int puntuacionAtencion) { this.puntuacionAtencion = puntuacionAtencion; }

    public int getPuntuacionProducto() { return puntuacionProducto; }
    public void setPuntuacionProducto(int puntuacionProducto) { this.puntuacionProducto = puntuacionProducto; }

    public int getPuntuacionTiempos() { return puntuacionTiempos; }
    public void setPuntuacionTiempos(int puntuacionTiempos) { this.puntuacionTiempos = puntuacionTiempos; }

    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
