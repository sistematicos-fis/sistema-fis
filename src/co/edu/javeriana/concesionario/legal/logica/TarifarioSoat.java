package co.edu.javeriana.concesionario.legal.logica;

import java.math.BigDecimal;

/**
 * Tarifas del SOAT por tipo de vehículo y cilindraje (CU-24, paso 5).
 *
 * Son valores de referencia simplificados para el proyecto, cercanos a los publicados para 2025;
 * no son la tarifa oficial. La tarifa real la fija cada año la Superintendencia Financiera y
 * distingue más categorías (edad del vehículo, servicio público, carga por toneladas, etc.).
 */
public final class TarifarioSoat {

    private TarifarioSoat() {
    }

    /**
     * @param tipoVehiculo AUTOMOVIL, CAMIONETA, MOTOCICLETA o UTILITARIO
     * @throws ReglaNegocioException si el tipo no tiene tarifa o el cilindraje no es válido
     */
    public static BigDecimal calcularValor(String tipoVehiculo, int cilindrajeCc) {
        if (cilindrajeCc <= 0) {
            throw new ReglaNegocioException("No se pudo calcular el SOAT: el cilindraje registrado en la matrícula no es válido.");
        }
        if (tipoVehiculo == null) {
            throw new ReglaNegocioException("No se pudo calcular el SOAT: el vehículo no tiene tipo registrado.");
        }
        long valor = switch (tipoVehiculo) {
            case "MOTOCICLETA" -> cilindrajeCc < 100 ? 260_000 : cilindrajeCc <= 200 ? 350_000 : 790_000;
            case "AUTOMOVIL" -> cilindrajeCc < 1500 ? 470_000 : cilindrajeCc <= 2500 ? 570_000 : 670_000;
            case "CAMIONETA" -> cilindrajeCc < 1500 ? 820_000 : cilindrajeCc <= 2500 ? 980_000 : 1_150_000;
            case "UTILITARIO" -> cilindrajeCc < 1500 ? 900_000 : cilindrajeCc <= 2500 ? 1_050_000 : 1_200_000;
            default -> throw new ReglaNegocioException("No se pudo calcular el SOAT: no hay tarifa para vehículos de tipo "
                    + tipoVehiculo + ".");
        };
        return BigDecimal.valueOf(valor);
    }
}
