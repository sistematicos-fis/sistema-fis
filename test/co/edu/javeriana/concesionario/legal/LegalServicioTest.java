package co.edu.javeriana.concesionario.legal;

import co.edu.javeriana.concesionario.comun.simuladores.SimuladorAseguradora;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class LegalServicioTest {

    @Test
    public void testSimuladorAseguradoraCotizarPolizaTodoRiesgo() {
        BigDecimal valorComercial = new BigDecimal("120000000.00");
        SimuladorAseguradora.CotizacionPolizaSimulada cot =
                SimuladorAseguradora.cotizarYEmitirPoliza("VIN999", valorComercial, "TODO_RIESGO_RESPONSABILIDAD");

        assertTrue(cot.isAprobada());
        assertNotNull(cot.getNumeroPoliza());
        assertNotNull(cot.getCodigoAutorizacion());
        assertTrue(cot.getValorPrima().compareTo(BigDecimal.ZERO) > 0);
        // Cobertura total = 120% del valor comercial = 144M
        assertEquals(new BigDecimal("144000000.00"), cot.getCoberturaTotal());
    }
}
