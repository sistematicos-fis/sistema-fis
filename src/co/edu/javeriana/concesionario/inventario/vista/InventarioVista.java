package co.edu.javeriana.concesionario.inventario.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;
import co.edu.javeriana.concesionario.inventario.logica.InventarioServicio;
import co.edu.javeriana.concesionario.inventario.modelo.CategoriaVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.Vehiculo;

import java.math.BigDecimal;
import java.util.List;
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

    private final InventarioServicio servicio;
    private final TextField txtVin;
    private final TextField txtPlaca;
    private final ComboBox<CategoriaVehiculo> cmbTipo;
    private final TextField txtMarca;
    private final TextField txtLinea;
    private final TextField txtAnio;
    private final TextField txtColor;
    private final TextField txtPrecio;
    private final TableView<Object[]> tblVehiculos;

    public InventarioVista() {
        this(new InventarioServicio());
    }

    public InventarioVista(InventarioServicio servicio) {
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

        Button btnRegistrar = new Button("Registrar en Stock");
        btnRegistrar.setOnAction(e -> registrarVehiculo());

        panelVehiculos.setTop(ComponentesFX.crearSeccion("Nuevo Vehículo / Motocicleta (CU-06)", ComponentesFX.crearFormulario(6,
                new Label("VIN:"), txtVin,
                new Label("Placa:"), txtPlaca,
                new Label("Tipo:"), cmbTipo,
                new Label("Marca:"), txtMarca,
                new Label("Línea:"), txtLinea,
                new Label("Año Modelo:"), txtAnio,
                new Label("Color:"), txtColor,
                new Label("Precio Venta ($):"), txtPrecio,
                btnRegistrar)));

        // Tabla Central
        tblVehiculos = ComponentesFX.crearTabla("ID", "VIN", "Placa", "Tipo", "Marca", "Línea", "Año", "Color", "Precio", "Estado");
        BorderPane.setMargin(tblVehiculos, new Insets(10, 0, 0, 0));
        panelVehiculos.setCenter(tblVehiculos);

        // Barra Inferior
        Button btnRefrescar = new Button("Actualizar Lista");
        btnRefrescar.setOnAction(e -> cargarVehiculos());
        panelVehiculos.setBottom(ComponentesFX.crearBarraBotones(Pos.CENTER_RIGHT, btnRefrescar));

        cargarVehiculos();

        setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        getTabs().add(new Tab("Vehículos", panelVehiculos));
        getTabs().add(new Tab("Bodegas", new BodegaVista(servicio)));
    }

    private void registrarVehiculo() {
        try {
            String vin = txtVin.getText().trim();
            String placa = txtPlaca.getText().trim();
            CategoriaVehiculo tipo = cmbTipo.getValue();
            String marca = txtMarca.getText().trim();
            String linea = txtLinea.getText().trim();
            int anio = Integer.parseInt(txtAnio.getText().trim());
            String color = txtColor.getText().trim();
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());

            servicio.registrarVehiculo(vin, placa, tipo, marca, anio, linea, color, precio);
            MensajeUtil.mostrarInfo(this, "Vehículo registrado en inventario.", "Éxito");
            cargarVehiculos();

            txtVin.clear();
            txtPlaca.clear();
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al registrar vehículo: " + ex.getMessage(), "Error");
        }
    }

    private void cargarVehiculos() {
        try {
            List<Vehiculo> lista = servicio.listarDisponibles();
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
        } catch (Exception ignored) {
        }
    }
}
