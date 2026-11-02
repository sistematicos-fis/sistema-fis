package co.edu.javeriana.concesionario.inventario.logica;

import co.edu.javeriana.concesionario.comun.util.ValidadorUtil;
import co.edu.javeriana.concesionario.financiacion.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.inventario.modelo.Bodega;
import co.edu.javeriana.concesionario.inventario.modelo.CategoriaVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.Vehiculo;
import co.edu.javeriana.concesionario.inventario.persistencia.BodegaDAO;
import co.edu.javeriana.concesionario.inventario.persistencia.BodegaRepositorio;
import co.edu.javeriana.concesionario.inventario.persistencia.VehiculoDAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de lógica de negocio para Inventario (CU-06 a CU-10).
 * Incluye la gestión de bodegas y patios de almacenamiento (CU-07) y el cálculo
 * de depreciación anual comercial (CU-10 - No CRUD).
 *
 * Responsable: Juan Pablo León
 */
public class InventarioServicio {

    static final int LONGITUD_MAX_CODIGO = 20;
    static final int LONGITUD_MAX_NOMBRE = 80;
    static final int LONGITUD_MAX_CIUDAD = 50;
    static final int CAPACIDAD_MINIMA = 1;

    /** Código de error de H2 cuando un DELETE viola una llave foránea. */
    static final int H2_ERROR_REGISTRO_CON_DEPENDENCIAS = 23503;

    private final VehiculoDAO vehiculoDAO;
    private final BodegaRepositorio bodegaRepositorio;

    public InventarioServicio() {
        this(new VehiculoDAO(), new BodegaDAO());
    }

    public InventarioServicio(VehiculoDAO vehiculoDAO) {
        this(vehiculoDAO, new BodegaDAO());
    }

    public InventarioServicio(VehiculoDAO vehiculoDAO, BodegaRepositorio bodegaRepositorio) {
        this.vehiculoDAO = vehiculoDAO;
        this.bodegaRepositorio = bodegaRepositorio;
    }

    // ------------------------------------------------------------------
    // CU-06 / CU-09: catálogo de vehículos
    // ------------------------------------------------------------------

    public Vehiculo registrarVehiculo(String vin, String placa, CategoriaVehiculo tipo, String marca,
                                      int modeloAnio, String linea, String color, BigDecimal precioVenta) throws SQLException {
        return registrarVehiculo(vin, placa, tipo, marca, modeloAnio, linea, color, precioVenta, 1L);
    }

    /**
     * Registra un vehículo en una sede concreta. Antes de guardar verifica que la bodega
     * exista, esté activa y tenga cupo disponible (RN-07-07 y RN-07-08).
     */
    public Vehiculo registrarVehiculo(String vin, String placa, CategoriaVehiculo tipo, String marca,
                                      int modeloAnio, String linea, String color, BigDecimal precioVenta,
                                      Long bodegaId) throws SQLException {
        ValidadorUtil.validarRequerido(vin, "VIN");
        ValidadorUtil.validarRequerido(marca, "Marca");
        ValidadorUtil.validarRequerido(linea, "Línea");
        ValidadorUtil.validarMontoPositivo(precioVenta, "Precio de Venta");

        if (bodegaId != null) {
            Bodega bodega = bodegaRepositorio.buscarPorId(bodegaId)
                    .orElseThrow(() -> new ReglaNegocioException(
                            "No se pudo registrar el vehículo: la bodega indicada no existe."));
            if (!bodega.isActivo()) {
                throw new ReglaNegocioException("No se pudo registrar el vehículo: la bodega "
                        + bodega.getNombre() + " está inactiva.");
            }
            if (!hayCupo(bodegaId)) {
                throw new ReglaNegocioException("No se pudo registrar el vehículo: la bodega "
                        + bodega.getNombre() + " ya alcanzó su capacidad de "
                        + bodega.getCapacidadVehiculos() + " vehículos.");
            }
        }

        Vehiculo v = new Vehiculo(null, vin, placa, tipo, marca, modeloAnio, linea, color,
                precioVenta, "DISPONIBLE", 0, bodegaId);
        return vehiculoDAO.guardar(v);
    }

    public List<Vehiculo> listarDisponibles() throws SQLException {
        return vehiculoDAO.listarDisponibles();
    }

    // ------------------------------------------------------------------
    // CU-07: gestionar bodegas y patios de almacenamiento
    // ------------------------------------------------------------------

    public List<Bodega> listarBodegas() throws SQLException {
        return bodegaRepositorio.listarTodas();
    }

    /** Solo las sedes activas. Alimenta el selector de bodega del CU-06 y del CU-08 (RN-07-07). */
    public List<Bodega> listarBodegasActivas() throws SQLException {
        return bodegaRepositorio.listarActivas();
    }

    public Optional<Bodega> buscarBodega(Long id) throws SQLException {
        return id == null ? Optional.empty() : bodegaRepositorio.buscarPorId(id);
    }

    public Bodega registrarBodega(Bodega bodega) throws SQLException {
        validarDatosBodega(bodega);
        validarCodigoUnico(bodega);
        return bodegaRepositorio.guardar(bodega);
    }

    public void actualizarBodega(Bodega bodega) throws SQLException {
        if (bodega == null || bodega.getId() == null
                || bodegaRepositorio.buscarPorId(bodega.getId()).isEmpty()) {
            throw new ReglaNegocioException("No se pudo actualizar: la bodega no existe.");
        }
        validarDatosBodega(bodega);
        validarCodigoUnico(bodega);

        int ocupacion = bodegaRepositorio.contarVehiculos(bodega.getId());
        if (bodega.getCapacidadVehiculos() < ocupacion) {
            throw new ReglaNegocioException("La capacidad no puede ser menor que los " + ocupacion
                    + " vehículos ya almacenados en la sede.");
        }
        bodegaRepositorio.actualizar(bodega);
    }

