package com.consesionario.alquiler.service;

import com.consesionario.alquiler.model.PuntoEntrega;
import com.consesionario.alquiler.repository.PuntoEntregaRepository;
import java.util.List;
import java.util.Optional;

public class PuntoEntregaService {
    private final PuntoEntregaRepository repository;

    public PuntoEntregaService(PuntoEntregaRepository repository) {
        this.repository = repository;
    }

    public PuntoEntrega registrarPunto(String id, String nombre, String direccion, String ciudad) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del punto de entrega es obligatorio.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del punto de entrega es obligatorio.");
        }
        if (repository.buscarPorId(id).isPresent()) {
            throw new IllegalArgumentException("Ya existe un punto de entrega con el ID: " + id);
        }

        PuntoEntrega nuevoPunto = new PuntoEntrega(id.trim(), nombre.trim(), direccion != null ? direccion.trim() : "", ciudad != null ? ciudad.trim() : "", true);
        return repository.guardar(nuevoPunto);
    }

    public Optional<PuntoEntrega> consultarPunto(String id) {
        return repository.buscarPorId(id);
    }

    public List<PuntoEntrega> listarPuntosActivos() {
        return repository.listarActivos();
    }

    public boolean deshabilitarPunto(String id) {
        return repository.eliminarLogico(id);
    }
}