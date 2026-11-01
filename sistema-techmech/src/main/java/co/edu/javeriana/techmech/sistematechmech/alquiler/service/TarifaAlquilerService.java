package co.edu.javeriana.techmech.sistematechmech.alquiler.service;

import co.edu.javeriana.techmech.sistematechmech.alquiler.model.TarifaAlquiler;
import co.edu.javeriana.techmech.sistematechmech.alquiler.persistencia.TarifaAlquilerDao;

import java.math.BigDecimal;
import java.util.List;

public class TarifaAlquilerService {

    private final TarifaAlquilerDao dao;

    public TarifaAlquilerService(TarifaAlquilerDao dao) {
        this.dao = dao;
    }

    public TarifaAlquiler registrarTarifa(String tipoVehiculo, BigDecimal tarifaDiaria, 
                                         BigDecimal depositoGarantia, String descripcion) {
        // Validaciones de presencia
        if (tipoVehiculo == null || tipoVehiculo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de vehículo es obligatorio.");
        }
        if (tarifaDiaria == null || tarifaDiaria.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La tarifa diaria debe ser mayor a 0.");
        }
        if (depositoGarantia == null || depositoGarantia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El depósito de garantía no puede ser negativo.");
        }

        // Regla de negocio: unicidad por categoría activa
        if (dao.buscarPorTipoVehiculo(tipoVehiculo).isPresent()) {
            throw new IllegalStateException("Ya existe una tarifa activa para la categoría: " + tipoVehiculo);
        }

        TarifaAlquiler nueva = new TarifaAlquiler(
            null,
            tipoVehiculo.trim().toUpperCase(),
            tarifaDiaria,
            depositoGarantia,
            descripcion != null ? descripcion.trim() : "",
            true
        );
        return dao.guardar(nueva);
    }

    public List<TarifaAlquiler> listarTarifasActivas() {
        return dao.listarActivas();
    }

    public boolean deshabilitarTarifa(Long idTarifa) {
        if (idTarifa == null || idTarifa <= 0) {
            throw new IllegalArgumentException("Identificador de tarifa inválido.");
        }
        return dao.eliminarLogico(idTarifa);
    }
}