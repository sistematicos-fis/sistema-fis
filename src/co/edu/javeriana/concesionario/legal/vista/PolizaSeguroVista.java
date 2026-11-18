package co.edu.javeriana.concesionario.legal.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;
import co.edu.javeriana.concesionario.legal.logica.PolizaSeguroServicio;
import co.edu.javeriana.concesionario.legal.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.legal.modelo.Aseguradora;
import co.edu.javeriana.concesionario.legal.modelo.EstadoPoliza;
import co.edu.javeriana.concesionario.legal.modelo.PolizaSeguro;
import co.edu.javeriana.concesionario.legal.modelo.TipoPoliza;
import co.edu.javeriana.concesionario.legal.modelo.VehiculoLegal;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
 * Pantalla del CU-23: gestionar las pólizas multirriesgo de la flota de alquiler (CRUD).
 */
public class PolizaSeguroVista extends BorderPane {
    private final PolizaSeguroServicio servicio;

    private final TextField txtNumero;
    private final ComboBox<Aseguradora> cmbAseguradora;
    private final ComboBox<VehiculoLegal> cmbVehiculo;
    private final ComboBox<TipoPoliza> cmbTipo;
    private final TextField txtPrima;
    private final TextField txtCobertura;
    private final DatePicker dpInicio;
    private final DatePicker dpFin;
    private final CheckBox chkCancelada;
    private final TableView<Object[]> tblPolizas;

    private Long idSeleccionado;

