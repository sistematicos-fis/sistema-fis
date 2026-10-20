package co.edu.javeriana.concesionario.legal.logica;

/**
 * Se lanza cuando una operación del módulo legal incumple una regla de negocio.
 * El mensaje está redactado para mostrarse tal cual al usuario.
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
