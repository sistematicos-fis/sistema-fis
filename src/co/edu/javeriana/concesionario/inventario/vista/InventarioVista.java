package co.edu.javeriana.concesionario.inventario.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.inventario.logica.InventarioServicio;
import co.edu.javeriana.concesionario.inventario.modelo.CategoriaVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.Vehiculo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Panel Swing para la gestión del inventario y catálogo de vehículos (CU-06 a CU-10).
 * Responsable: Juan Pablo León
 */
public class InventarioVista extends JPanel {

    private final InventarioServicio servicio;
    private final JTextField txtVin;
    private final JTextField txtPlaca;
    private final JComboBox<CategoriaVehiculo> cmbTipo;
    private final JTextField txtMarca;
    private final JTextField txtLinea;
    private final JTextField txtAnio;
    private final JTextField txtColor;
    private final JTextField txtPrecio;
    private final JTable tblVehiculos;
    private final DefaultTableModel modeloTabla;

    public InventarioVista() {
        this.servicio = new InventarioServicio();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulario
        JPanel pnlForm = new JPanel(new GridLayout(3, 6, 8, 8));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Nuevo Vehículo / Motocicleta (CU-06)"));

        pnlForm.add(new JLabel("VIN:"));
        txtVin = new JTextField();
        pnlForm.add(txtVin);

        pnlForm.add(new JLabel("Placa:"));
        txtPlaca = new JTextField();
        pnlForm.add(txtPlaca);

        pnlForm.add(new JLabel("Tipo:"));
        cmbTipo = new JComboBox<>(CategoriaVehiculo.values());
        pnlForm.add(cmbTipo);

        pnlForm.add(new JLabel("Marca:"));
        txtMarca = new JTextField("Toyota");
        pnlForm.add(txtMarca);

        pnlForm.add(new JLabel("Línea:"));
        txtLinea = new JTextField("Corolla Cross");
        pnlForm.add(txtLinea);

        pnlForm.add(new JLabel("Año Modelo:"));
        txtAnio = new JTextField("2024");
        pnlForm.add(txtAnio);

        pnlForm.add(new JLabel("Color:"));
        txtColor = new JTextField("Plata");
        pnlForm.add(txtColor);

        pnlForm.add(new JLabel("Precio Venta ($):"));
        txtPrecio = new JTextField("120000000");
        pnlForm.add(txtPrecio);

        JButton btnRegistrar = new JButton("Registrar en Stock");
        btnRegistrar.addActionListener(e -> registrarVehiculo());
        pnlForm.add(btnRegistrar);

        add(pnlForm, BorderLayout.NORTH);

        // Tabla Central
        String[] columnas = {"ID", "VIN", "Placa", "Tipo", "Marca", "Línea", "Año", "Color", "Precio", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblVehiculos = new JTable(modeloTabla);
        tblVehiculos.setRowHeight(22);
        add(new JScrollPane(tblVehiculos), BorderLayout.CENTER);

        // Barra Inferior
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRefrescar = new JButton("Actualizar Lista");
        btnRefrescar.addActionListener(e -> cargarVehiculos());
        pnlSur.add(btnRefrescar);

        add(pnlSur, BorderLayout.SOUTH);

        cargarVehiculos();
    }

    private void registrarVehiculo() {
        try {
            String vin = txtVin.getText().trim();
            String placa = txtPlaca.getText().trim();
            CategoriaVehiculo tipo = (CategoriaVehiculo) cmbTipo.getSelectedItem();
            String marca = txtMarca.getText().trim();
            String linea = txtLinea.getText().trim();
            int anio = Integer.parseInt(txtAnio.getText().trim());
            String color = txtColor.getText().trim();
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());

            servicio.registrarVehiculo(vin, placa, tipo, marca, anio, linea, color, precio);
            MensajeUtil.mostrarInfo(this, "Vehículo registrado en inventario.", "Éxito");
            cargarVehiculos();

            txtVin.setText("");
            txtPlaca.setText("");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al registrar vehículo: " + ex.getMessage(), "Error");
        }
    }

    private void cargarVehiculos() {
        try {
            List<Vehiculo> lista = servicio.listarDisponibles();
            modeloTabla.setRowCount(0);
            for (Vehiculo v : lista) {
                modeloTabla.addRow(new Object[]{
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
