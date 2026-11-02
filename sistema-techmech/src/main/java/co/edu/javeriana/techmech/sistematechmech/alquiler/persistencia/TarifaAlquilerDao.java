package co.edu.javeriana.techmech.sistematechmech.alquiler.persistencia;

import co.edu.javeriana.techmech.sistematechmech.alquiler.model.TarifaAlquiler;
import java.util.List;
import java.util.Optional;

public interface TarifaAlquilerDao {
    TarifaAlquiler guardar(TarifaAlquiler tarifa);
    Optional<TarifaAlquiler> buscarPorId(Long id);
    Optional<TarifaAlquiler> buscarPorTipoVehiculo(String tipoVehiculo);
    List<TarifaAlquiler> listarActivas();
    boolean actualizar(TarifaAlquiler tarifa);
    boolean eliminarLogico(Long id);
}