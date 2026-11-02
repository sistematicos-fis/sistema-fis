package co.edu.javeriana.concesionario.legal.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRevision;
import co.edu.javeriana.concesionario.legal.modelo.RevisionTecnicoMecanica;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoRtm;

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
 * Acceso JDBC a los certificados de revisión técnico-mecánica (CU-25).
 * Del inventario solo lee las columnas del vehículo que necesita para evaluar la revisión.
 */
public class RevisionTecnicoMecanicaDAO implements RevisionTecnicoMecanicaRepositorio {

    private static final String SELECT_VEHICULO =
            "SELECT id, vin, placa, tipo, marca, linea, modelo_anio FROM vehiculos";

    @Override
    public RevisionTecnicoMecanica guardar(RevisionTecnicoMecanica r) throws SQLException {
        String sql = "INSERT INTO revisiones_tecnico_mecanicas (vehiculo_id, numero_certificado, centro_diagnostico, "
                   + "fecha_expedicion, fecha_vencimiento, resultado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, r.getVehiculoId());
            stmt.setString(2, r.getNumeroCertificado());
            stmt.setString(3, r.getCentroDiagnostico());
            stmt.setDate(4, Date.valueOf(r.getFechaExpedicion()));
            stmt.setDate(5, r.getFechaVencimiento() == null ? null : Date.valueOf(r.getFechaVencimiento()));
            stmt.setString(6, r.getResultado().name());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    r.setId(rs.getLong(1));
                }
            }
        }
        return r;
    }

    @Override
    public List<RevisionTecnicoMecanica> listarPorVehiculo(Long vehiculoId) throws SQLException {
        String sql = "SELECT * FROM revisiones_tecnico_mecanicas WHERE vehiculo_id = ? "
                   + "ORDER BY fecha_expedicion DESC, id DESC";
        List<RevisionTecnicoMecanica> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vehiculoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearRevision(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public Optional<RevisionTecnicoMecanica> buscarPorCertificado(String numeroCertificado) throws SQLException {
        String sql = "SELECT * FROM revisiones_tecnico_mecanicas WHERE numero_certificado = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numeroCertificado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearRevision(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<VehiculoRtm> buscarVehiculo(Long vehiculoId) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(SELECT_VEHICULO + " WHERE id = ?")) {
            stmt.setLong(1, vehiculoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearVehiculo(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<VehiculoRtm> listarVehiculos() throws SQLException {
        List<VehiculoRtm> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_VEHICULO + " ORDER BY marca, linea, modelo_anio DESC")) {
            while (rs.next()) {
                lista.add(mapearVehiculo(rs));
            }
        }
        return lista;
    }

    private RevisionTecnicoMecanica mapearRevision(ResultSet rs) throws SQLException {
        RevisionTecnicoMecanica r = new RevisionTecnicoMecanica();
        r.setId(rs.getLong("id"));
        r.setVehiculoId(rs.getLong("vehiculo_id"));
        r.setNumeroCertificado(rs.getString("numero_certificado"));
        r.setCentroDiagnostico(rs.getString("centro_diagnostico"));
        r.setFechaExpedicion(rs.getDate("fecha_expedicion").toLocalDate());
        Date vencimiento = rs.getDate("fecha_vencimiento");
        r.setFechaVencimiento(vencimiento == null ? null : vencimiento.toLocalDate());
        r.setResultado(ResultadoRevision.valueOf(rs.getString("resultado")));
        return r;
    }

    private VehiculoRtm mapearVehiculo(ResultSet rs) throws SQLException {
        return new VehiculoRtm(rs.getLong("id"), rs.getString("vin"), rs.getString("placa"), rs.getString("tipo"),
                rs.getString("marca"), rs.getString("linea"), rs.getInt("modelo_anio"));
    }
}
