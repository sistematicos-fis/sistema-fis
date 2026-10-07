package co.edu.javeriana.concesionario.legal.modelo;

/**
 * Entidad de compañía aseguradora aliada (CU-33).
 */
public class Aseguradora {
    private Long id;
    private String codigo;
    private String nombre;
    private String nit;
    private String telefonoContacto;
    private boolean activo;

    public Aseguradora() {
        this.activo = true;
    }

    public Aseguradora(Long id, String codigo, String nombre, String nit, String telefonoContacto, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.nit = nit;
        this.telefonoContacto = telefonoContacto;
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

    public String getTelefonoContacto() { return telefonoContacto; }
    public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre + " (" + codigo + ")";
    }
}
