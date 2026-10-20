package co.edu.javeriana.concesionario.inventario.vista;

import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;
import co.edu.javeriana.concesionario.financiacion.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.inventario.logica.InventarioServicio;
import co.edu.javeriana.concesionario.inventario.modelo.Bodega;

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * Pantalla JavaFX para la gestión de bodegas y patios de almacenamiento (CU-07).
 * Captura los datos de la sede y delega todas las operaciones en InventarioServicio:
 * no ejecuta SQL ni aplica reglas de negocio.
 *
 * Responsable: Juan Pablo León
 */
public class BodegaVista extends BorderPane {

    private final InventarioServicio servicio;

    private final TextField txtCodigo = new TextField();
    private final TextField txtNombre = new TextField();
    private final TextField txtCiudad = new TextField();
    private final TextField txtCapacidad = new TextField();
    private final CheckBox chkActivo = new CheckBox("Activa");
    private final TableView<Object[]> tblBodegas;

    /** Id de la fila elegida; null cuando se está registrando una sede nueva. */
    private Long idSeleccionado;

    public BodegaVista(InventarioServicio servicio) {
        this.servicio = servicio;
        setPadding(new Insets(15));

        chkActivo.setSelected(true);

        Button btnNueva = new Button("Nueva");
        btnNueva.setOnAction(e -> limpiarFormulario());
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> guardarBodega());
        Button btnDesactivar = new Button("Desactivar");
        btnDesactivar.setOnAction(e -> desactivarBodega());
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarBodega());
        Button btnRefrescar = new Button("Refrescar");
        btnRefrescar.setOnAction(e -> cargarBodegas());

        setTop(ComponentesFX.crearSeccion("Datos de la bodega (CU-07)", ComponentesFX.crearFormulario(4,
                new Label("Código:"), txtCodigo,
                new Label("Nombre:"), txtNombre,
                new Label("Ciudad:"), txtCiudad,
                new Label("Capacidad:"), txtCapacidad,
                new Label("Estado:"), chkActivo)));

        tblBodegas = ComponentesFX.crearTabla("ID", "Código", "Nombre", "Ciudad",
                "Capacidad", "Ocupación", "Estado");
        tblBodegas.getSelectionModel().selectedIndexProperty()
                .addListener((obs, anterior, actual) -> cargarSeleccionEnFormulario());
        BorderPane.setMargin(tblBodegas, new Insets(10, 0, 0, 0));
        setCenter(tblBodegas);

        setBottom(ComponentesFX.crearBarraBotones(Pos.CENTER_LEFT,
                btnNueva, btnGuardar, btnDesactivar, btnEliminar, btnRefrescar));

        cargarBodegas();
    }

    private void cargarBodegas() {
        try {
            List<Bodega> lista = servicio.listarBodegas();
            tblBodegas.getItems().clear();
            for (Bodega b : lista) {
                int ocupacion = servicio.ocupacionBodega(b.getId());
                tblBodegas.getItems().add(new Object[]{
                        b.getId(),
                        b.getCodigo(),
                        b.getNombre(),
                        b.getCiudad(),
                        b.getCapacidadVehiculos(),
                        ocupacion + " / " + b.getCapacidadVehiculos(),
                        b.isActivo() ? "Activa" : "Inactiva"
                });
            }
            limpiarFormulario();
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "No se pudieron cargar las bodegas: " + ex.getMessage(), "Error");
        }
    }

    private void cargarSeleccionEnFormulario() {
        Object[] fila = tblBodegas.getSelectionModel().getSelectedItem();
        if (fila == null) {
            return;
        }
        try {
            Long id = (Long) fila[0];
            servicio.buscarBodega(id).ifPresent(b -> {
                idSeleccionado = b.getId();
                txtCodigo.setText(b.getCodigo());
                txtNombre.setText(b.getNombre());
                txtCiudad.setText(b.getCiudad());
                txtCapacidad.setText(String.valueOf(b.getCapacidadVehiculos()));
                chkActivo.setSelected(b.isActivo());
            });
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "No se pudo cargar la bodega: " + ex.getMessage(), "Error");
        }
    }

    private void guardarBodega() {
        try {
            Bodega bodega = leerFormulario();
            if (idSeleccionado == null) {
                servicio.registrarBodega(bodega);
                MensajeUtil.mostrarInfo(this, "Bodega " + bodega.getNombre()
                        + " registrada correctamente.", "Éxito");
            } else {
                servicio.actualizarBodega(bodega);
                MensajeUtil.mostrarInfo(this, "Bodega " + bodega.getNombre()
                        + " actualizada correctamente.", "Éxito");
            }
            cargarBodegas();
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            // Reglas de negocio y datos mal digitados: se conserva lo escrito en el formulario.
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Datos inválidos");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "No se pudo guardar la bodega: " + ex.getMessage(), "Error");
        }
    }

    private void desactivarBodega() {
        if (idSeleccionado == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione una bodega de la tabla.", "Sin selección");
            return;
        }
        if (!MensajeUtil.confirmar(this, "¿Desactivar la bodega seleccionada? "
                + "Dejará de ofrecerse al asignar vehículos, pero conservará su historial.",
                "Confirmar desactivación")) {
            return;
        }
        try {
            servicio.desactivarBodega(idSeleccionado);
            MensajeUtil.mostrarInfo(this, "Bodega desactivada.", "Éxito");
            cargarBodegas();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "No se pudo desactivar");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, ex.getMessage(), "Error");
        }
    }

    private void eliminarBodega() {
        if (idSeleccionado == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione una bodega de la tabla.", "Sin selección");
            return;
        }
        if (!MensajeUtil.confirmar(this, "¿Eliminar definitivamente la bodega seleccionada?",
                "Confirmar eliminación")) {
            return;
        }
        try {
            servicio.eliminarBodega(idSeleccionado);
            MensajeUtil.mostrarInfo(this, "Bodega eliminada.", "Éxito");
            cargarBodegas();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "No se pudo eliminar");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, ex.getMessage(), "Error");
        }
    }

    private Bodega leerFormulario() {
        Bodega bodega = new Bodega();
        bodega.setId(idSeleccionado);
        bodega.setCodigo(txtCodigo.getText());
        bodega.setNombre(txtNombre.getText());
        bodega.setCiudad(txtCiudad.getText());
        String capacidad = txtCapacidad.getText() == null ? "" : txtCapacidad.getText().trim();
        try {
            bodega.setCapacidadVehiculos(Integer.parseInt(capacidad));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("La capacidad debe ser un número entero.");
        }
        bodega.setActivo(chkActivo.isSelected());
        return bodega;
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        txtCodigo.clear();
        txtNombre.clear();
        txtCiudad.clear();
        txtCapacidad.clear();
        chkActivo.setSelected(true);
        tblBodegas.getSelectionModel().clearSelection();
    }
}
