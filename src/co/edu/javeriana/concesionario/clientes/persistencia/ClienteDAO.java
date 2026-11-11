package co.edu.javeriana.concesionario.clientes.persistencia;

import co.edu.javeriana.concesionario.clientes.modelo.CategoriaComercial;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.comun.bd.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO para gestión y consulta de Clientes en H2 con JDBC (CU-01).
 * Responsable: Andrés Alquerque
 */
public class ClienteDAO {

    public Cliente guardar(Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes (documento, tipo_documento, nombres, apellidos, email, telefono, " +
                     "ingresos_mensuales, categoria_comercial) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, c.getDocumento());
            stmt.setString(2, c.getTipoDocumento());
            stmt.setString(3, c.getNombres());
            stmt.setString(4, c.getApellidos());
            stmt.setString(5, c.getEmail());
            stmt.setString(6, c.getTelefono());
            stmt.setBigDecimal(7, c.getIngresosMensuales());
            stmt.setString(8, c.getCategoriaComercial() != null ? c.getCategoriaComercial().name() : CategoriaComercial.ESTANDAR.name());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getLong(1));
                }
            }
        }
        return c;
    }

    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY nombres, apellidos";
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearCliente(rs));
            }
        }
        return lista;
    }

    public Optional<Cliente> buscarPorDocumento(String documento) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE documento = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, documento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCliente(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Cliente> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCliente(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE clientes SET documento = ?, tipo_documento = ?, nombres = ?, apellidos = ?, email = ?, " +
                     "telefono = ?, ingresos_mensuales = ?, categoria_comercial = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getDocumento());
            stmt.setString(2, c.getTipoDocumento());
            stmt.setString(3, c.getNombres());
            stmt.setString(4, c.getApellidos());
            stmt.setString(5, c.getEmail());
            stmt.setString(6, c.getTelefono());
            stmt.setBigDecimal(7, c.getIngresosMensuales());
            stmt.setString(8, c.getCategoriaComercial().name());
            stmt.setLong(9, c.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM clientes WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public Map<String, Integer> contarRegistrosAsociados(Long id) throws SQLException {
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM pqrs WHERE cliente_id = ?) AS pqrs, " +
                     "(SELECT COUNT(*) FROM encuestas_satisfaccion WHERE cliente_id = ?) AS encuestas, " +
                     "(SELECT COUNT(*) FROM alertas_satisfaccion WHERE cliente_id = ?) AS alertas, " +
                     "(SELECT COUNT(*) FROM contratos_alquiler WHERE cliente_id = ?) AS alquileres, " +
                     "(SELECT COUNT(*) FROM polizas_seguro WHERE cliente_id = ?) AS polizas, " +
                     "(SELECT COUNT(*) FROM actas_traspaso WHERE comprador_id = ?) AS traspasos";
        Map<String, Integer> conteo = new LinkedHashMap<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 6; i++) {
                stmt.setLong(i, id);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    conteo.put("PQRS", rs.getInt("pqrs"));
                    conteo.put("encuesta(s) de satisfacción", rs.getInt("encuestas"));
                    conteo.put("alerta(s) de satisfacción", rs.getInt("alertas"));
                    conteo.put("contrato(s) de alquiler", rs.getInt("alquileres"));
                    conteo.put("póliza(s) de seguro", rs.getInt("polizas"));
                    conteo.put("acta(s) de traspaso", rs.getInt("traspasos"));
                }
            }
        }
        return conteo;
    }

    static Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getLong("id"));
        c.setDocumento(rs.getString("documento"));
        c.setTipoDocumento(rs.getString("tipo_documento"));
        c.setNombres(rs.getString("nombres"));
        c.setApellidos(rs.getString("apellidos"));
        c.setEmail(rs.getString("email"));
        c.setTelefono(rs.getString("telefono"));
        c.setIngresosMensuales(rs.getBigDecimal("ingresos_mensuales"));
        String cat = rs.getString("categoria_comercial");
        if (cat != null) {
            try {
                c.setCategoriaComercial(CategoriaComercial.valueOf(cat));
            } catch (Exception ex) {
                c.setCategoriaComercial(CategoriaComercial.ESTANDAR);
            }
        }

        long asesorId = rs.getLong("asesor_id");
        c.setAsesorId(rs.wasNull() ? null : asesorId);
        c.setIndiceSatisfaccion(rs.getBigDecimal("indice_satisfaccion"));
        String nivel = rs.getString("nivel_satisfaccion");
        if (nivel != null) {
            try {
                c.setNivelSatisfaccion(CategoriaSatisfaccion.valueOf(nivel));
            } catch (IllegalArgumentException ex) {
                c.setNivelSatisfaccion(null);
            }
        }
        Timestamp fechaIndice = rs.getTimestamp("fecha_indice_satisfaccion");
        c.setFechaIndiceSatisfaccion(fechaIndice != null ? fechaIndice.toLocalDateTime() : null);
        return c;
    }
}
