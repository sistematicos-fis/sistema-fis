package co.edu.javeriana.concesionario.legal.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lecturas de la tabla vehiculos (del área de Inventario) que comparten los DAO de matrículas,
 * pólizas y SOAT. Solo consulta: el módulo legal no modifica el inventario, salvo la placa al matricular.
 */
final class ConsultasVehiculoLegal {
    private static final String SELECT = "SELECT id, vin, placa, tipo, marca, linea, modelo_anio, estado FROM vehiculos";
    private static final String ORDEN = " ORDER BY marca, linea, modelo_anio DESC";

    private ConsultasVehiculoLegal() {
    }

    static List<VehiculoLegal> listarTodos() throws SQLException {
        return listar(SELECT + ORDEN);
    }

    /** Vehículos sin matrícula registrada (para el CU-21). */
    static List<VehiculoLegal> listarSinMatricula() throws SQLException {
        return listar(SELECT + " v WHERE NOT EXISTS (SELECT 1 FROM matriculas m WHERE m.vehiculo_id = v.id)" + ORDEN);
    }

    /** Unidades que siguen en la flota (no vendidas), a las que se les puede asociar una póliza (CU-23). */
    static List<VehiculoLegal> listarFlota() throws SQLException {
        return listar(SELECT + " WHERE estado <> 'VENDIDO'" + ORDEN);
    }

    static Optional<VehiculoLegal> buscarPorId(Long id) throws SQLException {
        return buscarUno(SELECT + " WHERE id = ?", id);
    }

    static Optional<VehiculoLegal> buscarPorPlaca(String placa) throws SQLException {
        return buscarUno(SELECT + " WHERE placa = ?", placa);
    }

    private static Optional<VehiculoLegal> buscarUno(String sql, Object parametro) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, parametro);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    private static List<VehiculoLegal> listar(String sql) throws SQLException {
        List<VehiculoLegal> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private static VehiculoLegal mapear(ResultSet rs) throws SQLException {
        return new VehiculoLegal(rs.getLong("id"), rs.getString("vin"), rs.getString("placa"), rs.getString("tipo"),
                rs.getString("marca"), rs.getString("linea"), rs.getInt("modelo_anio"), rs.getString("estado"));
    }
}
