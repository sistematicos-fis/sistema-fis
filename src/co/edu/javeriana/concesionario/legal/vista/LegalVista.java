package co.edu.javeriana.concesionario.legal.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.legal.logica.LegalServicio;
import co.edu.javeriana.concesionario.legal.modelo.PolizaSeguro;

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
 * Panel Swing para la gestión legal y emisión de pólizas con aseguradora simulada (CU-31 a CU-35).
 * Responsable: Samuel Montaña
 */
public class LegalVista extends JPanel {

    private final LegalServicio servicio;
    private final JTextField txtVin;
    private final JTextField txtValorComercial;
    private final JComboBox<String> cmbTipoPoliza;
    private final JTable tblPolizas;
    private final DefaultTableModel modeloTabla;

    public LegalVista() {
        this.servicio = new LegalServicio();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulario
        JPanel pnlForm = new JPanel(new GridLayout(2, 4, 8, 8));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Emitir Póliza con Aseguradora Simulada (CU-34)"));

        pnlForm.add(new JLabel("VIN Vehículo:"));
        txtVin = new JTextField("93HBL4820P1029381");
        pnlForm.add(txtVin);

        pnlForm.add(new JLabel("Valor Comercial ($):"));
        txtValorComercial = new JTextField("115000000");
        pnlForm.add(txtValorComercial);

        pnlForm.add(new JLabel("Tipo de Póliza:"));
        cmbTipoPoliza = new JComboBox<>(new String[]{"TODO_RIESGO_RESPONSABILIDAD", "SOAT", "EXTRACONTRACTUAL"});
        pnlForm.add(cmbTipoPoliza);

        JButton btnEmitir = new JButton("Emitir Póliza (Simulador)");
        btnEmitir.setBackground(new java.awt.Color(34, 139, 34));
        btnEmitir.setForeground(java.awt.Color.WHITE);
        btnEmitir.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        btnEmitir.addActionListener(e -> emitirPoliza());
        pnlForm.add(btnEmitir);

        add(pnlForm, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"ID", "N° Póliza", "Tipo", "Prima Anual", "Cobertura Total", "Estado", "Cód. Autorización"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblPolizas = new JTable(modeloTabla);
        tblPolizas.setRowHeight(22);
        add(new JScrollPane(tblPolizas), BorderLayout.CENTER);

        // Barra Sur
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRefrescar = new JButton("Actualizar Pólizas");
        btnRefrescar.addActionListener(e -> cargarPolizas());
        pnlSur.add(btnRefrescar);

        add(pnlSur, BorderLayout.SOUTH);

        cargarPolizas();
    }

    private void emitirPoliza() {
        try {
            String vin = txtVin.getText().trim();
            BigDecimal valor = new BigDecimal(txtValorComercial.getText().trim());
            String tipo = (String) cmbTipoPoliza.getSelectedItem();

            PolizaSeguro poliza = servicio.emitirPolizaConSimulador(1L, 1L, 1L, vin, valor, tipo);
            MensajeUtil.mostrarInfo(this, "¡Póliza emitida exitosamente!\nN°: " + poliza.getNumeroPoliza() +
                    "\nCód. Autorización Aseguradora: " + poliza.getCodigoAprobacionSimulada() +
                    "\nValor Prima Anual: " + FormatoUtil.formatoMoneda(poliza.getValorPrima()) +
                    "\nCobertura Total: " + FormatoUtil.formatoMoneda(poliza.getValorCobertura()), "Emisión Exitosa");

            cargarPolizas();
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al emitir: " + ex.getMessage(), "Error");
        }
    }

    private void cargarPolizas() {
        try {
            List<PolizaSeguro> lista = servicio.listarPolizas();
            modeloTabla.setRowCount(0);
            for (PolizaSeguro p : lista) {
                modeloTabla.addRow(new Object[]{
                    p.getId(),
                    p.getNumeroPoliza(),
                    p.getTipoPoliza(),
                    FormatoUtil.formatoMoneda(p.getValorPrima()),
                    FormatoUtil.formatoMoneda(p.getValorCobertura()),
                    p.getEstado(),
                    p.getCodigoAprobacionSimulada()
                });
            }
        } catch (Exception ignored) {
        }
    }
}
