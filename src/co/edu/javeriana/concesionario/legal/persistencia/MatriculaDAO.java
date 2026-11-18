package co.edu.javeriana.concesionario.legal.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.legal.modelo.EstadoMatricula;
import co.edu.javeriana.concesionario.legal.modelo.Matricula;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso JDBC a la tabla matriculas (CU-21).
 */
public class MatriculaDAO implements MatriculaRepositorio {

    @Override
    public Matricula guardar(Matricula m) throws SQLException {
        String sqlMatricula = "INSERT INTO matriculas (vehiculo_id, placa, numero_licencia_transito, organismo_transito, "
                + "fecha_matricula, cilindraje_cc, estado_registro, ultimo_anio_impuesto_pagado, valor_impuesto_anual) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlPlaca = "UPDATE vehiculos SET placa = ? WHERE id = ? AND placa IS NULL";
        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sqlMatricula, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement stmtPlaca = conn.prepareStatement(sqlPlaca)) {
                llenarParametros(stmt, m);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        m.setId(rs.getLong(1));
                    }
                }
                stmtPlaca.setString(1, m.getPlaca());
                stmtPlaca.setLong(2, m.getVehiculoId());
                stmtPlaca.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                m.setId(null);
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return m;
    }

    @Override
    public void actualizar(Matricula m) throws SQLException {
        String sql = "UPDATE matriculas SET vehiculo_id = ?, placa = ?, numero_licencia_transito = ?, organismo_transito = ?, "
                + "fecha_matricula = ?, cilindraje_cc = ?, estado_registro = ?, ultimo_anio_impuesto_pagado = ?, "
                + "valor_impuesto_anual = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            llenarParametros(stmt, m);
            stmt.setLong(10, m.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM matriculas WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Matricula> listarTodas() throws SQLException {
        List<Matricula> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM matriculas ORDER BY placa")) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public Optional<Matricula> buscarPorId(Long id) throws SQLException {
        return buscarUno("SELECT * FROM matriculas WHERE id = ?", id);
    }

    @Override
    public Optional<Matricula> buscarPorVehiculo(Long vehiculoId) throws SQLException {
        return buscarUno("SELECT * FROM matriculas WHERE vehiculo_id = ?", vehiculoId);
    }

    @Override
    public Optional<Matricula> buscarPorPlaca(String placa) throws SQLException {
        return buscarUno("SELECT * FROM matriculas WHERE placa = ?", placa);
    }

    @Override
    public Optional<Matricula> buscarPorLicencia(String numeroLicencia) throws SQLException {
        return buscarUno("SELECT * FROM matriculas WHERE numero_licencia_transito = ?", numeroLicencia);
    }

    @Override
    public List<VehiculoLegal> listarVehiculos() throws SQLException {
        return ConsultasVehiculoLegal.listarTodos();
    }

    @Override
    public List<VehiculoLegal> listarVehiculosSinMatricula() throws SQLException {
        return ConsultasVehiculoLegal.listarSinMatricula();
    }

    @Override
    public Optional<VehiculoLegal> buscarVehiculo(Long vehiculoId) throws SQLException {
        return ConsultasVehiculoLegal.buscarPorId(vehiculoId);
    }

    @Override
    public Optional<VehiculoLegal> buscarVehiculoPorPlaca(String placa) throws SQLException {
        return ConsultasVehiculoLegal.buscarPorPlaca(placa);
    }

    private void llenarParametros(PreparedStatement stmt, Matricula m) throws SQLException {
        stmt.setLong(1, m.getVehiculoId());
        stmt.setString(2, m.getPlaca());
        stmt.setString(3, m.getNumeroLicenciaTransito());
        stmt.setString(4, m.getOrganismoTransito());
        stmt.setDate(5, Date.valueOf(m.getFechaMatricula()));
        stmt.setInt(6, m.getCilindrajeCc());
        stmt.setString(7, m.getEstadoRegistro().name());
        if (m.getUltimoAnioImpuestoPagado() == null) {
            stmt.setNull(8, Types.INTEGER);
        } else {
            stmt.setInt(8, m.getUltimoAnioImpuestoPagado());
        }
        stmt.setBigDecimal(9, m.getValorImpuestoAnual());
    }

    private Optional<Matricula> buscarUno(String sql, Object parametro) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, parametro);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    private Matricula mapear(ResultSet rs) throws SQLException {
        int ultimoAnio = rs.getInt("ultimo_anio_impuesto_pagado");
        Integer ultimoAnioPagado = rs.wasNull() ? null : ultimoAnio;
        BigDecimal valorImpuesto = rs.getBigDecimal("valor_impuesto_anual");
        return new Matricula(rs.getLong("id"), rs.getLong("vehiculo_id"), rs.getString("placa"),
                rs.getString("numero_licencia_transito"), rs.getString("organismo_transito"),
                rs.getDate("fecha_matricula").toLocalDate(), rs.getInt("cilindraje_cc"),
                EstadoMatricula.valueOf(rs.getString("estado_registro")), ultimoAnioPagado, valorImpuesto);
    }
}
