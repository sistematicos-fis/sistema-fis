package co.edu.javeriana.concesionario.clientes.vista;

import co.edu.javeriana.concesionario.clientes.logica.ClienteServicio;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;

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
 * Panel Swing para la gestión de Clientes (CU-01 a CU-03).
 * Responsable: Andrés Alquerque
 */
public class ClienteVista extends JPanel {

    private final ClienteServicio servicio;
    private final JTextField txtDoc;
    private final JComboBox<String> cmbTipoDoc;
    private final JTextField txtNombres;
    private final JTextField txtApellidos;
    private final JTextField txtEmail;
    private final JTextField txtTelefono;
    private final JTextField txtIngresos;
    private final JTable tblClientes;
    private final DefaultTableModel modeloTabla;

    public ClienteVista() {
        this.servicio = new ClienteServicio();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulario Superior
        JPanel pnlForm = new JPanel(new GridLayout(4, 4, 8, 8));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Registrar Cliente (CU-01 & CU-02)"));

        pnlForm.add(new JLabel("Tipo Doc:"));
        cmbTipoDoc = new JComboBox<>(new String[]{"CC", "NIT", "CE", "PASAPORTE"});
        pnlForm.add(cmbTipoDoc);

        pnlForm.add(new JLabel("Documento / NIT:"));
        txtDoc = new JTextField();
        pnlForm.add(txtDoc);

        pnlForm.add(new JLabel("Nombres:"));
        txtNombres = new JTextField();
        pnlForm.add(txtNombres);

        pnlForm.add(new JLabel("Apellidos / Razón Social:"));
        txtApellidos = new JTextField();
        pnlForm.add(txtApellidos);

        pnlForm.add(new JLabel("Correo Electrónico:"));
        txtEmail = new JTextField();
        pnlForm.add(txtEmail);

        pnlForm.add(new JLabel("Teléfono:"));
        txtTelefono = new JTextField();
        pnlForm.add(txtTelefono);

        pnlForm.add(new JLabel("Ingresos Mensuales ($):"));
        txtIngresos = new JTextField("5000000");
        pnlForm.add(txtIngresos);

        JButton btnGuardar = new JButton("Guardar Cliente");
        btnGuardar.addActionListener(e -> guardarCliente());
        pnlForm.add(btnGuardar);

        add(pnlForm, BorderLayout.NORTH);

        // Tabla Central
        String[] columnas = {"ID", "Documento", "Nombre Completo", "Email", "Teléfono", "Ingresos", "Categoría"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblClientes = new JTable(modeloTabla);
        tblClientes.setRowHeight(22);
        add(new JScrollPane(tblClientes), BorderLayout.CENTER);

        // Barra Inferior
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRefrescar = new JButton("Actualizar Lista");
        btnRefrescar.addActionListener(e -> cargarClientes());
        pnlSur.add(btnRefrescar);

        JButton btnIndices = new JButton("Ver Índices de Satisfacción (CU-05)");
        btnIndices.addActionListener(e -> IndiceSatisfaccionVista.abrir());
        pnlSur.add(btnIndices);

        add(pnlSur, BorderLayout.SOUTH);

        cargarClientes();
    }

    private void guardarCliente() {
        try {
            String doc = txtDoc.getText().trim();
            String tipo = (String) cmbTipoDoc.getSelectedItem();
            String nom = txtNombres.getText().trim();
            String ape = txtApellidos.getText().trim();
            String mail = txtEmail.getText().trim();
            String tel = txtTelefono.getText().trim();
            BigDecimal ing = new BigDecimal(txtIngresos.getText().trim());

            servicio.registrarCliente(doc, tipo, nom, ape, mail, tel, ing);
            MensajeUtil.mostrarInfo(this, "Cliente registrado exitosamente.", "Éxito");
            cargarClientes();

            txtDoc.setText("");
            txtNombres.setText("");
            txtApellidos.setText("");
            txtEmail.setText("");
            txtTelefono.setText("");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al guardar: " + ex.getMessage(), "Error");
        }
    }

    private void cargarClientes() {
        try {
            List<Cliente> clientes = servicio.listarClientes();
            modeloTabla.setRowCount(0);
            for (Cliente c : clientes) {
                modeloTabla.addRow(new Object[]{
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
