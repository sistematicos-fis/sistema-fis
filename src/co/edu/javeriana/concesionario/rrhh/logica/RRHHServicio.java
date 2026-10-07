package co.edu.javeriana.concesionario.rrhh.logica;

import co.edu.javeriana.concesionario.comun.util.ValidadorUtil;
import co.edu.javeriana.concesionario.rrhh.modelo.Empleado;
import co.edu.javeriana.concesionario.rrhh.persistencia.EmpleadoDAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de lógica de negocio para Recursos Humanos (CU-26 a CU-30).
 * Incluye cálculo no trivial de liquidación de comisiones comerciales escalonadas (CU-28 - No CRUD).
 * 
 * Responsable: Sergio Alejandro Ramírez
 */
public class RRHHServicio {

    private final EmpleadoDAO dao;

    public RRHHServicio() {
        this.dao = new EmpleadoDAO();
    }

    public RRHHServicio(EmpleadoDAO dao) {
        this.dao = dao;
    }

    public Empleado registrarEmpleado(String doc, String nom, String ape, String cargo, String depto, BigDecimal salario) throws SQLException {
        ValidadorUtil.validarRequerido(doc, "Documento");
        ValidadorUtil.validarRequerido(nom, "Nombres");
        ValidadorUtil.validarMontoPositivo(salario, "Salario Base");

        Empleado e = new Empleado(null, doc, nom, ape, cargo, depto, salario, LocalDate.now(), true);
        return dao.guardar(e);
    }

    /**
     * Liquidación de comisión escalonada por ventas de vehículos y colocación de crédito (CU-28 - No CRUD).
     * Regla:
     * - Hasta $100M: 1.0%
     * - Entre $100M y $250M: 1.5%
     * - Más de $250M: 2.2% + Bono $1.000.000
     */
    public BigDecimal liquidarComisionMensual(BigDecimal ventasTotales) {
        if (ventasTotales == null || ventasTotales.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal comision;
        if (ventasTotales.compareTo(new BigDecimal("100000000")) <= 0) {
            comision = ventasTotales.multiply(new BigDecimal("0.010"));
        } else if (ventasTotales.compareTo(new BigDecimal("250000000")) <= 0) {
            comision = ventasTotales.multiply(new BigDecimal("0.015"));
        } else {
            comision = ventasTotales.multiply(new BigDecimal("0.022")).add(new BigDecimal("1000000"));
        }

        return comision.setScale(2, RoundingMode.HALF_UP);
    }

    public List<Empleado> listarEmpleados() throws SQLException {
        return dao.listarActivos();
    }
}
