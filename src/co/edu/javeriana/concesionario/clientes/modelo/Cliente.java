package co.edu.javeriana.concesionario.clientes.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un cliente en el concesionario.
 * Responsable: Andrés Alquerque
 */
public class Cliente {
    private Long id;
    private String documento;
    private String tipoDocumento;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private BigDecimal ingresosMensuales;
    private CategoriaComercial categoriaComercial;
    private Long asesorId;
    private BigDecimal indiceSatisfaccion;
    private CategoriaSatisfaccion nivelSatisfaccion;
    private LocalDateTime fechaIndiceSatisfaccion;

    public Cliente() {
        this.categoriaComercial = CategoriaComercial.ESTANDAR;
        this.ingresosMensuales = BigDecimal.ZERO;
    }

    public Cliente(Long id, String documento, String tipoDocumento, String nombres, String apellidos,
                   String email, String telefono, BigDecimal ingresosMensuales, CategoriaComercial categoriaComercial) {
        this.id = id;
        this.documento = documento;
        this.tipoDocumento = tipoDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.ingresosMensuales = ingresosMensuales;
        this.categoriaComercial = categoriaComercial;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getNombreCompleto() { return nombres + " " + apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public BigDecimal getIngresosMensuales() { return ingresosMensuales; }
    public void setIngresosMensuales(BigDecimal ingresosMensuales) { this.ingresosMensuales = ingresosMensuales; }

    public CategoriaComercial getCategoriaComercial() { return categoriaComercial; }
    public void setCategoriaComercial(CategoriaComercial categoriaComercial) { this.categoriaComercial = categoriaComercial; }

    public Long getAsesorId() { return asesorId; }
    public void setAsesorId(Long asesorId) { this.asesorId = asesorId; }

    public BigDecimal getIndiceSatisfaccion() { return indiceSatisfaccion; }
    public void setIndiceSatisfaccion(BigDecimal indiceSatisfaccion) { this.indiceSatisfaccion = indiceSatisfaccion; }

    public CategoriaSatisfaccion getNivelSatisfaccion() { return nivelSatisfaccion; }
    public void setNivelSatisfaccion(CategoriaSatisfaccion nivelSatisfaccion) { this.nivelSatisfaccion = nivelSatisfaccion; }

    public LocalDateTime getFechaIndiceSatisfaccion() { return fechaIndiceSatisfaccion; }
    public void setFechaIndiceSatisfaccion(LocalDateTime fechaIndiceSatisfaccion) { this.fechaIndiceSatisfaccion = fechaIndiceSatisfaccion; }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + documento + ")";
    }
}
