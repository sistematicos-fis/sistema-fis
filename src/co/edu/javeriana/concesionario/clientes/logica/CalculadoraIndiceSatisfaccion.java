package co.edu.javeriana.concesionario.clientes.logica;

import co.edu.javeriana.concesionario.clientes.modelo.BaseCalculoIndice;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.modelo.EncuestaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.Pqrs;
import co.edu.javeriana.concesionario.clientes.modelo.ResultadoIndiceSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.TipoPqrs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;

public final class CalculadoraIndiceSatisfaccion {

    static final BigDecimal PESO_ATENCION = new BigDecimal("0.40");
    static final BigDecimal PESO_PRODUCTO = new BigDecimal("0.35");
    static final BigDecimal PESO_TIEMPOS = new BigDecimal("0.25");

    static final BigDecimal PESO_TIPO_PQRS = new BigDecimal("0.50");
    static final BigDecimal PESO_OPORTUNIDAD_PQRS = new BigDecimal("0.50");

    static final BigDecimal PESO_ENCUESTAS = new BigDecimal("0.60");
    static final BigDecimal PESO_PQRS = new BigDecimal("0.40");

    static final BigDecimal UMBRAL_SATISFECHO = new BigDecimal("70");
    static final BigDecimal UMBRAL_NEUTRAL = new BigDecimal("50");

    static final int PUNTUACION_MINIMA = 1;
    static final int PUNTUACION_MAXIMA = 5;

    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal VEINTICINCO = new BigDecimal("25");
    private static final int ESCALA = 2;

    private CalculadoraIndiceSatisfaccion() {
    }

