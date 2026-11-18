package co.edu.javeriana.concesionario.legal.persistencia;

import co.edu.javeriana.concesionario.legal.modelo.EstadoDocumental;
import co.edu.javeriana.concesionario.legal.modelo.Matricula;
import co.edu.javeriana.concesionario.legal.modelo.RegistroDocumental;
import co.edu.javeriana.concesionario.legal.modelo.Soat;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface SoatRepositorio {
    /** SOAT del vehículo, del que vence más tarde al que vence más temprano. */
    List<Soat> listarPorVehiculo(Long vehiculoId) throws SQLException;

    Optional<Soat> buscarPorNumero(String numeroPoliza) throws SQLException;

    /**
     * Guarda el SOAT y actualiza el estado documental del vehículo en una sola transacción (RF-28):
     * o quedan los dos cambios o ninguno.
     */
    Soat guardarConEstadoDocumental(Soat soat, EstadoDocumental estado, String observacion) throws SQLException;

    void actualizarEstadoDocumental(Long vehiculoId, EstadoDocumental estado, String observacion) throws SQLException;

    Optional<RegistroDocumental> buscarEstadoDocumental(Long vehiculoId) throws SQLException;

    /** La placa se busca en las matrículas (CU-21), que también guardan el cilindraje. */
    Optional<Matricula> buscarMatriculaPorPlaca(String placa) throws SQLException;

    Optional<VehiculoLegal> buscarVehiculo(Long vehiculoId) throws SQLException;
}
