package co.edu.javeriana.techmech.sistematechmech.alquiler.service;

import co.edu.javeriana.techmech.sistematechmech.alquiler.model.ContratoAlquiler;
import co.edu.javeriana.techmech.sistematechmech.alquiler.model.TarifaAlquiler;
import co.edu.javeriana.techmech.sistematechmech.alquiler.persistencia.ContratoAlquilerDao;
import co.edu.javeriana.techmech.sistematechmech.alquiler.persistencia.TarifaAlquilerDao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RealizarAlquilerService {

    private final ContratoAlquilerDao contratoDao;
    private final TarifaAlquilerDao tarifaDao;

    public RealizarAlquilerService(ContratoAlquilerDao contratoDao, TarifaAlquilerDao tarifaDao) {
        this.contratoDao = contratoDao;
        this.tarifaDao = tarifaDao;
    }

    public ContratoAlquiler procesarAlquiler(Long idCliente, Long idVehiculo, String tipoVehiculo, 
                                            LocalDate fechaInicio, LocalDate fechaFin) {
        if (idCliente == null || idCliente <= 0) {
            throw new IllegalArgumentException("El cliente debe ser válido.");
        }
        if (idVehiculo == null || idVehiculo <= 0) {
            throw new IllegalArgumentException("El vehículo debe ser válido.");
        }
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la de inicio.");
        }

        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        if (dias <= 0) {
            dias = 1; // Mínimo 1 día de alquiler
        }

        // Obtener tarifa configurada para la categoría del vehículo
        TarifaAlquiler tarifa = tarifaDao.buscarPorTipoVehiculo(tipoVehiculo)
            .orElseThrow(() -> new IllegalStateException("No existe tarifa configurada para el tipo: " + tipoVehiculo));

        BigDecimal total = tarifa.getTarifaDiaria().multiply(BigDecimal.valueOf(dias));

        ContratoAlquiler contrato = new ContratoAlquiler(
            null,
            idCliente,
            idVehiculo,
            fechaInicio,
            fechaFin,
            dias,
            total,
            tarifa.getDepositoGarantia(),
            "ACTIVO",
            true
        );

        return contratoDao.guardar(contrato);
    }
}