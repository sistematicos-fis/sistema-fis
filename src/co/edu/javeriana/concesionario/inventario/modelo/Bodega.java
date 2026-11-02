package co.edu.javeriana.concesionario.inventario.modelo;

/**
 * Entidad que representa una bodega o patio de inventario (CU-07).
 */
public class Bodega {
    private Long id;
    private String codigo;
    private String nombre;
    private String ciudad;
    private int capacidadVehiculos;
    private boolean activo;

    public Bodega() {
        this.activo = true;
    }

    public Bodega(Long id, String codigo, String nombre, String ciudad, int capacidadVehiculos, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.capacidadVehiculos = capacidadVehiculos;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public int getCapacidadVehiculos() { return capacidadVehiculos; }
    public void setCapacidadVehiculos(int capacidadVehiculos) { this.capacidadVehiculos = capacidadVehiculos; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre + " (" + ciudad + ")";
    }
}
