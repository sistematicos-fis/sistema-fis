package co.edu.javeriana.concesionario.legal.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.legal.modelo.EstadoDocumental;
import co.edu.javeriana.concesionario.legal.modelo.Matricula;
import co.edu.javeriana.concesionario.legal.modelo.RegistroDocumental;
import co.edu.javeriana.concesionario.legal.modelo.Soat;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso JDBC a las tablas soats y documentacion_vehiculos (CU-24).
 */
public class SoatDAO implements SoatRepositorio {

    private static final String SQL_ESTADO_DOCUMENTAL =
            "MERGE INTO documentacion_vehiculos (vehiculo_id, estado_documental, observacion, fecha_actualizacion) "
            + "KEY(vehiculo_id) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

    private final MatriculaDAO matriculaDAO = new MatriculaDAO();

    @Override
    public List<Soat> listarPorVehiculo(Long vehiculoId) throws SQLException {
        String sql = "SELECT * FROM soats WHERE vehiculo_id = ? ORDER BY fecha_vencimiento DESC, id DESC";
        List<Soat> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vehiculoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public Optional<Soat> buscarPorNumero(String numeroPoliza) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM soats WHERE numero_poliza = ?")) {
            stmt.setString(1, numeroPoliza);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Soat guardarConEstadoDocumental(Soat s, EstadoDocumental estado, String observacion) throws SQLException {
        String sql = "INSERT INTO soats (numero_poliza, vehiculo_id, aseguradora_id, fecha_inicio, fecha_vencimiento, "
                + "valor_pagado, codigo_autorizacion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement stmtEstado = conn.prepareStatement(SQL_ESTADO_DOCUMENTAL)) {
                stmt.setString(1, s.getNumeroPoliza());
                stmt.setLong(2, s.getVehiculoId());
                stmt.setLong(3, s.getAseguradoraId());
                stmt.setDate(4, Date.valueOf(s.getFechaInicio()));
                stmt.setDate(5, Date.valueOf(s.getFechaVencimiento()));
                stmt.setBigDecimal(6, s.getValorPagado());
                stmt.setString(7, s.getCodigoAutorizacion());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        s.setId(rs.getLong(1));
                    }
                }
                llenarEstado(stmtEstado, s.getVehiculoId(), estado, observacion);
                stmtEstado.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                s.setId(null);
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return s;
    }

    @Override
    public void actualizarEstadoDocumental(Long vehiculoId, EstadoDocumental estado, String observacion) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(SQL_ESTADO_DOCUMENTAL)) {
            llenarEstado(stmt, vehiculoId, estado, observacion);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<RegistroDocumental> buscarEstadoDocumental(Long vehiculoId) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM documentacion_vehiculos WHERE vehiculo_id = ?")) {
            stmt.setLong(1, vehiculoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new RegistroDocumental(rs.getLong("vehiculo_id"),
                            EstadoDocumental.valueOf(rs.getString("estado_documental")), rs.getString("observacion"),
                            rs.getTimestamp("fecha_actualizacion").toLocalDateTime()));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Matricula> buscarMatriculaPorPlaca(String placa) throws SQLException {
        return matriculaDAO.buscarPorPlaca(placa);
    }

    @Override
    public Optional<VehiculoLegal> buscarVehiculo(Long vehiculoId) throws SQLException {
        return ConsultasVehiculoLegal.buscarPorId(vehiculoId);
    }

    private void llenarEstado(PreparedStatement stmt, Long vehiculoId, EstadoDocumental estado, String observacion)
            throws SQLException {
        stmt.setLong(1, vehiculoId);
        stmt.setString(2, estado.name());
        stmt.setString(3, observacion == null || observacion.length() <= 255 ? observacion : observacion.substring(0, 255));
    }

    private Soat mapear(ResultSet rs) throws SQLException {
        return new Soat(rs.getLong("id"), rs.getString("numero_poliza"), rs.getLong("vehiculo_id"),
                rs.getLong("aseguradora_id"), rs.getDate("fecha_inicio").toLocalDate(),
                rs.getDate("fecha_vencimiento").toLocalDate(), rs.getBigDecimal("valor_pagado"),
                rs.getString("codigo_autorizacion"));
    }
}
