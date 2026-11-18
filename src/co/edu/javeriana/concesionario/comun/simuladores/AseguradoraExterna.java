package co.edu.javeriana.concesionario.comun.simuladores;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Servicios que el concesionario consume de una aseguradora. En el proyecto no hay conexión real
 * (restricción de Servicios Externos del SRS): la implementación es {@link SimuladorAseguradora}.
 * Al ser una interfaz, las pruebas pueden reemplazar la aseguradora por una respuesta controlada.
 */
public interface AseguradoraExterna {

    /** CU-24, paso 4: siniestros pendientes y bloqueos administrativos del vehículo ante la aseguradora. */
    ConsultaRestricciones consultarRestricciones(String codigoAseguradora, String placa);

    /** CU-24, paso 6: solicitud de emisión del SOAT. */
    RespuestaEmision emitirSoat(SolicitudEmisionSoat solicitud);

    record ConsultaRestricciones(boolean siniestrosPendientes, boolean bloqueoAdministrativo, String detalle) {
        public boolean tieneRestricciones() {
            return siniestrosPendientes || bloqueoAdministrativo;
        }
    }

    record SolicitudEmisionSoat(String codigoAseguradora, String placa, String tipoVehiculo, int cilindrajeCc,
                                LocalDate fechaInicio, LocalDate fechaVencimiento, BigDecimal valor) {
    }

    record RespuestaEmision(boolean aprobada, String numeroPoliza, String codigoAutorizacion, String motivoRechazo) {
        public static RespuestaEmision aprobada(String numeroPoliza, String codigoAutorizacion) {
            return new RespuestaEmision(true, numeroPoliza, codigoAutorizacion, null);
        }

        public static RespuestaEmision rechazada(String motivo) {
            return new RespuestaEmision(false, null, null, motivo);
        }
    }
}
