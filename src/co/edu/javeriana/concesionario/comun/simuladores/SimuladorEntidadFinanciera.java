package co.edu.javeriana.concesionario.comun.simuladores;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Simulador interno para consultas de Scoring y Aprobación Bancaria Externa (CU-15).
 * Cumple con la restricción de arquitectura: Prohibidas conexiones reales a entidades financieras.
 * Responsable de Financiación: Jerónimo Martin
 */
public class SimuladorEntidadFinanciera {

    public static class RespuestaSimulacionBanco {
        private final boolean aprobado;
        private final String codigoAprobacion;
        private final int scoreCalculado;
        private final BigDecimal tasaEaAprobada;
        private final String observacion;

        public RespuestaSimulacionBanco(boolean aprobado, String codigoAprobacion, int scoreCalculado, BigDecimal tasaEaAprobada, String observacion) {
            this.aprobado = aprobado;
            this.codigoAprobacion = codigoAprobacion;
            this.scoreCalculado = scoreCalculado;
            this.tasaEaAprobada = tasaEaAprobada;
            this.observacion = observacion;
        }

        public boolean isAprobado() {
            return aprobado;
        }

        public String getCodigoAprobacion() {
            return codigoAprobacion;
        }

        public int getScoreCalculado() {
            return scoreCalculado;
        }

        public BigDecimal getTasaEaAprobada() {
            return tasaEaAprobada;
        }

        public String getObservacion() {
            return observacion;
        }
    }

    /**
     * Simula la evaluación crediticia de un banco aliado.
     * 
     * @param documentoCliente Documento de identidad del solicitante.
     * @param ingresos          Ingresos mensuales verificados.
     * @param cuotaEstimada     Cuota mensual calculada para el crédito.
     * @param scoreMinimoExigido Scoring mínimo que exige la entidad (ej: 650).
     * @param tasaBase          Tasa base Efectiva Anual pactada con la entidad.
     * @return RespuestaSimulacionBanco con la decisión y código de autorización simulado.
     */
    public static RespuestaSimulacionBanco evaluarCreditoExterno(
            String documentoCliente, 
            BigDecimal ingresos, 
            BigDecimal cuotaEstimada, 
            int scoreMinimoExigido, 
            BigDecimal tasaBase) {

        if (ingresos == null || ingresos.compareTo(BigDecimal.ZERO) <= 0) {
            return new RespuestaSimulacionBanco(false, null, 0, tasaBase, "Rechazo: Ingresos inválidos o cero.");
        }

        // Calcular relación cuota / ingreso (Regla del 30%)
        BigDecimal ratioEndeudamiento = cuotaEstimada.divide(ingresos, 4, RoundingMode.HALF_UP);

        // Algoritmo de simulación determinista basado en el documento del cliente
        int hashDoc = Math.abs(documentoCliente.hashCode()) % 350;
        int scoreBase = 550 + hashDoc; // Score entre 550 y 900

        // Si la cuota supera el 30% del ingreso, se penaliza fuertemente el score simulado
        if (ratioEndeudamiento.compareTo(new BigDecimal("0.30")) > 0) {
            scoreBase -= 150;
        }

        boolean aprobado = (scoreBase >= scoreMinimoExigido) && (ratioEndeudamiento.compareTo(new BigDecimal("0.40")) <= 0);

        if (aprobado) {
            String codigoAprobacion = "BANC-APROB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            return new RespuestaSimulacionBanco(true, codigoAprobacion, scoreBase, tasaBase, "Aprobado por el comité de riesgo simulado.");
        } else {
            String motivo = scoreBase < scoreMinimoExigido 
                    ? "Score insuficiente (" + scoreBase + " < " + scoreMinimoExigido + ")"
                    : "Capacidad de endeudamiento excedida (" + ratioEndeudamiento.multiply(new BigDecimal(100)).setScale(1, RoundingMode.HALF_UP) + "%)";
            return new RespuestaSimulacionBanco(false, null, scoreBase, tasaBase, "Rechazado por política de riesgo: " + motivo);
        }
    }
}
