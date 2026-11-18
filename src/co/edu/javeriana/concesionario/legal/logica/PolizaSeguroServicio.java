package co.edu.javeriana.concesionario.legal.logica;

import co.edu.javeriana.concesionario.legal.modelo.Aseguradora;
import co.edu.javeriana.concesionario.legal.modelo.EstadoPoliza;
import co.edu.javeriana.concesionario.legal.modelo.PolizaSeguro;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;
import co.edu.javeriana.concesionario.legal.persistencia.AseguradoraRepositorio;
import co.edu.javeriana.concesionario.legal.persistencia.PolizaSeguroRepositorio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * CU-23: gestionar pólizas de seguro (CRUD).
 * Asocia pólizas multirriesgo (todo riesgo o responsabilidad civil extracontractual) a las unidades
 * de la flota de alquiler.
 *
 * Reglas:
 * - La póliza se asocia a un vehículo de la flota (no vendido) y a una aseguradora activa cuyo
 *   convenio cubra daños materiales a terceros (CU-22).
 * - La prima y el valor asegurado son positivos y el valor asegurado es mayor que la prima.
 * - La vigencia termina después de empezar y dura como máximo un año.
 * - Un vehículo no puede tener dos pólizas no canceladas del mismo tipo con vigencias que se crucen.
 * - Mientras un vehículo está alquilado no se puede cancelar ni eliminar la póliza que lo cubre hoy.
 */
public class PolizaSeguroServicio {
    private static final Pattern PATRON_NUMERO = Pattern.compile("^[A-Z0-9-]{4,50}$");

    private final PolizaSeguroRepositorio repositorio;
    private final AseguradoraRepositorio aseguradoras;
    private final Clock reloj;

    public PolizaSeguroServicio(PolizaSeguroRepositorio repositorio, AseguradoraRepositorio aseguradoras) {
        this(repositorio, aseguradoras, Clock.systemDefaultZone());
    }

    public PolizaSeguroServicio(PolizaSeguroRepositorio repositorio, AseguradoraRepositorio aseguradoras, Clock reloj) {
        this.repositorio = repositorio;
        this.aseguradoras = aseguradoras;
        this.reloj = reloj;
    }

    public List<PolizaSeguro> listarPolizas() throws SQLException {
        return repositorio.listarTodas();
    }

    public Optional<PolizaSeguro> buscarPoliza(Long id) throws SQLException {
        return repositorio.buscarPorId(id);
    }

    public List<VehiculoLegal> listarVehiculosFlota() throws SQLException {
        return repositorio.listarVehiculosFlota();
    }

    /** Todos los vehículos, para mostrar en la tabla los de pólizas antiguas aunque ya se hayan vendido. */
    public List<VehiculoLegal> listarVehiculos() throws SQLException {
        return repositorio.listarVehiculos();
    }

    public List<Aseguradora> listarAseguradoras() throws SQLException {
        return aseguradoras.listarTodas();
    }

    /** Aseguradoras con las que se pueden emitir pólizas nuevas: activas y con cobertura de daños a terceros. */
    public List<Aseguradora> listarAseguradorasDisponibles() throws SQLException {
        return aseguradoras.listarActivas().stream().filter(Aseguradora::isCubreResponsabilidadCivil).toList();
    }

    public EstadoPoliza estadoEfectivo(PolizaSeguro poliza) {
        return poliza.estadoEfectivo(hoy());
    }

    public PolizaSeguro registrarPoliza(PolizaSeguro p) throws SQLException {
        validarDatos(p);
        validarAseguradora(p.getAseguradoraId());
        VehiculoLegal vehiculo = validarVehiculo(p.getVehiculoId());
        if (p.getEstado() == null) {
            p.setEstado(EstadoPoliza.VIGENTE);
        }
        validarNumeroUnico(p);
        validarSinCruce(p, vehiculo);
        return repositorio.guardar(p);
    }

    public void actualizarPoliza(PolizaSeguro p) throws SQLException {
        PolizaSeguro actual = p.getId() == null ? null : repositorio.buscarPorId(p.getId()).orElse(null);
        if (actual == null) {
            throw new ReglaNegocioException("No se pudo actualizar: la póliza seleccionada no existe. Seleccione una póliza de la tabla.");
        }
        validarDatos(p);
        // Si la aseguradora o el vehículo no cambian, se respeta la póliza ya emitida aunque la
        // aseguradora se haya desactivado o el vehículo se haya vendido después.
        if (!actual.getAseguradoraId().equals(p.getAseguradoraId())) {
            validarAseguradora(p.getAseguradoraId());
        }
        VehiculoLegal vehiculo = actual.getVehiculoId().equals(p.getVehiculoId())
                ? buscarVehiculo(p.getVehiculoId())
                : validarVehiculo(p.getVehiculoId());
        if (p.getEstado() == null) {
            p.setEstado(actual.getEstado());
        }
        if (p.estaCancelada() && !actual.estaCancelada()) {
            exigirQueNoDejeSinSeguro(actual, "cancelar");
        }
        validarNumeroUnico(p);
        if (!p.estaCancelada()) {
            validarSinCruce(p, vehiculo);
        }
        repositorio.actualizar(p);
    }

    public void eliminarPoliza(Long id) throws SQLException {
        PolizaSeguro actual = id == null ? null : repositorio.buscarPorId(id).orElse(null);
        if (actual == null) {
            throw new ReglaNegocioException("No se pudo eliminar: la póliza seleccionada no existe.");
        }
        exigirQueNoDejeSinSeguro(actual, "eliminar");
        repositorio.eliminar(id);
    }

