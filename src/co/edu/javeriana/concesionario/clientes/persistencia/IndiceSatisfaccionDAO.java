package co.edu.javeriana.concesionario.clientes.persistencia;

import co.edu.javeriana.concesionario.clientes.modelo.AlertaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.modelo.EncuestaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.Pqrs;
import co.edu.javeriana.concesionario.clientes.modelo.ResultadoIndiceSatisfaccion;
import co.edu.javeriana.concesionario.comun.bd.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IndiceSatisfaccionDAO {

    public List<Cliente> listarClientesEvaluables(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT c.* FROM clientes c "
                   + "WHERE EXISTS (SELECT 1 FROM encuestas_satisfaccion e "
                   + "              WHERE e.cliente_id = c.id AND e.fecha >= ? AND e.fecha < ?) "
                   + "   OR EXISTS (SELECT 1 FROM pqrs p "
                   + "              WHERE p.cliente_id = c.id AND p.fecha_resolucion >= ? AND p.fecha_resolucion < ?) "
                   + "ORDER BY c.nombres, c.apellidos";
        List<Cliente> clientes = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, inicio(desde));
            stmt.setTimestamp(2, finExclusivo(hasta));
            stmt.setTimestamp(3, inicio(desde));
            stmt.setTimestamp(4, finExclusivo(hasta));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(ClienteDAO.mapearCliente(rs));
                }
            }
        }
        return clientes;
    }

    public Optional<Cliente> buscarClientePorId(Long clienteId) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, clienteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(ClienteDAO.mapearCliente(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<EncuestaSatisfaccion> listarEncuestasPorCliente(Long clienteId, LocalDate desde, LocalDate hasta)
            throws SQLException {
        String sql = "SELECT * FROM encuestas_satisfaccion "
                   + "WHERE cliente_id = ? AND fecha >= ? AND fecha < ? ORDER BY fecha";
        List<EncuestaSatisfaccion> encuestas = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, clienteId);
            stmt.setTimestamp(2, inicio(desde));
            stmt.setTimestamp(3, finExclusivo(hasta));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EncuestaSatisfaccion e = new EncuestaSatisfaccion();
                    e.setId(rs.getLong("id"));
                    e.setClienteId(rs.getLong("cliente_id"));
                    e.setPuntuacionAtencion(rs.getInt("puntuacion_atencion"));
                    e.setPuntuacionProducto(rs.getInt("puntuacion_producto"));
                    e.setPuntuacionTiempos(rs.getInt("puntuacion_tiempos"));
                    e.setComentarios(rs.getString("comentarios"));
                    e.setFecha(aFecha(rs.getTimestamp("fecha")));
                    encuestas.add(e);
                }
            }
        }
        return encuestas;
    }

    public List<Pqrs> listarPqrsResueltasPorCliente(Long clienteId, LocalDate desde, LocalDate hasta)
            throws SQLException {
        String sql = "SELECT * FROM pqrs "
                   + "WHERE cliente_id = ? AND fecha_resolucion IS NOT NULL "
                   + "  AND fecha_resolucion >= ? AND fecha_resolucion < ? ORDER BY fecha_resolucion";
        List<Pqrs> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, clienteId);
            stmt.setTimestamp(2, inicio(desde));
            stmt.setTimestamp(3, finExclusivo(hasta));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Pqrs p = new Pqrs();
                    p.setId(rs.getLong("id"));
                    p.setClienteId(rs.getLong("cliente_id"));
                    p.setTipo(rs.getString("tipo"));
                    p.setDescripcion(rs.getString("descripcion"));
                    p.setEstado(rs.getString("estado"));
                    p.setFechaRadicacion(aFecha(rs.getTimestamp("fecha_radicacion")));
                    p.setFechaResolucion(aFecha(rs.getTimestamp("fecha_resolucion")));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    public int guardarResultados(List<ResultadoIndiceSatisfaccion> resultados, String mensajeAlerta,
                                 LocalDateTime fechaCalculo) throws SQLException {
        String sqlPerfil = "UPDATE clientes SET indice_satisfaccion = ?, nivel_satisfaccion = ?, "
                         + "fecha_indice_satisfaccion = ? WHERE id = ?";
        String sqlAlertaPendiente = "SELECT COUNT(*) FROM alertas_satisfaccion WHERE cliente_id = ? AND atendida = FALSE";
        String sqlAlerta = "INSERT INTO alertas_satisfaccion (cliente_id, asesor_id, indice, mensaje, fecha) "
                         + "VALUES (?, ?, ?, ?, ?)";

        int alertasCreadas = 0;
        try (Connection conn = ConexionBD.obtenerConexion()) {
            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement stmtPerfil = conn.prepareStatement(sqlPerfil);
                 PreparedStatement stmtPendiente = conn.prepareStatement(sqlAlertaPendiente);
                 PreparedStatement stmtAlerta = conn.prepareStatement(sqlAlerta)) {
                for (ResultadoIndiceSatisfaccion r : resultados) {
                    stmtPerfil.setBigDecimal(1, r.getIndice());
                    stmtPerfil.setString(2, r.getCategoria().name());
                    stmtPerfil.setTimestamp(3, Timestamp.valueOf(fechaCalculo));
                    stmtPerfil.setLong(4, r.getClienteId());
                    if (stmtPerfil.executeUpdate() != 1) {
                        throw new SQLException("No se encontró el cliente con id " + r.getClienteId()
                                + " para actualizar su índice de satisfacción.");
                    }

                    if (r.requiereAlerta() && !tieneAlertaPendiente(stmtPendiente, r.getClienteId())) {
                        stmtAlerta.setLong(1, r.getClienteId());
                        if (r.getAsesorId() != null) {
                            stmtAlerta.setLong(2, r.getAsesorId());
                        } else {
                            stmtAlerta.setNull(2, java.sql.Types.BIGINT);
                        }
                        stmtAlerta.setBigDecimal(3, r.getIndice());
                        stmtAlerta.setString(4, mensajeAlerta);
                        stmtAlerta.setTimestamp(5, Timestamp.valueOf(fechaCalculo));
                        stmtAlerta.executeUpdate();
                        alertasCreadas++;
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        }
        return alertasCreadas;
    }

    public List<AlertaSatisfaccion> listarAlertasPendientes() throws SQLException {
        String sql = "SELECT a.*, c.nombres, c.apellidos FROM alertas_satisfaccion a "
                   + "JOIN clientes c ON c.id = a.cliente_id "
                   + "WHERE a.atendida = FALSE ORDER BY a.fecha DESC, a.id DESC";
        List<AlertaSatisfaccion> alertas = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                AlertaSatisfaccion a = new AlertaSatisfaccion();
                a.setId(rs.getLong("id"));
                a.setClienteId(rs.getLong("cliente_id"));
                a.setNombreCliente(rs.getString("nombres") + " " + rs.getString("apellidos"));
                long asesorId = rs.getLong("asesor_id");
                a.setAsesorId(rs.wasNull() ? null : asesorId);
                a.setIndice(rs.getBigDecimal("indice"));
                a.setMensaje(rs.getString("mensaje"));
                a.setFecha(aFecha(rs.getTimestamp("fecha")));
                a.setAtendida(rs.getBoolean("atendida"));
                alertas.add(a);
            }
        }
        return alertas;
    }

    public boolean marcarAlertaAtendida(Long alertaId) throws SQLException {
        String sql = "UPDATE alertas_satisfaccion SET atendida = TRUE WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, alertaId);
            return stmt.executeUpdate() == 1;
        }
    }

    private boolean tieneAlertaPendiente(PreparedStatement stmt, Long clienteId) throws SQLException {
        stmt.setLong(1, clienteId);
        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static Timestamp inicio(LocalDate fecha) {
        return Timestamp.valueOf(fecha.atStartOfDay());
    }

    private static Timestamp finExclusivo(LocalDate fecha) {
        return Timestamp.valueOf(fecha.plusDays(1).atStartOfDay());
    }

    private static LocalDateTime aFecha(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }
}
