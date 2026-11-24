package co.edu.javeriana.concesionario.inventario.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;
import co.edu.javeriana.concesionario.financiacion.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.inventario.logica.InventarioServicio;
import co.edu.javeriana.concesionario.inventario.modelo.CategoriaVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.EstadoVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.Vehiculo;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * Pantalla JavaFX para la gestión del inventario y catálogo de vehículos (CU-06 a CU-10).
 * Responsable: Juan Pablo León
 */
public class InventarioVista extends TabPane {

    private static final Logger LOGGER = Logger.getLogger(InventarioVista.class.getName());

    private final InventarioServicio servicio;
    private final TextField txtVin;
    private final TextField txtPlaca;
    private final ComboBox<CategoriaVehiculo> cmbTipo;
    private final TextField txtMarca;
    private final TextField txtLinea;
    private final TextField txtAnio;
    private final TextField txtColor;
    private final TextField txtPrecio;
    private final ComboBox<EstadoVehiculo> cmbEstado;
    private final Button btnGuardar;
    private final TableView<Object[]> tblVehiculos;
    /** Vehículo cargado en el formulario; null cuando se está registrando uno nuevo. */
    private Long idSeleccionado;

    public InventarioVista() {
        this(new InventarioServicio());
    }

    public InventarioVista(InventarioServicio servicio) {
        this(servicio, new VerificacionInventarioVista());
    }

    public InventarioVista(InventarioServicio servicio, VerificacionInventarioVista verificacionVista) {
        this.servicio = servicio;
        BorderPane panelVehiculos = new BorderPane();
        panelVehiculos.setPadding(new Insets(15));

        // Formulario
        txtVin = new TextField();
        txtPlaca = new TextField();
        cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll(CategoriaVehiculo.values());
        cmbTipo.getSelectionModel().selectFirst();
        txtMarca = new TextField("Toyota");
        txtLinea = new TextField("Corolla Cross");
        txtAnio = new TextField("2024");
        txtColor = new TextField("Plata");
        txtPrecio = new TextField("120000000");

        btnGuardar = new Button("Registrar en Stock");
        btnGuardar.setOnAction(e -> guardarVehiculo());

        panelVehiculos.setTop(ComponentesFX.crearSeccion("Catálogo de Vehículos / Motocicletas (CU-06)", ComponentesFX.crearFormulario(6,
                new Label("VIN:"), txtVin,
                new Label("Placa:"), txtPlaca,
                new Label("Tipo:"), cmbTipo,
                new Label("Marca:"), txtMarca,
                new Label("Línea:"), txtLinea,
                new Label("Año Modelo:"), txtAnio,
                new Label("Color:"), txtColor,
                new Label("Precio Venta ($):"), txtPrecio,
                btnGuardar)));

        // Tabla Central: todos los vehículos; al seleccionar una fila se carga en el formulario
        tblVehiculos = ComponentesFX.crearTabla("ID", "VIN", "Placa", "Tipo", "Marca", "Línea", "Año", "Color", "Precio", "Estado");
        tblVehiculos.getSelectionModel().selectedIndexProperty().addListener((obs, anterior, actual) -> {
            if (actual != null && actual.intValue() >= 0) {
                cargarSeleccionEnFormulario((Long) tblVehiculos.getItems().get(actual.intValue())[0]);
            }
        });
        BorderPane.setMargin(tblVehiculos, new Insets(10, 0, 0, 0));
        panelVehiculos.setCenter(tblVehiculos);

        // Barra Inferior: ciclo de vida de la unidad seleccionada
        cmbEstado = new ComboBox<>();
        cmbEstado.getItems().addAll(EstadoVehiculo.DISPONIBLE, EstadoVehiculo.MANTENIMIENTO);
        cmbEstado.getSelectionModel().selectFirst();
        Button btnNuevo = new Button("Nuevo");
        btnNuevo.setOnAction(e -> limpiarFormulario());
        Button btnCambiarEstado = new Button("Cambiar estado");
        btnCambiarEstado.setOnAction(e -> cambiarEstado());
        Button btnBaja = new Button("Dar de baja");
        btnBaja.setOnAction(e -> darDeBaja());
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarVehiculo());
        Button btnRefrescar = new Button("Actualizar Lista");
        btnRefrescar.setOnAction(e -> cargarVehiculos());
        panelVehiculos.setBottom(ComponentesFX.crearBarraBotones(Pos.CENTER_LEFT,
                btnNuevo, new Label("Estado:"), cmbEstado, btnCambiarEstado, btnBaja, btnEliminar, btnRefrescar));

        cargarVehiculos();

        setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        getTabs().add(new Tab("Vehículos", panelVehiculos));
        getTabs().add(new Tab("Bodegas", new BodegaVista(servicio)));
        getTabs().add(new Tab("Verificación de existencias", verificacionVista));
    }

    /** Registra un vehículo nuevo o guarda los cambios del seleccionado. */
    private void guardarVehiculo() {
        try {
            String placa = txtPlaca.getText().trim();
            CategoriaVehiculo tipo = cmbTipo.getValue();
            String marca = txtMarca.getText().trim();
            String linea = txtLinea.getText().trim();
            int anio = Integer.parseInt(txtAnio.getText().trim());
            String color = txtColor.getText().trim();
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());

            if (idSeleccionado == null) {
                servicio.registrarVehiculo(txtVin.getText().trim(), placa, tipo, marca, anio, linea, color, precio);
                MensajeUtil.mostrarInfo(this, "Vehículo registrado en inventario.", "Éxito");
            } else {
                Vehiculo cambios = servicio.buscarVehiculo(idSeleccionado)
                        .orElseThrow(() -> new ReglaNegocioException("El vehículo ya no existe."));
                cambios.setPlaca(placa.isEmpty() ? null : placa);
                cambios.setTipo(tipo);
                cambios.setMarca(marca);
                cambios.setLinea(linea);
                cambios.setModeloAnio(anio);
                cambios.setColor(color);
                cambios.setPrecioVenta(precio);
                servicio.actualizarVehiculo(cambios);
                MensajeUtil.mostrarInfo(this, "Datos del vehículo actualizados.", "Éxito");
            }
            limpiarFormulario();
            cargarVehiculos();
        } catch (NumberFormatException ex) {
            MensajeUtil.mostrarAdvertencia(this, "El año y el precio deben ser números.", "Datos inválidos");
        } catch (ReglaNegocioException | IllegalArgumentException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Validación");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al guardar el vehículo: " + ex.getMessage(), "Error");
        }
    }

    private void cambiarEstado() {
        if (!haySeleccion()) return;
        try {
            servicio.cambiarEstadoVehiculo(idSeleccionado, cmbEstado.getValue());
            MensajeUtil.mostrarInfo(this, "Estado actualizado a " + cmbEstado.getValue() + ".", "Éxito");
            limpiarFormulario();
            cargarVehiculos();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Validación");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al cambiar el estado: " + ex.getMessage(), "Error");
        }
    }

    private void darDeBaja() {
        if (!haySeleccion()) return;
        if (!MensajeUtil.confirmar(this, "La unidad saldrá del catálogo de forma definitiva y su historial "
                + "se conservará. ¿Desea darla de baja?", "Dar de baja")) {
            return;
        }
        try {
            servicio.darDeBajaVehiculo(idSeleccionado);
            MensajeUtil.mostrarInfo(this, "Vehículo dado de baja.", "Éxito");
            limpiarFormulario();
            cargarVehiculos();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Validación");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al dar de baja: " + ex.getMessage(), "Error");
        }
    }

    private void eliminarVehiculo() {
        if (!haySeleccion()) return;
        if (!MensajeUtil.confirmar(this, "Se eliminará el registro del vehículo. Esta acción no se puede deshacer. "
                + "¿Desea continuar?", "Eliminar vehículo")) {
            return;
        }
        try {
            servicio.eliminarVehiculo(idSeleccionado);
            MensajeUtil.mostrarInfo(this, "Vehículo eliminado.", "Éxito");
            limpiarFormulario();
            cargarVehiculos();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "No se puede eliminar");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al eliminar: " + ex.getMessage(), "Error");
        }
    }

    private boolean haySeleccion() {
        if (idSeleccionado == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione un vehículo de la tabla.", "Sin selección");
            return false;
        }
        return true;
    }

    private void cargarSeleccionEnFormulario(Long id) {
        try {
            servicio.buscarVehiculo(id).ifPresent(v -> {
                idSeleccionado = v.getId();
                txtVin.setText(v.getVin());
                txtVin.setDisable(true); // RN-06-02: el VIN no se edita
                txtPlaca.setText(v.getPlaca() != null ? v.getPlaca() : "");
                cmbTipo.setValue(v.getTipo());
                txtMarca.setText(v.getMarca());
                txtLinea.setText(v.getLinea());
                txtAnio.setText(String.valueOf(v.getModeloAnio()));
                txtColor.setText(v.getColor() != null ? v.getColor() : "");
                txtPrecio.setText(v.getPrecioVenta() != null ? v.getPrecioVenta().toPlainString() : "");
                btnGuardar.setText("Guardar cambios");
            });
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "No se pudo cargar el vehículo: " + ex.getMessage(), "Error");
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        txtVin.clear();
        txtVin.setDisable(false);
        txtPlaca.clear();
        btnGuardar.setText("Registrar en Stock");
        tblVehiculos.getSelectionModel().clearSelection();
    }

    private void cargarVehiculos() {
        try {
            List<Vehiculo> lista = servicio.listarVehiculos();
            tblVehiculos.getItems().clear();
            for (Vehiculo v : lista) {
                tblVehiculos.getItems().add(new Object[]{
                    v.getId(),
                    v.getVin(),
                    v.getPlaca() != null ? v.getPlaca() : "-",
                    v.getTipo(),
                    v.getMarca(),
                    v.getLinea(),
                    v.getModeloAnio(),
                    v.getColor(),
                    FormatoUtil.formatoMoneda(v.getPrecioVenta()),
                    v.getEstado()
                });
            }
        } catch (Exception ex) {
            // Se llama desde el constructor, antes de que exista la ventana: no se puede
            // abrir un diálogo modal aquí, así que el error se registra y se avisa en la tabla.
            LOGGER.log(Level.SEVERE, "No se pudo cargar el inventario", ex);
            tblVehiculos.setPlaceholder(new Label("No se pudo cargar el inventario: " + ex.getMessage()));
        }
    }
}
