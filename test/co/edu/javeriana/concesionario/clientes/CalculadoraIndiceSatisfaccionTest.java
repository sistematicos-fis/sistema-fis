package co.edu.javeriana.concesionario.clientes;

import co.edu.javeriana.concesionario.clientes.logica.CalculadoraIndiceSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.EncuestaSatisfaccion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CalculadoraIndiceSatisfaccionTest {

    @Test
    public void testCalculoIndiceIndividual() {
        EncuestaSatisfaccion enc = new EncuestaSatisfaccion(1L, 1L, 5, 5, 5, "Excelente");
        BigDecimal puntaje = CalculadoraIndiceSatisfaccion.calcularIndiceIndividual(enc);
        assertEquals(new BigDecimal("5.00"), puntaje);
        assertEquals(CategoriaSatisfaccion.MUY_SATISFECHO, CalculadoraIndiceSatisfaccion.clasificarNivel(puntaje));
    }

    @Test
    public void testCalculoIndicePonderadoDiferentesPuntajes() {
        // Atencion=4 (40% = 1.6), Producto=3 (35% = 1.05), Tiempos=2 (25% = 0.50) -> Total = 3.15
        EncuestaSatisfaccion enc = new EncuestaSatisfaccion(2L, 1L, 4, 3, 2, "Regular");
        BigDecimal puntaje = CalculadoraIndiceSatisfaccion.calcularIndiceIndividual(enc);
        assertEquals(new BigDecimal("3.15"), puntaje);
        assertEquals(CategoriaSatisfaccion.NEUTRO, CalculadoraIndiceSatisfaccion.clasificarNivel(puntaje));
    }
}
