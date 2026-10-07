package co.edu.javeriana.concesionario.mantenimiento.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.mantenimiento.logica.MantenimientoServicio;
import co.edu.javeriana.concesionario.mantenimiento.modelo.OrdenMantenimiento;

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
 * Panel Swing para la gestión de Mantenimiento y Taller (CU-21 a CU-25).
 * Responsable: Jaime Andrés Molina
 */
public class MantenimientoVista extends JPanel {

    private final MantenimientoServicio servicio;
    private final JComboBox<String> cmbTipo;
    private final JTextField txtDiagnostico;
    private final JTextField txtHoras;
    private final JTextField txtRepuestos;
    private final JTable tblOrdenes;
    private final DefaultTableModel modeloTabla;

    public MantenimientoVista() {
        this.servicio = new MantenimientoServicio();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulario
        JPanel pnlForm = new JPanel(new GridLayout(2, 5, 8, 8));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Abrir Orden de Mantenimiento (CU-21 & CU-24)"));

        pnlForm.add(new JLabel("Tipo Servicio:"));
        cmbTipo = new JComboBox<>(new String[]{"PREVENTIVO", "CORRECTIVO", "ALISTAMIENTO"});
        pnlForm.add(cmbTipo);

        pnlForm.add(new JLabel("Diagnóstico Preliminar:"));
        txtDiagnostico = new JTextField("Revisión de 10.000 km y cambio de aceite");
        pnlForm.add(txtDiagnostico);

        pnlForm.add(new JLabel("Horas Mano de Obra:"));
        txtHoras = new JTextField("3");
        pnlForm.add(txtHoras);

        pnlForm.add(new JLabel("Costo Repuestos ($):"));
        txtRepuestos = new JTextField("250000");
        pnlForm.add(txtRepuestos);

        JButton btnCrear = new JButton("Crear Orden");
        btnCrear.addActionListener(e -> crearOrden());
        pnlForm.add(btnCrear);

        add(pnlForm, BorderLayout.NORTH);

        // Tabla Central
        String[] columnas = {"ID", "N° Orden", "Tipo", "Costo Total (c/IVA)", "Estado", "Diagnóstico"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblOrdenes = new JTable(modeloTabla);
        tblOrdenes.setRowHeight(22);
        add(new JScrollPane(tblOrdenes), BorderLayout.CENTER);

        // Barra Sur
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRefrescar = new JButton("Actualizar Órdenes");
        btnRefrescar.addActionListener(e -> cargarOrdenes());
        pnlSur.add(btnRefrescar);

        add(pnlSur, BorderLayout.SOUTH);

        cargarOrdenes();
    }

    private void crearOrden() {
        try {
            String tipo = (String) cmbTipo.getSelectedItem();
            String diag = txtDiagnostico.getText().trim();
            int horas = Integer.parseInt(txtHoras.getText().trim());
            BigDecimal rep = new BigDecimal(txtRepuestos.getText().trim());

            OrdenMantenimiento orden = servicio.crearOrden(1L, 1L, tipo, diag, horas, rep);
            MensajeUtil.mostrarInfo(this, "Orden creada con éxito.\nN°: " + orden.getNumeroOrden() +
                    "\nCosto Total Liquidado (c/IVA): " + FormatoUtil.formatoMoneda(orden.getCostoTotal()), "Éxito");
            cargarOrdenes();
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al crear orden: " + ex.getMessage(), "Error");
        }
    }

    private void cargarOrdenes() {
        try {
            List<OrdenMantenimiento> lista = servicio.listarOrdenes();
            modeloTabla.setRowCount(0);
            for (OrdenMantenimiento o : lista) {
                modeloTabla.addRow(new Object[]{
                    o.getId(),
                    o.getNumeroOrden(),
                    o.getTipo(),
                    FormatoUtil.formatoMoneda(o.getCostoTotal()),
                    o.getEstado(),
                    o.getDiagnostico()
                });
            }
        } catch (Exception ignored) {
        }
    }
}
