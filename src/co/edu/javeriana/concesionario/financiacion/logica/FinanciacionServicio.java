package co.edu.javeriana.concesionario.financiacion.logica;

import co.edu.javeriana.concesionario.financiacion.modelo.EntidadFinanciera;
import co.edu.javeriana.concesionario.financiacion.persistencia.EntidadFinancieraDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Servicio de lógica de negocio del Módulo de Financiación.
 * CU-11: Gestionar entidades financieras aliadas (CRUD).
 *
 * Responsable: Jerónimo Martin
 */
public class FinanciacionServicio {

    /** NIT colombiano: dígitos (con o sin puntos) seguidos de guion y dígito de verificación. Ej: 890.903.938-8 */
    private static final Pattern PATRON_NIT = Pattern.compile("^[0-9.]{6,15}-[0-9]$");
    private static final int LONGITUD_MAX_CODIGO = 20;
    private static final int LONGITUD_MAX_NOMBRE = 100;
    private static final int SCORING_MINIMO_PERMITIDO = 300;
    private static final int SCORING_MAXIMO_PERMITIDO = 900;

    /** Código de error de H2 cuando se intenta borrar un registro que otra tabla referencia (llave foránea). */
    private static final int H2_ERROR_REGISTRO_CON_DEPENDENCIAS = 23503;

    private final EntidadFinancieraDAO entidadDAO;

    public FinanciacionServicio() {
        this(new EntidadFinancieraDAO());
    }

    public FinanciacionServicio(EntidadFinancieraDAO entidadDAO) {
        this.entidadDAO = entidadDAO;
    }

    // --- CU-11: Consultar ---
    public List<EntidadFinanciera> listarEntidades() throws SQLException {
        return entidadDAO.listarTodas();
    }

    public List<EntidadFinanciera> listarEntidadesActivas() throws SQLException {
        return entidadDAO.listarActivas();
    }

    public Optional<EntidadFinanciera> buscarEntidad(Long id) throws SQLException {
        return entidadDAO.buscarPorId(id);
    }

    // --- CU-11: Crear ---
    public EntidadFinanciera registrarEntidad(EntidadFinanciera entidad) throws SQLException {
        validarDatos(entidad);
        validarCodigoYNitUnicos(entidad);
        return entidadDAO.guardar(entidad);
    }

    // --- CU-11: Editar ---
    public void actualizarEntidad(EntidadFinanciera entidad) throws SQLException {
        if (entidad.getId() == null || entidadDAO.buscarPorId(entidad.getId()).isEmpty()) {
            throw new ReglaNegocioException("No se pudo actualizar: la entidad financiera seleccionada no existe. Seleccione una entidad de la tabla.");
        }
        validarDatos(entidad);
        validarCodigoYNitUnicos(entidad);
        entidadDAO.actualizar(entidad);
    }

    // --- CU-11: Eliminar ---
    public void eliminarEntidad(Long id) throws SQLException {
        if (id == null || entidadDAO.buscarPorId(id).isEmpty()) {
            throw new ReglaNegocioException("No se pudo eliminar: la entidad financiera seleccionada no existe.");
        }
        try {
            entidadDAO.eliminar(id);
        } catch (SQLException e) {
            // Excepción del CU-11: la entidad tiene relaciones activas con otros módulos
            if (e.getErrorCode() == H2_ERROR_REGISTRO_CON_DEPENDENCIAS) {
                throw new ReglaNegocioException("No se pudo eliminar la entidad financiera: tiene registros asociados en otros módulos. "
                        + "Puede marcarla como inactiva en lugar de eliminarla.");
            }
            throw e;
        }
    }

    /**
     * Valida campos obligatorios, formatos y reglas de negocio (RNF-04).
     * Cada mensaje indica qué falló y cómo corregirlo (RNF-06).
     */
    private void validarDatos(EntidadFinanciera entidad) {
        if (estaVacio(entidad.getCodigo())) {
            throw new ReglaNegocioException("No se pudo guardar la entidad: el campo Código es obligatorio.");
        }
        if (entidad.getCodigo().trim().length() > LONGITUD_MAX_CODIGO) {
            throw new ReglaNegocioException("No se pudo guardar la entidad: el Código no puede superar " + LONGITUD_MAX_CODIGO + " caracteres.");
        }
        if (estaVacio(entidad.getNombre())) {
            throw new ReglaNegocioException("No se pudo guardar la entidad: el campo Nombre es obligatorio.");
        }
        if (entidad.getNombre().trim().length() > LONGITUD_MAX_NOMBRE) {
            throw new ReglaNegocioException("No se pudo guardar la entidad: el Nombre no puede superar " + LONGITUD_MAX_NOMBRE + " caracteres.");
        }
        if (estaVacio(entidad.getNit())) {
            throw new ReglaNegocioException("No se pudo guardar la entidad: el campo NIT es obligatorio.");
        }
        if (!PATRON_NIT.matcher(entidad.getNit().trim()).matches()) {
            throw new ReglaNegocioException("No se pudo guardar la entidad: el NIT tiene un formato inválido. Use el formato 890.903.938-8.");
        }
        BigDecimal tasa = entidad.getTasaBaseEa();
        if (tasa == null || tasa.compareTo(BigDecimal.ZERO) <= 0 || tasa.compareTo(BigDecimal.ONE) >= 0) {
            throw new ReglaNegocioException("No se pudo guardar la entidad: la Tasa E.A. debe ser mayor a 0% y menor a 100%.");
        }
        if (entidad.getScoringMinimo() < SCORING_MINIMO_PERMITIDO || entidad.getScoringMinimo() > SCORING_MAXIMO_PERMITIDO) {
            throw new ReglaNegocioException("No se pudo guardar la entidad: el Scoring mínimo debe estar entre "
                    + SCORING_MINIMO_PERMITIDO + " y " + SCORING_MAXIMO_PERMITIDO + ".");
        }

        entidad.setCodigo(entidad.getCodigo().trim().toUpperCase());
        entidad.setNombre(entidad.getNombre().trim());
        entidad.setNit(entidad.getNit().trim());
    }

    /** El código y el NIT no se pueden repetir entre entidades distintas. */
    private void validarCodigoYNitUnicos(EntidadFinanciera entidad) throws SQLException {
        Optional<EntidadFinanciera> mismoCodigo = entidadDAO.buscarPorCodigo(entidad.getCodigo());
        if (mismoCodigo.isPresent() && !mismoCodigo.get().getId().equals(entidad.getId())) {
            throw new ReglaNegocioException("No se pudo guardar la entidad: ya existe otra entidad con el código " + entidad.getCodigo() + ".");
        }
        Optional<EntidadFinanciera> mismoNit = entidadDAO.buscarPorNit(entidad.getNit());
        if (mismoNit.isPresent() && !mismoNit.get().getId().equals(entidad.getId())) {
            throw new ReglaNegocioException("No se pudo guardar la entidad: ya existe otra entidad con el NIT " + entidad.getNit() + ".");
        }
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
