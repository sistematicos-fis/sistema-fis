package co.edu.javeriana.concesionario.legal.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;
import co.edu.javeriana.concesionario.legal.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.legal.logica.RevisionTecnicoMecanicaServicio;
import co.edu.javeriana.concesionario.legal.modelo.EstadoRtm;
import co.edu.javeriana.concesionario.legal.modelo.OperacionComercial;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoRevision;
import co.edu.javeriana.concesionario.legal.modelo.ResultadoVerificacionRtm;
import co.edu.javeriana.concesionario.legal.modelo.RevisionTecnicoMecanica;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoRtm;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
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
 * Pantalla del CU-25: verificar la vigencia de la revisión técnico-mecánica antes de habilitar un
 * vehículo para una venta o un alquiler, y registrar los certificados emitidos por los CDA.
 */
public class RevisionTecnicoMecanicaVista extends BorderPane {
    private static final String ESTILO_BASE = "-fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 4; -fx-text-fill: white; ";
    private static final String COLOR_HABILITADO = "-fx-background-color: #228b22;";
    private static final String COLOR_ADVERTENCIA = "-fx-background-color: #c2410c;";
    private static final String COLOR_BLOQUEADO = "-fx-background-color: #b00020;";

    private final RevisionTecnicoMecanicaServicio servicio;

    private final ComboBox<VehiculoRtm> cmbVehiculo;
    private final ComboBox<OperacionComercial> cmbOperacion;
    private final DatePicker dpFinAlquiler;
    private final Label lblResultado;

    private final TextField txtCertificado;
    private final TextField txtCentro;
    private final DatePicker dpExpedicion;
    private final ComboBox<ResultadoRevision> cmbResultado;

    private final TableView<Object[]> tblRevisiones;

    public RevisionTecnicoMecanicaVista(RevisionTecnicoMecanicaServicio servicio) {
        this.servicio = servicio;
        setPadding(new Insets(15));

        // --- Verificación ---
        lblResultado = new Label();
        lblResultado.setWrapText(true);
        lblResultado.setMaxWidth(Double.MAX_VALUE);
        ocultarResultado();

        cmbVehiculo = new ComboBox<>();
        cmbVehiculo.setPromptText("Seleccione un vehículo");
        cmbVehiculo.valueProperty().addListener((obs, anterior, nuevo) -> {
            ocultarResultado();
            cargarRevisiones();
        });
        cmbOperacion = new ComboBox<>();
        cmbOperacion.getItems().addAll(OperacionComercial.values());
        cmbOperacion.getSelectionModel().selectFirst();
        dpFinAlquiler = new DatePicker();
        dpFinAlquiler.setPromptText("Fecha de devolución");
        dpFinAlquiler.setDisable(true);
        cmbOperacion.valueProperty().addListener((obs, anterior, nuevo) -> {
            boolean esAlquiler = nuevo == OperacionComercial.ALQUILER;
            dpFinAlquiler.setDisable(!esAlquiler);
            if (!esAlquiler) {
                dpFinAlquiler.setValue(null);
            }
        });
        Button btnVerificar = new Button("Verificar vigencia");
        btnVerificar.setOnAction(e -> verificar());

        VBox pnlVerificar = new VBox(8, ComponentesFX.crearFormulario(4,
                new Label("Vehículo:"), cmbVehiculo,
                new Label("Operación:"), cmbOperacion,
                new Label("Devolución del alquiler:"), dpFinAlquiler,
                btnVerificar),
                lblResultado);
        cmbVehiculo.setMaxWidth(Double.MAX_VALUE);

        // --- Registro de certificados ---
        txtCertificado = new TextField();
        txtCentro = new TextField();
        dpExpedicion = new DatePicker(LocalDate.now());
        cmbResultado = new ComboBox<>();
        cmbResultado.getItems().addAll(ResultadoRevision.values());
        cmbResultado.getSelectionModel().selectFirst();
        Button btnRegistrar = new Button("Registrar revisión");
        btnRegistrar.setOnAction(e -> registrar());

        VBox pnlRegistrar = new VBox(ComponentesFX.crearFormulario(4,
                new Label("N° de certificado:"), txtCertificado,
                new Label("Centro de diagnóstico (CDA):"), txtCentro,
                new Label("Fecha de expedición:"), dpExpedicion,
                new Label("Resultado:"), cmbResultado,
                btnRegistrar));

        VBox arriba = new VBox(10,
                ComponentesFX.crearSeccion("Verificar vigencia antes de vender o alquilar (CU-25)", pnlVerificar),
                ComponentesFX.crearSeccion("Registrar revisión técnico-mecánica del vehículo seleccionado", pnlRegistrar));
        setTop(arriba);

        tblRevisiones = ComponentesFX.crearTabla("ID", "N° Certificado", "Centro de diagnóstico", "Expedición", "Vencimiento", "Resultado");
        TitledPane seccionTabla = ComponentesFX.crearSeccion("Historial de revisiones del vehículo seleccionado", tblRevisiones);
        seccionTabla.setMaxHeight(Double.MAX_VALUE);
        BorderPane.setMargin(seccionTabla, new Insets(10, 0, 0, 0));
        setCenter(seccionTabla);

        cargarVehiculos();
    }

