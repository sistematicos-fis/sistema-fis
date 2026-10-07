package co.edu.javeriana.concesionario.legal.logica;

import co.edu.javeriana.concesionario.comun.simuladores.SimuladorAseguradora;
import co.edu.javeriana.concesionario.comun.util.ValidadorUtil;
import co.edu.javeriana.concesionario.legal.modelo.PolizaSeguro;
import co.edu.javeriana.concesionario.legal.persistencia.LegalDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio de lógica de negocio para Documentación Legal y Pólizas (CU-31 a CU-35).
 * Invoca el Simulador de Aseguradora (CU-34).
 * 
 * Responsable: Samuel Montaña
 */
public class LegalServicio {

    private final LegalDAO dao;

    public LegalServicio() {
        this.dao = new LegalDAO();
    }

    public LegalServicio(LegalDAO dao) {
        this.dao = dao;
    }

    /**
     * Emite una póliza conectando con el simulador interno de aseguradora (CU-34).
     */
    public PolizaSeguro emitirPolizaConSimulador(Long aseguradoraId, Long vehiculoId, Long clienteId,
                                                String vinVehiculo, BigDecimal valorComercial, String tipoPoliza) throws SQLException {
        ValidadorUtil.validarRequerido(vinVehiculo, "VIN");
        ValidadorUtil.validarMontoPositivo(valorComercial, "Valor Comercial");

        SimuladorAseguradora.CotizacionPolizaSimulada cotizacion =
                SimuladorAseguradora.cotizarYEmitirPoliza(vinVehiculo, valorComercial, tipoPoliza);

        if (!cotizacion.isAprobada()) {
            throw new IllegalArgumentException("No fue posible emitir la póliza con el simulador de aseguradora.");
        }

        PolizaSeguro poliza = new PolizaSeguro(
                null,
                cotizacion.getNumeroPoliza(),
                aseguradoraId,
                vehiculoId,
                clienteId,
                tipoPoliza,
                cotizacion.getValorPrima(),
                cotizacion.getCoberturaTotal(),
                "VIGENTE",
                cotizacion.getCodigoAutorizacion()
        );

        return dao.guardarPoliza(poliza);
    }

    public List<PolizaSeguro> listarPolizas() throws SQLException {
        return dao.listarPolizas();
    }
}
