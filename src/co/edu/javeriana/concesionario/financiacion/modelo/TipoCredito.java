package co.edu.javeriana.concesionario.financiacion.modelo;

import java.math.BigDecimal;

/**
 * Entidad que representa una línea o producto de crédito automotriz (CU-12).
 */
public class TipoCredito {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Long entidadFinancieraId;
    private BigDecimal tasaInteresEa;
    private int plazoMinimoMeses;
    private int plazoMaximoMeses;
    private BigDecimal porcentajeFinanciacionMax;
    private boolean requiereGarantiaPrenda;
    private boolean activo;

    public TipoCredito() {
        this.activo = true;
        this.requiereGarantiaPrenda = true;
    }

    public TipoCredito(Long id, String codigo, String nombre, String descripcion, Long entidadFinancieraId,
                       BigDecimal tasaInteresEa, int plazoMinimoMeses, int plazoMaximoMeses,
                       BigDecimal porcentajeFinanciacionMax, boolean requiereGarantiaPrenda, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.entidadFinancieraId = entidadFinancieraId;
        this.tasaInteresEa = tasaInteresEa;
        this.plazoMinimoMeses = plazoMinimoMeses;
        this.plazoMaximoMeses = plazoMaximoMeses;
        this.porcentajeFinanciacionMax = porcentajeFinanciacionMax;
        this.requiereGarantiaPrenda = requiereGarantiaPrenda;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Long getEntidadFinancieraId() { return entidadFinancieraId; }
    public void setEntidadFinancieraId(Long entidadFinancieraId) { this.entidadFinancieraId = entidadFinancieraId; }

    public BigDecimal getTasaInteresEa() { return tasaInteresEa; }
    public void setTasaInteresEa(BigDecimal tasaInteresEa) { this.tasaInteresEa = tasaInteresEa; }

    public int getPlazoMinimoMeses() { return plazoMinimoMeses; }
    public void setPlazoMinimoMeses(int plazoMinimoMeses) { this.plazoMinimoMeses = plazoMinimoMeses; }

    public int getPlazoMaximoMeses() { return plazoMaximoMeses; }
    public void setPlazoMaximoMeses(int plazoMaximoMeses) { this.plazoMaximoMeses = plazoMaximoMeses; }

    public BigDecimal getPorcentajeFinanciacionMax() { return porcentajeFinanciacionMax; }
    public void setPorcentajeFinanciacionMax(BigDecimal porcentajeFinanciacionMax) { this.porcentajeFinanciacionMax = porcentajeFinanciacionMax; }

    public boolean isRequiereGarantiaPrenda() { return requiereGarantiaPrenda; }
    public void setRequiereGarantiaPrenda(boolean requiereGarantiaPrenda) { this.requiereGarantiaPrenda = requiereGarantiaPrenda; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre + " (" + plazoMinimoMeses + "-" + plazoMaximoMeses + " meses)";
    }
}
