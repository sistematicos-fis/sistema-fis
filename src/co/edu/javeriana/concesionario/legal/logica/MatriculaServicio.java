package co.edu.javeriana.concesionario.legal.logica;

import co.edu.javeriana.concesionario.legal.modelo.EstadoMatricula;
import co.edu.javeriana.concesionario.legal.modelo.Matricula;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;
import co.edu.javeriana.concesionario.legal.persistencia.MatriculaRepositorio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/*
  CU-21: gestionar matrículas de vehículos (CRUD).
  Registra el estado del registro oficial (licencia de tránsito) y del impuesto vehicular de cada vehículo.
 
 * Reglas:
  - Un vehículo tiene una sola matrícula y una placa no puede repetirse.
  - La placa tiene el formato colombiano: AAA123 (carros) o AAA12A (motos).
  - Si el vehículo del inventario ya tiene placa, la matrícula debe usar esa misma; si no tiene,
    al matricularlo se le asigna. Una vez registrada, la placa y el vehículo no se modifican.
  - La fecha de matrícula no puede ser futura ni anterior al año del modelo menos uno.
  - Una matrícula ACTIVA no se elimina: se cambia a CANCELADA.
 */
public class MatriculaServicio {
    private static final Pattern PATRON_PLACA = Pattern.compile("^[A-Z]{3}[0-9]{2}[0-9A-Z]$");
    private static final Pattern PATRON_LICENCIA = Pattern.compile("^[A-Z0-9-]{6,20}$");
    private static final int LONGITUD_MAX_ORGANISMO = 80;
    static final int CILINDRAJE_MINIMO = 50;
    static final int CILINDRAJE_MAXIMO = 9000;

    private final MatriculaRepositorio repositorio;
    private final Clock reloj;

    public MatriculaServicio(MatriculaRepositorio repositorio) {
        this(repositorio, Clock.systemDefaultZone());
    }

