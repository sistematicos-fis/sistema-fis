package co.edu.javeriana.concesionario.financiacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EntidadFinanciera {
    private Long id;
    private String codigo;
    private String nombre;
    private String nit;
    private BigDecimal tasaBaseEa;
    private int scoringMinimo;
    private boolean activo;
    private LocalDateTime fechaCreacion;

    public EntidadFinanciera() {
        this.activo = true;
    }

    public EntidadFinanciera(Long id, String codigo, String nombre, String nit, BigDecimal tasaBaseEa, int scoringMinimo, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.nit = nit;
        this.tasaBaseEa = tasaBaseEa;
        this.scoringMinimo = scoringMinimo;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public BigDecimal getTasaBaseEa() { return tasaBaseEa; }
    public void setTasaBaseEa(BigDecimal tasaBaseEa) { this.tasaBaseEa = tasaBaseEa; }

    public int getScoringMinimo() { return scoringMinimo; }
    public void setScoringMinimo(int scoringMinimo) { this.scoringMinimo = scoringMinimo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public String toString() {
        return nombre + " (Min Score: " + scoringMinimo + ")";
    }
}
