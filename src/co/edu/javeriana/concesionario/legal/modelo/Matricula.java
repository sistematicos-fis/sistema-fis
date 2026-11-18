package co.edu.javeriana.concesionario.legal.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Matrícula de un vehículo (CU-21): su registro oficial ante el organismo de tránsito, con los datos
 * de la licencia de tránsito, y el estado de pago del impuesto vehicular.
 * Cada vehículo tiene a lo sumo una matrícula y la placa no cambia una vez asignada.
 */
public class Matricula {
    private Long id;
    private Long vehiculoId;
    private String placa;
    private String numeroLicenciaTransito;
    private String organismoTransito;
    private LocalDate fechaMatricula;
    private Integer cilindrajeCc;
    private EstadoMatricula estadoRegistro;
    private Integer ultimoAnioImpuestoPagado;
    private BigDecimal valorImpuestoAnual;

    public Matricula() {
        this.estadoRegistro = EstadoMatricula.ACTIVA;
    }

    public Matricula(Long id, Long vehiculoId, String placa, String numeroLicenciaTransito, String organismoTransito,
                     LocalDate fechaMatricula, Integer cilindrajeCc, EstadoMatricula estadoRegistro,
                     Integer ultimoAnioImpuestoPagado, BigDecimal valorImpuestoAnual) {
        this.id = id;
        this.vehiculoId = vehiculoId;
        this.placa = placa;
        this.numeroLicenciaTransito = numeroLicenciaTransito;
        this.organismoTransito = organismoTransito;
        this.fechaMatricula = fechaMatricula;
        this.cilindrajeCc = cilindrajeCc;
        this.estadoRegistro = estadoRegistro;
        this.ultimoAnioImpuestoPagado = ultimoAnioImpuestoPagado;
        this.valorImpuestoAnual = valorImpuestoAnual;
    }

    /*
     Al día si ya pagó la vigencia del año actual; pendiente si solo debe la vigencia actual;
     en mora si debe vigencias de años anteriores.
     */
    public EstadoImpuesto estadoImpuesto(int anioActual) {
        int ultimoPagado = ultimoAnioImpuestoPagado != null
                ? ultimoAnioImpuestoPagado
                : fechaMatricula.getYear() - 1;
        if (ultimoPagado >= anioActual) {
            return EstadoImpuesto.AL_DIA;
        }
        return ultimoPagado == anioActual - 1 ? EstadoImpuesto.PENDIENTE : EstadoImpuesto.EN_MORA;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getNumeroLicenciaTransito() { return numeroLicenciaTransito; }
    public void setNumeroLicenciaTransito(String numeroLicenciaTransito) { this.numeroLicenciaTransito = numeroLicenciaTransito; }

    public String getOrganismoTransito() { return organismoTransito; }
    public void setOrganismoTransito(String organismoTransito) { this.organismoTransito = organismoTransito; }

    public LocalDate getFechaMatricula() { return fechaMatricula; }
    public void setFechaMatricula(LocalDate fechaMatricula) { this.fechaMatricula = fechaMatricula; }

    public Integer getCilindrajeCc() { return cilindrajeCc; }
    public void setCilindrajeCc(Integer cilindrajeCc) { this.cilindrajeCc = cilindrajeCc; }

    public EstadoMatricula getEstadoRegistro() { return estadoRegistro; }
    public void setEstadoRegistro(EstadoMatricula estadoRegistro) { this.estadoRegistro = estadoRegistro; }

    public Integer getUltimoAnioImpuestoPagado() { return ultimoAnioImpuestoPagado; }
    public void setUltimoAnioImpuestoPagado(Integer ultimoAnioImpuestoPagado) { this.ultimoAnioImpuestoPagado = ultimoAnioImpuestoPagado; }

    public BigDecimal getValorImpuestoAnual() { return valorImpuestoAnual; }
    public void setValorImpuestoAnual(BigDecimal valorImpuestoAnual) { this.valorImpuestoAnual = valorImpuestoAnual; }
}
