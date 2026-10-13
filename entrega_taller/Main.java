import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Reporte de ventas con enfoque declarativo funcional.
 *
 * Uso: java Main <archivo.csv> <fechaInicio aaaa-mm-dd> <fechaFin aaaa-mm-dd>
 *
 * Principios aplicados:
 *  - Datos inmutables (record) y colecciones inmutables (toList()).
 *  - Ningun identificador se reasigna y no se usan ciclos for/while.
 *  - Funciones de orden superior (resumirPor, top, filas) que reciben o
 *    devuelven funciones (Function, Predicate, Comparator).
 *  - Las funciones que construyen el reporte son puras: reciben datos y
 *    devuelven un String. Los unicos efectos (leer el archivo e imprimir)
 *    quedan aislados en leerVentasCsv y main.
 */
public class Main {

    // ---------- Modelo inmutable ----------
    public record Venta(String cliente, String producto, double precio, LocalDate fecha) {}

    /** Resumen de un grupo de ventas: nombre (cliente o producto), numero de ventas y dinero total. */
    public record Resumen(String nombre, long cantidad, double total) {}

    // ---------- Constantes ----------
    private static final int TOP = 5;
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final String NL = System.lineSeparator();
    private static final String LINEA = "=================================================";
    private static final String USO =
            "Uso: java Main <archivo.csv> <fechaInicio aaaa-mm-dd> <fechaFin aaaa-mm-dd>" + NL
          + "Ejemplo: java Main ventas.csv 2026-01-01 2026-06-30";

    // ---------- Criterios de ordenamiento (funciones como valores) ----------
    private static final Comparator<Resumen> POR_NOMBRE = Comparator.comparing(Resumen::nombre);

    private static final Comparator<Resumen> POR_CANTIDAD =
            Comparator.comparingLong(Resumen::cantidad).reversed()
                    .thenComparing(Comparator.comparingDouble(Resumen::total).reversed())
                    .thenComparing(POR_NOMBRE);

    private static final Comparator<Resumen> POR_DINERO =
            Comparator.comparingDouble(Resumen::total).reversed()
                    .thenComparing(Comparator.comparingLong(Resumen::cantidad).reversed())
                    .thenComparing(POR_NOMBRE);

    // ---------- Punto de entrada: unico lugar con efecto de salida ----------
    public static void main(String[] args) {
        System.out.println(ejecutar(args));
    }

    /** Valida los parametros y devuelve el reporte o el mensaje de error correspondiente. */
    static String ejecutar(String[] args) {
        if (args.length < 3) {
            return USO;
        }
        Optional<LocalDate> inicio = parsearFecha(args[1]);
        Optional<LocalDate> fin = parsearFecha(args[2]);
        if (inicio.isEmpty() || fin.isEmpty()) {
            return "Error: formato de fecha invalido. Use aaaa-mm-dd" + NL + USO;
        }
        if (inicio.get().isAfter(fin.get())) {
            return "Error: la fecha inicial es posterior a la fecha final";
        }
        return leerVentasCsv(args[0])
                .map(ventas -> generarReporte(ventas, inicio.get(), fin.get()))
                .orElse("Error: no se pudo leer el archivo " + args[0]);
    }

