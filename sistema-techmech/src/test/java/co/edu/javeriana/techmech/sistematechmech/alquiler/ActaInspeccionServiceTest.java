package co.edu.javeriana.techmech.sistematechmech.alquiler;

import co.edu.javeriana.techmech.sistematechmech.alquiler.model.ActaInspeccion;
import co.edu.javeriana.techmech.sistematechmech.alquiler.persistencia.ActaInspeccionDao;
import co.edu.javeriana.techmech.sistematechmech.alquiler.service.ActaInspeccionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ActaInspeccionServiceTest {

    private ActaInspeccionDao mockDao;
    private ActaInspeccionService service;

    @BeforeEach
    void setUp() {
        mockDao = new ActaInspeccionDao() {
            @Override
            public ActaInspeccion guardar(ActaInspeccion a) {
                a.setIdActa(1L);
                return a;
            }

            @Override
            public Optional<ActaInspeccion> buscarPorId(Long id) {
                return Optional.empty();
            }

            @Override
            public List<ActaInspeccion> listarPorVehiculo(Long idVehiculo) {
                return Collections.emptyList();
            }

            @Override
            public List<ActaInspeccion> listarTodas() {
                return Collections.emptyList();
            }

            @Override
            public boolean eliminarLogico(Long id) {
                return true;
            }
        };

        service = new ActaInspeccionService(mockDao);
    }

    @Test
    void testRegistrarActaExitosa() {
        ActaInspeccion acta = service.registrarActa(
            10L,
            "ENTREGA",
            15400,
            100,
            "BUENO",
            "Vehículo entregado limpio"
        );

        assertNotNull(acta.getIdActa());
        assertEquals("ENTREGA", acta.getTipoInspeccion());
        assertEquals(100, acta.getNivelCombustible());
        assertTrue(acta.isActivo());
    }

    @Test
    void testRechazarCombustibleInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
            service.registrarActa(10L, "ENTREGA", 15400, 120, "BUENO", "")
        );
    }

    @Test
    void testRechazarTipoInspeccionDesconocido() {
        assertThrows(IllegalArgumentException.class, () ->
            service.registrarActa(10L, "VENTA", 15400, 80, "BUENO", "")
        );
    }
}