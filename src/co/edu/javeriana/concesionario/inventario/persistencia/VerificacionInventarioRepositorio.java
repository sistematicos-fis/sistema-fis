package co.edu.javeriana.concesionario.inventario.persistencia;

import co.edu.javeriana.concesionario.inventario.modelo.ConteoFisico;
import co.edu.javeriana.concesionario.inventario.modelo.EstadoVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.IncidenciaInventario;
import co.edu.javeriana.concesionario.inventario.modelo.VerificacionInventario;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio del proceso de verificación de existencias (CU-10).
 * Define las operaciones sin exponer SQL, de modo que en pruebas se pueda sustituir
 * por otra implementación, igual que {@link BodegaRepositorio} en el CU-07.
 *
 * Responsable: Juan Pablo León
 */
public interface VerificacionInventarioRepositorio {

    // --- Conteos físicos ---

    ConteoFisico registrarConteo(ConteoFisico conteo) throws SQLException;

    /** Conteo más reciente de la unidad, que es el que vale para comparar. */
    Optional<ConteoFisico> ultimoConteo(Long vehiculoId) throws SQLException;

    // --- Incidencias ---

    Optional<IncidenciaInventario> incidenciaAbierta(Long vehiculoId) throws SQLException;

    List<IncidenciaInventario> listarIncidenciasAbiertas() throws SQLException;

    Optional<IncidenciaInventario> buscarIncidencia(Long incidenciaId) throws SQLException;

    /**
     * Cierra la incidencia y deja el vehículo en el estado confirmado por el
     * Administrador de Bodega. Ambos cambios van en una sola transacción (RF-28).
     */
    void resolverIncidencia(Long incidenciaId, EstadoVehiculo estadoConfirmado,
                            String resueltaPor, String observacion) throws SQLException;

    // --- Verificaciones ---

    /**
     * Guarda el resultado de la verificación y, en la misma transacción, la incidencia
     * asociada (si la hay) y el nuevo estado del vehículo (si cambia). RF-28.
     *
     * @param nuevoEstadoVehiculo estado al que pasa la unidad, o null si no cambia.
     */
    VerificacionInventario registrarResultado(VerificacionInventario verificacion,
                                              EstadoVehiculo nuevoEstadoVehiculo) throws SQLException;

    List<VerificacionInventario> listarVerificaciones(Long vehiculoId) throws SQLException;

    // --- Consultas de apoyo ---

    /** true si la unidad tiene un contrato de alquiler RESERVADO o EN_CURSO (flujo alterno 3A). */
    boolean tieneOperacionActiva(Long vehiculoId) throws SQLException;
}
