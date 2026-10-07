package co.edu.javeriana.concesionario.rrhh.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa a un colaborador del concesionario (CU-26).
 * Responsable: Sergio Alejandro Ramírez
 */
public class Empleado {
    private Long id;
    private String documento;
    private String nombres;
    private String apellidos;
    private String cargo;
    private String departamento;
    private BigDecimal salarioBase;
    private LocalDate fechaIngreso;
    private boolean activo;

    public Empleado() {
        this.activo = true;
        this.fechaIngreso = LocalDate.now();
    }

    public Empleado(Long id, String documento, String nombres, String apellidos, String cargo,
                    String departamento, BigDecimal salarioBase, LocalDate fechaIngreso, boolean activo) {
        this.id = id;
        this.documento = documento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.cargo = cargo;
        this.departamento = departamento;
        this.salarioBase = salarioBase;
        this.fechaIngreso = fechaIngreso;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getNombreCompleto() { return nombres + " " + apellidos; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public BigDecimal getSalarioBase() { return salarioBase; }
    public void setSalarioBase(BigDecimal salarioBase) { this.salarioBase = salarioBase; }

    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
