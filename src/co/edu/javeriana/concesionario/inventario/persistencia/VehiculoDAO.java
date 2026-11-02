package co.edu.javeriana.concesionario.inventario.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.inventario.modelo.CategoriaVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para gestión del catálogo de Vehículos y Motocicletas en H2 con JDBC (CU-06, CU-09).
 * Responsable: Juan Pablo León
 */
public class VehiculoDAO {

    public Vehiculo guardar(Vehiculo v) throws SQLException {
        String sql = "INSERT INTO vehiculos (vin, placa, tipo, marca, modelo_anio, linea, color, " +
                     "precio_venta, estado, kilometraje, bodega_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, v.getVin());
            stmt.setString(2, v.getPlaca());
            stmt.setString(3, v.getTipo() != null ? v.getTipo().name() : CategoriaVehiculo.AUTOMOVIL.name());
            stmt.setString(4, v.getMarca());
            stmt.setInt(5, v.getModeloAnio());
            stmt.setString(6, v.getLinea());
            stmt.setString(7, v.getColor());
            stmt.setBigDecimal(8, v.getPrecioVenta());
            stmt.setString(9, v.getEstado());
            stmt.setInt(10, v.getKilometraje());
            if (v.getBodegaId() != null) {
                stmt.setLong(11, v.getBodegaId());
            } else {
                stmt.setNull(11, java.sql.Types.BIGINT);
            }
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    v.setId(rs.getLong(1));
                }
            }
        }
        return v;
    }

    public List<Vehiculo> listarDisponibles() throws SQLException {
        List<Vehiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM vehiculos WHERE estado = 'DISPONIBLE' ORDER BY marca, modelo_anio DESC";
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        }
        return lista;
    }

    public Optional<Vehiculo> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM vehiculos WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Vehiculo mapearResultSet(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo();
        v.setId(rs.getLong("id"));
        v.setVin(rs.getString("vin"));
        v.setPlaca(rs.getString("placa"));
        String t = rs.getString("tipo");
        if (t != null) {
            try {
                v.setTipo(CategoriaVehiculo.valueOf(t));
            } catch (Exception ex) {
                v.setTipo(CategoriaVehiculo.AUTOMOVIL);
            }
        }
        v.setMarca(rs.getString("marca"));
        v.setModeloAnio(rs.getInt("modelo_anio"));
        v.setLinea(rs.getString("linea"));
        v.setColor(rs.getString("color"));
        v.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        v.setEstado(rs.getString("estado"));
        v.setKilometraje(rs.getInt("kilometraje"));
        long bod = rs.getLong("bodega_id");
        if (!rs.wasNull()) {
            v.setBodegaId(bod);
        }
        return v;
    }
}
