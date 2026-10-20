package co.edu.javeriana.concesionario.legal.logica;

import co.edu.javeriana.concesionario.legal.modelo.Aseguradora;
import co.edu.javeriana.concesionario.legal.persistencia.AseguradoraRepositorio;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * CU-22: gestionar aseguradoras (CRUD).
 * Administra los convenios con aseguradoras para las pólizas obligatorias (SOAT) y de
 * responsabilidad civil por daños materiales a terceros.
 */
public class AseguradoraServicio {
    private static final Pattern PATRON_NIT = Pattern.compile("^[0-9.]{6,15}-[0-9]$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^[0-9+() -]{7,20}$");
    private static final int LONGITUD_MAX_CODIGO = 20;
    private static final int LONGITUD_MAX_NOMBRE = 100;

    /** Código de error de H2 cuando se viola una llave foránea (hay registros que dependen del que se borra). */
    private static final int H2_ERROR_REGISTRO_CON_DEPENDENCIAS = 23503;

    private final AseguradoraRepositorio repositorio;

    public AseguradoraServicio(AseguradoraRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Aseguradora> listarAseguradoras() throws SQLException {
        return repositorio.listarTodas();
    }

    public List<Aseguradora> listarAseguradorasActivas() throws SQLException {
        return repositorio.listarActivas();
    }

    public Optional<Aseguradora> buscarAseguradora(Long id) throws SQLException {
        return repositorio.buscarPorId(id);
    }

    public Aseguradora registrarAseguradora(Aseguradora aseguradora) throws SQLException {
        validarDatos(aseguradora);
        validarCodigoYNitUnicos(aseguradora);
        return repositorio.guardar(aseguradora);
    }

    public void actualizarAseguradora(Aseguradora aseguradora) throws SQLException {
        if (aseguradora.getId() == null || repositorio.buscarPorId(aseguradora.getId()).isEmpty()) {
            throw new ReglaNegocioException("No se pudo actualizar: la aseguradora seleccionada no existe. Seleccione una aseguradora de la tabla.");
        }
        validarDatos(aseguradora);
        validarCodigoYNitUnicos(aseguradora);
        repositorio.actualizar(aseguradora);
    }

    public void eliminarAseguradora(Long id) throws SQLException {
        if (id == null || repositorio.buscarPorId(id).isEmpty()) {
            throw new ReglaNegocioException("No se pudo eliminar: la aseguradora seleccionada no existe.");
        }
        try {
            repositorio.eliminar(id);
        } catch (SQLException e) {
            if (e.getErrorCode() == H2_ERROR_REGISTRO_CON_DEPENDENCIAS) {
                throw new ReglaNegocioException("No se pudo eliminar la aseguradora: tiene pólizas emitidas asociadas. "
                        + "Puede marcarla como inactiva en lugar de eliminarla.");
            }
            throw e;
        }
    }

    private void validarDatos(Aseguradora a) {
        if (estaVacio(a.getCodigo())) {
            throw new ReglaNegocioException("No se pudo guardar la aseguradora: el campo Código es obligatorio.");
        }
        if (a.getCodigo().trim().length() > LONGITUD_MAX_CODIGO) {
            throw new ReglaNegocioException("No se pudo guardar la aseguradora: el Código no puede superar " + LONGITUD_MAX_CODIGO + " caracteres.");
        }
        if (estaVacio(a.getNombre())) {
            throw new ReglaNegocioException("No se pudo guardar la aseguradora: el campo Nombre es obligatorio.");
        }
        if (a.getNombre().trim().length() > LONGITUD_MAX_NOMBRE) {
            throw new ReglaNegocioException("No se pudo guardar la aseguradora: el Nombre no puede superar " + LONGITUD_MAX_NOMBRE + " caracteres.");
        }
        if (estaVacio(a.getNit())) {
            throw new ReglaNegocioException("No se pudo guardar la aseguradora: el campo NIT es obligatorio.");
        }
        if (!PATRON_NIT.matcher(a.getNit().trim()).matches()) {
            throw new ReglaNegocioException("No se pudo guardar la aseguradora: el NIT tiene un formato inválido. Use el formato 890.903.407-9.");
        }
        if (!estaVacio(a.getTelefonoContacto()) && !PATRON_TELEFONO.matcher(a.getTelefonoContacto().trim()).matches()) {
            throw new ReglaNegocioException("No se pudo guardar la aseguradora: el Teléfono de contacto solo admite números, "
                    + "espacios y los símbolos + - ( ), entre 7 y 20 caracteres.");
        }
        if (!a.isCubreSoat() && !a.isCubreResponsabilidadCivil()) {
            throw new ReglaNegocioException("No se pudo guardar la aseguradora: el convenio debe cubrir al menos un tipo de póliza "
                    + "(SOAT o responsabilidad civil por daños materiales a terceros).");
        }

        a.setCodigo(a.getCodigo().trim().toUpperCase());
        a.setNombre(a.getNombre().trim());
        a.setNit(a.getNit().trim());
        a.setTelefonoContacto(estaVacio(a.getTelefonoContacto()) ? null : a.getTelefonoContacto().trim());
    }

    private void validarCodigoYNitUnicos(Aseguradora a) throws SQLException {
        Optional<Aseguradora> mismoCodigo = repositorio.buscarPorCodigo(a.getCodigo());
        if (mismoCodigo.isPresent() && !mismoCodigo.get().getId().equals(a.getId())) {
            throw new ReglaNegocioException("No se pudo guardar la aseguradora: ya existe otra aseguradora con el código " + a.getCodigo() + ".");
        }
        Optional<Aseguradora> mismoNit = repositorio.buscarPorNit(a.getNit());
        if (mismoNit.isPresent() && !mismoNit.get().getId().equals(a.getId())) {
            throw new ReglaNegocioException("No se pudo guardar la aseguradora: ya existe otra aseguradora con el NIT " + a.getNit() + ".");
        }
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
