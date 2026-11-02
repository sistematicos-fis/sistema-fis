package co.edu.javeriana.techmech.sistematechmech.alquiler;

import co.edu.javeriana.techmech.sistematechmech.alquiler.model.ContratoAlquiler;
import co.edu.javeriana.techmech.sistematechmech.alquiler.model.TarifaAlquiler;
import co.edu.javeriana.techmech.sistematechmech.alquiler.persistencia.ContratoAlquilerDao;
import co.edu.javeriana.techmech.sistematechmech.alquiler.persistencia.TarifaAlquilerDao;
import co.edu.javeriana.techmech.sistematechmech.alquiler.service.RealizarAlquilerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RealizarAlquilerServiceTest {

    private ContratoAlquilerDao mockContratoDao;
    private TarifaAlquilerDao mockTarifaDao;
    private RealizarAlquilerService service;

    @BeforeEach
    void setUp() {
        mockContratoDao = new ContratoAlquilerDao() {
            @Override
            public ContratoAlquiler guardar(ContratoAlquiler c) {
                c.setIdContrato(100L);
                return c;
            }

            @Override
            public Optional<ContratoAlquiler> buscarPorId(Long id) {
                return Optional.empty();
            }

            @Override
            public List<ContratoAlquiler> listarPorCliente(Long idCliente) {
                return Collections.emptyList();
            }

            @Override
            public List<ContratoAlquiler> listarActivos() {
                return Collections.emptyList();
            }

            @Override
            public boolean actualizarEstado(Long idContrato, String nuevoEstado) {
                return true;
            }
        };

        mockTarifaDao = new TarifaAlquilerDao() {
            @Override
            public TarifaAlquiler guardar(TarifaAlquiler tarifa) { return tarifa; }

            @Override
            public Optional<TarifaAlquiler> buscarPorId(Long id) { return Optional.empty(); }

            @Override
            public Optional<TarifaAlquiler> buscarPorTipoVehiculo(String tipo) {
                if ("CAMIONETA".equalsIgnoreCase(tipo)) {
                    return Optional.of(new TarifaAlquiler(1L, "CAMIONETA", new BigDecimal("200000.00"), new BigDecimal("800000.00"), "", true));
                }
                return Optional.empty();
            }

            @Override
            public List<TarifaAlquiler> listarActivas() { return Collections.emptyList(); }

            @Override
            public boolean actualizar(TarifaAlquiler tarifa) { return true; }

            @Override
            public boolean eliminarLogico(Long id) { return true; }
        };

        service = new RealizarAlquilerService(mockContratoDao, mockTarifaDao);
    }

    @Test
    void testProcesarAlquilerExitoso() {
        LocalDate inicio = LocalDate.now();
        LocalDate fin = inicio.plusDays(3);

        ContratoAlquiler contrato = service.procesarAlquiler(1L, 5L, "CAMIONETA", inicio, fin);

        assertNotNull(contrato.getIdContrato());
        assertEquals(3, contrato.getDiasAlquiler());
        assertEquals(new BigDecimal("600000.00"), contrato.getValorTotal()); // 3 días * 200,000
        assertEquals("ACTIVO", contrato.getEstado());
    }

    @Test
    void testRechazarFechasInvertidas() {
        LocalDate inicio = LocalDate.now();
        LocalDate fin = inicio.minusDays(1);

        assertThrows(IllegalArgumentException.class, () ->
            service.procesarAlquiler(1L, 5L, "CAMIONETA", inicio, fin)
        );
    }

    @Test
    void testRechazarTipoVehiculoSinTarifa() {
        LocalDate inicio = LocalDate.now();
        LocalDate fin = inicio.plusDays(2);

        assertThrows(IllegalStateException.class, () ->
            service.procesarAlquiler(1L, 5L, "MOTO_NO_EXISTE", inicio, fin)
        );
    }
}