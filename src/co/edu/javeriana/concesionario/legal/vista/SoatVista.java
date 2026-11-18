package co.edu.javeriana.concesionario.legal.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;
import co.edu.javeriana.concesionario.legal.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.legal.logica.SoatServicio;
import co.edu.javeriana.concesionario.legal.modelo.Aseguradora;
import co.edu.javeriana.concesionario.legal.modelo.EstadoDocumental;
import co.edu.javeriana.concesionario.legal.modelo.RegistroDocumental;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRenovacion;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRenovacionSoat;
import co.edu.javeriana.concesionario.legal.modelo.Soat;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Pantalla del CU-24: validar y renovar el SOAT de un vehículo, con su historial y estado documental.
 */
public class SoatVista extends BorderPane {
    private static final String ESTILO_BASE = "-fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 4; -fx-text-fill: white; ";
    private static final String COLOR_EXITO = "-fx-background-color: #228b22;";
    private static final String COLOR_INFORMATIVO = "-fx-background-color: #1d4ed8;";
    private static final String COLOR_BLOQUEADO = "-fx-background-color: #b00020;";

    private final SoatServicio servicio;

    // Formulario de renovación (paso 1 del CU-24)
    private final TextField txtPlaca;
    private final ComboBox<Aseguradora> cmbAseguradora;
    private final Label lblVehiculo;
    private final Label lblResultado;

    // Registro de un SOAT expedido por fuera del sistema
    private final TextField txtNumeroExistente;
    private final ComboBox<Aseguradora> cmbAseguradoraExistente;
    private final DatePicker dpInicioExistente;
    private final TextField txtValorExistente;

    private final TableView<Object[]> tblHistorial;

    public SoatVista(SoatServicio servicio) {
        this.servicio = servicio;
        setPadding(new Insets(15));

        txtPlaca = new TextField();
        txtPlaca.setPromptText("Ej: KLU382");
        txtPlaca.setOnAction(e -> consultar());
        txtPlaca.textProperty().addListener((obs, anterior, nuevo) -> ocultarResultado());
        cmbAseguradora = new ComboBox<>();
        cmbAseguradora.setPromptText("Activas con convenio SOAT");
        Button btnConsultar = new Button("Consultar");
        btnConsultar.setOnAction(e -> consultar());
        Button btnRenovar = new Button("Validar y renovar SOAT");
        btnRenovar.setStyle("-fx-background-color: #228b22; -fx-text-fill: white; -fx-font-weight: bold;");
        btnRenovar.setOnAction(e -> validarYRenovar());

        lblVehiculo = new Label();
        lblVehiculo.setWrapText(true);
        lblResultado = new Label();
        lblResultado.setWrapText(true);
        lblResultado.setMaxWidth(Double.MAX_VALUE);
        ocultarResultado();

        VBox pnlRenovar = new VBox(8, ComponentesFX.crearFormulario(4,
                new Label("Placa del vehículo:"), txtPlaca,
                new Label("Aseguradora:"), cmbAseguradora,
                btnConsultar, btnRenovar),
                lblVehiculo, lblResultado);

        txtNumeroExistente = new TextField();
        cmbAseguradoraExistente = new ComboBox<>();
        dpInicioExistente = new DatePicker(LocalDate.now());
        txtValorExistente = new TextField();
        txtValorExistente.setPromptText("Ej: 570000");
        Button btnRegistrarExistente = new Button("Registrar SOAT");
        btnRegistrarExistente.setOnAction(e -> registrarSoatExistente());

        VBox pnlExistente = new VBox(ComponentesFX.crearFormulario(4,
                new Label("N° de póliza SOAT:"), txtNumeroExistente,
                new Label("Aseguradora que lo expidió:"), cmbAseguradoraExistente,
                new Label("Inicio de vigencia:"), dpInicioExistente,
                new Label("Valor pagado ($):"), txtValorExistente,
                btnRegistrarExistente));

        VBox arriba = new VBox(10,
                ComponentesFX.crearSeccion("Validar y renovar SOAT del vehículo (CU-24)", pnlRenovar),
                ComponentesFX.crearSeccion("Registrar un SOAT expedido por fuera del sistema (para la placa de arriba)", pnlExistente));
        setTop(arriba);

        tblHistorial = ComponentesFX.crearTabla("ID", "N° Póliza", "Aseguradora", "Inicio", "Vencimiento", "Valor",
                "Cód. Autorización", "Estado");
        TitledPane seccionTabla = ComponentesFX.crearSeccion("Historial de SOAT del vehículo consultado", tblHistorial);
        seccionTabla.setMaxHeight(Double.MAX_VALUE);
        BorderPane.setMargin(seccionTabla, new Insets(10, 0, 0, 0));
        setCenter(seccionTabla);

        cargarAseguradoras();
    }

