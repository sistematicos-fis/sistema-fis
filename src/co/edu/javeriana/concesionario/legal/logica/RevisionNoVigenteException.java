package co.edu.javeriana.concesionario.legal.logica;

import co.edu.javeriana.concesionario.legal.modelo.ResultadoVerificacionRtm;

/**
 * Se lanza cuando se intenta vender o alquilar un vehículo sin revisión técnico-mecánica vigente.
 * Los módulos de venta y alquiler pueden capturarla para mostrar getMessage() al usuario.
 */
public class RevisionNoVigenteException extends ReglaNegocioException {
    private final ResultadoVerificacionRtm verificacion;

    public RevisionNoVigenteException(ResultadoVerificacionRtm verificacion) {
        super("Operación no permitida: " + verificacion.motivo());
        this.verificacion = verificacion;
    }

    public ResultadoVerificacionRtm getVerificacion() {
        return verificacion;
    }
}
