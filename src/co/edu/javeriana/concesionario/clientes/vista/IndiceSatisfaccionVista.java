package co.edu.javeriana.concesionario.clientes.vista;

import co.edu.javeriana.concesionario.clientes.logica.IndiceSatisfaccionServicio;
import co.edu.javeriana.concesionario.clientes.logica.IndiceSatisfaccionServicio.ResumenCalculo;
import co.edu.javeriana.concesionario.clientes.modelo.AlertaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.ResultadoIndiceSatisfaccion;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IndiceSatisfaccionVista {

    private static final AtomicBoolean TOOLKIT_INICIADO = new AtomicBoolean(false);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IndiceSatisfaccionServicio servicio = new IndiceSatisfaccionServicio();

    private final DatePicker dpDesde = new DatePicker(
            LocalDate.now().minusDays(IndiceSatisfaccionServicio.DIAS_PERIODO_POR_DEFECTO));
    private final DatePicker dpHasta = new DatePicker(LocalDate.now());
    private final Button btnCalcular = new Button("Calcular índices");
    private final Label lblResumen = new Label("Seleccione el período y presione \"Calcular índices\".");

    private final ObservableList<ResultadoIndiceSatisfaccion> resultados = FXCollections.observableArrayList();
    private final ObservableList<AlertaSatisfaccion> alertas = FXCollections.observableArrayList();
    private final TableView<ResultadoIndiceSatisfaccion> tblResultados = new TableView<>(resultados);
    private final TableView<AlertaSatisfaccion> tblAlertas = new TableView<>(alertas);

    private Stage stage;

    public static void abrir() {
        if (TOOLKIT_INICIADO.compareAndSet(false, true)) {
            try {
                Platform.startup(IndiceSatisfaccionVista::crearYMostrar);
                Platform.setImplicitExit(false);
                return;
            } catch (IllegalStateException yaIniciado) {
            }
        }
        Platform.runLater(IndiceSatisfaccionVista::crearYMostrar);
    }

    private static void crearYMostrar() {
        new IndiceSatisfaccionVista().mostrar();
    }

    private void mostrar() {
        stage = new Stage();
        stage.setTitle("Índice combinado de satisfacción (CU-05)");

        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(15));
        raiz.setTop(crearEncabezado());
        raiz.setCenter(crearTablaResultados());
        raiz.setBottom(crearPanelAlertas());

        stage.setScene(new Scene(raiz, 1050, 640));
        stage.show();

        cargarAlertas();
    }

    private VBox crearEncabezado() {
        Label titulo = new Label("Índice combinado de satisfacción del cliente");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label formula = new Label("Índice = 60 % encuestas + 40 % PQRS (si falta una de las dos, se usa solo la otra).  "
                + "Satisfecho ≥ 70 · Neutral 50 a 69,99 · En riesgo < 50");
        formula.setStyle("-fx-text-fill: #555555;");

        btnCalcular.setDefaultButton(true);
        btnCalcular.setOnAction(e -> calcular());

        HBox filtros = new HBox(10, new Label("Desde:"), dpDesde, new Label("Hasta:"), dpHasta, btnCalcular);
        filtros.setAlignment(Pos.CENTER_LEFT);

        lblResumen.setStyle("-fx-font-weight: bold;");

        VBox encabezado = new VBox(8, titulo, formula, filtros, lblResumen);
        encabezado.setPadding(new Insets(0, 0, 10, 0));
        return encabezado;
    }

    private TableView<ResultadoIndiceSatisfaccion> crearTablaResultados() {
        tblResultados.getColumns().add(columna("Cliente", 210, ResultadoIndiceSatisfaccion::getNombreCliente));
        tblResultados.getColumns().add(columna("Documento", 100, ResultadoIndiceSatisfaccion::getDocumentoCliente));
        tblResultados.getColumns().add(columna("Encuestas", 75, r -> String.valueOf(r.getCantidadEncuestas())));
        tblResultados.getColumns().add(columna("Comp. encuestas", 110, r -> numero(r.getComponenteEncuestas())));
        tblResultados.getColumns().add(columna("PQRS resueltas", 100, r -> String.valueOf(r.getCantidadPqrs())));
        tblResultados.getColumns().add(columna("Comp. PQRS", 90, r -> numero(r.getComponentePqrs())));
        tblResultados.getColumns().add(columna("Base de cálculo", 120, r -> r.getBaseCalculo().getEtiqueta()));
        tblResultados.getColumns().add(columna("Índice", 70, r -> numero(r.getIndice())));
        tblResultados.getColumns().add(columna("Categoría", 100, r -> r.getCategoria().getEtiqueta()));

        tblResultados.setRowFactory(tabla -> new TableRow<>() {
            @Override
            protected void updateItem(ResultadoIndiceSatisfaccion item, boolean vacio) {
                super.updateItem(item, vacio);
                if (vacio || item == null) {
                    setStyle("");
                    return;
                }
                switch (item.getCategoria()) {
                    case EN_RIESGO:
                        setStyle("-fx-background-color: #f8d7da;");
                        break;
                    case NEUTRAL:
                        setStyle("-fx-background-color: #fff3cd;");
                        break;
                    default:
                        setStyle("-fx-background-color: #d4edda;");
                }
            }
        });
        tblResultados.setPlaceholder(new Label("Todavía no se ha calculado el índice para este período."));
        return tblResultados;
    }

    private VBox crearPanelAlertas() {
        Label titulo = new Label("Alertas pendientes para el asesor comercial");
        titulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        tblAlertas.getColumns().add(columnaAlerta("Fecha", 130,
                a -> a.getFecha() != null ? a.getFecha().format(FORMATO_FECHA) : "-"));
        tblAlertas.getColumns().add(columnaAlerta("Cliente", 200, AlertaSatisfaccion::getNombreCliente));
        tblAlertas.getColumns().add(columnaAlerta("Índice", 70, a -> numero(a.getIndice())));
        tblAlertas.getColumns().add(columnaAlerta("Asesor (id)", 85,
                a -> a.getAsesorId() != null ? String.valueOf(a.getAsesorId()) : "Sin asignar"));
        tblAlertas.getColumns().add(columnaAlerta("Mensaje", 480, AlertaSatisfaccion::getMensaje));
        tblAlertas.setPlaceholder(new Label("No hay alertas pendientes."));
        tblAlertas.setPrefHeight(170);

        Button btnAtendida = new Button("Marcar alerta como atendida");
        btnAtendida.setOnAction(e -> marcarAtendida());

        VBox panel = new VBox(8, titulo, tblAlertas, btnAtendida);
        panel.setPadding(new Insets(12, 0, 0, 0));
        VBox.setVgrow(tblAlertas, Priority.ALWAYS);
        return panel;
    }

    private void calcular() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        Task<ResumenCalculo> tarea = new Task<>() {
            @Override
            protected ResumenCalculo call() throws Exception {
                return servicio.calcularIndices(desde, hasta);
            }
        };

        tarea.setOnSucceeded(e -> {
            btnCalcular.setDisable(false);
            mostrarResultado(tarea.getValue());
        });

        tarea.setOnFailed(e -> {
            btnCalcular.setDisable(false);
            Throwable error = tarea.getException();
            if (error instanceof IllegalArgumentException || error instanceof IllegalStateException) {
                mostrarMensaje(Alert.AlertType.WARNING, "No se pudo calcular el índice", error.getMessage());
            } else {
                mostrarMensaje(Alert.AlertType.ERROR, "No se pudo calcular el índice",
                        "Ocurrió un error al consultar o guardar la información en la base de datos ("
                        + error.getMessage() + "). No se modificó ningún cliente. Intente de nuevo y, "
                        + "si el problema continúa, reinicie la aplicación.");
            }
        });

        btnCalcular.setDisable(true);
        lblResumen.setText("Calculando...");
        Thread hilo = new Thread(tarea, "calculo-indice-satisfaccion");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarResultado(ResumenCalculo resumen) {
        resultados.setAll(resumen.getResultados());
        cargarAlertas();

        if (resumen.estaVacio()) {
            lblResumen.setText("Sin clientes para evaluar en el período.");
            mostrarMensaje(Alert.AlertType.INFORMATION, "Sin datos en el período",
                    "No hay clientes con encuestas respondidas ni PQRS resueltas entre "
                    + dpDesde.getValue() + " y " + dpHasta.getValue()
                    + ". Amplíe el período e intente de nuevo.");
            return;
        }

        long satisfechos = contar(resumen, CategoriaSatisfaccion.SATISFECHO);
        long neutrales = contar(resumen, CategoriaSatisfaccion.NEUTRAL);
        long enRiesgo = resumen.contarEnRiesgo();
        lblResumen.setText("Clientes evaluados: " + resumen.getResultados().size()
                + "   ·   Satisfechos: " + satisfechos
                + "   ·   Neutrales: " + neutrales
                + "   ·   En riesgo: " + enRiesgo);

        if (enRiesgo > 0) {
            String nombres = resumen.getResultados().stream()
                    .filter(ResultadoIndiceSatisfaccion::requiereAlerta)
                    .map(r -> "• " + r.getNombreCliente() + " (índice " + numero(r.getIndice()) + ")")
                    .collect(Collectors.joining("\n"));
            mostrarMensaje(Alert.AlertType.WARNING, "Clientes en riesgo",
                    IndiceSatisfaccionServicio.MENSAJE_CLIENTE_EN_RIESGO + "\n\n" + nombres
                    + "\n\nAlertas nuevas generadas para el asesor comercial: " + resumen.getAlertasGeneradas());
        }

        mostrarMensaje(Alert.AlertType.INFORMATION, "Cálculo completado",
                "Se calculó y guardó el índice de satisfacción de " + resumen.getResultados().size()
                + " cliente(s). Puede revisar el detalle en la tabla.");
    }

    private void cargarAlertas() {
        try {
            List<AlertaSatisfaccion> pendientes = servicio.listarAlertasPendientes();
            alertas.setAll(pendientes);
        } catch (Exception e) {
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudieron cargar las alertas",
                    "Error al consultar las alertas pendientes (" + e.getMessage()
                    + "). Cierre y vuelva a abrir esta ventana.");
        }
    }

    private void marcarAtendida() {
        AlertaSatisfaccion seleccionada = tblAlertas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarMensaje(Alert.AlertType.WARNING, "Seleccione una alerta",
                    "Seleccione en la tabla la alerta que ya fue atendida y presione el botón de nuevo.");
            return;
        }
        try {
            servicio.marcarAlertaAtendida(seleccionada.getId());
            cargarAlertas();
        } catch (Exception e) {
            mostrarMensaje(Alert.AlertType.ERROR, "No se pudo actualizar la alerta",
                    "Error al marcar la alerta como atendida (" + e.getMessage() + "). Intente de nuevo.");
        }
    }

    private static TableColumn<ResultadoIndiceSatisfaccion, String> columna(
            String titulo, double ancho, Function<ResultadoIndiceSatisfaccion, String> valor) {
        TableColumn<ResultadoIndiceSatisfaccion, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(ancho);
        col.setCellValueFactory(celda -> new SimpleStringProperty(valor.apply(celda.getValue())));
        return col;
    }

    private static TableColumn<AlertaSatisfaccion, String> columnaAlerta(
            String titulo, double ancho, Function<AlertaSatisfaccion, String> valor) {
        TableColumn<AlertaSatisfaccion, String> col = new TableColumn<>(titulo);
        col.setPrefWidth(ancho);
        col.setCellValueFactory(celda -> new SimpleStringProperty(valor.apply(celda.getValue())));
        return col;
    }

    private static String numero(BigDecimal valor) {
        return valor != null ? valor.toPlainString() : "—";
    }

    private static long contar(ResumenCalculo resumen, CategoriaSatisfaccion categoria) {
        return resumen.getResultados().stream()
                .filter(r -> r.getCategoria() == categoria)
                .count();
    }

    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.initOwner(stage);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
