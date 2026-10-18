package co.edu.javeriana.concesionario.financiacion.persistencia;

import co.edu.javeriana.concesionario.financiacion.modelo.EntidadFinanciera;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EntidadFinancieraRepositorio {
    EntidadFinanciera guardar(EntidadFinanciera entidad) throws SQLException;

    void actualizar(EntidadFinanciera entidad) throws SQLException;

    void eliminar(Long id) throws SQLException;

    List<EntidadFinanciera> listarTodas() throws SQLException;

    List<EntidadFinanciera> listarActivas() throws SQLException;

    Optional<EntidadFinanciera> buscarPorId(Long id) throws SQLException;

    Optional<EntidadFinanciera> buscarPorCodigo(String codigo) throws SQLException;

    Optional<EntidadFinanciera> buscarPorNit(String nit) throws SQLException;
}
