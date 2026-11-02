package co.edu.javeriana.techmech.sistematechmech.alquiler.persistencia;

import co.edu.javeriana.techmech.sistematechmech.alquiler.model.ContratoAlquiler;
import co.edu.javeriana.techmech.sistematechmech.comun.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContratoAlquilerDaoH2 implements ContratoAlquilerDao {

    @Override
    public ContratoAlquiler guardar(ContratoAlquiler c) {
        String sql = "INSERT INTO CONTRATOS_ALQUILER (ID_CLIENTE, ID_VEHICULO, FECHA_INICIO, FECHA_FIN, " +
                     "DIAS_ALQUILER, VALOR_TOTAL, DEPOSITO_GARANTIA, ESTADO, ACTIVO) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, c.getIdCliente());
            ps.setLong(2, c.getIdVehiculo());
            ps.setDate(3, Date.valueOf(c.getFechaInicio()));
            ps.setDate(4, Date.valueOf(c.getFechaFin()));
            ps.setLong(5, c.getDiasAlquiler());
            ps.setBigDecimal(6, c.getValorTotal());
            ps.setBigDecimal(7, c.getDepositoGarantia());
            ps.setString(8, c.getEstado());
            ps.setBoolean(9, c.isActivo());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setIdContrato(rs.getLong(1));
                }
            }
            return c;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar contrato de alquiler en H2: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ContratoAlquiler> buscarPorId(Long id) {
        String sql = "SELECT * FROM CONTRATOS_ALQUILER WHERE ID_CONTRATO = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar contrato: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<ContratoAlquiler> listarPorCliente(Long idCliente) {
        List<ContratoAlquiler> lista = new ArrayList<>();
        String sql = "SELECT * FROM CONTRATOS_ALQUILER WHERE ID_CLIENTE = ? AND ACTIVO = TRUE ORDER BY ID_CONTRATO DESC";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar contratos por cliente: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public List<ContratoAlquiler> listarActivos() {
        List<ContratoAlquiler> lista = new ArrayList<>();
        String sql = "SELECT * FROM CONTRATOS_ALQUILER WHERE ESTADO = 'ACTIVO' AND ACTIVO = TRUE ORDER BY ID_CONTRATO";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar contratos activos: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public boolean actualizarEstado(Long idContrato, String nuevoEstado) {
        String sql = "UPDATE CONTRATOS_ALQUILER SET ESTADO = ? WHERE ID_CONTRATO = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.toUpperCase().trim());
            ps.setLong(2, idContrato);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado del contrato: " + e.getMessage(), e);
        }
    }

    private ContratoAlquiler mapear(ResultSet rs) throws SQLException {
        return new ContratoAlquiler(
            rs.getLong("ID_CONTRATO"),
            rs.getLong("ID_CLIENTE"),
            rs.getLong("ID_VEHICULO"),
            rs.getDate("FECHA_INICIO").toLocalDate(),
            rs.getDate("FECHA_FIN").toLocalDate(),
            rs.getLong("DIAS_ALQUILER"),
            rs.getBigDecimal("VALOR_TOTAL"),
            rs.getBigDecimal("DEPOSITO_GARANTIA"),
            rs.getString("ESTADO"),
            rs.getBoolean("ACTIVO")
        );
    }
}