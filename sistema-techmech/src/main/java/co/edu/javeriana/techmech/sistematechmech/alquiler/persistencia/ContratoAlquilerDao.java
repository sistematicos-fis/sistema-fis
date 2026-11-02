package co.edu.javeriana.techmech.sistematechmech.alquiler.persistencia;

import co.edu.javeriana.techmech.sistematechmech.alquiler.model.ContratoAlquiler;
import java.util.List;
import java.util.Optional;

public interface ContratoAlquilerDao {
    ContratoAlquiler guardar(ContratoAlquiler contrato);
    Optional<ContratoAlquiler> buscarPorId(Long id);
    List<ContratoAlquiler> listarPorCliente(Long idCliente);
    List<ContratoAlquiler> listarActivos();
    boolean actualizarEstado(Long idContrato, String nuevoEstado);
}