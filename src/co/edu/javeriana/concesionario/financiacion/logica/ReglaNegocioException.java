package co.edu.javeriana.concesionario.financiacion.logica;

/**
 * Excepción lanzada cuando una operación viola una regla de negocio o política financiera.
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
