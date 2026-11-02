package co.edu.javeriana.techmech.sistematechmech.alquiler.persistencia;

import co.edu.javeriana.techmech.sistematechmech.alquiler.model.TarifaAlquiler;
// Si ConexionBD está en otra subcarpeta de sistematechmech, ajusta este import:
import co.edu.javeriana.techmech.sistematechmech.comun.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TarifaAlquilerDaoH2 implements TarifaAlquilerDao {

    @Override
    public TarifaAlquiler guardar(TarifaAlquiler t) {
        String sql = "INSERT INTO TARIFAS_ALQUILER (TIPO_VEHICULO, TARIFA_DIARIA, DEPOSITO_GARANTIA, DESCRIPCION, ACTIVO) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, t.getTipoVehiculo().toUpperCase().trim());
            ps.setBigDecimal(2, t.getTarifaDiaria());
            ps.setBigDecimal(3, t.getDepositoGarantia());
            ps.setString(4, t.getDescripcion());
            ps.setBoolean(5, t.isActivo());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    t.setIdTarifa(rs.getLong(1));
                }
            }
            return t;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar tarifa en H2: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<TarifaAlquiler> buscarPorId(Long id) {
        String sql = "SELECT * FROM TARIFAS_ALQUILER WHERE ID_TARIFA = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar tarifa: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<TarifaAlquiler> buscarPorTipoVehiculo(String tipoVehiculo) {
        String sql = "SELECT * FROM TARIFAS_ALQUILER WHERE UPPER(TIPO_VEHICULO) = ? AND ACTIVO = TRUE";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipoVehiculo.toUpperCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar tarifa por tipo: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<TarifaAlquiler> listarActivas() {
        List<TarifaAlquiler> lista = new ArrayList<>();
        String sql = "SELECT * FROM TARIFAS_ALQUILER WHERE ACTIVO = TRUE ORDER BY ID_TARIFA";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar tarifas activas: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public boolean actualizar(TarifaAlquiler t) {
        String sql = "UPDATE TARIFAS_ALQUILER SET TARIFA_DIARIA = ?, DEPOSITO_GARANTIA = ?, DESCRIPCION = ? " +
                     "WHERE ID_TARIFA = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, t.getTarifaDiaria());
            ps.setBigDecimal(2, t.getDepositoGarantia());
            ps.setString(3, t.getDescripcion());
            ps.setLong(4, t.getIdTarifa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar tarifa: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminarLogico(Long id) {
        String sql = "UPDATE TARIFAS_ALQUILER SET ACTIVO = FALSE WHERE ID_TARIFA = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al deshabilitar tarifa: " + e.getMessage(), e);
        }
    }

    private TarifaAlquiler mapear(ResultSet rs) throws SQLException {
        return new TarifaAlquiler(
            rs.getLong("ID_TARIFA"),
            rs.getString("TIPO_VEHICULO"),
            rs.getBigDecimal("TARIFA_DIARIA"),
            rs.getBigDecimal("DEPOSITO_GARANTIA"),
            rs.getString("DESCRIPCION"),
            rs.getBoolean("ACTIVO")
        );
    }
}