    /** Marca activo = FALSE sin borrar la fila ni los vehículos asociados. */
    public void desactivarBodega(Long id) throws SQLException {
        Bodega bodega = bodegaRepositorio.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("No se pudo desactivar: la bodega no existe."));
        bodega.setActivo(false);
        bodegaRepositorio.actualizar(bodega);
    }

    public void eliminarBodega(Long id) throws SQLException {
        if (id == null || bodegaRepositorio.buscarPorId(id).isEmpty()) {
            throw new ReglaNegocioException("No se pudo eliminar: la bodega no existe.");
        }
        try {
            bodegaRepositorio.eliminar(id);
        } catch (SQLException e) {
            if (e.getErrorCode() == H2_ERROR_REGISTRO_CON_DEPENDENCIAS) {
                throw new ReglaNegocioException(
                        "La bodega tiene vehículos asociados; márquela como inactiva en lugar de eliminarla.");
            }
            throw e;
        }
    }

    public int ocupacionBodega(Long bodegaId) throws SQLException {
        return bodegaRepositorio.contarVehiculos(bodegaId);
    }

    /** true si la sede todavía admite al menos un vehículo más (RN-07-08). */
    public boolean hayCupo(Long bodegaId) throws SQLException {
        Optional<Bodega> bodega = bodegaRepositorio.buscarPorId(bodegaId);
        if (bodega.isEmpty()) {
            return false;
        }
        return bodegaRepositorio.contarVehiculos(bodegaId) < bodega.get().getCapacidadVehiculos();
    }

    /**
     * Revisa campos obligatorios, longitudes y rango de capacidad; luego normaliza
     * con trim y pasa el código a mayúsculas (RN-07-01 a RN-07-04).
     */
    void validarDatosBodega(Bodega bodega) {
        if (bodega == null) {
            throw new ReglaNegocioException("No se pudo guardar la bodega: no hay datos para procesar.");
        }
        if (estaVacio(bodega.getCodigo())) {
            throw new ReglaNegocioException("No se pudo guardar la bodega: el código es obligatorio.");
        }
        if (bodega.getCodigo().trim().length() > LONGITUD_MAX_CODIGO) {
            throw new ReglaNegocioException("No se pudo guardar la bodega: el código no puede superar "
                    + LONGITUD_MAX_CODIGO + " caracteres.");
        }
        if (estaVacio(bodega.getNombre())) {
            throw new ReglaNegocioException("No se pudo guardar la bodega: el nombre es obligatorio.");
        }
        if (bodega.getNombre().trim().length() > LONGITUD_MAX_NOMBRE) {
            throw new ReglaNegocioException("No se pudo guardar la bodega: el nombre no puede superar "
                    + LONGITUD_MAX_NOMBRE + " caracteres.");
        }
        if (estaVacio(bodega.getCiudad())) {
            throw new ReglaNegocioException("No se pudo guardar la bodega: la ciudad es obligatoria.");
        }
        if (bodega.getCiudad().trim().length() > LONGITUD_MAX_CIUDAD) {
            throw new ReglaNegocioException("No se pudo guardar la bodega: la ciudad no puede superar "
                    + LONGITUD_MAX_CIUDAD + " caracteres.");
        }
        if (bodega.getCapacidadVehiculos() < CAPACIDAD_MINIMA) {
            throw new ReglaNegocioException("No se pudo guardar la bodega: la capacidad debe ser al menos "
                    + CAPACIDAD_MINIMA + " vehículo.");
        }

        bodega.setCodigo(bodega.getCodigo().trim().toUpperCase());
        bodega.setNombre(bodega.getNombre().trim());
        bodega.setCiudad(bodega.getCiudad().trim());
    }

    /** Falla si ya existe otra bodega con el mismo código e id distinto (RN-07-01). */
    void validarCodigoUnico(Bodega bodega) throws SQLException {
        Optional<Bodega> existente = bodegaRepositorio.buscarPorCodigo(bodega.getCodigo());
        if (existente.isPresent() && !existente.get().getId().equals(bodega.getId())) {
            throw new ReglaNegocioException("No se pudo guardar la bodega: ya existe otra sede con el código "
                    + bodega.getCodigo() + ".");
        }
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    // ------------------------------------------------------------------
    // CU-10: valoración y depreciación (No CRUD)
    // ------------------------------------------------------------------

    /**
     * Calcula la depreciación estimada del vehículo según años de antigüedad (CU-10 - No CRUD).
     * Regla: 15% el primer año, 10% por año subsecuente, con valor de rescate mínimo del 30%.
     */
    public BigDecimal calcularValorDepreciado(Vehiculo v) {
        if (v == null || v.getPrecioVenta() == null) return BigDecimal.ZERO;

        int anioActual = Year.now().getValue();
        int antiguedad = Math.max(0, anioActual - v.getModeloAnio());

        if (antiguedad == 0) {
            return v.getPrecioVenta();
        }

        double tasaDepreciacion = 0.15 + (antiguedad - 1) * 0.10;
        tasaDepreciacion = Math.min(tasaDepreciacion, 0.70); // Máximo 70% de depreciación

        BigDecimal factor = BigDecimal.valueOf(1.0 - tasaDepreciacion);
        return v.getPrecioVenta().multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
