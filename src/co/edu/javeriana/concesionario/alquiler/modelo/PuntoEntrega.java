package co.edu.javeriana.concesionario.alquiler.modelo;

/**
 * Entidad que representa una sede o punto de entrega y devolución de vehículos.
 * Responsable: Samuel Borras (CU-16)
 */
public class PuntoEntrega {
    private String id;
    private String nombre;
    private String direccion;
    private String ciudad;
    private boolean activo;

    public PuntoEntrega() {
    }

    public PuntoEntrega(String id, String nombre, String direccion, String ciudad, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.activo = activo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre + " (" + ciudad + ")";
    }
}
