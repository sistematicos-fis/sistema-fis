package co.edu.javeriana.concesionario.legal.persistencia;

import co.edu.javeriana.concesionario.comun.bd.ConexionBD;
import co.edu.javeriana.concesionario.legal.modelo.EstadoPoliza;
import co.edu.javeriana.concesionario.legal.modelo.PolizaSeguro;
import co.edu.javeriana.concesionario.legal.modelo.TipoPoliza;
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
 * Acceso JDBC a la tabla polizas_seguro (CU-23). Solo maneja los tipos multirriesgo;
 * las filas antiguas de tipo SOAT de esa tabla se ignoran (el SOAT vive en la tabla soats).
 */
public class PolizaSeguroDAO implements PolizaSeguroRepositorio {
    private static final String FILTRO_TIPOS = "tipo_poliza IN ('TODO_RIESGO_RESPONSABILIDAD', 'EXTRACONTRACTUAL')";

    @Override
    public PolizaSeguro guardar(PolizaSeguro p) throws SQLException {
        String sql = "INSERT INTO polizas_seguro (numero_poliza, aseguradora_id, vehiculo_id, tipo_poliza, valor_prima, "
                + "valor_cobertura, fecha_emision, fecha_vencimiento, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            llenarParametros(stmt, p);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getLong(1));
                }
            }
        }
        return p;
    }

    @Override
    public void actualizar(PolizaSeguro p) throws SQLException {
        String sql = "UPDATE polizas_seguro SET numero_poliza = ?, aseguradora_id = ?, vehiculo_id = ?, tipo_poliza = ?, "
                + "valor_prima = ?, valor_cobertura = ?, fecha_emision = ?, fecha_vencimiento = ?, estado = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            llenarParametros(stmt, p);
            stmt.setLong(10, p.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM polizas_seguro WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<PolizaSeguro> listarTodas() throws SQLException {
        return listar("SELECT * FROM polizas_seguro WHERE " + FILTRO_TIPOS + " ORDER BY fecha_emision DESC, id DESC", null);
    }

    @Override
    public Optional<PolizaSeguro> buscarPorId(Long id) throws SQLException {
        return listar("SELECT * FROM polizas_seguro WHERE id = ? AND " + FILTRO_TIPOS, id).stream().findFirst();
    }

    @Override
    public Optional<PolizaSeguro> buscarPorNumero(String numeroPoliza) throws SQLException {
        // Sin filtrar por tipo: el número de póliza es único en toda la tabla
        return listar("SELECT * FROM polizas_seguro WHERE numero_poliza = ?", numeroPoliza).stream().findFirst();
    }

    @Override
    public List<PolizaSeguro> listarPorVehiculo(Long vehiculoId) throws SQLException {
        return listar("SELECT * FROM polizas_seguro WHERE vehiculo_id = ? AND " + FILTRO_TIPOS
                + " ORDER BY fecha_emision DESC", vehiculoId);
    }

    @Override
    public List<VehiculoLegal> listarVehiculosFlota() throws SQLException {
        return ConsultasVehiculoLegal.listarFlota();
    }

    @Override
    public List<VehiculoLegal> listarVehiculos() throws SQLException {
        return ConsultasVehiculoLegal.listarTodos();
    }

    @Override
    public Optional<VehiculoLegal> buscarVehiculo(Long vehiculoId) throws SQLException {
        return ConsultasVehiculoLegal.buscarPorId(vehiculoId);
    }

    private void llenarParametros(PreparedStatement stmt, PolizaSeguro p) throws SQLException {
        stmt.setString(1, p.getNumeroPoliza());
        stmt.setLong(2, p.getAseguradoraId());
        stmt.setLong(3, p.getVehiculoId());
        stmt.setString(4, p.getTipo().name());
        stmt.setBigDecimal(5, p.getValorPrima());
        stmt.setBigDecimal(6, p.getValorCobertura());
        stmt.setDate(7, Date.valueOf(p.getFechaInicio()));
        stmt.setDate(8, Date.valueOf(p.getFechaVencimiento()));
        stmt.setString(9, p.getEstado() == EstadoPoliza.CANCELADA ? "CANCELADA" : "VIGENTE");
    }

    private List<PolizaSeguro> listar(String sql, Object parametro) throws SQLException {
        List<PolizaSeguro> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (parametro != null) {
                stmt.setObject(1, parametro);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private PolizaSeguro mapear(ResultSet rs) throws SQLException {
        return new PolizaSeguro(rs.getLong("id"), rs.getString("numero_poliza"), rs.getLong("aseguradora_id"),
                rs.getLong("vehiculo_id"),
                tipoDesdeTexto(rs.getString("tipo_poliza")),
                rs.getBigDecimal("valor_prima"), rs.getBigDecimal("valor_cobertura"),
                rs.getDate("fecha_emision").toLocalDate(), rs.getDate("fecha_vencimiento").toLocalDate(),
                "CANCELADA".equals(rs.getString("estado")) ? EstadoPoliza.CANCELADA : EstadoPoliza.VIGENTE);
    }

    /** Las filas antiguas de tipo SOAT no corresponden a ningún tipo del CU-23. */
    private TipoPoliza tipoDesdeTexto(String texto) {
        for (TipoPoliza tipo : TipoPoliza.values()) {
            if (tipo.name().equals(texto)) {
                return tipo;
            }
        }
        return null;
    }
}
