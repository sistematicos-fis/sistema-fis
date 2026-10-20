package co.edu.javeriana.concesionario.financiacion.persistencia;

import co.edu.javeriana.concesionario.financiacion.modelo.EntidadFinanciera;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Operaciones de datos sobre la tabla entidades_financieras. El servicio depende de esta
 * interfaz y no de la implementación JDBC, para poder cambiarla o probarla por separado.
 */
public interface EntidadFinancieraRepositorio {
    /** Inserta la entidad y le asigna el id generado por la base de datos. */
    EntidadFinanciera guardar(EntidadFinanciera entidad) throws SQLException;

    /** Actualiza todos los campos de la entidad con el id dado. */
    void actualizar(EntidadFinanciera entidad) throws SQLException;

    /** Borra la fila; falla si otra tabla la referencia. */
    void eliminar(Long id) throws SQLException;

    /** Todas las entidades ordenadas por nombre. */
    List<EntidadFinanciera> listarTodas() throws SQLException;

    /** Solo las entidades activas, que son las que puede usar un crédito. */
    List<EntidadFinanciera> listarActivas() throws SQLException;

    Optional<EntidadFinanciera> buscarPorId(Long id) throws SQLException;

    Optional<EntidadFinanciera> buscarPorCodigo(String codigo) throws SQLException;

    Optional<EntidadFinanciera> buscarPorNit(String nit) throws SQLException;
}