    private void validarDatos(PolizaSeguro p) {
        String prefijo = "No se pudo guardar la póliza: ";
        if (p.getNumeroPoliza() == null || p.getNumeroPoliza().trim().isEmpty()) {
            throw new ReglaNegocioException(prefijo + "el N° de póliza es obligatorio.");
        }
        p.setNumeroPoliza(p.getNumeroPoliza().trim().toUpperCase());
        if (!PATRON_NUMERO.matcher(p.getNumeroPoliza()).matches()) {
            throw new ReglaNegocioException(prefijo + "el N° de póliza debe tener entre 4 y 50 letras, números o guiones.");
        }
        if (p.getAseguradoraId() == null) {
            throw new ReglaNegocioException(prefijo + "seleccione la Aseguradora.");
        }
        if (p.getVehiculoId() == null) {
            throw new ReglaNegocioException(prefijo + "seleccione el Vehículo de la flota.");
        }
        if (p.getTipo() == null) {
            throw new ReglaNegocioException(prefijo + "seleccione el Tipo de póliza.");
        }
        if (p.getValorPrima() == null || p.getValorPrima().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException(prefijo + "la Prima anual debe ser mayor que cero.");
        }
        if (p.getValorCobertura() == null || p.getValorCobertura().compareTo(p.getValorPrima()) <= 0) {
            throw new ReglaNegocioException(prefijo + "el Valor asegurado debe ser mayor que la prima.");
        }
        if (p.getFechaInicio() == null || p.getFechaVencimiento() == null) {
            throw new ReglaNegocioException(prefijo + "las fechas de Inicio y Fin de vigencia son obligatorias.");
        }
        if (!p.getFechaVencimiento().isAfter(p.getFechaInicio())) {
            throw new ReglaNegocioException(prefijo + "el Fin de vigencia debe ser posterior al Inicio.");
        }
        if (p.getFechaVencimiento().isAfter(p.getFechaInicio().plusYears(1))) {
            throw new ReglaNegocioException(prefijo + "la vigencia de la póliza no puede superar un año.");
        }
    }

    private void validarAseguradora(Long aseguradoraId) throws SQLException {
        Aseguradora aseguradora = aseguradoras.buscarPorId(aseguradoraId)
                .orElseThrow(() -> new ReglaNegocioException("No se pudo guardar la póliza: la aseguradora seleccionada no existe."));
        if (!aseguradora.isActivo()) {
            throw new ReglaNegocioException("No se pudo guardar la póliza: la aseguradora " + aseguradora.getNombre()
                    + " está inactiva. Actívela en Aseguradoras (CU-22) o seleccione otra.");
        }
        if (!aseguradora.isCubreResponsabilidadCivil()) {
            throw new ReglaNegocioException("No se pudo guardar la póliza: el convenio con " + aseguradora.getNombre()
                    + " no cubre pólizas de daños materiales a terceros.");
        }
    }

    private VehiculoLegal validarVehiculo(Long vehiculoId) throws SQLException {
        VehiculoLegal vehiculo = buscarVehiculo(vehiculoId);
        if (!vehiculo.perteneceAFlota()) {
            throw new ReglaNegocioException("No se pudo guardar la póliza: el vehículo " + vehiculo
                    + " ya fue vendido y no hace parte de la flota de alquiler.");
        }
        return vehiculo;
    }

    private VehiculoLegal buscarVehiculo(Long vehiculoId) throws SQLException {
        return repositorio.buscarVehiculo(vehiculoId)
                .orElseThrow(() -> new ReglaNegocioException("No se pudo guardar la póliza: el vehículo seleccionado no existe."));
    }

    private void validarNumeroUnico(PolizaSeguro p) throws SQLException {
        Optional<PolizaSeguro> mismoNumero = repositorio.buscarPorNumero(p.getNumeroPoliza());
        if (mismoNumero.isPresent() && !mismoNumero.get().getId().equals(p.getId())) {
            throw new ReglaNegocioException("No se pudo guardar la póliza: ya existe una póliza con el número " + p.getNumeroPoliza() + ".");
        }
    }

    private void validarSinCruce(PolizaSeguro p, VehiculoLegal vehiculo) throws SQLException {
        for (PolizaSeguro otra : repositorio.listarPorVehiculo(p.getVehiculoId())) {
            boolean esLaMisma = otra.getId().equals(p.getId());
            if (!esLaMisma && !otra.estaCancelada() && otra.getTipo() == p.getTipo()
                    && otra.seCruzaCon(p.getFechaInicio(), p.getFechaVencimiento())) {
                throw new ReglaNegocioException("No se pudo guardar la póliza: el vehículo " + vehiculo + " ya tiene la póliza "
                        + otra.getNumeroPoliza() + " de tipo " + p.getTipo() + " en esas fechas. "
                        + "Ajuste la vigencia o cancele la póliza anterior.");
            }
        }
    }

    private void exigirQueNoDejeSinSeguro(PolizaSeguro poliza, String accion) throws SQLException {
        if (!poliza.cubre(hoy())) {
            return;
        }
        Optional<VehiculoLegal> vehiculo = repositorio.buscarVehiculo(poliza.getVehiculoId());
        if (vehiculo.isPresent() && vehiculo.get().estaAlquilado()) {
            throw new ReglaNegocioException("No se pudo " + accion + " la póliza " + poliza.getNumeroPoliza()
                    + ": el vehículo " + vehiculo.get() + " está alquilado y quedaría sin seguro. "
                    + "Espere a la devolución del vehículo.");
        }
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj);
    }
}
