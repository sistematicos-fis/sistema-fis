package co.edu.javeriana.concesionario.alquiler.persistencia;

import co.edu.javeriana.concesionario.alquiler.modelo.PuntoEntrega;
import java.util.List;
import java.util.Optional;

public interface PuntoEntregaRepository {
    PuntoEntrega guardar(PuntoEntrega punto);
    Optional<PuntoEntrega> buscarPorId(String id);
    List<PuntoEntrega> listarTodos();
    List<PuntoEntrega> listarActivos();
    boolean eliminarLogico(String id);
}
