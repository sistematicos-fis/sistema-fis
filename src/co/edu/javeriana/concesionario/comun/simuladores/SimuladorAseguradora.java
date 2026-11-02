package co.edu.javeriana.concesionario.comun.simuladores;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Simulador interno para cotización y emisión de pólizas de seguro (CU-34).
 * Cumple con la restricción de arquitectura: Prohibidas conexiones reales a aseguradoras.
 * Responsable de Legal: Samuel Montaña
 */
public class SimuladorAseguradora {

    public static class CotizacionPolizaSimulada {
        private final boolean aprobada;
        private final String numeroPoliza;
        private final String codigoAutorizacion;
        private final BigDecimal valorPrima;
        private final BigDecimal coberturaTotal;

        public CotizacionPolizaSimulada(boolean aprobada, String numeroPoliza, String codigoAutorizacion, BigDecimal valorPrima, BigDecimal coberturaTotal) {
            this.aprobada = aprobada;
            this.numeroPoliza = numeroPoliza;
            this.codigoAutorizacion = codigoAutorizacion;
            this.valorPrima = valorPrima;
            this.coberturaTotal = coberturaTotal;
        }

        public boolean isAprobada() {
            return aprobada;
        }

        public String getNumeroPoliza() {
            return numeroPoliza;
        }

        public String getCodigoAutorizacion() {
            return codigoAutorizacion;
        }

        public BigDecimal getValorPrima() {
            return valorPrima;
        }

        public BigDecimal getCoberturaTotal() {
            return coberturaTotal;
        }
    }

    /**
     * Simula la emisión de una póliza Todo Riesgo o SOAT para un vehículo.
     */
    public static CotizacionPolizaSimulada cotizarYEmitirPoliza(String vinVehiculo, BigDecimal valorComercial, String tipoPoliza) {
        if (valorComercial == null || valorComercial.compareTo(BigDecimal.ZERO) <= 0) {
            return new CotizacionPolizaSimulada(false, null, null, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // Tasa estimada de prima anual según tipo de póliza
        BigDecimal tasaPrima = "SOAT".equalsIgnoreCase(tipoPoliza) ? new BigDecimal("0.008") : new BigDecimal("0.035");
        BigDecimal prima = valorComercial.multiply(tasaPrima).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cobertura = valorComercial.multiply(new BigDecimal("1.2")).setScale(2, RoundingMode.HALF_UP);

        String numPoliza = "POL-" + System.currentTimeMillis() % 100000000;
        String authCode = "SEG-AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new CotizacionPolizaSimulada(true, numPoliza, authCode, prima, cobertura);
    }
}
