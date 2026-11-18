package co.edu.javeriana.concesionario.legal;

import co.edu.javeriana.concesionario.legal.logica.MatriculaServicio;
import co.edu.javeriana.concesionario.legal.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.legal.modelo.EstadoImpuesto;
import co.edu.javeriana.concesionario.legal.modelo.EstadoMatricula;
import co.edu.javeriana.concesionario.legal.modelo.Matricula;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;
import co.edu.javeriana.concesionario.legal.persistencia.MatriculaRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reglas del CU-21 con un repositorio en memoria y un reloj fijo: hoy es 19/10/2026.
 */
public class MatriculaServicioTest {
    private static final Long SIN_PLACA = 1L;     // automóvil nuevo, sin placa en el inventario
    private static final Long CON_PLACA = 2L;     // camioneta que ya tiene placa ABC123 en el inventario
    private static final Long MOTO = 3L;          // motocicleta sin placa

    private RepositorioEnMemoria repositorio;
    private MatriculaServicio servicio;

    @BeforeEach
    public void setUp() {
        repositorio = new RepositorioEnMemoria();
        repositorio.vehiculos.add(new VehiculoLegal(SIN_PLACA, "VIN1", null, "AUTOMOVIL", "Toyota", "Corolla", 2026, "DISPONIBLE"));
        repositorio.vehiculos.add(new VehiculoLegal(CON_PLACA, "VIN2", "ABC123", "CAMIONETA", "Mazda", "CX-30", 2024, "DISPONIBLE"));
        repositorio.vehiculos.add(new VehiculoLegal(MOTO, "VIN3", null, "MOTOCICLETA", "Yamaha", "MT-09", 2025, "DISPONIBLE"));
        servicio = new MatriculaServicio(repositorio, Clock.fixed(Instant.parse("2026-10-19T12:00:00Z"), ZoneOffset.UTC));
    }

    private Matricula matricula(Long vehiculoId, String placa, String licencia) {
        return new Matricula(null, vehiculoId, placa, licencia, "Secretaría Distrital de Movilidad de Bogotá",
                LocalDate.of(2026, 3, 10), 1798, EstadoMatricula.ACTIVA, 2026, new BigDecimal("1725000"));
    }

    // ---------------------------------------------------------------- registrar

    @Test
    public void testRegistrarMatriculaAsignaLaPlacaNormalizada() throws Exception {
        Matricula m = servicio.registrarMatricula(matricula(SIN_PLACA, " klu-382 ", "lt-0001"));

        assertNotNull(m.getId());
        assertEquals("KLU382", m.getPlaca());
        assertEquals("LT-0001", m.getNumeroLicenciaTransito());
        assertEquals(1, repositorio.matriculas.size());
    }

    @Test
    public void testPlacaDeMotoConLetraFinalEsValida() throws Exception {
        Matricula m = matricula(MOTO, "NKS81A", "LT-0002");
        m.setCilindrajeCc(890);
        assertEquals("NKS81A", servicio.registrarMatricula(m).getPlaca());
    }

