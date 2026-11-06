package co.edu.javeriana.concesionario.clientes.vista;

import co.edu.javeriana.concesionario.clientes.logica.ClienteServicio;
import co.edu.javeriana.concesionario.clientes.logica.IndiceSatisfaccionServicio;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;

import java.math.BigDecimal;
import java.util.List;
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

    private final ClienteServicio servicio;
    private final IndiceSatisfaccionServicio indiceServicio;
    private final TextField txtDoc;
    private final ComboBox<String> cmbTipoDoc;
    private final TextField txtNombres;
    private final TextField txtApellidos;
    private final TextField txtEmail;
    private final TextField txtTelefono;
    private final TextField txtIngresos;
    private final TableView<Object[]> tblClientes;

    public ClienteVista(ClienteServicio servicio, IndiceSatisfaccionServicio indiceServicio) {
        this.servicio = servicio;
        this.indiceServicio = indiceServicio;
        setPadding(new Insets(15));

        cmbTipoDoc = new ComboBox<>();
        cmbTipoDoc.getItems().addAll("CC", "NIT", "CE", "PASAPORTE");
        cmbTipoDoc.getSelectionModel().selectFirst();
        txtDoc = new TextField();
        txtNombres = new TextField();
        txtApellidos = new TextField();
        txtEmail = new TextField();
        txtTelefono = new TextField();
        txtIngresos = new TextField("5000000");

        Button btnGuardar = new Button("Guardar Cliente");
        btnGuardar.setOnAction(e -> guardarCliente());

        setTop(ComponentesFX.crearSeccion("Registrar Cliente", ComponentesFX.crearFormulario(4,
                new Label("Tipo Doc:"), cmbTipoDoc,
                new Label("Documento / NIT:"), txtDoc,
                new Label("Nombres:"), txtNombres,
                new Label("Apellidos / Razón Social:"), txtApellidos,
                new Label("Correo Electrónico:"), txtEmail,
                new Label("Teléfono:"), txtTelefono,
                new Label("Ingresos Mensuales ($):"), txtIngresos,
                btnGuardar)));

        tblClientes = ComponentesFX.crearTabla("ID", "Documento", "Nombre Completo", "Email", "Teléfono", "Ingresos", "Categoría");
        BorderPane.setMargin(tblClientes, new Insets(10, 0, 0, 0));
        setCenter(tblClientes);

        Button btnRefrescar = new Button("Actualizar Lista");
        btnRefrescar.setOnAction(e -> cargarClientes());

        Button btnIndices = new Button("Ver Índices de Satisfacción");
        btnIndices.setOnAction(e -> IndiceSatisfaccionVista.abrir(indiceServicio));

        setBottom(ComponentesFX.crearBarraBotones(Pos.CENTER_RIGHT, btnRefrescar, btnIndices));

        cargarClientes();
    }

    private void guardarCliente() {
        try {
            String doc = txtDoc.getText().trim();
            String tipo = cmbTipoDoc.getValue();
            String nom = txtNombres.getText().trim();
            String ape = txtApellidos.getText().trim();
            String mail = txtEmail.getText().trim();
            String tel = txtTelefono.getText().trim();
            BigDecimal ing = new BigDecimal(txtIngresos.getText().trim());

            servicio.registrarCliente(doc, tipo, nom, ape, mail, tel, ing);
            MensajeUtil.mostrarInfo(this, "Cliente registrado exitosamente.", "Éxito");
            cargarClientes();

            txtDoc.clear();
            txtNombres.clear();
            txtApellidos.clear();
            txtEmail.clear();
            txtTelefono.clear();
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al guardar: " + ex.getMessage(), "Error");
        }
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
        } catch (Exception ignored) {
        }
    }
}
