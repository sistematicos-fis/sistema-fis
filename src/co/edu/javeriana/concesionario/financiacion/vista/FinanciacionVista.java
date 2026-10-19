package co.edu.javeriana.concesionario.financiacion.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;
import co.edu.javeriana.concesionario.financiacion.logica.FinanciacionServicio;
import co.edu.javeriana.concesionario.financiacion.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.financiacion.modelo.EntidadFinanciera;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class FinanciacionVista extends BorderPane {
    private final FinanciacionServicio servicio;

    private final TextField txtCodigo;
    private final TextField txtNombre;
    private final TextField txtNit;
    private final TextField txtTasaEa;
    private final TextField txtScoring;
    private final CheckBox chkActivo;
    private final TableView<Object[]> tblEntidades;

    private Long idSeleccionado;

    public FinanciacionVista(FinanciacionServicio servicio) {
        this.servicio = servicio;

        setPadding(new Insets(15));

        txtCodigo = new TextField();
        txtNombre = new TextField();
        txtNit = new TextField();
        txtTasaEa = new TextField();
        txtScoring = new TextField();
        chkActivo = new CheckBox("Activa");
        chkActivo.setSelected(true);

        Button btnNuevo = new Button("Nuevo");
        btnNuevo.setOnAction(e -> limpiarFormulario());
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> guardarEntidad());
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarEntidad());
        Button btnRefrescar = new Button("Refrescar");
        btnRefrescar.setOnAction(e -> cargarEntidades());

        VBox pnlForm = new VBox(ComponentesFX.crearFormulario(4,
                new Label("Código (ej: BANCOL):"), txtCodigo,
                new Label("Nombre:"), txtNombre,
                new Label("NIT (ej: 890.903.938-8):"), txtNit,
                new Label("Tasa base E.A. (% ej: 16.5):"), txtTasaEa,
                new Label("Scoring mínimo (300 - 900):"), txtScoring,
                new Label("Estado:"), chkActivo),
                ComponentesFX.crearBarraBotones(Pos.CENTER_LEFT, btnNuevo, btnGuardar, btnEliminar, btnRefrescar));
        setTop(ComponentesFX.crearSeccion("Entidad Financiera Aliada (CU-11)", pnlForm));

        tblEntidades = ComponentesFX.crearTabla("ID", "Código", "Nombre", "NIT", "Tasa E.A.", "Scoring Mín.", "Estado");
        tblEntidades.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tblEntidades.getSelectionModel().selectedItemProperty().addListener((obs, anterior, fila) -> {
            if (fila != null) {
                cargarSeleccionEnFormulario((Long) fila[0]);
            }
        });
        TitledPane seccionTabla = ComponentesFX.crearSeccion(
                "Entidades registradas (seleccione una fila para editarla o eliminarla)", tblEntidades);
        seccionTabla.setMaxHeight(Double.MAX_VALUE);
        BorderPane.setMargin(seccionTabla, new Insets(10, 0, 0, 0));
        setCenter(seccionTabla);

        cargarEntidades();
    }

    private void cargarEntidades() {
        try {
            List<EntidadFinanciera> lista = servicio.listarEntidades();
            tblEntidades.getItems().clear();
            for (EntidadFinanciera e : lista) {
                tblEntidades.getItems().add(new Object[]{
                    e.getId(),
                    e.getCodigo(),
                    e.getNombre(),
                    e.getNit(),
                    FormatoUtil.formatoPorcentaje(e.getTasaBaseEa()),
                    e.getScoringMinimo(),
                    e.isActivo() ? "Activa" : "Inactiva"
                });
            }
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudieron consultar las entidades financieras: " + ex.getMessage()
                    + "\nVerifique la conexión con la base de datos e intente de nuevo.", "Error de Consulta");
        }
    }

    private void cargarSeleccionEnFormulario(Long id) {
        try {
            servicio.buscarEntidad(id).ifPresent(e -> {
                idSeleccionado = e.getId();
                txtCodigo.setText(e.getCodigo());
                txtNombre.setText(e.getNombre());
                txtNit.setText(e.getNit());
                txtTasaEa.setText(e.getTasaBaseEa().multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                txtScoring.setText(String.valueOf(e.getScoringMinimo()));
                chkActivo.setSelected(e.isActivo());
            });
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo cargar la entidad seleccionada: " + ex.getMessage(), "Error de Consulta");
        }
    }

    private void guardarEntidad() {
        EntidadFinanciera entidad;
        try {
            entidad = leerFormulario();
        } catch (NumberFormatException ex) {
            MensajeUtil.mostrarAdvertencia(this, "No se pudo guardar la entidad: la Tasa E.A. y el Scoring mínimo deben ser números "
                    + "(ej: 16.5 y 650).", "Datos Inválidos");
            return;
        }

        try {
            if (idSeleccionado == null) {
                servicio.registrarEntidad(entidad);
                MensajeUtil.mostrarInfo(this, "Entidad financiera " + entidad.getNombre() + " registrada correctamente.", "Registro Exitoso");
            } else {
                servicio.actualizarEntidad(entidad);
                MensajeUtil.mostrarInfo(this, "Entidad financiera " + entidad.getNombre() + " actualizada correctamente.", "Actualización Exitosa");
            }
            limpiarFormulario();
            cargarEntidades();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Datos Inválidos");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo guardar la entidad en la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
    }

    private void eliminarEntidad() {
        if (idSeleccionado == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione una entidad de la tabla para eliminarla.", "Aviso");
            return;
        }
        if (!MensajeUtil.confirmar(this, "¿Desea eliminar la entidad financiera " + txtNombre.getText() + "?", "Confirmar Eliminación")) {
            return;
        }
        try {
            servicio.eliminarEntidad(idSeleccionado);
            MensajeUtil.mostrarInfo(this, "Entidad financiera eliminada correctamente.", "Eliminación Exitosa");
            limpiarFormulario();
            cargarEntidades();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "No se puede eliminar");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo eliminar la entidad de la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
    }

    private EntidadFinanciera leerFormulario() {
        EntidadFinanciera entidad = new EntidadFinanciera();
        entidad.setId(idSeleccionado);
        entidad.setCodigo(txtCodigo.getText());
        entidad.setNombre(txtNombre.getText());
        entidad.setNit(txtNit.getText());
        BigDecimal tasaPorcentaje = new BigDecimal(txtTasaEa.getText().trim());
        entidad.setTasaBaseEa(tasaPorcentaje.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        entidad.setScoringMinimo(Integer.parseInt(txtScoring.getText().trim()));
        entidad.setActivo(chkActivo.isSelected());
        return entidad;
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        txtCodigo.clear();
        txtNombre.clear();
        txtNit.clear();
        txtTasaEa.clear();
        txtScoring.clear();
        chkActivo.setSelected(true);
        tblEntidades.getSelectionModel().clearSelection();
    }
}