    // ---------- Lectura y transformacion de datos ----------
    static Optional<LocalDate> parsearFecha(String texto) {
        try {
            return Optional.of(LocalDate.parse(texto.trim()));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    /** Lee el CSV como un flujo de lineas; devuelve Optional.empty() si el archivo no se puede leer. */
    static Optional<List<Venta>> leerVentasCsv(String ruta) {
        try (Stream<String> lineas = Files.lines(Path.of(ruta), StandardCharsets.UTF_8)) {
            return Optional.of(lineas
                    .skip(1)                       // encabezado
                    .map(String::trim)
                    .filter(Predicate.not(String::isEmpty))
                    .map(Main::lineaAVenta)
                    .flatMap(Optional::stream)     // descarta lineas invalidas
                    .toList());
        } catch (IOException | UncheckedIOException e) {
            return Optional.empty();
        }
    }

    /** Convierte una linea en Venta. Acepta ';' (formato del enunciado) o ',' (archivo de ejemplo). */
    static Optional<Venta> lineaAVenta(String linea) {
        String[] campos = linea.split(linea.contains(";") ? ";" : ",");
        if (campos.length < 4) {
            return Optional.empty();
        }
        try {
            return Optional.of(new Venta(
                    campos[0].trim(),
                    campos[1].trim(),
                    Double.parseDouble(campos[2].trim()),
                    LocalDate.parse(campos[3].trim())));
        } catch (NumberFormatException | DateTimeParseException e) {
            return Optional.empty();
        }
    }

    /** Funcion de orden superior: devuelve el predicado "la venta esta dentro del periodo". */
    static Predicate<Venta> enPeriodo(LocalDate inicio, LocalDate fin) {
        return v -> !v.fecha().isBefore(inicio) && !v.fecha().isAfter(fin);
    }

    /** Funcion de orden superior: agrupa las ventas por la clave indicada (cliente, producto, ...). */
    static List<Resumen> resumirPor(List<Venta> ventas, Function<Venta, String> clave) {
        return ventas.stream()
                .collect(Collectors.groupingBy(clave))
                .entrySet().stream()
                .map(grupo -> new Resumen(
                        grupo.getKey(),
                        grupo.getValue().size(),
                        grupo.getValue().stream().mapToDouble(Venta::precio).sum()))
                .toList();
    }

    /** Funcion de orden superior: los primeros TOP elementos segun el criterio recibido. */
    static <T> List<T> top(List<T> elementos, Comparator<T> criterio) {
        return elementos.stream().sorted(criterio).limit(TOP).toList();
    }

    // ---------- Construccion del reporte (funciones puras) ----------
    static String generarReporte(List<Venta> todas, LocalDate inicio, LocalDate fin) {
        List<Venta> ventas = todas.stream().filter(enPeriodo(inicio, fin)).toList();
        List<Resumen> clientes = resumirPor(ventas, Venta::cliente);
        List<Resumen> productos = resumirPor(ventas, Venta::producto);
        double recaudado = ventas.stream().mapToDouble(Venta::precio).sum();

        return Stream.of(
                LINEA,
                "           REPORTE DE VENTAS",
                "Periodo: " + inicio + " a " + fin,
                LINEA,
                String.format("Totales del periodo: Ventas Registradas -> %-6d Dinero Recaudado-> $%s",
                        ventas.size(), pesos(recaudado)),
                "",
                "--- CLIENTES QUE MAS COMPRARON (por cantidad de ventas) ---",
                String.format("%-4s %-25s %10s %15s", "#", "Cliente", "Cantidad", "Dinero gastado"),
                filas(top(clientes, POR_CANTIDAD), (i, r) -> String.format("%-4d %-25s %10d %15s",
                        i, r.nombre(), r.cantidad(), pesos(r.total()))),
                "",
                "--- CLIENTES QUE MAS COMPRARON (por dinero) ---",
                String.format("%-4s %-25s %15s", "#", "Cliente", "Dinero gastado"),
                filas(top(clientes, POR_DINERO), (i, r) -> String.format("%-4d %-25s %15s",
                        i, r.nombre(), pesos(r.total()))),
                "",
                "--- PRODUCTOS MAS VENDIDOS (por ingresos generados) ---",
                String.format("%-4s %-25s %10s %15s", "#", "Producto", "Unidades", "Ingresos"),
                filas(top(productos, POR_DINERO), (i, r) -> String.format("%-4d %-25s %10d %15s",
                        i, r.nombre(), r.cantidad(), pesos(r.total()))),
                "",
                LINEA,
                "           FIN DEL REPORTE",
                LINEA)
                .collect(Collectors.joining(NL));
    }

    /** Funcion de orden superior: convierte un ranking en lineas de texto numeradas desde 1. */
    static String filas(List<Resumen> ranking, BiFunction<Integer, Resumen, String> formato) {
        return ranking.isEmpty()
                ? "(sin ventas en el periodo)"
                : IntStream.range(0, ranking.size())
                        .mapToObj(i -> formato.apply(i + 1, ranking.get(i)))
                        .collect(Collectors.joining(NL));
    }

    /** Formato de pesos colombianos: separador de miles '.' y sin decimales (ej. 14.210.000). */
    static String pesos(double valor) {
        return String.format(COLOMBIA, "%,.0f", valor);
    }
}
