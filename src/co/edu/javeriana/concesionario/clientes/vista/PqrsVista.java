package co.edu.javeriana.concesionario.clientes.vista;

import co.edu.javeriana.concesionario.clientes.logica.ClienteServicio;
import co.edu.javeriana.concesionario.clientes.logica.PqrsServicio;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.modelo.EstadoPqrs;
import co.edu.javeriana.concesionario.clientes.modelo.Pqrs;
import co.edu.javeriana.concesionario.clientes.modelo.TipoPqrs;
import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;

/**
 * Pantalla JavaFX del CU-03 Gestionar PQRS.
 * Responsable: Andrés Alquerque
 */
public class PqrsVista extends BorderPane {

    private final PqrsServicio servicio;
    private final ClienteServicio clienteServicio;
    private final ComboBox<Cliente> cmbCliente;
    private final ComboBox<TipoPqrs> cmbTipo;
    private final TextArea txtDescripcion;
    private final Button btnGuardar;
    private final TableView<Object[]> tblPqrs;
    private final Map<Long, String> nombresClientes = new HashMap<>();
    private Long idSeleccionado;

    public PqrsVista(PqrsServicio servicio, ClienteServicio clienteServicio) {
        this.servicio = servicio;
        this.clienteServicio = clienteServicio;
        setPadding(new Insets(15));

        cmbCliente = new ComboBox<>();
        cmbCliente.setPromptText("Seleccione el cliente");
        cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll(TipoPqrs.values());
        cmbTipo.getSelectionModel().selectFirst();
        txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Describa la petición, queja, reclamo o sugerencia (máximo 500 caracteres)");
        txtDescripcion.setPrefRowCount(3);
        txtDescripcion.setWrapText(true);

        btnGuardar = new Button("Radicar PQRS");
        btnGuardar.setOnAction(e -> guardarPqrs());
        Button btnNuevo = new Button("Nuevo");
        btnNuevo.setOnAction(e -> limpiarFormulario());

        setTop(ComponentesFX.crearSeccion("Radicar o editar PQRS", ComponentesFX.crearFormulario(4,
                new Label("Cliente:"), cmbCliente,
                new Label("Tipo:"), cmbTipo,
                new Label("Descripción:"), txtDescripcion,
                btnGuardar, btnNuevo)));

        tblPqrs = ComponentesFX.crearTabla("ID", "Cliente", "Tipo", "Estado", "Descripción",
                "Radicada", "Resuelta", "Respuesta");
        tblPqrs.getSelectionModel().selectedItemProperty().addListener((obs, anterior, fila) -> cargarSeleccion(fila));
        BorderPane.setMargin(tblPqrs, new Insets(10, 0, 0, 0));
        setCenter(tblPqrs);

        Button btnIniciar = new Button("Iniciar gestión");
        btnIniciar.setOnAction(e -> iniciarGestion());
        Button btnCerrar = new Button("Cerrar PQRS");
        btnCerrar.setOnAction(e -> cerrarPqrs());
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarPqrs());
        Button btnRefrescar = new Button("Actualizar Lista");
        btnRefrescar.setOnAction(e -> recargar());

        setBottom(ComponentesFX.crearBarraBotones(Pos.CENTER_RIGHT, btnIniciar, btnCerrar, btnEliminar, btnRefrescar));

