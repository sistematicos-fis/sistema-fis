package co.edu.javeriana.concesionario.rrhh.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.rrhh.modelo.Empleado;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestión de empleados en H2 (CU-26).
 * Responsable: Sergio Alejandro Ramírez
 */
public class EmpleadoDAO {

    public Empleado guardar(Empleado emp) throws SQLException {
        String sql = "INSERT INTO empleados (documento, nombres, apellidos, cargo, departamento, salario_base, fecha_ingreso, activo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, emp.getDocumento());
            stmt.setString(2, emp.getNombres());
            stmt.setString(3, emp.getApellidos());
            stmt.setString(4, emp.getCargo());
            stmt.setString(5, emp.getDepartamento());
            stmt.setBigDecimal(6, emp.getSalarioBase());
            stmt.setDate(7, Date.valueOf(emp.getFechaIngreso()));
            stmt.setBoolean(8, emp.isActivo());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    emp.setId(rs.getLong(1));
                }
            }
        }
        return emp;
    }

    public List<Empleado> listarActivos() throws SQLException {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM empleados WHERE activo = TRUE ORDER BY nombres, apellidos";
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Empleado e = new Empleado();
                e.setId(rs.getLong("id"));
                e.setDocumento(rs.getString("documento"));
                e.setNombres(rs.getString("nombres"));
                e.setApellidos(rs.getString("apellidos"));
                e.setCargo(rs.getString("cargo"));
                e.setDepartamento(rs.getString("departamento"));
                e.setSalarioBase(rs.getBigDecimal("salario_base"));
                Date d = rs.getDate("fecha_ingreso");
                if (d != null) e.setFechaIngreso(d.toLocalDate());
                e.setActivo(rs.getBoolean("activo"));
                lista.add(e);
            }
        }
        return lista;
    }
}