    private void cargarVehiculos() {
        try {
            VehiculoRtm seleccionado = cmbVehiculo.getValue();
            cmbVehiculo.getItems().setAll(servicio.listarVehiculos());
            cmbVehiculo.setValue(seleccionado);
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudieron consultar los vehículos: " + ex.getMessage()
                    + "\nVerifique la conexión con la base de datos e intente de nuevo.", "Error de Consulta");
        }
    }

    private void cargarRevisiones() {
        tblRevisiones.getItems().clear();
        VehiculoRtm vehiculo = cmbVehiculo.getValue();
        if (vehiculo == null) {
            return;
        }
        try {
            List<RevisionTecnicoMecanica> lista = servicio.listarRevisiones(vehiculo.id());
            for (RevisionTecnicoMecanica r : lista) {
                tblRevisiones.getItems().add(new Object[]{
                    r.getId(),
                    r.getNumeroCertificado(),
                    r.getCentroDiagnostico(),
                    FormatoUtil.formatoFecha(r.getFechaExpedicion()),
                    r.getFechaVencimiento() == null ? "—" : FormatoUtil.formatoFecha(r.getFechaVencimiento()),
                    r.getResultado()
                });
            }
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo consultar el historial de revisiones: " + ex.getMessage(), "Error de Consulta");
        }
    }

    private void verificar() {
        OperacionComercial operacion = cmbOperacion.getValue();
        if (operacion == OperacionComercial.ALQUILER && dpFinAlquiler.getValue() == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione la fecha de devolución: la revisión debe estar vigente durante todo el alquiler.", "Datos Incompletos");
            return;
        }
        try {
            VehiculoRtm vehiculo = cmbVehiculo.getValue();
            ResultadoVerificacionRtm resultado = servicio.verificarVigencia(
                    vehiculo == null ? null : vehiculo.id(), operacion, dpFinAlquiler.getValue());
            mostrarResultado(resultado);
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Datos Inválidos");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo verificar la revisión en la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
    }

    private void mostrarResultado(ResultadoVerificacionRtm resultado) {
        String color;
        String titulo;
        if (!resultado.habilitado()) {
            color = COLOR_BLOQUEADO;
            titulo = "NO HABILITADO — " + resultado.estado();
        } else if (resultado.estado() == EstadoRtm.POR_VENCER) {
            color = COLOR_ADVERTENCIA;
            titulo = "HABILITADO (con aviso) — " + resultado.estado();
        } else {
            color = COLOR_HABILITADO;
            titulo = "HABILITADO — " + resultado.estado();
        }
        lblResultado.setText(titulo + "\n" + resultado.motivo());
        lblResultado.setStyle(ESTILO_BASE + color);
        lblResultado.setVisible(true);
        lblResultado.setManaged(true);
    }

    private void ocultarResultado() {
        lblResultado.setVisible(false);
        lblResultado.setManaged(false);
    }

    private void registrar() {
        VehiculoRtm vehiculo = cmbVehiculo.getValue();
        RevisionTecnicoMecanica revision = new RevisionTecnicoMecanica();
        revision.setVehiculoId(vehiculo == null ? null : vehiculo.id());
        revision.setNumeroCertificado(txtCertificado.getText());
        revision.setCentroDiagnostico(txtCentro.getText());
        revision.setFechaExpedicion(dpExpedicion.getValue());
        revision.setResultado(cmbResultado.getValue());
        try {
            servicio.registrarRevision(revision);
            MensajeUtil.mostrarInfo(this, "Revisión " + revision.getNumeroCertificado() + " registrada correctamente.", "Registro Exitoso");
            txtCertificado.clear();
            txtCentro.clear();
            dpExpedicion.setValue(LocalDate.now());
            cargarRevisiones();
            ocultarResultado();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Datos Inválidos");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo guardar la revisión en la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
    }
}