    public MatriculaServicio(MatriculaRepositorio repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public List<Matricula> listarMatriculas() throws SQLException {
        return repositorio.listarTodas();
    }

    public List<VehiculoLegal> listarVehiculos() throws SQLException {
        return repositorio.listarVehiculos();
    }

    public List<VehiculoLegal> listarVehiculosSinMatricula() throws SQLException {
        return repositorio.listarVehiculosSinMatricula();
    }

    public Optional<Matricula> buscarMatricula(Long id) throws SQLException {
        return repositorio.buscarPorId(id);
    }

    public int anioActual() {
        return LocalDate.now(reloj).getYear();
    }

    public Matricula registrarMatricula(Matricula m) throws SQLException {
        if (m.getVehiculoId() == null) {
            throw new ReglaNegocioException("No se pudo registrar la matrícula: seleccione un vehículo.");
        }
        VehiculoLegal vehiculo = repositorio.buscarVehiculo(m.getVehiculoId())
                .orElseThrow(() -> new ReglaNegocioException("No se pudo registrar la matrícula: el vehículo seleccionado no existe."));
        if (repositorio.buscarPorVehiculo(vehiculo.id()).isPresent()) {
            throw new ReglaNegocioException("No se pudo registrar la matrícula: el vehículo " + vehiculo
                    + " ya está matriculado. Selecciónelo en la tabla para editar su matrícula.");
        }

        m.setPlaca(normalizarPlaca(m.getPlaca()));
        validarPlaca(m.getPlaca(), vehiculo);
        validarDatos(m, vehiculo);
        validarUnicidad(m);
        return repositorio.guardar(m);
    }

    public void actualizarMatricula(Matricula m) throws SQLException {
        Matricula actual = m.getId() == null ? null : repositorio.buscarPorId(m.getId()).orElse(null);
        if (actual == null) {
            throw new ReglaNegocioException("No se pudo actualizar: la matrícula seleccionada no existe. Seleccione una matrícula de la tabla.");
        }
        if (m.getVehiculoId() != null && !m.getVehiculoId().equals(actual.getVehiculoId())) {
            throw new ReglaNegocioException("No se pudo actualizar la matrícula: no se puede cambiar el vehículo de una matrícula. "
                    + "Elimínela o cancélela y registre una nueva.");
        }
        if (m.getPlaca() != null && !normalizarPlaca(m.getPlaca()).equals(actual.getPlaca())) {
            throw new ReglaNegocioException("No se pudo actualizar la matrícula: la placa " + actual.getPlaca()
                    + " no se puede modificar una vez asignada.");
        }
        m.setVehiculoId(actual.getVehiculoId());
        m.setPlaca(actual.getPlaca());
        VehiculoLegal vehiculo = repositorio.buscarVehiculo(actual.getVehiculoId())
                .orElseThrow(() -> new ReglaNegocioException("No se pudo actualizar: el vehículo de la matrícula ya no existe en el inventario."));
        validarDatos(m, vehiculo);
        validarUnicidad(m);
        repositorio.actualizar(m);
    }

    public void eliminarMatricula(Long id) throws SQLException {
        Matricula actual = id == null ? null : repositorio.buscarPorId(id).orElse(null);
        if (actual == null) {
            throw new ReglaNegocioException("No se pudo eliminar: la matrícula seleccionada no existe.");
        }
        if (actual.getEstadoRegistro() == EstadoMatricula.ACTIVA) {
            throw new ReglaNegocioException("No se pudo eliminar la matrícula " + actual.getPlaca()
                    + ": está activa ante el organismo de tránsito. Cambie su estado a Cancelada en lugar de eliminarla.");
        }
        repositorio.eliminar(id);
    }

    /** Pasa la placa a mayúsculas y quita espacios y guiones ("abc-123" queda "ABC123"). */
    public static String normalizarPlaca(String placa) {
        return placa == null ? null : placa.replaceAll("[\\s-]", "").toUpperCase();
    }

    private void validarPlaca(String placa, VehiculoLegal vehiculo) throws SQLException {
        if (placa == null || placa.isEmpty()) {
            throw new ReglaNegocioException("No se pudo registrar la matrícula: el campo Placa es obligatorio.");
        }
        if (!PATRON_PLACA.matcher(placa).matches()) {
            throw new ReglaNegocioException("No se pudo registrar la matrícula: la placa " + placa
                    + " tiene un formato inválido. Use AAA123 para carros o AAA12A para motos.");
        }
        if (vehiculo.tienePlaca() && !vehiculo.placa().equals(placa)) {
            throw new ReglaNegocioException("No se pudo registrar la matrícula: el vehículo ya tiene la placa "
                    + vehiculo.placa() + " en el inventario. Use esa placa.");
        }
        Optional<VehiculoLegal> otroVehiculo = repositorio.buscarVehiculoPorPlaca(placa);
        if (otroVehiculo.isPresent() && !otroVehiculo.get().id().equals(vehiculo.id())) {
            throw new ReglaNegocioException("No se pudo registrar la matrícula: la placa " + placa
                    + " ya pertenece a otro vehículo del inventario (" + otroVehiculo.get() + ").");
        }
    }

    private void validarDatos(Matricula m, VehiculoLegal vehiculo) {
        String prefijo = "No se pudo guardar la matrícula: ";
        if (estaVacio(m.getNumeroLicenciaTransito())) {
            throw new ReglaNegocioException(prefijo + "el N° de licencia de tránsito es obligatorio.");
        }
        m.setNumeroLicenciaTransito(m.getNumeroLicenciaTransito().trim().toUpperCase());
        if (!PATRON_LICENCIA.matcher(m.getNumeroLicenciaTransito()).matches()) {
            throw new ReglaNegocioException(prefijo + "el N° de licencia de tránsito debe tener entre 6 y 20 letras, números o guiones.");
        }
        if (estaVacio(m.getOrganismoTransito())) {
            throw new ReglaNegocioException(prefijo + "el Organismo de tránsito es obligatorio.");
        }
        m.setOrganismoTransito(m.getOrganismoTransito().trim());
        if (m.getOrganismoTransito().length() > LONGITUD_MAX_ORGANISMO) {
            throw new ReglaNegocioException(prefijo + "el Organismo de tránsito no puede superar " + LONGITUD_MAX_ORGANISMO + " caracteres.");
        }

        LocalDate hoy = LocalDate.now(reloj);
        if (m.getFechaMatricula() == null) {
            throw new ReglaNegocioException(prefijo + "la Fecha de matrícula es obligatoria.");
        }
        if (m.getFechaMatricula().isAfter(hoy)) {
            throw new ReglaNegocioException(prefijo + "la Fecha de matrícula no puede ser futura.");
        }
        if (m.getFechaMatricula().getYear() < vehiculo.modeloAnio() - 1) {
            throw new ReglaNegocioException(prefijo + "la Fecha de matrícula (" + m.getFechaMatricula().getYear()
                    + ") no puede ser anterior al modelo del vehículo (" + vehiculo.modeloAnio() + ").");
        }

        if (m.getCilindrajeCc() == null) {
            throw new ReglaNegocioException(prefijo + "el Cilindraje es obligatorio.");
        }
        if (m.getCilindrajeCc() < CILINDRAJE_MINIMO || m.getCilindrajeCc() > CILINDRAJE_MAXIMO) {
            throw new ReglaNegocioException(prefijo + "el Cilindraje debe estar entre " + CILINDRAJE_MINIMO
                    + " y " + CILINDRAJE_MAXIMO + " cc.");
        }
        if (m.getEstadoRegistro() == null) {
            throw new ReglaNegocioException(prefijo + "seleccione el Estado del registro.");
        }

        Integer ultimoAnio = m.getUltimoAnioImpuestoPagado();
        if (ultimoAnio != null && (ultimoAnio < m.getFechaMatricula().getYear() || ultimoAnio > hoy.getYear())) {
            throw new ReglaNegocioException(prefijo + "el Último año de impuesto pagado debe estar entre el año de matrícula ("
                    + m.getFechaMatricula().getYear() + ") y el año actual (" + hoy.getYear() + ").");
        }
        BigDecimal valorImpuesto = m.getValorImpuestoAnual();
        if (valorImpuesto != null && valorImpuesto.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException(prefijo + "el Valor del impuesto anual no puede ser negativo.");
        }
    }

    private void validarUnicidad(Matricula m) throws SQLException {
        Optional<Matricula> mismaPlaca = repositorio.buscarPorPlaca(m.getPlaca());
        if (mismaPlaca.isPresent() && !mismaPlaca.get().getId().equals(m.getId())) {
            throw new ReglaNegocioException("No se pudo guardar la matrícula: ya existe otra matrícula con la placa " + m.getPlaca() + ".");
        }
        Optional<Matricula> mismaLicencia = repositorio.buscarPorLicencia(m.getNumeroLicenciaTransito());
        if (mismaLicencia.isPresent() && !mismaLicencia.get().getId().equals(m.getId())) {
            throw new ReglaNegocioException("No se pudo guardar la matrícula: la licencia de tránsito "
                    + m.getNumeroLicenciaTransito() + " ya está registrada en otra matrícula.");
        }
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
