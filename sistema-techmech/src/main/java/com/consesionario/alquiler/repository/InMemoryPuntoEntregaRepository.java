package com.consesionario.alquiler.repository;

import com.consesionario.alquiler.model.PuntoEntrega;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryPuntoEntregaRepository implements PuntoEntregaRepository {
    private final Map<String, PuntoEntrega> tabla = new HashMap<>();

    @Override
    public PuntoEntrega guardar(PuntoEntrega punto) {
        tabla.put(punto.getId(), punto);
        return punto;
    }

    @Override
    public Optional<PuntoEntrega> buscarPorId(String id) {
        return Optional.ofNullable(tabla.get(id));
    }

    @Override
    public List<PuntoEntrega> listarTodos() {
        return new ArrayList<>(tabla.values());
    }

    @Override
    public List<PuntoEntrega> listarActivos() {
        return tabla.values().stream()
                .filter(PuntoEntrega::isActivo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean eliminarLogico(String id) {
        PuntoEntrega punto = tabla.get(id);
        if (punto != null) {
            punto.setActivo(false);
            return true;
        }
        return false;
    }
}