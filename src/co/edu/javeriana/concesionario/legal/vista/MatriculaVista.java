package co.edu.javeriana.concesionario.legal.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;
import co.edu.javeriana.concesionario.legal.logica.MatriculaServicio;
import co.edu.javeriana.concesionario.legal.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.legal.modelo.EstadoMatricula;
import co.edu.javeriana.concesionario.legal.modelo.Matricula;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Pantalla del CU-21: gestionar matrículas de vehículos (CRUD).
 */
public class MatriculaVista extends BorderPane {
    private static final String ORGANISMO_POR_DEFECTO = "Secretaría Distrital de Movilidad de Bogotá";

    private final MatriculaServicio servicio;

    private final ComboBox<VehiculoLegal> cmbVehiculo;
    private final TextField txtPlaca;
    private final TextField txtLicencia;
    private final TextField txtOrganismo;
    private final DatePicker dpFechaMatricula;
    private final TextField txtCilindraje;
    private final ComboBox<EstadoMatricula> cmbEstado;
    private final TextField txtUltimoAnioImpuesto;
    private final TextField txtValorImpuesto;
    private final TableView<Object[]> tblMatriculas;

    private Long idSeleccionado;

    public MatriculaVista(MatriculaServicio servicio) {
        this.servicio = servicio;
        setPadding(new Insets(15));

        cmbVehiculo = new ComboBox<>();
        cmbVehiculo.setPromptText("Vehículos sin matrícula");
        txtPlaca = new TextField();
        txtPlaca.setPromptText("AAA123 o AAA12A");
        cmbVehiculo.valueProperty().addListener((obs, anterior, vehiculo) -> {
            // Si el vehículo ya tiene placa en el inventario, la matrícula debe usar esa
            if (idSeleccionado == null && vehiculo != null) {
                txtPlaca.setText(vehiculo.tienePlaca() ? vehiculo.placa() : "");
                txtPlaca.setDisable(vehiculo.tienePlaca());
            }
        });
        txtLicencia = new TextField();
        txtOrganismo = new TextField(ORGANISMO_POR_DEFECTO);
        dpFechaMatricula = new DatePicker(LocalDate.now());
        txtCilindraje = new TextField();
        txtCilindraje.setPromptText("Ej: 1600");
        cmbEstado = new ComboBox<>();
        cmbEstado.getItems().addAll(EstadoMatricula.values());
        cmbEstado.setValue(EstadoMatricula.ACTIVA);
        txtUltimoAnioImpuesto = new TextField();
        txtUltimoAnioImpuesto.setPromptText("Ej: " + servicio.anioActual());
        txtValorImpuesto = new TextField();
        txtValorImpuesto.setPromptText("Opcional");

        Button btnNuevo = new Button("Nuevo");
        btnNuevo.setOnAction(e -> limpiarFormulario());
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> guardarMatricula());
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarMatricula());
        Button btnRefrescar = new Button("Refrescar");
        btnRefrescar.setOnAction(e -> {
            limpiarFormulario();
            cargarMatriculas();
        });

        VBox pnlForm = new VBox(ComponentesFX.crearFormulario(4,
                new Label("Vehículo:"), cmbVehiculo,
                new Label("Placa:"), txtPlaca,
                new Label("N° licencia de tránsito:"), txtLicencia,
                new Label("Organismo de tránsito:"), txtOrganismo,
                new Label("Fecha de matrícula:"), dpFechaMatricula,
                new Label("Cilindraje (cc):"), txtCilindraje,
                new Label("Estado del registro:"), cmbEstado,
                new Label("Último año de impuesto pagado:"), txtUltimoAnioImpuesto,
                new Label("Valor impuesto anual ($):"), txtValorImpuesto),
                ComponentesFX.crearBarraBotones(Pos.CENTER_LEFT, btnNuevo, btnGuardar, btnEliminar, btnRefrescar));
        setTop(ComponentesFX.crearSeccion("Matrícula del vehículo (CU-21)", pnlForm));

        tblMatriculas = ComponentesFX.crearTabla("ID", "Placa", "Vehículo", "Licencia de tránsito", "Organismo",
                "Fecha matrícula", "Cilindraje", "Registro", "Impuesto");
        tblMatriculas.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tblMatriculas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, fila) -> {
            if (fila != null) {
                cargarSeleccionEnFormulario((Long) fila[0]);
            }
        });
        TitledPane seccionTabla = ComponentesFX.crearSeccion(
                "Matrículas registradas (seleccione una fila para editarla o eliminarla)", tblMatriculas);
        seccionTabla.setMaxHeight(Double.MAX_VALUE);
        BorderPane.setMargin(seccionTabla, new Insets(10, 0, 0, 0));
        setCenter(seccionTabla);

        cargarVehiculosSinMatricula();
        cargarMatriculas();
    }

    private void cargarMatriculas() {
        try {
            Map<Long, VehiculoLegal> vehiculos = servicio.listarVehiculos().stream()
                    .collect(Collectors.toMap(VehiculoLegal::id, Function.identity()));
            int anioActual = servicio.anioActual();
            tblMatriculas.getItems().clear();
            for (Matricula m : servicio.listarMatriculas()) {
                VehiculoLegal v = vehiculos.get(m.getVehiculoId());
                String impuesto = m.estadoImpuesto(anioActual)
                        + (m.getUltimoAnioImpuestoPagado() == null ? "" : " (pagó " + m.getUltimoAnioImpuestoPagado() + ")");
                tblMatriculas.getItems().add(new Object[]{
                    m.getId(),
                    m.getPlaca(),
                    v == null ? "Vehículo " + m.getVehiculoId() : v.marca() + " " + v.linea() + " " + v.modeloAnio(),
                    m.getNumeroLicenciaTransito(),
                    m.getOrganismoTransito(),
                    FormatoUtil.formatoFecha(m.getFechaMatricula()),
                    m.getCilindrajeCc() + " cc",
                    m.getEstadoRegistro(),
                    impuesto
                });
            }
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudieron consultar las matrículas: " + ex.getMessage()
                    + "\nVerifique la conexión con la base de datos e intente de nuevo.", "Error de Consulta");
        }
    }

    private void cargarVehiculosSinMatricula() {
        try {
            cmbVehiculo.getItems().setAll(servicio.listarVehiculosSinMatricula());
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudieron consultar los vehículos: " + ex.getMessage(), "Error de Consulta");
        }
    }

    private void cargarSeleccionEnFormulario(Long id) {
        try {
            Matricula m = servicio.buscarMatricula(id).orElse(null);
            if (m == null) {
                return;
            }
            idSeleccionado = m.getId();
            VehiculoLegal vehiculo = servicio.listarVehiculos().stream()
                    .filter(v -> v.id().equals(m.getVehiculoId())).findFirst().orElse(null);
            cmbVehiculo.getItems().setAll(vehiculo == null ? List.of() : List.of(vehiculo));
            cmbVehiculo.setValue(vehiculo);
            cmbVehiculo.setDisable(true);
            txtPlaca.setText(m.getPlaca());
            txtPlaca.setDisable(true);
            txtLicencia.setText(m.getNumeroLicenciaTransito());
            txtOrganismo.setText(m.getOrganismoTransito());
            dpFechaMatricula.setValue(m.getFechaMatricula());
            txtCilindraje.setText(String.valueOf(m.getCilindrajeCc()));
            cmbEstado.setValue(m.getEstadoRegistro());
            txtUltimoAnioImpuesto.setText(m.getUltimoAnioImpuestoPagado() == null ? "" : String.valueOf(m.getUltimoAnioImpuestoPagado()));
            txtValorImpuesto.setText(CamposFX.mostrarMonto(m.getValorImpuestoAnual()));
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo cargar la matrícula seleccionada: " + ex.getMessage(), "Error de Consulta");
        }
    }

    private void guardarMatricula() {
        try {
            Matricula m = leerFormulario();
            if (idSeleccionado == null) {
                servicio.registrarMatricula(m);
                MensajeUtil.mostrarInfo(this, "Matrícula del vehículo " + m.getPlaca() + " registrada correctamente.", "Registro Exitoso");
            } else {
                servicio.actualizarMatricula(m);
                MensajeUtil.mostrarInfo(this, "Matrícula " + m.getPlaca() + " actualizada correctamente.", "Actualización Exitosa");
            }
            limpiarFormulario();
            cargarMatriculas();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Datos Inválidos");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo guardar la matrícula en la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
    }

    private void eliminarMatricula() {
        if (idSeleccionado == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione una matrícula de la tabla para eliminarla.", "Aviso");
            return;
        }
        if (!MensajeUtil.confirmar(this, "¿Desea eliminar la matrícula " + txtPlaca.getText() + "?", "Confirmar Eliminación")) {
            return;
        }
        try {
            servicio.eliminarMatricula(idSeleccionado);
            MensajeUtil.mostrarInfo(this, "Matrícula eliminada correctamente.", "Eliminación Exitosa");
            limpiarFormulario();
            cargarMatriculas();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "No se puede eliminar");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo eliminar la matrícula de la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
    }

    private Matricula leerFormulario() {
        Matricula m = new Matricula();
        m.setId(idSeleccionado);
        VehiculoLegal vehiculo = cmbVehiculo.getValue();
        m.setVehiculoId(vehiculo == null ? null : vehiculo.id());
        m.setPlaca(txtPlaca.getText());
        m.setNumeroLicenciaTransito(txtLicencia.getText());
        m.setOrganismoTransito(txtOrganismo.getText());
        m.setFechaMatricula(dpFechaMatricula.getValue());
        m.setCilindrajeCc(CamposFX.leerEntero(txtCilindraje, "Cilindraje"));
        m.setEstadoRegistro(cmbEstado.getValue());
        m.setUltimoAnioImpuestoPagado(CamposFX.leerEntero(txtUltimoAnioImpuesto, "Último año de impuesto pagado"));
        m.setValorImpuestoAnual(CamposFX.leerMonto(txtValorImpuesto, "Valor impuesto anual"));
        return m;
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        cmbVehiculo.setDisable(false);
        cmbVehiculo.setValue(null);
        cargarVehiculosSinMatricula();
        txtPlaca.clear();
        txtPlaca.setDisable(false);
        txtLicencia.clear();
        txtOrganismo.setText(ORGANISMO_POR_DEFECTO);
        dpFechaMatricula.setValue(LocalDate.now());
        txtCilindraje.clear();
        cmbEstado.setValue(EstadoMatricula.ACTIVA);
        txtUltimoAnioImpuesto.clear();
        txtValorImpuesto.clear();
        tblMatriculas.getSelectionModel().clearSelection();
    }
}
