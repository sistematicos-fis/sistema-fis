package co.edu.javeriana.concesionario.comun.simuladores;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Aseguradora simulada (restricción de Servicios Externos del SRS: no hay conexiones reales).
 * Responsable de Legal: Samuel Montaña
 *
 * Las respuestas son fijas para que los flujos alternos del CU-24 se puedan mostrar en la sustentación:
 * - La placa LMP941 (Mazda CX-30 de la semilla) tiene un siniestro pendiente: flujo 4A.
 * - Allianz no expide SOAT para motocicletas: con la moto NKS817 se ve el rechazo del flujo 6A.
 *   Con otra aseguradora la misma moto sí se renueva.
 */
public class SimuladorAseguradora implements AseguradoraExterna {

    private static final Map<String, String> SINIESTROS_PENDIENTES = Map.of(
            "LMP941", "reclamación por siniestro N° SIN-2026-0418 en estudio");

    private static final Map<String, String> BLOQUEOS_ADMINISTRATIVOS = Map.of();

    private static final Map<String, Set<String>> TIPOS_NO_ASEGURABLES = Map.of(
            "ALLIANZ", Set.of("MOTOCICLETA"));

    @Override
    public ConsultaRestricciones consultarRestricciones(String codigoAseguradora, String placa) {
        String siniestro = SINIESTROS_PENDIENTES.get(placa);
        String bloqueo = BLOQUEOS_ADMINISTRATIVOS.get(placa);
        if (siniestro == null && bloqueo == null) {
            return new ConsultaRestricciones(false, false, null);
        }
        String detalle = siniestro != null ? siniestro : bloqueo;
        return new ConsultaRestricciones(siniestro != null, bloqueo != null, detalle);
    }

    @Override
    public RespuestaEmision emitirSoat(SolicitudEmisionSoat solicitud) {
        if (solicitud.valor() == null || solicitud.valor().compareTo(BigDecimal.ZERO) <= 0) {
            return RespuestaEmision.rechazada("el valor del SOAT no es válido");
        }
        Set<String> tiposExcluidos = TIPOS_NO_ASEGURABLES.getOrDefault(solicitud.codigoAseguradora(), Set.of());
        if (tiposExcluidos.contains(solicitud.tipoVehiculo())) {
            return RespuestaEmision.rechazada("la aseguradora no está expidiendo SOAT para vehículos de tipo "
                    + solicitud.tipoVehiculo().toLowerCase() + " (política de suscripción)");
        }
        String numero = "SOAT-" + solicitud.codigoAseguradora() + "-" + aleatorio(10);
        return RespuestaEmision.aprobada(numero, "AUT-" + aleatorio(8));
    }

    private static String aleatorio(int longitud) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, longitud).toUpperCase();
    }
}