    private void cargarAseguradoras() {
        try {
            cmbAseguradora.getItems().setAll(servicio.listarAseguradorasSoat());
            cmbAseguradoraExistente.getItems().setAll(servicio.listarAseguradoras().stream()
                    .filter(Aseguradora::isCubreSoat).toList());
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudieron consultar las aseguradoras: " + ex.getMessage()
                    + "\nVerifique la conexión con la base de datos e intente de nuevo.", "Error de Consulta");
        }
    }

    /** Muestra el vehículo, su estado documental y su historial de SOAT. Devuelve false si no se encontró. */
    private boolean consultar() {
        tblHistorial.getItems().clear();
        lblVehiculo.setText("");
        try {
            VehiculoLegal vehiculo = servicio.buscarVehiculoPorPlaca(txtPlaca.getText());
            RegistroDocumental estado = servicio.consultarEstadoDocumental(vehiculo.id());
            lblVehiculo.setText("Vehículo: " + vehiculo + " (" + vehiculo.tipo().toLowerCase() + ")\n"
                    + "Estado documental: " + estado.estado()
                    + (estado.observacion() == null ? "" : " — " + estado.observacion()));
            lblVehiculo.setStyle(estado.estado() == EstadoDocumental.AL_DIA
                    ? "-fx-text-fill: #166534;" : "-fx-text-fill: #b00020;");
            cargarHistorial(vehiculo.id());
            return true;
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Vehículo no encontrado");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo consultar el SOAT en la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
        return false;
    }

    private void cargarHistorial(Long vehiculoId) throws SQLException {
        Map<Long, Aseguradora> aseguradoras = servicio.listarAseguradoras().stream()
                .collect(Collectors.toMap(Aseguradora::getId, Function.identity()));
        LocalDate hoy = LocalDate.now();
        for (Soat s : servicio.listarSoats(vehiculoId)) {
            Aseguradora a = aseguradoras.get(s.getAseguradoraId());
            String estado = s.estaVigente(hoy) ? "Vigente"
                    : s.getFechaInicio().isAfter(hoy) ? "Programado (inicia " + FormatoUtil.formatoFecha(s.getFechaInicio()) + ")"
                    : "Vencido";
            tblHistorial.getItems().add(new Object[]{
                s.getId(),
                s.getNumeroPoliza(),
                a == null ? "Aseguradora " + s.getAseguradoraId() : a.getNombre(),
                FormatoUtil.formatoFecha(s.getFechaInicio()),
                FormatoUtil.formatoFecha(s.getFechaVencimiento()),
                FormatoUtil.formatoMoneda(s.getValorPagado()),
                s.getCodigoAutorizacion() == null ? "—" : s.getCodigoAutorizacion(),
                estado
            });
        }
    }

    private void validarYRenovar() {
        Aseguradora aseguradora = cmbAseguradora.getValue();
        if (aseguradora == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione la aseguradora con la que se renovará el SOAT.", "Datos Incompletos");
            return;
        }
        if (!consultar()) {
            return;
        }
        try {
            ResultadoRenovacionSoat resultado = servicio.validarYRenovar(txtPlaca.getText(), aseguradora.getId());
            mostrarResultado(resultado);
            consultar();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "No se puede renovar");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo registrar la renovación en la base de datos: " + ex.getMessage()
                    + "\nNo se guardó ningún cambio.", "Error de Base de Datos");
        }
    }

    private void mostrarResultado(ResultadoRenovacionSoat resultado) {
        String color;
        String titulo;
        switch (resultado.resultado()) {
            case RENOVADO -> {
                color = COLOR_EXITO;
                titulo = "SOAT RENOVADO";
            }
            case NO_REQUIERE_RENOVACION -> {
                color = COLOR_INFORMATIVO;
                titulo = "NO REQUIERE RENOVACIÓN";
            }
            case NO_CUMPLE_REQUISITOS -> {
                color = COLOR_BLOQUEADO;
                titulo = "NO CUMPLE REQUISITOS";
            }
            default -> {
                color = COLOR_BLOQUEADO;
                titulo = "RECHAZADO POR LA ASEGURADORA";
            }
        }
        lblResultado.setText(titulo + "\n" + resultado.mensaje());
        lblResultado.setStyle(ESTILO_BASE + color);
        lblResultado.setVisible(true);
        lblResultado.setManaged(true);
        if (resultado.resultado() == ResultadoRenovacion.RENOVADO) {
            MensajeUtil.mostrarInfo(this, resultado.mensaje(), "Renovación Exitosa");
        }
    }

    private void ocultarResultado() {
        lblResultado.setVisible(false);
        lblResultado.setManaged(false);
    }

    private void registrarSoatExistente() {
        Aseguradora aseguradora = cmbAseguradoraExistente.getValue();
        try {
            Soat soat = servicio.registrarSoatExistente(txtPlaca.getText(), txtNumeroExistente.getText(),
                    aseguradora == null ? null : aseguradora.getId(), dpInicioExistente.getValue(),
                    CamposFX.leerMonto(txtValorExistente, "Valor pagado"));
            MensajeUtil.mostrarInfo(this, "SOAT " + soat.getNumeroPoliza() + " registrado. Vigente hasta el "
                    + FormatoUtil.formatoFecha(soat.getFechaVencimiento()) + ".", "Registro Exitoso");
            txtNumeroExistente.clear();
            txtValorExistente.clear();
            dpInicioExistente.setValue(LocalDate.now());
            ocultarResultado();
            consultar();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Datos Inválidos");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo guardar el SOAT en la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
    }
}
