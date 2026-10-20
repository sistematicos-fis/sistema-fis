package co.edu.javeriana.concesionario.clientes.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlertaSatisfaccion {

    private Long id;
    private Long clienteId;
    private String nombreCliente;
    private Long asesorId;
    private BigDecimal indice;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean atendida;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public Long getAsesorId() { return asesorId; }
    public void setAsesorId(Long asesorId) { this.asesorId = asesorId; }

    public BigDecimal getIndice() { return indice; }
    public void setIndice(BigDecimal indice) { this.indice = indice; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public boolean isAtendida() { return atendida; }
    public void setAtendida(boolean atendida) { this.atendida = atendida; }
}
