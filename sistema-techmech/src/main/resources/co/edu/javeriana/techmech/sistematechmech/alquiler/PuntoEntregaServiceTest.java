package com.consesionario.alquiler;

import com.consesionario.alquiler.model.PuntoEntrega;
import com.consesionario.alquiler.repository.InMemoryPuntoEntregaRepository;
import com.consesionario.alquiler.service.PuntoEntregaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PuntoEntregaServiceTest {

    private PuntoEntregaService service;

    @BeforeEach
    public void setUp() {
        service = new PuntoEntregaService(new InMemoryPuntoEntregaRepository());
    }

    @Test
    public void testRegistrarPuntoExitoso() {
        PuntoEntrega punto = service.registrarPunto("SEDE-01", "Sede Norte", "Calle 170 # 15-20", "Bogotá");
        assertNotNull(punto);
        assertEquals("SEDE-01", punto.getId());
        assertEquals("Sede Norte", punto.getNombre());
        assertTrue(punto.isActivo());
    }

    @Test
    public void testRegistrarPuntoConIdDuplicadoDebeLanzarExcepcion() {
        service.registrarPunto("SEDE-01", "Sede Norte", "Calle 170 # 15-20", "Bogotá");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.registrarPunto("SEDE-01", "Sede Duplicada", "Carrera 7 # 45-10", "Bogotá");
        });
        assertTrue(exception.getMessage().contains("Ya existe un punto de entrega con el ID"));
    }

    @Test
    public void testDeshabilitarPuntoExitoso() {
        service.registrarPunto("SEDE-02", "Sede Centro", "Carrera 10 # 20-30", "Bogotá");
        boolean deshabilitado = service.deshabilitarPunto("SEDE-02");
        assertTrue(deshabilitado);

        List<PuntoEntrega> activos = service.listarPuntosActivos();
        assertTrue(activos.isEmpty());
    }
}