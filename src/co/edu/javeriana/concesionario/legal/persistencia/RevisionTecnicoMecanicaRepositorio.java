package co.edu.javeriana.concesionario.legal.persistencia;

import co.edu.javeriana.concesionario.legal.modelo.RevisionTecnicoMecanica;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoRtm;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface RevisionTecnicoMecanicaRepositorio {
    RevisionTecnicoMecanica guardar(RevisionTecnicoMecanica revision) throws SQLException;

    /** Revisiones del vehículo, de la más reciente a la más antigua. */
    List<RevisionTecnicoMecanica> listarPorVehiculo(Long vehiculoId) throws SQLException;

    Optional<RevisionTecnicoMecanica> buscarPorCertificado(String numeroCertificado) throws SQLException;

    Optional<VehiculoRtm> buscarVehiculo(Long vehiculoId) throws SQLException;

    List<VehiculoRtm> listarVehiculos() throws SQLException;
}
