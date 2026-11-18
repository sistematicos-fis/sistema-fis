package co.edu.javeriana.concesionario.legal.persistencia;

import co.edu.javeriana.concesionario.legal.modelo.PolizaSeguro;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface PolizaSeguroRepositorio {
    PolizaSeguro guardar(PolizaSeguro poliza) throws SQLException;

    void actualizar(PolizaSeguro poliza) throws SQLException;

    void eliminar(Long id) throws SQLException;

    /** Pólizas multirriesgo (no incluye SOAT), de la más reciente a la más antigua. */
    List<PolizaSeguro> listarTodas() throws SQLException;

    Optional<PolizaSeguro> buscarPorId(Long id) throws SQLException;

    Optional<PolizaSeguro> buscarPorNumero(String numeroPoliza) throws SQLException;

    List<PolizaSeguro> listarPorVehiculo(Long vehiculoId) throws SQLException;

    /** Unidades de la flota (vehículos no vendidos). */
    List<VehiculoLegal> listarVehiculosFlota() throws SQLException;

    List<VehiculoLegal> listarVehiculos() throws SQLException;

    Optional<VehiculoLegal> buscarVehiculo(Long vehiculoId) throws SQLException;
}