    public PolizaSeguroVista(PolizaSeguroServicio servicio) {
        this.servicio = servicio;
        setPadding(new Insets(15));

        txtNumero = new TextField();
        cmbAseguradora = new ComboBox<>();
        cmbAseguradora.setPromptText("Activas con cobertura a terceros");
        cmbVehiculo = new ComboBox<>();
        cmbVehiculo.setPromptText("Unidades de la flota");
        cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll(TipoPoliza.values());
        cmbTipo.setValue(TipoPoliza.TODO_RIESGO_RESPONSABILIDAD);
        txtPrima = new TextField();
        txtPrima.setPromptText("Ej: 3500000");
        txtCobertura = new TextField();
        txtCobertura.setPromptText("Ej: 120000000");
        dpInicio = new DatePicker(LocalDate.now());
        dpFin = new DatePicker(LocalDate.now().plusYears(1).minusDays(1));
        // Al cambiar el inicio de una póliza nueva se propone la vigencia anual
        dpInicio.valueProperty().addListener((obs, anterior, inicio) -> {
            if (idSeleccionado == null && inicio != null) {
                dpFin.setValue(inicio.plusYears(1).minusDays(1));
            }
        });
        chkCancelada = new CheckBox("Póliza cancelada");

        Button btnNuevo = new Button("Nuevo");
        btnNuevo.setOnAction(e -> limpiarFormulario());
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> guardarPoliza());
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarPoliza());
        Button btnRefrescar = new Button("Refrescar");
        btnRefrescar.setOnAction(e -> {
            limpiarFormulario();
            cargarPolizas();
        });

        VBox pnlForm = new VBox(ComponentesFX.crearFormulario(4,
                new Label("N° de póliza:"), txtNumero,
                new Label("Tipo de póliza:"), cmbTipo,
                new Label("Vehículo de la flota:"), cmbVehiculo,
                new Label("Aseguradora:"), cmbAseguradora,
                new Label("Prima anual ($):"), txtPrima,
                new Label("Valor asegurado ($):"), txtCobertura,
                new Label("Inicio de vigencia:"), dpInicio,
                new Label("Fin de vigencia:"), dpFin,
                new Label("Estado:"), chkCancelada),
                ComponentesFX.crearBarraBotones(Pos.CENTER_LEFT, btnNuevo, btnGuardar, btnEliminar, btnRefrescar));
        setTop(ComponentesFX.crearSeccion("Póliza multirriesgo de la flota de alquiler (CU-23)", pnlForm));

        tblPolizas = ComponentesFX.crearTabla("ID", "N° Póliza", "Tipo", "Vehículo", "Aseguradora", "Prima anual",
                "Valor asegurado", "Vigencia", "Estado");
        tblPolizas.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tblPolizas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, fila) -> {
            if (fila != null) {
                cargarSeleccionEnFormulario((Long) fila[0]);
            }
        });
        TitledPane seccionTabla = ComponentesFX.crearSeccion(
                "Pólizas registradas (seleccione una fila para editarla o eliminarla)", tblPolizas);
        seccionTabla.setMaxHeight(Double.MAX_VALUE);
        BorderPane.setMargin(seccionTabla, new Insets(10, 0, 0, 0));
        setCenter(seccionTabla);

        cargarOpcionesParaNueva();
        cargarPolizas();
    }

    private void cargarPolizas() {
        try {
            Map<Long, VehiculoLegal> vehiculos = servicio.listarVehiculos().stream()
                    .collect(Collectors.toMap(VehiculoLegal::id, Function.identity()));
            Map<Long, Aseguradora> aseguradoras = servicio.listarAseguradoras().stream()
                    .collect(Collectors.toMap(Aseguradora::getId, Function.identity()));
            tblPolizas.getItems().clear();
            for (PolizaSeguro p : servicio.listarPolizas()) {
                VehiculoLegal v = vehiculos.get(p.getVehiculoId());
                Aseguradora a = aseguradoras.get(p.getAseguradoraId());
                tblPolizas.getItems().add(new Object[]{
                    p.getId(),
                    p.getNumeroPoliza(),
                    p.getTipo(),
                    v == null ? "Vehículo " + p.getVehiculoId() : v.toString(),
                    a == null ? "Aseguradora " + p.getAseguradoraId() : a.getNombre(),
                    FormatoUtil.formatoMoneda(p.getValorPrima()),
                    FormatoUtil.formatoMoneda(p.getValorCobertura()),
                    FormatoUtil.formatoFecha(p.getFechaInicio()) + " a " + FormatoUtil.formatoFecha(p.getFechaVencimiento()),
                    servicio.estadoEfectivo(p)
                });
            }
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudieron consultar las pólizas: " + ex.getMessage()
                    + "\nVerifique la conexión con la base de datos e intente de nuevo.", "Error de Consulta");
        }
    }

    /** Para una póliza nueva solo se ofrecen unidades de la flota y aseguradoras disponibles. */
    private void cargarOpcionesParaNueva() {
        try {
            cmbVehiculo.getItems().setAll(servicio.listarVehiculosFlota());
            cmbAseguradora.getItems().setAll(servicio.listarAseguradorasDisponibles());
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudieron consultar los vehículos y aseguradoras: " + ex.getMessage(), "Error de Consulta");
        }
    }

    private void cargarSeleccionEnFormulario(Long id) {
        try {
            PolizaSeguro p = servicio.buscarPoliza(id).orElse(null);
            if (p == null) {
                return;
            }
            idSeleccionado = p.getId();
            // Al editar se agregan el vehículo y la aseguradora de la póliza aunque ya no estén disponibles
            List<VehiculoLegal> vehiculos = new ArrayList<>(servicio.listarVehiculosFlota());
            servicio.listarVehiculos().stream()
                    .filter(v -> v.id().equals(p.getVehiculoId()) && vehiculos.stream().noneMatch(f -> f.id().equals(v.id())))
                    .forEach(vehiculos::add);
            cmbVehiculo.getItems().setAll(vehiculos);
            cmbVehiculo.setValue(vehiculos.stream().filter(v -> v.id().equals(p.getVehiculoId())).findFirst().orElse(null));

            List<Aseguradora> aseguradoras = new ArrayList<>(servicio.listarAseguradorasDisponibles());
            servicio.listarAseguradoras().stream()
                    .filter(a -> a.getId().equals(p.getAseguradoraId()) && aseguradoras.stream().noneMatch(d -> d.getId().equals(a.getId())))
                    .forEach(aseguradoras::add);
            cmbAseguradora.getItems().setAll(aseguradoras);
            cmbAseguradora.setValue(aseguradoras.stream().filter(a -> a.getId().equals(p.getAseguradoraId())).findFirst().orElse(null));

            txtNumero.setText(p.getNumeroPoliza());
            cmbTipo.setValue(p.getTipo());
            txtPrima.setText(CamposFX.mostrarMonto(p.getValorPrima()));
            txtCobertura.setText(CamposFX.mostrarMonto(p.getValorCobertura()));
            dpInicio.setValue(p.getFechaInicio());
            dpFin.setValue(p.getFechaVencimiento());
            chkCancelada.setSelected(p.estaCancelada());
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo cargar la póliza seleccionada: " + ex.getMessage(), "Error de Consulta");
        }
    }

    private void guardarPoliza() {
        try {
            PolizaSeguro p = leerFormulario();
            if (idSeleccionado == null) {
                servicio.registrarPoliza(p);
                MensajeUtil.mostrarInfo(this, "Póliza " + p.getNumeroPoliza() + " registrada correctamente.", "Registro Exitoso");
            } else {
                servicio.actualizarPoliza(p);
                MensajeUtil.mostrarInfo(this, "Póliza " + p.getNumeroPoliza() + " actualizada correctamente.", "Actualización Exitosa");
            }
            limpiarFormulario();
            cargarPolizas();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Datos Inválidos");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo guardar la póliza en la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
    }

    private void eliminarPoliza() {
        if (idSeleccionado == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione una póliza de la tabla para eliminarla.", "Aviso");
            return;
        }
        if (!MensajeUtil.confirmar(this, "¿Desea eliminar la póliza " + txtNumero.getText() + "?", "Confirmar Eliminación")) {
            return;
        }
        try {
            servicio.eliminarPoliza(idSeleccionado);
            MensajeUtil.mostrarInfo(this, "Póliza eliminada correctamente.", "Eliminación Exitosa");
            limpiarFormulario();
            cargarPolizas();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "No se puede eliminar");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo eliminar la póliza de la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
    }

    private PolizaSeguro leerFormulario() {
        PolizaSeguro p = new PolizaSeguro();
        p.setId(idSeleccionado);
        p.setNumeroPoliza(txtNumero.getText());
        Aseguradora aseguradora = cmbAseguradora.getValue();
        p.setAseguradoraId(aseguradora == null ? null : aseguradora.getId());
        VehiculoLegal vehiculo = cmbVehiculo.getValue();
        p.setVehiculoId(vehiculo == null ? null : vehiculo.id());
        p.setTipo(cmbTipo.getValue());
        p.setValorPrima(CamposFX.leerMonto(txtPrima, "Prima anual"));
        p.setValorCobertura(CamposFX.leerMonto(txtCobertura, "Valor asegurado"));
        p.setFechaInicio(dpInicio.getValue());
        p.setFechaVencimiento(dpFin.getValue());
        p.setEstado(chkCancelada.isSelected() ? EstadoPoliza.CANCELADA : EstadoPoliza.VIGENTE);
        return p;
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        txtNumero.clear();
        cmbTipo.setValue(TipoPoliza.TODO_RIESGO_RESPONSABILIDAD);
        cargarOpcionesParaNueva();
        cmbVehiculo.setValue(null);
        cmbAseguradora.setValue(null);
        txtPrima.clear();
        txtCobertura.clear();
        dpInicio.setValue(LocalDate.now());
        dpFin.setValue(LocalDate.now().plusYears(1).minusDays(1));
        chkCancelada.setSelected(false);
        tblPolizas.getSelectionModel().clearSelection();
    }
}
