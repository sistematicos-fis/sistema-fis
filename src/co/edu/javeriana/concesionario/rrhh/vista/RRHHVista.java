package co.edu.javeriana.concesionario.rrhh.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.rrhh.logica.RRHHServicio;
import co.edu.javeriana.concesionario.rrhh.modelo.Empleado;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Panel Swing para la gestión de Recursos Humanos (CU-26 a CU-30).
 * Responsable: Sergio Alejandro Ramírez
 */
public class RRHHVista extends JPanel {

    private final RRHHServicio servicio;
    private final JTextField txtDoc;
    private final JTextField txtNombres;
    private final JTextField txtApellidos;
    private final JTextField txtCargo;
    private final JTextField txtDepto;
    private final JTextField txtSalario;
    private final JTable tblEmpleados;
    private final DefaultTableModel modeloTabla;

    public RRHHVista() {
        this.servicio = new RRHHServicio();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulario
        JPanel pnlForm = new JPanel(new GridLayout(2, 6, 8, 8));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Registrar Colaborador (CU-26)"));

        pnlForm.add(new JLabel("Documento:"));
        txtDoc = new JTextField();
        pnlForm.add(txtDoc);

        pnlForm.add(new JLabel("Nombres:"));
        txtNombres = new JTextField();
        pnlForm.add(txtNombres);

        pnlForm.add(new JLabel("Apellidos:"));
        txtApellidos = new JTextField();
        pnlForm.add(txtApellidos);

        pnlForm.add(new JLabel("Cargo:"));
        txtCargo = new JTextField("Asesor Financiero");
        pnlForm.add(txtCargo);

        pnlForm.add(new JLabel("Departamento:"));
        txtDepto = new JTextField("Ventas & Crédito");
        pnlForm.add(txtDepto);

        pnlForm.add(new JLabel("Salario Base ($):"));
        txtSalario = new JTextField("3500000");
        pnlForm.add(txtSalario);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardarEmpleado());
        pnlForm.add(btnGuardar);

        add(pnlForm, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"ID", "Documento", "Nombre Completo", "Cargo", "Departamento", "Salario Base"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblEmpleados = new JTable(modeloTabla);
        tblEmpleados.setRowHeight(22);
        add(new JScrollPane(tblEmpleados), BorderLayout.CENTER);

        // Barra Sur
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRefrescar = new JButton("Actualizar Colaboradores");
        btnRefrescar.addActionListener(e -> cargarEmpleados());
        pnlSur.add(btnRefrescar);

        JButton btnComisiones = new JButton("Calcular Comisiones (CU-28)");
        btnComisiones.addActionListener(e -> {
            String strVentas = javax.swing.JOptionPane.showInputDialog(this, "Ingrese el volumen de ventas del mes ($):", "180000000");
            if (strVentas != null && !strVentas.trim().isEmpty()) {
                BigDecimal comision = servicio.liquidarComisionMensual(new BigDecimal(strVentas.trim()));
                MensajeUtil.mostrarInfo(this, "Comisión Liquidada: " + FormatoUtil.formatoMoneda(comision), "Liquidación CU-28");
            }
        });
        pnlSur.add(btnComisiones);

        add(pnlSur, BorderLayout.SOUTH);

        cargarEmpleados();
    }

    private void guardarEmpleado() {
        try {
            String doc = txtDoc.getText().trim();
            String nom = txtNombres.getText().trim();
            String ape = txtApellidos.getText().trim();
            String cargo = txtCargo.getText().trim();
            String depto = txtDepto.getText().trim();
            BigDecimal sal = new BigDecimal(txtSalario.getText().trim());

            servicio.registrarEmpleado(doc, nom, ape, cargo, depto, sal);
            MensajeUtil.mostrarInfo(this, "Colaborador registrado exitosamente.", "Éxito");
            cargarEmpleados();

            txtDoc.setText("");
            txtNombres.setText("");
            txtApellidos.setText("");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al guardar: " + ex.getMessage(), "Error");
        }
    }

    private void cargarEmpleados() {
        try {
            List<Empleado> lista = servicio.listarEmpleados();
            modeloTabla.setRowCount(0);
            for (Empleado e : lista) {
                modeloTabla.addRow(new Object[]{
                    e.getId(),
                    e.getDocumento(),
                    e.getNombreCompleto(),
                    e.getCargo(),
                    e.getDepartamento(),
                    FormatoUtil.formatoMoneda(e.getSalarioBase())
                });
            }
        } catch (Exception ignored) {
        }
    }
}
