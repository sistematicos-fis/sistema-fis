package co.edu.javeriana.concesionario.alquiler.persistencia;

import co.edu.javeriana.concesionario.alquiler.modelo.PuntoEntrega;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryPuntoEntregaRepository implements PuntoEntregaRepository {
    private final Map<String, PuntoEntrega> storage = Collections.synchronizedMap(new LinkedHashMap<>());

    @Override
    public PuntoEntrega guardar(PuntoEntrega punto) {
        if (punto == null || punto.getId() == null) {
            throw new IllegalArgumentException("El punto de entrega y su ID no pueden ser nulos.");
        }
        storage.put(punto.getId(), punto);
        return punto;
    }

    @Override
    public Optional<PuntoEntrega> buscarPorId(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<PuntoEntrega> listarTodos() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<PuntoEntrega> listarActivos() {
        return storage.values().stream()
                .filter(PuntoEntrega::isActivo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean eliminarLogico(String id) {
        PuntoEntrega punto = storage.get(id);
        if (punto != null && punto.isActivo()) {
            punto.setActivo(false);
            return true;
        }
        return false;
    }
}
