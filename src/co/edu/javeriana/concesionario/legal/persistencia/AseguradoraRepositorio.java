package co.edu.javeriana.concesionario.legal.persistencia;

import co.edu.javeriana.concesionario.legal.modelo.Aseguradora;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AseguradoraRepositorio {
    Aseguradora guardar(Aseguradora aseguradora) throws SQLException;

    void actualizar(Aseguradora aseguradora) throws SQLException;

    void eliminar(Long id) throws SQLException;

    List<Aseguradora> listarTodas() throws SQLException;

    List<Aseguradora> listarActivas() throws SQLException;

    Optional<Aseguradora> buscarPorId(Long id) throws SQLException;

    Optional<Aseguradora> buscarPorCodigo(String codigo) throws SQLException;

    Optional<Aseguradora> buscarPorNit(String nit) throws SQLException;
}
