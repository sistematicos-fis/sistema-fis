package co.edu.javeriana.concesionario.legal.vista;

import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;
import co.edu.javeriana.concesionario.legal.logica.AseguradoraServicio;
import co.edu.javeriana.concesionario.legal.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.legal.modelo.Aseguradora;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Pantalla del CU-22: gestionar aseguradoras y sus convenios de pólizas (CRUD).
 */
public class AseguradoraVista extends BorderPane {
    private final AseguradoraServicio servicio;

    private final TextField txtCodigo;
    private final TextField txtNombre;
    private final TextField txtNit;
    private final TextField txtTelefono;
    private final CheckBox chkSoat;
    private final CheckBox chkResponsabilidadCivil;
    private final CheckBox chkActivo;
    private final TableView<Object[]> tblAseguradoras;

    private Long idSeleccionado;

    public AseguradoraVista(AseguradoraServicio servicio) {
        this.servicio = servicio;

        setPadding(new Insets(15));

        txtCodigo = new TextField();
        txtNombre = new TextField();
        txtNit = new TextField();
        txtTelefono = new TextField();
        chkSoat = new CheckBox("SOAT (obligatoria)");
        chkSoat.setSelected(true);
        chkResponsabilidadCivil = new CheckBox("Resp. civil (daños materiales a terceros)");
        chkResponsabilidadCivil.setSelected(true);
        chkActivo = new CheckBox("Activa");
        chkActivo.setSelected(true);

        Button btnNuevo = new Button("Nuevo");
        btnNuevo.setOnAction(e -> limpiarFormulario());
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> guardarAseguradora());
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarAseguradora());
        Button btnRefrescar = new Button("Refrescar");
        btnRefrescar.setOnAction(e -> cargarAseguradoras());

        VBox pnlForm = new VBox(ComponentesFX.crearFormulario(4,
                new Label("Código (ej: SURA):"), txtCodigo,
                new Label("Nombre:"), txtNombre,
                new Label("NIT (ej: 890.903.407-9):"), txtNit,
                new Label("Teléfono de contacto:"), txtTelefono,
                new Label("El convenio cubre:"), new HBox(15, chkSoat, chkResponsabilidadCivil),
                new Label("Estado:"), chkActivo),
                ComponentesFX.crearBarraBotones(Pos.CENTER_LEFT, btnNuevo, btnGuardar, btnEliminar, btnRefrescar));
        setTop(ComponentesFX.crearSeccion("Aseguradora Aliada (CU-22)", pnlForm));

        tblAseguradoras = ComponentesFX.crearTabla("ID", "Código", "Nombre", "NIT", "Teléfono", "SOAT", "Resp. Civil", "Estado");
        tblAseguradoras.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tblAseguradoras.getSelectionModel().selectedItemProperty().addListener((obs, anterior, fila) -> {
            if (fila != null) {
                cargarSeleccionEnFormulario((Long) fila[0]);
            }
        });
        TitledPane seccionTabla = ComponentesFX.crearSeccion(
                "Aseguradoras registradas (seleccione una fila para editarla o eliminarla)", tblAseguradoras);
        seccionTabla.setMaxHeight(Double.MAX_VALUE);
        BorderPane.setMargin(seccionTabla, new Insets(10, 0, 0, 0));
        setCenter(seccionTabla);

        cargarAseguradoras();
    }

    private void cargarAseguradoras() {
        try {
            List<Aseguradora> lista = servicio.listarAseguradoras();
            tblAseguradoras.getItems().clear();
            for (Aseguradora a : lista) {
                tblAseguradoras.getItems().add(new Object[]{
                    a.getId(),
                    a.getCodigo(),
                    a.getNombre(),
                    a.getNit(),
                    a.getTelefonoContacto() == null ? "" : a.getTelefonoContacto(),
                    a.isCubreSoat() ? "Sí" : "No",
                    a.isCubreResponsabilidadCivil() ? "Sí" : "No",
                    a.isActivo() ? "Activa" : "Inactiva"
                });
            }
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudieron consultar las aseguradoras: " + ex.getMessage()
                    + "\nVerifique la conexión con la base de datos e intente de nuevo.", "Error de Consulta");
        }
    }

    private void cargarSeleccionEnFormulario(Long id) {
        try {
            servicio.buscarAseguradora(id).ifPresent(a -> {
                idSeleccionado = a.getId();
                txtCodigo.setText(a.getCodigo());
                txtNombre.setText(a.getNombre());
                txtNit.setText(a.getNit());
                txtTelefono.setText(a.getTelefonoContacto() == null ? "" : a.getTelefonoContacto());
                chkSoat.setSelected(a.isCubreSoat());
                chkResponsabilidadCivil.setSelected(a.isCubreResponsabilidadCivil());
                chkActivo.setSelected(a.isActivo());
            });
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo cargar la aseguradora seleccionada: " + ex.getMessage(), "Error de Consulta");
        }
    }

    private void guardarAseguradora() {
        Aseguradora aseguradora = leerFormulario();
        try {
            if (idSeleccionado == null) {
                servicio.registrarAseguradora(aseguradora);
                MensajeUtil.mostrarInfo(this, "Aseguradora " + aseguradora.getNombre() + " registrada correctamente.", "Registro Exitoso");
            } else {
                servicio.actualizarAseguradora(aseguradora);
                MensajeUtil.mostrarInfo(this, "Aseguradora " + aseguradora.getNombre() + " actualizada correctamente.", "Actualización Exitosa");
            }
            limpiarFormulario();
            cargarAseguradoras();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Datos Inválidos");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo guardar la aseguradora en la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
    }

    private void eliminarAseguradora() {
        if (idSeleccionado == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione una aseguradora de la tabla para eliminarla.", "Aviso");
            return;
        }
        if (!MensajeUtil.confirmar(this, "¿Desea eliminar la aseguradora " + txtNombre.getText() + "?", "Confirmar Eliminación")) {
            return;
        }
        try {
            servicio.eliminarAseguradora(idSeleccionado);
            MensajeUtil.mostrarInfo(this, "Aseguradora eliminada correctamente.", "Eliminación Exitosa");
            limpiarFormulario();
            cargarAseguradoras();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "No se puede eliminar");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo eliminar la aseguradora de la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
    }

    private Aseguradora leerFormulario() {
        Aseguradora a = new Aseguradora();
        a.setId(idSeleccionado);
        a.setCodigo(txtCodigo.getText());
        a.setNombre(txtNombre.getText());
        a.setNit(txtNit.getText());
        a.setTelefonoContacto(txtTelefono.getText());
        a.setCubreSoat(chkSoat.isSelected());
        a.setCubreResponsabilidadCivil(chkResponsabilidadCivil.isSelected());
        a.setActivo(chkActivo.isSelected());
        return a;
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        txtCodigo.clear();
        txtNombre.clear();
        txtNit.clear();
        txtTelefono.clear();
        chkSoat.setSelected(true);
        chkResponsabilidadCivil.setSelected(true);
        chkActivo.setSelected(true);
        tblAseguradoras.getSelectionModel().clearSelection();
    }
}