    @Test
    public void testPlacaConFormatoInvalidoEsRechazada() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarMatricula(matricula(SIN_PLACA, "AB1234", "LT-0003")));
        assertTrue(ex.getMessage().contains("formato inválido"));
    }

    @Test
    public void testDebeUsarLaPlacaQueYaTieneElVehiculo() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarMatricula(matricula(CON_PLACA, "XYZ999", "LT-0004")));
        assertTrue(ex.getMessage().contains("ABC123"));
    }

    @Test
    public void testPlacaDeOtroVehiculoDelInventarioEsRechazada() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarMatricula(matricula(SIN_PLACA, "ABC123", "LT-0005")));
        assertTrue(ex.getMessage().contains("otro vehículo"));
    }

    @Test
    public void testUnVehiculoSoloTieneUnaMatricula() throws Exception {
        servicio.registrarMatricula(matricula(CON_PLACA, "ABC123", "LT-0006"));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarMatricula(matricula(CON_PLACA, "ABC123", "LT-0007")));
        assertTrue(ex.getMessage().contains("ya está matriculado"));
    }

    @Test
    public void testLicenciaDeTransitoRepetidaEsRechazada() throws Exception {
        servicio.registrarMatricula(matricula(CON_PLACA, "ABC123", "LT-0008"));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrarMatricula(matricula(SIN_PLACA, "KLU382", "LT-0008")));
        assertTrue(ex.getMessage().contains("licencia de tránsito"));
    }

    @Test
    public void testFechaDeMatriculaFuturaEsRechazada() {
        Matricula m = matricula(SIN_PLACA, "KLU382", "LT-0009");
        m.setFechaMatricula(LocalDate.of(2026, 10, 20));
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarMatricula(m));
    }

    @Test
    public void testFechaDeMatriculaAnteriorAlModeloEsRechazada() {
        Matricula m = matricula(SIN_PLACA, "KLU382", "LT-0010"); // modelo 2026
        m.setFechaMatricula(LocalDate.of(2024, 12, 31));
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.registrarMatricula(m));
        assertTrue(ex.getMessage().contains("modelo"));
    }

    @Test
    public void testCilindrajeFueraDeRangoEsRechazado() {
        Matricula m = matricula(SIN_PLACA, "KLU382", "LT-0011");
        m.setCilindrajeCc(20);
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarMatricula(m));
    }

    @Test
    public void testAnioDeImpuestoFuturoEsRechazado() {
        Matricula m = matricula(SIN_PLACA, "KLU382", "LT-0012");
        m.setUltimoAnioImpuestoPagado(2027);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.registrarMatricula(m));
        assertTrue(ex.getMessage().contains("impuesto"));
    }

    // ---------------------------------------------------------------- actualizar y eliminar

    @Test
    public void testActualizarCambiaEstadoEImpuesto() throws Exception {
        Matricula m = servicio.registrarMatricula(matricula(CON_PLACA, "ABC123", "LT-0013"));
        Matricula cambios = matricula(CON_PLACA, "ABC123", "LT-0013");
        cambios.setId(m.getId());
        cambios.setFechaMatricula(LocalDate.of(2024, 5, 2));
        cambios.setEstadoRegistro(EstadoMatricula.SUSPENDIDA);
        cambios.setUltimoAnioImpuestoPagado(2025);

        servicio.actualizarMatricula(cambios);

        Matricula guardada = servicio.buscarMatricula(m.getId()).orElseThrow();
        assertEquals(EstadoMatricula.SUSPENDIDA, guardada.getEstadoRegistro());
        assertEquals(2025, guardada.getUltimoAnioImpuestoPagado());
    }

    @Test
    public void testLaPlacaNoSePuedeModificar() throws Exception {
        Matricula m = servicio.registrarMatricula(matricula(SIN_PLACA, "KLU382", "LT-0014"));
        Matricula cambios = matricula(SIN_PLACA, "KLU383", "LT-0014");
        cambios.setId(m.getId());

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.actualizarMatricula(cambios));
        assertTrue(ex.getMessage().contains("no se puede modificar"));
    }

    @Test
    public void testMatriculaActivaNoSeElimina() throws Exception {
        Matricula m = servicio.registrarMatricula(matricula(SIN_PLACA, "KLU382", "LT-0015"));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicio.eliminarMatricula(m.getId()));
        assertTrue(ex.getMessage().contains("Cancelada"));
        assertEquals(1, repositorio.matriculas.size());
    }

    @Test
    public void testMatriculaCanceladaSiSeElimina() throws Exception {
        Matricula m = matricula(SIN_PLACA, "KLU382", "LT-0016");
        m.setEstadoRegistro(EstadoMatricula.CANCELADA);
        servicio.registrarMatricula(m);

        servicio.eliminarMatricula(m.getId());

        assertTrue(repositorio.matriculas.isEmpty());
    }

    // ---------------------------------------------------------------- impuesto

    @Test
    public void testEstadoDelImpuesto() {
        Matricula m = matricula(SIN_PLACA, "KLU382", "LT-0017");
        m.setFechaMatricula(LocalDate.of(2022, 1, 15));

        m.setUltimoAnioImpuestoPagado(2026);
        assertEquals(EstadoImpuesto.AL_DIA, m.estadoImpuesto(2026));
        m.setUltimoAnioImpuestoPagado(2025);
        assertEquals(EstadoImpuesto.PENDIENTE, m.estadoImpuesto(2026));
        m.setUltimoAnioImpuestoPagado(2023);
        assertEquals(EstadoImpuesto.EN_MORA, m.estadoImpuesto(2026));
        m.setUltimoAnioImpuestoPagado(null);
        assertEquals(EstadoImpuesto.EN_MORA, m.estadoImpuesto(2026), "Nunca ha pagado desde 2022");
    }

    // ---------------------------------------------------------------- repositorio en memoria

    private static class RepositorioEnMemoria implements MatriculaRepositorio {
        final List<VehiculoLegal> vehiculos = new ArrayList<>();
        final List<Matricula> matriculas = new ArrayList<>();
        private long secuencia = 1;

        @Override
        public Matricula guardar(Matricula m) {
            m.setId(secuencia++);
            matriculas.add(m);
            // Igual que el DAO: si el vehículo no tenía placa, se le asigna
            for (int i = 0; i < vehiculos.size(); i++) {
                VehiculoLegal v = vehiculos.get(i);
                if (v.id().equals(m.getVehiculoId()) && !v.tienePlaca()) {
                    vehiculos.set(i, new VehiculoLegal(v.id(), v.vin(), m.getPlaca(), v.tipo(), v.marca(), v.linea(), v.modeloAnio(), v.estado()));
                }
            }
            return m;
        }

        @Override
        public void actualizar(Matricula m) {
            matriculas.replaceAll(actual -> actual.getId().equals(m.getId()) ? m : actual);
        }

        @Override public void eliminar(Long id) { matriculas.removeIf(m -> m.getId().equals(id)); }
        @Override public List<Matricula> listarTodas() { return new ArrayList<>(matriculas); }
        @Override public Optional<Matricula> buscarPorId(Long id) { return matriculas.stream().filter(m -> m.getId().equals(id)).findFirst(); }
        @Override public Optional<Matricula> buscarPorVehiculo(Long id) { return matriculas.stream().filter(m -> m.getVehiculoId().equals(id)).findFirst(); }
        @Override public Optional<Matricula> buscarPorPlaca(String placa) { return matriculas.stream().filter(m -> m.getPlaca().equals(placa)).findFirst(); }
        @Override public Optional<Matricula> buscarPorLicencia(String lic) { return matriculas.stream().filter(m -> m.getNumeroLicenciaTransito().equals(lic)).findFirst(); }
        @Override public List<VehiculoLegal> listarVehiculos() { return new ArrayList<>(vehiculos); }

        @Override
        public List<VehiculoLegal> listarVehiculosSinMatricula() {
            return vehiculos.stream().filter(v -> buscarPorVehiculo(v.id()).isEmpty()).toList();
        }

        @Override public Optional<VehiculoLegal> buscarVehiculo(Long id) { return vehiculos.stream().filter(v -> v.id().equals(id)).findFirst(); }
        @Override public Optional<VehiculoLegal> buscarVehiculoPorPlaca(String placa) { return vehiculos.stream().filter(v -> placa.equals(v.placa())).findFirst(); }
    }
}
