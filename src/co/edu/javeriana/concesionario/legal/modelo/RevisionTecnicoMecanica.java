package co.edu.javeriana.concesionario.legal.modelo;

import java.time.LocalDate;

/**
 * Certificado de revisión técnico-mecánica de un vehículo (CU-25).
 * Una revisión aprobada tiene una vigencia de un año desde su expedición; una rechazada no tiene vigencia.
 */
public class RevisionTecnicoMecanica {
    private Long id;
    private Long vehiculoId;
    private String numeroCertificado;
    private String centroDiagnostico;
    private LocalDate fechaExpedicion;
    private LocalDate fechaVencimiento;
    private ResultadoRevision resultado;

    public RevisionTecnicoMecanica() {
    }

    public RevisionTecnicoMecanica(Long id, Long vehiculoId, String numeroCertificado, String centroDiagnostico,
                                   LocalDate fechaExpedicion, LocalDate fechaVencimiento, ResultadoRevision resultado) {
        this.id = id;
        this.vehiculoId = vehiculoId;
        this.numeroCertificado = numeroCertificado;
        this.centroDiagnostico = centroDiagnostico;
        this.fechaExpedicion = fechaExpedicion;
        this.fechaVencimiento = fechaVencimiento;
        this.resultado = resultado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }

    public String getNumeroCertificado() { return numeroCertificado; }
    public void setNumeroCertificado(String numeroCertificado) { this.numeroCertificado = numeroCertificado; }

    public String getCentroDiagnostico() { return centroDiagnostico; }
    public void setCentroDiagnostico(String centroDiagnostico) { this.centroDiagnostico = centroDiagnostico; }

    public LocalDate getFechaExpedicion() { return fechaExpedicion; }
    public void setFechaExpedicion(LocalDate fechaExpedicion) { this.fechaExpedicion = fechaExpedicion; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public ResultadoRevision getResultado() { return resultado; }
    public void setResultado(ResultadoRevision resultado) { this.resultado = resultado; }
}
