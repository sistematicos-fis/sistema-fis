package co.edu.javeriana.concesionario.legal;

import co.edu.javeriana.concesionario.comun.simuladores.AseguradoraExterna.ConsultaRestricciones;
import co.edu.javeriana.concesionario.comun.simuladores.AseguradoraExterna.RespuestaEmision;
import co.edu.javeriana.concesionario.comun.simuladores.AseguradoraExterna.SolicitudEmisionSoat;
import co.edu.javeriana.concesionario.comun.simuladores.SimuladorAseguradora;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class SimuladorAseguradoraTest {
    private final SimuladorAseguradora simulador = new SimuladorAseguradora();

    private SolicitudEmisionSoat solicitud(String aseguradora, String tipo, BigDecimal valor) {
        LocalDate hoy = LocalDate.now();
        return new SolicitudEmisionSoat(aseguradora, "KLU382", tipo, 1798, hoy, hoy.plusYears(1).minusDays(1), valor);
    }

    @Test
    public void testEmiteSoatConNumeroYAutorizacion() {
        RespuestaEmision r = simulador.emitirSoat(solicitud("SURA", "AUTOMOVIL", new BigDecimal("570000")));
        assertTrue(r.aprobada());
        assertTrue(r.numeroPoliza().startsWith("SOAT-SURA-"));
        assertNotNull(r.codigoAutorizacion());
    }

    @Test
    public void testAllianzNoExpideSoatDeMotos() {
        RespuestaEmision r = simulador.emitirSoat(solicitud("ALLIANZ", "MOTOCICLETA", new BigDecimal("790000")));
        assertFalse(r.aprobada());
        assertTrue(r.motivoRechazo().contains("motocicleta"));
    }

    @Test
    public void testValorInvalidoEsRechazado() {
        assertFalse(simulador.emitirSoat(solicitud("SURA", "AUTOMOVIL", BigDecimal.ZERO)).aprobada());
    }

    @Test
    public void testRestriccionesDeDemostracion() {
        ConsultaRestricciones conSiniestro = simulador.consultarRestricciones("SURA", "LMP941");
        assertTrue(conSiniestro.siniestrosPendientes());
        assertFalse(simulador.consultarRestricciones("SURA", "KLU382").tieneRestricciones());
    }
}
