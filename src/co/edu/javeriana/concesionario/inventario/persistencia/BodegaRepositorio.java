package co.edu.javeriana.concesionario.inventario.persistencia;

import co.edu.javeriana.concesionario.inventario.modelo.Bodega;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de bodegas y patios de almacenamiento (CU-07).
 * Define el CRUD y las consultas de la sede sin exponer SQL, de modo que en pruebas
 * se pueda sustituir por otra implementación.
 *
 * Responsable: Juan Pablo León
 */
public interface BodegaRepositorio {

    Bodega guardar(Bodega bodega) throws SQLException;

    void actualizar(Bodega bodega) throws SQLException;

    void eliminar(Long id) throws SQLException;

    List<Bodega> listarTodas() throws SQLException;

    List<Bodega> listarActivas() throws SQLException;

    Optional<Bodega> buscarPorId(Long id) throws SQLException;

    Optional<Bodega> buscarPorCodigo(String codigo) throws SQLException;

    /**
     * Número de vehículos actualmente asignados a la sede.
     * La ocupación no se almacena como columna: se calcula contra la tabla vehiculos.
     */
    int contarVehiculos(Long bodegaId) throws SQLException;
}