        recargar();
    }

    public void recargar() {
        cargarClientes();
        cargarPqrs();
    }

    private void cargarClientes() {
        try {
            Cliente seleccionado = cmbCliente.getValue();
            List<Cliente> clientes = clienteServicio.listarClientes();
            nombresClientes.clear();
            for (Cliente c : clientes) {
                nombresClientes.put(c.getId(), c.getNombreCompleto());
            }
            cmbCliente.getItems().setAll(clientes);
            if (seleccionado != null) {
                seleccionarCliente(seleccionado.getId());
            }
        } catch (Exception e) {
            MensajeUtil.mostrarError(this, "No se pudieron cargar los clientes: " + e.getMessage(), "Error");
        }
    }

    private void cargarPqrs() {
        try {
            tblPqrs.getItems().clear();
            for (Pqrs p : servicio.listarPqrs()) {
                tblPqrs.getItems().add(new Object[]{
                    p.getId(),
                    nombresClientes.getOrDefault(p.getClienteId(), "Cliente " + p.getClienteId()),
                    p.getTipo(),
                    etiquetaEstado(p.getEstado()),
                    p.getDescripcion(),
                    fecha(p.getFechaRadicacion()),
                    fecha(p.getFechaResolucion()),
                    p.getRespuesta() != null ? p.getRespuesta() : ""
                });
            }
        } catch (Exception e) {
            MensajeUtil.mostrarError(this, "No se pudieron cargar las PQRS: " + e.getMessage(), "Error");
        }
    }

    private void cargarSeleccion(Object[] fila) {
        if (fila == null) {
            return;
        }
        try {
            Optional<Pqrs> pqrs = servicio.buscarPqrs((Long) fila[0]);
            if (pqrs.isEmpty()) {
                return;
            }
            Pqrs p = pqrs.get();
            idSeleccionado = p.getId();
            seleccionarCliente(p.getClienteId());
            cmbCliente.setDisable(true);
            cmbTipo.setValue(TipoPqrs.desdeTexto(p.getTipo()));
            txtDescripcion.setText(p.getDescripcion());
            btnGuardar.setText("Guardar cambios");
        } catch (Exception e) {
            MensajeUtil.mostrarError(this, "No se pudo cargar la PQRS: " + e.getMessage(), "Error");
        }
    }

    private void guardarPqrs() {
        try {
            if (idSeleccionado == null) {
                Cliente cliente = cmbCliente.getValue();
                Pqrs p = servicio.radicarPqrs(cliente != null ? cliente.getId() : null,
                        nombreTipo(), txtDescripcion.getText());
                MensajeUtil.mostrarInfo(this, "PQRS radicada con el número " + p.getId() + ".", "PQRS radicada");
            } else {
                servicio.editarPqrs(idSeleccionado, nombreTipo(), txtDescripcion.getText());
                MensajeUtil.mostrarInfo(this, "PQRS " + idSeleccionado + " actualizada.", "PQRS actualizada");
            }
            limpiarFormulario();
            cargarPqrs();
        } catch (IllegalArgumentException e) {
            MensajeUtil.mostrarAdvertencia(this, e.getMessage(), "Datos inválidos");
        } catch (Exception e) {
            MensajeUtil.mostrarError(this, "No se pudo guardar la PQRS: " + e.getMessage(), "Error");
        }
    }

    private void iniciarGestion() {
        Long id = idDeLaFilaSeleccionada();
        if (id == null) {
            return;
        }
        try {
            servicio.iniciarGestion(id);
            MensajeUtil.mostrarInfo(this, "La PQRS " + id + " quedó en proceso.", "PQRS en proceso");
            limpiarFormulario();
            cargarPqrs();
        } catch (IllegalArgumentException e) {
            MensajeUtil.mostrarAdvertencia(this, e.getMessage(), "No se puede iniciar la gestión");
        } catch (Exception e) {
            MensajeUtil.mostrarError(this, "No se pudo cambiar el estado: " + e.getMessage(), "Error");
        }
    }

    private void cerrarPqrs() {
        Long id = idDeLaFilaSeleccionada();
        if (id == null) {
            return;
        }
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Cerrar PQRS");
        dialogo.setHeaderText("Respuesta al cliente para la PQRS " + id);
        dialogo.setContentText("Respuesta:");
        if (getScene() != null) {
            dialogo.initOwner(getScene().getWindow());
        }
        Optional<String> respuesta = dialogo.showAndWait();
        if (respuesta.isEmpty()) {
            return;
        }
        try {
            servicio.cerrarPqrs(id, respuesta.get());
            MensajeUtil.mostrarInfo(this, "La PQRS " + id + " quedó cerrada. Desde ahora cuenta en el índice "
                    + "de satisfacción del cliente.", "PQRS cerrada");
            limpiarFormulario();
            cargarPqrs();
        } catch (IllegalArgumentException e) {
            MensajeUtil.mostrarAdvertencia(this, e.getMessage(), "No se puede cerrar la PQRS");
        } catch (Exception e) {
            MensajeUtil.mostrarError(this, "No se pudo cerrar la PQRS: " + e.getMessage(), "Error");
        }
    }

    private void eliminarPqrs() {
        Long id = idDeLaFilaSeleccionada();
        if (id == null) {
            return;
        }
        if (!MensajeUtil.confirmar(this, "¿Eliminar la PQRS " + id + "?", "Confirmar eliminación")) {
            return;
        }
        try {
            servicio.eliminarPqrs(id);
            MensajeUtil.mostrarInfo(this, "PQRS " + id + " eliminada.", "PQRS eliminada");
            limpiarFormulario();
            cargarPqrs();
        } catch (IllegalArgumentException e) {
            MensajeUtil.mostrarAdvertencia(this, e.getMessage(), "No se puede eliminar la PQRS");
        } catch (Exception e) {
            MensajeUtil.mostrarError(this, "No se pudo eliminar la PQRS: " + e.getMessage(), "Error");
        }
    }

    private Long idDeLaFilaSeleccionada() {
        Object[] fila = tblPqrs.getSelectionModel().getSelectedItem();
        if (fila == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione una PQRS en la tabla.", "Seleccione una PQRS");
            return null;
        }
        return (Long) fila[0];
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        cmbCliente.setDisable(false);
        cmbCliente.getSelectionModel().clearSelection();
        cmbTipo.getSelectionModel().selectFirst();
        txtDescripcion.clear();
        btnGuardar.setText("Radicar PQRS");
        tblPqrs.getSelectionModel().clearSelection();
    }

    private void seleccionarCliente(Long clienteId) {
        for (Cliente c : cmbCliente.getItems()) {
            if (c.getId().equals(clienteId)) {
                cmbCliente.setValue(c);
                return;
            }
        }
    }

    private String nombreTipo() {
        TipoPqrs tipo = cmbTipo.getValue();
        return tipo != null ? tipo.name() : null;
    }

    private static String etiquetaEstado(String estado) {
        try {
            return EstadoPqrs.desdeTexto(estado).getEtiqueta();
        } catch (IllegalArgumentException e) {
            return estado;
        }
    }

    private static String fecha(LocalDateTime fecha) {
        return fecha != null ? FormatoUtil.formatoFecha(fecha.toLocalDate()) : "";
    }
}
