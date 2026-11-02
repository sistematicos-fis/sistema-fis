package co.edu.javeriana.techmech.sistematechmech.alquiler.persistencia;

import co.edu.javeriana.techmech.sistematechmech.alquiler.model.ActaInspeccion;
import java.util.List;
import java.util.Optional;

public interface ActaInspeccionDao {
    ActaInspeccion guardar(ActaInspeccion acta);
    Optional<ActaInspeccion> buscarPorId(Long id);
    List<ActaInspeccion> listarPorVehiculo(Long idVehiculo);
    List<ActaInspeccion> listarTodas();
    boolean eliminarLogico(Long id);
}