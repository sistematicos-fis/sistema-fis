package co.edu.javeriana.concesionario.mantenimiento.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.mantenimiento.modelo.OrdenMantenimiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestión de órdenes de trabajo en el taller en H2 (CU-21).
 * Responsable: Jaime Andrés Molina
 */
public class MantenimientoDAO {

    public OrdenMantenimiento crearOrden(OrdenMantenimiento orden) throws SQLException {
        String sql = "INSERT INTO ordenes_mantenimiento (numero_orden, vehiculo_id, taller_id, tipo, costo_total, estado, diagnostico) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, orden.getNumeroOrden());
            stmt.setLong(2, orden.getVehiculoId());
            stmt.setLong(3, orden.getTallerId());
            stmt.setString(4, orden.getTipo());
            stmt.setBigDecimal(5, orden.getCostoTotal());
            stmt.setString(6, orden.getEstado());
            stmt.setString(7, orden.getDiagnostico());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    orden.setId(rs.getLong(1));
                }
            }
        }
        return orden;
    }

    public List<OrdenMantenimiento> listarOrdenes() throws SQLException {
        List<OrdenMantenimiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM ordenes_mantenimiento ORDER BY fecha_ingreso DESC";
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                OrdenMantenimiento o = new OrdenMantenimiento();
                o.setId(rs.getLong("id"));
                o.setNumeroOrden(rs.getString("numero_orden"));
                o.setVehiculoId(rs.getLong("vehiculo_id"));
                o.setTallerId(rs.getLong("taller_id"));
                o.setTipo(rs.getString("tipo"));
                o.setCostoTotal(rs.getBigDecimal("costo_total"));
                o.setEstado(rs.getString("estado"));
                o.setDiagnostico(rs.getString("diagnostico"));
                java.sql.Timestamp ts = rs.getTimestamp("fecha_ingreso");
                if (ts != null) o.setFechaIngreso(ts.toLocalDateTime());
                lista.add(o);
            }
        }
        return lista;
    }
}
