package co.edu.javeriana.concesionario.mantenimiento.logica;

import co.edu.javeriana.concesionario.comun.util.ValidadorUtil;
import co.edu.javeriana.concesionario.mantenimiento.modelo.OrdenMantenimiento;
import co.edu.javeriana.concesionario.mantenimiento.persistencia.MantenimientoDAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio de lógica de negocio para Mantenimiento (CU-21 a CU-25).
 * Incluye cálculo no trivial de costo total por horas de mano de obra e insumos con IVA (CU-24).
 * 
 * Responsable: Jaime Andrés Molina
 */
public class MantenimientoServicio {

    private final MantenimientoDAO dao;
    private static final BigDecimal COSTO_HORA_MO = new BigDecimal("85000.00");
    private static final BigDecimal IVA = new BigDecimal("0.19");

    public MantenimientoServicio() {
        this.dao = new MantenimientoDAO();
    }

    public MantenimientoServicio(MantenimientoDAO dao) {
        this.dao = dao;
    }

    public OrdenMantenimiento crearOrden(Long vehiculoId, Long tallerId, String tipo, String diagnostico, int horasEstimadas, BigDecimal costoRepuestos) throws SQLException {
        ValidadorUtil.validarRequerido(tipo, "Tipo de Mantenimiento");
        ValidadorUtil.validarRequerido(diagnostico, "Diagnóstico");

        BigDecimal total = calcularCostoReparacion(horasEstimadas, costoRepuestos);
        String numOrden = "OT-" + System.currentTimeMillis() % 10000000;

        OrdenMantenimiento orden = new OrdenMantenimiento(null, numOrden, vehiculoId, tallerId, tipo, total, "EN_DIAGNOSTICO", diagnostico);
        return dao.crearOrden(orden);
    }

    /**
     * Cálculo de liquidación de taller: (Horas * Tarifa + Repuestos) * (1 + IVA) (CU-24 - No CRUD).
     */
    public BigDecimal calcularCostoReparacion(int horas, BigDecimal repuestos) {
        BigDecimal subtotalMO = COSTO_HORA_MO.multiply(BigDecimal.valueOf(Math.max(1, horas)));
        BigDecimal subtotalRep = repuestos != null ? repuestos : BigDecimal.ZERO;
        BigDecimal subtotal = subtotalMO.add(subtotalRep);
        return subtotal.multiply(BigDecimal.ONE.add(IVA)).setScale(2, RoundingMode.HALF_UP);
    }

    public List<OrdenMantenimiento> listarOrdenes() throws SQLException {
        return dao.listarOrdenes();
    }
}
