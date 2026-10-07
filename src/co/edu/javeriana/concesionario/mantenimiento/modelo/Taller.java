package co.edu.javeriana.concesionario.mantenimiento.modelo;

/**
 * Entidad que representa un taller o centro de servicio técnico (CU-22).
 */
public class Taller {
    private Long id;
    private String codigo;
    private String nombre;
    private String direccion;
    private int capacidadBahias;
    private boolean activo;

    public Taller() {
        this.activo = true;
    }

    public Taller(Long id, String codigo, String nombre, String direccion, int capacidadBahias, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidadBahias = capacidadBahias;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public int getCapacidadBahias() { return capacidadBahias; }
    public void setCapacidadBahias(int capacidadBahias) { this.capacidadBahias = capacidadBahias; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
