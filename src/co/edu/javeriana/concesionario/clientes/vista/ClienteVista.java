package co.edu.javeriana.concesionario.clientes.vista;

import co.edu.javeriana.concesionario.clientes.logica.ClienteServicio;
import co.edu.javeriana.concesionario.clientes.logica.IndiceSatisfaccionServicio;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * Pantalla JavaFX para la gestión de Clientes (CU-01 y CU-02).
 * Responsable: Andrés Alquerque
 */
public class ClienteVista extends BorderPane {

    private static final String INGRESOS_POR_DEFECTO = "5000000";

    private final ClienteServicio servicio;
    private final IndiceSatisfaccionServicio indiceServicio;
    private final TextField txtDoc;
    private final ComboBox<String> cmbTipoDoc;
    private final TextField txtNombres;
    private final TextField txtApellidos;
    private final TextField txtEmail;
    private final TextField txtTelefono;
    private final TextField txtIngresos;
    private final Button btnGuardar;
    private final TableView<Object[]> tblClientes;
    private Long idSeleccionado;

    public ClienteVista(ClienteServicio servicio, IndiceSatisfaccionServicio indiceServicio) {
        this.servicio = servicio;
        this.indiceServicio = indiceServicio;
        setPadding(new Insets(15));

        cmbTipoDoc = new ComboBox<>();
        cmbTipoDoc.getItems().addAll(ClienteServicio.TIPOS_DOCUMENTO);
        cmbTipoDoc.getSelectionModel().selectFirst();
        txtDoc = new TextField();
        txtNombres = new TextField();
        txtApellidos = new TextField();
        txtEmail = new TextField();
        txtTelefono = new TextField();
        txtIngresos = new TextField(INGRESOS_POR_DEFECTO);

        btnGuardar = new Button("Guardar Cliente");
        btnGuardar.setOnAction(e -> guardarCliente());
        Button btnNuevo = new Button("Nuevo");
        btnNuevo.setOnAction(e -> limpiarFormulario());

        setTop(ComponentesFX.crearSeccion("Registrar o editar Cliente", ComponentesFX.crearFormulario(4,
                new Label("Tipo Doc:"), cmbTipoDoc,
                new Label("Documento / NIT:"), txtDoc,
                new Label("Nombres:"), txtNombres,
                new Label("Apellidos / Razón Social:"), txtApellidos,
                new Label("Correo Electrónico:"), txtEmail,
                new Label("Teléfono:"), txtTelefono,
                new Label("Ingresos Mensuales ($):"), txtIngresos,
                btnGuardar, btnNuevo)));

        tblClientes = ComponentesFX.crearTabla("ID", "Documento", "Nombre Completo", "Email",
                "Teléfono", "Ingresos", "Categoría");
        tblClientes.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, fila) -> cargarSeleccion(fila));
        BorderPane.setMargin(tblClientes, new Insets(10, 0, 0, 0));
        setCenter(tblClientes);

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarCliente());

        Button btnRefrescar = new Button("Actualizar Lista");
        btnRefrescar.setOnAction(e -> cargarClientes());

        Button btnIndices = new Button("Ver Índices de Satisfacción");
        btnIndices.setOnAction(e -> IndiceSatisfaccionVista.abrir(indiceServicio));

        setBottom(ComponentesFX.crearBarraBotones(Pos.CENTER_RIGHT, btnEliminar, btnRefrescar, btnIndices));

        cargarClientes();
    }

    private void guardarCliente() {
        try {
            BigDecimal ingresos = leerIngresos();
            if (idSeleccionado == null) {
                servicio.registrarCliente(txtDoc.getText(), cmbTipoDoc.getValue(), txtNombres.getText(),
                        txtApellidos.getText(), txtEmail.getText(), txtTelefono.getText(), ingresos);
                MensajeUtil.mostrarInfo(this, "Cliente registrado exitosamente.", "Éxito");
            } else {
                Cliente c = servicio.editarCliente(idSeleccionado, txtDoc.getText(), cmbTipoDoc.getValue(),
                        txtNombres.getText(), txtApellidos.getText(), txtEmail.getText(), txtTelefono.getText(), ingresos);
                MensajeUtil.mostrarInfo(this, "Cliente " + c.getNombreCompleto() + " actualizado. Categoría: "
                        + c.getCategoriaComercial() + ".", "Cliente actualizado");
            }
            limpiarFormulario();
            cargarClientes();
        } catch (IllegalArgumentException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Datos inválidos");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al guardar: " + ex.getMessage(), "Error");
        }
    }

    private void eliminarCliente() {
        Object[] fila = tblClientes.getSelectionModel().getSelectedItem();
        if (fila == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione un cliente en la tabla.", "Seleccione un cliente");
            return;
        }
        Long id = (Long) fila[0];
        if (!MensajeUtil.confirmar(this, "¿Eliminar al cliente " + fila[2] + "?", "Confirmar eliminación")) {
            return;
        }
        try {
            servicio.eliminarCliente(id);
            MensajeUtil.mostrarInfo(this, "Cliente " + fila[2] + " eliminado.", "Cliente eliminado");
            limpiarFormulario();
            cargarClientes();
        } catch (IllegalArgumentException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "No se puede eliminar el cliente");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "No se pudo eliminar el cliente: " + ex.getMessage(), "Error");
        }
    }

    private void cargarSeleccion(Object[] fila) {
        if (fila == null) {
            return;
        }
        try {
            Optional<Cliente> cliente = servicio.buscarPorId((Long) fila[0]);
            if (cliente.isEmpty()) {
                return;
            }
            Cliente c = cliente.get();
            idSeleccionado = c.getId();
            cmbTipoDoc.setValue(c.getTipoDocumento());
            txtDoc.setText(c.getDocumento());
            txtNombres.setText(c.getNombres());
            txtApellidos.setText(c.getApellidos());
            txtEmail.setText(c.getEmail() != null ? c.getEmail() : "");
            txtTelefono.setText(c.getTelefono() != null ? c.getTelefono() : "");
            txtIngresos.setText(c.getIngresosMensuales() != null
                    ? c.getIngresosMensuales().stripTrailingZeros().toPlainString() : "");
            btnGuardar.setText("Guardar cambios");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "No se pudo cargar el cliente: " + ex.getMessage(), "Error");
        }
    }

    private BigDecimal leerIngresos() {
        String texto = txtIngresos.getText().trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Los ingresos mensuales deben ser un número, sin puntos ni signo $.");
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        cmbTipoDoc.getSelectionModel().selectFirst();
        txtDoc.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtEmail.clear();
        txtTelefono.clear();
        txtIngresos.setText(INGRESOS_POR_DEFECTO);
        btnGuardar.setText("Guardar Cliente");
        tblClientes.getSelectionModel().clearSelection();
    }

    private void cargarClientes() {
        try {
            List<Cliente> clientes = servicio.listarClientes();
            tblClientes.getItems().clear();
            for (Cliente c : clientes) {
                tblClientes.getItems().add(new Object[]{
                    c.getId(),
                    c.getDocumento(),
                    c.getNombreCompleto(),
                    c.getEmail(),
                    c.getTelefono(),
                    FormatoUtil.formatoMoneda(c.getIngresosMensuales()),
                    c.getCategoriaComercial()
                });
            }
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "No se pudieron cargar los clientes: " + ex.getMessage(), "Error");
        }
    }
}
