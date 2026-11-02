package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.CalculadoraIndiceSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.BaseCalculoIndice;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.modelo.EncuestaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.Pqrs;
import co.edu.javeriana.concesionario.clientes.modelo.ResultadoIndiceSatisfaccion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalculadoraIndiceSatisfaccionTest {

    private static final LocalDateTime RADICACION = LocalDateTime.of(2026, 9, 1, 10, 0);

    private static EncuestaSatisfaccion encuesta(int atencion, int producto, int tiempos) {
        return new EncuestaSatisfaccion(1L, 1L, atencion, producto, tiempos, "prueba");
    }

    private static Pqrs pqrsResuelta(String tipo, int diasResolucion) {
        Pqrs p = new Pqrs(1L, 1L, tipo, "prueba", "CERRADO");
        p.setFechaRadicacion(RADICACION);
        p.setFechaResolucion(RADICACION.plusDays(diasResolucion));
        return p;
    }

    private static Cliente cliente() {
        Cliente c = new Cliente();
        c.setId(7L);
        c.setDocumento("123");
        c.setNombres("Ana");
        c.setApellidos("Prueba");
        c.setAsesorId(2L);
        return c;
    }

    @Test
    public void notaPonderadaUsaPesos40_35_25() {
        assertEquals(new BigDecimal("3.15"), CalculadoraIndiceSatisfaccion.calcularNotaPonderadaEncuesta(encuesta(4, 3, 2)));
    }

    @Test
    public void puntajeEncuestaSeNormalizaDe0a100() {
        assertEquals(new BigDecimal("100.00"), CalculadoraIndiceSatisfaccion.calcularPuntajeEncuesta(encuesta(5, 5, 5)));
        assertEquals(new BigDecimal("0.00"), CalculadoraIndiceSatisfaccion.calcularPuntajeEncuesta(encuesta(1, 1, 1)));
        assertEquals(new BigDecimal("53.75"), CalculadoraIndiceSatisfaccion.calcularPuntajeEncuesta(encuesta(4, 3, 2)));
    }

    @Test
    public void puntuacionFueraDeRangoEsRechazada() {
        assertThrows(IllegalArgumentException.class,
                () -> CalculadoraIndiceSatisfaccion.calcularPuntajeEncuesta(encuesta(6, 3, 3)));
        assertThrows(IllegalArgumentException.class,
                () -> CalculadoraIndiceSatisfaccion.calcularPuntajeEncuesta(encuesta(3, 0, 3)));
    }

    @Test
    public void componenteEncuestasEsElPromedio() {
        BigDecimal e = CalculadoraIndiceSatisfaccion.calcularComponenteEncuestas(
                List.of(encuesta(5, 5, 5), encuesta(1, 1, 1)));
        assertEquals(new BigDecimal("50.00"), e);
    }

    @Test
    public void sinEncuestasElComponenteEsNulo() {
        assertNull(CalculadoraIndiceSatisfaccion.calcularComponenteEncuestas(Collections.emptyList()));
        assertNull(CalculadoraIndiceSatisfaccion.calcularComponenteEncuestas(null));
    }

    @Test
    public void oportunidadCompletaSiSeResuelveDentroDelSla() {
        assertEquals(new BigDecimal("100.00"), CalculadoraIndiceSatisfaccion.calcularPuntajeOportunidad(10, 10));
        assertEquals(new BigDecimal("100.00"), CalculadoraIndiceSatisfaccion.calcularPuntajeOportunidad(0, 10));
    }

    @Test
    public void oportunidadBajaEnLineaRectaConElRetraso() {
        assertEquals(new BigDecimal("20.00"), CalculadoraIndiceSatisfaccion.calcularPuntajeOportunidad(18, 10));
        assertEquals(new BigDecimal("50.00"), CalculadoraIndiceSatisfaccion.calcularPuntajeOportunidad(15, 10));
    }

    @Test
    public void oportunidadEsCeroAlDobleDelSla() {
        assertEquals(new BigDecimal("0.00"), CalculadoraIndiceSatisfaccion.calcularPuntajeOportunidad(20, 10));
        assertEquals(new BigDecimal("0.00"), CalculadoraIndiceSatisfaccion.calcularPuntajeOportunidad(45, 10));
    }

    @Test
    public void puntajePqrsCombinaTipoYOportunidad() {
        assertEquals(new BigDecimal("100.00"), CalculadoraIndiceSatisfaccion.calcularPuntajePqrs(pqrsResuelta("PETICION", 5)));
        assertEquals(new BigDecimal("70.00"), CalculadoraIndiceSatisfaccion.calcularPuntajePqrs(pqrsResuelta("RECLAMO", 10)));
        assertEquals(new BigDecimal("30.00"), CalculadoraIndiceSatisfaccion.calcularPuntajePqrs(pqrsResuelta("RECLAMO", 18)));
        assertEquals(new BigDecimal("30.00"), CalculadoraIndiceSatisfaccion.calcularPuntajePqrs(pqrsResuelta("QUEJA", 25)));
    }

    @Test
    public void pqrsSinResolverNoCuenta() {
        Pqrs abierta = new Pqrs(2L, 1L, "RECLAMO", "abierta", "ABIERTO");
        abierta.setFechaRadicacion(RADICACION);
        assertEquals(new BigDecimal("100.00"),
                CalculadoraIndiceSatisfaccion.calcularComponentePqrs(List.of(pqrsResuelta("PETICION", 3), abierta)));
        assertNull(CalculadoraIndiceSatisfaccion.calcularComponentePqrs(List.of(abierta)));
    }

    @Test
    public void resolucionAnteriorARadicacionEsRechazada() {
        Pqrs p = pqrsResuelta("QUEJA", 0);
        p.setFechaResolucion(RADICACION.minusDays(2));
        assertThrows(IllegalArgumentException.class, () -> CalculadoraIndiceSatisfaccion.calcularPuntajePqrs(p));
    }

    @Test
    public void tipoDePqrsInvalidoEsRechazado() {
        assertThrows(IllegalArgumentException.class,
                () -> CalculadoraIndiceSatisfaccion.calcularPuntajePqrs(pqrsResuelta("FELICITACION", 1)));
    }

    @Test
    public void flujoBasicoCombina60EncuestasY40Pqrs() {
        assertEquals(new BigDecimal("36.50"),
                CalculadoraIndiceSatisfaccion.combinarComponentes(new BigDecimal("27.50"), new BigDecimal("50.00")));
        assertEquals(BaseCalculoIndice.COMBINADO,
                CalculadoraIndiceSatisfaccion.determinarBaseCalculo(BigDecimal.ONE, BigDecimal.ONE));
    }

    @Test
    public void flujo2ASinEncuestasUsaSoloPqrs() {
        assertEquals(new BigDecimal("68.34"),
                CalculadoraIndiceSatisfaccion.combinarComponentes(null, new BigDecimal("68.34")));
        assertEquals(BaseCalculoIndice.SOLO_PQRS,
                CalculadoraIndiceSatisfaccion.determinarBaseCalculo(null, BigDecimal.ONE));
    }

    @Test
    public void flujo3ASinPqrsUsaSoloEncuestas() {
        assertEquals(new BigDecimal("92.50"),
                CalculadoraIndiceSatisfaccion.combinarComponentes(new BigDecimal("92.50"), null));
        assertEquals(BaseCalculoIndice.SOLO_ENCUESTAS,
                CalculadoraIndiceSatisfaccion.determinarBaseCalculo(BigDecimal.ONE, null));
    }

    @Test
    public void sinEncuestasNiPqrsNoSePuedeCalcular() {
        assertThrows(IllegalStateException.class,
                () -> CalculadoraIndiceSatisfaccion.combinarComponentes(null, null));
    }

    @Test
    public void clasificacionRespetaLosUmbrales() {
        assertEquals(CategoriaSatisfaccion.SATISFECHO, CalculadoraIndiceSatisfaccion.clasificar(new BigDecimal("100.00")));
        assertEquals(CategoriaSatisfaccion.SATISFECHO, CalculadoraIndiceSatisfaccion.clasificar(new BigDecimal("70.00")));
        assertEquals(CategoriaSatisfaccion.NEUTRAL, CalculadoraIndiceSatisfaccion.clasificar(new BigDecimal("69.99")));
        assertEquals(CategoriaSatisfaccion.NEUTRAL, CalculadoraIndiceSatisfaccion.clasificar(new BigDecimal("50.00")));
        assertEquals(CategoriaSatisfaccion.EN_RIESGO, CalculadoraIndiceSatisfaccion.clasificar(new BigDecimal("49.99")));
        assertEquals(CategoriaSatisfaccion.EN_RIESGO, CalculadoraIndiceSatisfaccion.clasificar(new BigDecimal("0.00")));
    }

    @Test
    public void clienteEnRiesgoConEncuestasYPqrs() {
        ResultadoIndiceSatisfaccion r = CalculadoraIndiceSatisfaccion.calcularIndiceCliente(cliente(),
                List.of(encuesta(2, 3, 1)),
                List.of(pqrsResuelta("RECLAMO", 18), pqrsResuelta("QUEJA", 12)));

        assertEquals(new BigDecimal("27.50"), r.getComponenteEncuestas());
        assertEquals(new BigDecimal("50.00"), r.getComponentePqrs());
        assertEquals(new BigDecimal("36.50"), r.getIndice());
        assertEquals(CategoriaSatisfaccion.EN_RIESGO, r.getCategoria());
        assertEquals(BaseCalculoIndice.COMBINADO, r.getBaseCalculo());
        assertEquals(1, r.getCantidadEncuestas());
        assertEquals(2, r.getCantidadPqrs());
        assertEquals(2L, r.getAsesorId());
        assertTrue(r.requiereAlerta());
    }

    @Test
    public void clienteSinDatosIncumplePrecondicion() {
        assertThrows(IllegalStateException.class, () -> CalculadoraIndiceSatisfaccion.calcularIndiceCliente(
                cliente(), Collections.emptyList(), Collections.emptyList()));
    }
}
