package co.edu.javeriana.techmech.sistematechmech.alquiler.service;

import co.edu.javeriana.techmech.sistematechmech.alquiler.model.ActaInspeccion;
import co.edu.javeriana.techmech.sistematechmech.alquiler.persistencia.ActaInspeccionDao;

import java.time.LocalDateTime;
import java.util.List;

public class ActaInspeccionService {

    private final ActaInspeccionDao dao;

    public ActaInspeccionService(ActaInspeccionDao dao) {
        this.dao = dao;
    }

    public ActaInspeccion registrarActa(Long idVehiculo, String tipoInspeccion, 
                                        int kilometraje, int nivelCombustible, 
                                        String estadoCarroceria, String observaciones) {
        if (idVehiculo == null || idVehiculo <= 0) {
            throw new IllegalArgumentException("El identificador del vehículo es obligatorio.");
        }
        if (tipoInspeccion == null || (!tipoInspeccion.equalsIgnoreCase("ENTREGA") && !tipoInspeccion.equalsIgnoreCase("DEVOLUCION"))) {
            throw new IllegalArgumentException("El tipo de inspección debe ser 'ENTREGA' o 'DEVOLUCION'.");
        }
        if (kilometraje < 0) {
            throw new IllegalArgumentException("El kilometraje no puede ser negativo.");
        }
        if (nivelCombustible < 0 || nivelCombustible > 100) {
            throw new IllegalArgumentException("El nivel de combustible debe estar entre 0% y 100%.");
        }

        ActaInspeccion nueva = new ActaInspeccion(
            null,
            idVehiculo,
            tipoInspeccion.toUpperCase().trim(),
            kilometraje,
            nivelCombustible,
            estadoCarroceria != null ? estadoCarroceria.toUpperCase().trim() : "BUENO",
            observaciones != null ? observaciones.trim() : "",
            LocalDateTime.now(),
            true
        );

        return dao.guardar(nueva);
    }

    public List<ActaInspeccion> consultarPorVehiculo(Long idVehiculo) {
        if (idVehiculo == null || idVehiculo <= 0) {
            throw new IllegalArgumentException("Vehículo inválido.");
        }
        return dao.listarPorVehiculo(idVehiculo);
    }

    public boolean deshabilitarActa(Long idActa) {
        if (idActa == null || idActa <= 0) {
            throw new IllegalArgumentException("Identificador de acta inválido.");
        }
        return dao.eliminarLogico(idActa);
    }
}