    public static BigDecimal calcularNotaPonderadaEncuesta(EncuestaSatisfaccion encuesta) {
        validarEncuesta(encuesta);
        BigDecimal atencion = BigDecimal.valueOf(encuesta.getPuntuacionAtencion()).multiply(PESO_ATENCION);
        BigDecimal producto = BigDecimal.valueOf(encuesta.getPuntuacionProducto()).multiply(PESO_PRODUCTO);
        BigDecimal tiempos = BigDecimal.valueOf(encuesta.getPuntuacionTiempos()).multiply(PESO_TIEMPOS);
        return atencion.add(producto).add(tiempos).setScale(ESCALA, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcularPuntajeEncuesta(EncuestaSatisfaccion encuesta) {
        BigDecimal nota = calcularNotaPonderadaEncuesta(encuesta);
        return nota.subtract(BigDecimal.ONE).multiply(VEINTICINCO).setScale(ESCALA, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcularComponenteEncuestas(List<EncuestaSatisfaccion> encuestas) {
        if (encuestas == null || encuestas.isEmpty()) {
            return null;
        }
        BigDecimal suma = BigDecimal.ZERO;
        for (EncuestaSatisfaccion encuesta : encuestas) {
            suma = suma.add(calcularPuntajeEncuesta(encuesta));
        }
        return suma.divide(BigDecimal.valueOf(encuestas.size()), ESCALA, RoundingMode.HALF_UP);
    }

    public static long calcularDiasResolucion(Pqrs pqrs) {
        if (pqrs == null || pqrs.getFechaRadicacion() == null || !pqrs.estaResuelta()) {
            throw new IllegalArgumentException("La PQRS debe tener fecha de radicación y de resolución.");
        }
        long dias = ChronoUnit.DAYS.between(pqrs.getFechaRadicacion().toLocalDate(),
                                            pqrs.getFechaResolucion().toLocalDate());
        if (dias < 0) {
            throw new IllegalArgumentException("La PQRS " + pqrs.getId()
                    + " tiene una fecha de resolución anterior a su fecha de radicación.");
        }
        return dias;
    }

    public static BigDecimal calcularPuntajeOportunidad(long diasResolucion, int diasSla) {
        if (diasResolucion <= diasSla) {
            return CIEN.setScale(ESCALA);
        }
        long diasRetraso = diasResolucion - diasSla;
        if (diasRetraso >= diasSla) {
            return BigDecimal.ZERO.setScale(ESCALA);
        }
        BigDecimal proporcionRetraso = BigDecimal.valueOf(diasRetraso)
                .divide(BigDecimal.valueOf(diasSla), 4, RoundingMode.HALF_UP);
        return BigDecimal.ONE.subtract(proporcionRetraso).multiply(CIEN).setScale(ESCALA, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcularPuntajePqrs(Pqrs pqrs) {
        TipoPqrs tipo = TipoPqrs.desdeTexto(pqrs.getTipo());
        long dias = calcularDiasResolucion(pqrs);
        BigDecimal base = BigDecimal.valueOf(tipo.getPuntajeBase());
        BigDecimal oportunidad = calcularPuntajeOportunidad(dias, tipo.getDiasSla());
        return base.multiply(PESO_TIPO_PQRS)
                .add(oportunidad.multiply(PESO_OPORTUNIDAD_PQRS))
                .setScale(ESCALA, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcularComponentePqrs(List<Pqrs> pqrs) {
        if (pqrs == null) {
            return null;
        }
        BigDecimal suma = BigDecimal.ZERO;
        int resueltas = 0;
        for (Pqrs p : pqrs) {
            if (p != null && p.estaResuelta()) {
                suma = suma.add(calcularPuntajePqrs(p));
                resueltas++;
            }
        }
        if (resueltas == 0) {
            return null;
        }
        return suma.divide(BigDecimal.valueOf(resueltas), ESCALA, RoundingMode.HALF_UP);
    }

    public static BaseCalculoIndice determinarBaseCalculo(BigDecimal componenteEncuestas, BigDecimal componentePqrs) {
        if (componenteEncuestas != null && componentePqrs != null) {
            return BaseCalculoIndice.COMBINADO;
        }
        if (componenteEncuestas != null) {
            return BaseCalculoIndice.SOLO_ENCUESTAS;
        }
        if (componentePqrs != null) {
            return BaseCalculoIndice.SOLO_PQRS;
        }
        throw new IllegalStateException(
                "El cliente no tiene encuestas respondidas ni PQRS resueltas en el período: no se puede calcular el índice.");
    }

    public static BigDecimal combinarComponentes(BigDecimal componenteEncuestas, BigDecimal componentePqrs) {
        switch (determinarBaseCalculo(componenteEncuestas, componentePqrs)) {
            case COMBINADO:
                return componenteEncuestas.multiply(PESO_ENCUESTAS)
                        .add(componentePqrs.multiply(PESO_PQRS))
                        .setScale(ESCALA, RoundingMode.HALF_UP);
            case SOLO_ENCUESTAS:
                return componenteEncuestas.setScale(ESCALA, RoundingMode.HALF_UP);
            default:
                return componentePqrs.setScale(ESCALA, RoundingMode.HALF_UP);
        }
    }

    public static CategoriaSatisfaccion clasificar(BigDecimal indice) {
        if (indice == null) {
            throw new IllegalArgumentException("El índice a clasificar no puede ser nulo.");
        }
        if (indice.compareTo(UMBRAL_SATISFECHO) >= 0) {
            return CategoriaSatisfaccion.SATISFECHO;
        }
        if (indice.compareTo(UMBRAL_NEUTRAL) >= 0) {
            return CategoriaSatisfaccion.NEUTRAL;
        }
        return CategoriaSatisfaccion.EN_RIESGO;
    }

    public static ResultadoIndiceSatisfaccion calcularIndiceCliente(Cliente cliente,
                                                                    List<EncuestaSatisfaccion> encuestas,
                                                                    List<Pqrs> pqrs) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente es obligatorio para calcular el índice.");
        }
        BigDecimal componenteEncuestas = calcularComponenteEncuestas(encuestas);
        BigDecimal componentePqrs = calcularComponentePqrs(pqrs);
        BaseCalculoIndice base = determinarBaseCalculo(componenteEncuestas, componentePqrs);
        BigDecimal indice = combinarComponentes(componenteEncuestas, componentePqrs);
        CategoriaSatisfaccion categoria = clasificar(indice);

        int cantidadEncuestas = encuestas == null ? 0 : encuestas.size();
        int cantidadPqrs = pqrs == null ? 0 : (int) pqrs.stream().filter(p -> p != null && p.estaResuelta()).count();

        return new ResultadoIndiceSatisfaccion(cliente.getId(), cliente.getDocumento(), cliente.getNombreCompleto(),
                cliente.getAsesorId(), cantidadEncuestas, cantidadPqrs,
                componenteEncuestas, componentePqrs, indice, base, categoria);
    }

    private static void validarEncuesta(EncuestaSatisfaccion encuesta) {
        if (encuesta == null) {
            throw new IllegalArgumentException("La encuesta no puede ser nula.");
        }
        validarPuntuacion(encuesta.getPuntuacionAtencion(), "Atención", encuesta);
        validarPuntuacion(encuesta.getPuntuacionProducto(), "Producto", encuesta);
        validarPuntuacion(encuesta.getPuntuacionTiempos(), "Tiempos", encuesta);
    }

    private static void validarPuntuacion(int valor, String pregunta, EncuestaSatisfaccion encuesta) {
        if (valor < PUNTUACION_MINIMA || valor > PUNTUACION_MAXIMA) {
            throw new IllegalArgumentException("La encuesta " + encuesta.getId() + " tiene una puntuación de "
                    + pregunta + " fuera del rango 1 a 5: " + valor);
        }
    }
}
