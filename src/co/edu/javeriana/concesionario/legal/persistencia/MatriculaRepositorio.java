package co.edu.javeriana.concesionario.legal.persistencia;

import co.edu.javeriana.concesionario.legal.modelo.Matricula;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface MatriculaRepositorio {
    /** Guarda la matrícula y, en la misma transacción, asigna la placa al vehículo si aún no la tiene (RF-28). */
    Matricula guardar(Matricula matricula) throws SQLException;

    void actualizar(Matricula matricula) throws SQLException;

    void eliminar(Long id) throws SQLException;

    List<Matricula> listarTodas() throws SQLException;

    Optional<Matricula> buscarPorId(Long id) throws SQLException;

    Optional<Matricula> buscarPorVehiculo(Long vehiculoId) throws SQLException;

    Optional<Matricula> buscarPorPlaca(String placa) throws SQLException;

    Optional<Matricula> buscarPorLicencia(String numeroLicencia) throws SQLException;

    List<VehiculoLegal> listarVehiculos() throws SQLException;

    List<VehiculoLegal> listarVehiculosSinMatricula() throws SQLException;

    Optional<VehiculoLegal> buscarVehiculo(Long vehiculoId) throws SQLException;

    /** Vehículo del inventario que ya tiene esa placa, si lo hay. */
    Optional<VehiculoLegal> buscarVehiculoPorPlaca(String placa) throws SQLException;
}
