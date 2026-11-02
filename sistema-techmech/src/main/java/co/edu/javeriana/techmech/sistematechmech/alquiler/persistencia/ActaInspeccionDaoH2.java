package co.edu.javeriana.techmech.sistematechmech.alquiler.persistencia;

import co.edu.javeriana.techmech.sistematechmech.alquiler.model.ActaInspeccion;
import co.edu.javeriana.techmech.sistematechmech.comun.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActaInspeccionDaoH2 implements ActaInspeccionDao {

    @Override
    public ActaInspeccion guardar(ActaInspeccion acta) {
        String sql = "INSERT INTO ACTAS_INSPECCION (ID_VEHICULO, TIPO_INSPECCION, KILOMETRAJE, NIVEL_COMBUSTIBLE, " +
                     "ESTADO_CARROCERIA, OBSERVACIONES, FECHA_REGISTRO, ACTIVO) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, acta.getIdVehiculo());
            ps.setString(2, acta.getTipoInspeccion().toUpperCase().trim());
            ps.setInt(3, acta.getKilometraje());
            ps.setInt(4, acta.getNivelCombustible());
            ps.setString(5, acta.getEstadoCarroceria().toUpperCase().trim());
            ps.setString(6, acta.getObservaciones());
            ps.setTimestamp(7, Timestamp.valueOf(acta.getFechaRegistro()));
            ps.setBoolean(8, acta.isActivo());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    acta.setIdActa(rs.getLong(1));
                }
            }
            return acta;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar acta de inspección en H2: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ActaInspeccion> buscarPorId(Long id) {
        String sql = "SELECT * FROM ACTAS_INSPECCION WHERE ID_ACTA = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar acta: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<ActaInspeccion> listarPorVehiculo(Long idVehiculo) {
        List<ActaInspeccion> lista = new ArrayList<>();
        String sql = "SELECT * FROM ACTAS_INSPECCION WHERE ID_VEHICULO = ? AND ACTIVO = TRUE ORDER BY FECHA_REGISTRO DESC";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idVehiculo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar actas del vehículo: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public List<ActaInspeccion> listarTodas() {
        List<ActaInspeccion> lista = new ArrayList<>();
        String sql = "SELECT * FROM ACTAS_INSPECCION WHERE ACTIVO = TRUE ORDER BY ID_ACTA";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar todas las actas: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public boolean eliminarLogico(Long id) {
        String sql = "UPDATE ACTAS_INSPECCION SET ACTIVO = FALSE WHERE ID_ACTA = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al deshabilitar acta: " + e.getMessage(), e);
        }
    }

    private ActaInspeccion mapear(ResultSet rs) throws SQLException {
        return new ActaInspeccion(
            rs.getLong("ID_ACTA"),
            rs.getLong("ID_VEHICULO"),
            rs.getString("TIPO_INSPECCION"),
            rs.getInt("KILOMETRAJE"),
            rs.getInt("NIVEL_COMBUSTIBLE"),
            rs.getString("ESTADO_CARROCERIA"),
            rs.getString("OBSERVACIONES"),
            rs.getTimestamp("FECHA_REGISTRO").toLocalDateTime(),
            rs.getBoolean("ACTIVO")
        );
    }
}