package co.edu.javeriana.concesionario.legal.modelo;

/**
 * Compañía aseguradora aliada con la que el concesionario tiene un convenio (CU-22).
 * El convenio define qué pólizas puede ofrecer: SOAT (obligatoria) y/o responsabilidad civil
 * por daños materiales a terceros.
 */
public class Aseguradora {
    private Long id;
    private String codigo;
    private String nombre;
    private String nit;
    private String telefonoContacto;
    private boolean cubreSoat;
    private boolean cubreResponsabilidadCivil;
    private boolean activo;

    public Aseguradora() {
        this.cubreSoat = true;
        this.cubreResponsabilidadCivil = true;
        this.activo = true;
    }

    public Aseguradora(Long id, String codigo, String nombre, String nit, String telefonoContacto, boolean activo) {
        this(id, codigo, nombre, nit, telefonoContacto, true, true, activo);
    }

    public Aseguradora(Long id, String codigo, String nombre, String nit, String telefonoContacto,
                       boolean cubreSoat, boolean cubreResponsabilidadCivil, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.nit = nit;
        this.telefonoContacto = telefonoContacto;
        this.cubreSoat = cubreSoat;
        this.cubreResponsabilidadCivil = cubreResponsabilidadCivil;
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

    public boolean isCubreSoat() { return cubreSoat; }
    public void setCubreSoat(boolean cubreSoat) { this.cubreSoat = cubreSoat; }

    public boolean isCubreResponsabilidadCivil() { return cubreResponsabilidadCivil; }
    public void setCubreResponsabilidadCivil(boolean cubreResponsabilidadCivil) { this.cubreResponsabilidadCivil = cubreResponsabilidadCivil; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre + " (" + codigo + ")";
    }
}
