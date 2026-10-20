package co.edu.javeriana.concesionario.financiacion.logica;

/**
 * Se lanza cuando un dato no cumple una regla del CU-11. Su mensaje se muestra tal cual al usuario.
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
