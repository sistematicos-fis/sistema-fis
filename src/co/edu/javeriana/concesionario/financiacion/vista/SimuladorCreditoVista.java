package co.edu.javeriana.concesionario.financiacion.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.financiacion.logica.CalculadoraAmortizacion;
import co.edu.javeriana.concesionario.financiacion.modelo.CuotaAmortizacion;
import co.edu.javeriana.concesionario.financiacion.modelo.TablaAmortizacion;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Vista Swing para la Simulación de Créditos y Tabla de Amortización (CU-13).
 * Responsable: Jerónimo Martin
 */
public class SimuladorCreditoVista extends JPanel {

    private final JTextField txtMonto;
    private final JTextField txtPlazo;
    private final JTextField txtTasaEa;
    private final JLabel lblCuotaEstimada;
    private final JLabel lblTotalIntereses;
    private final JLabel lblTotalPagar;
    private final JTable tblAmortizacion;
    private final DefaultTableModel modeloTabla;

    public SimuladorCreditoVista() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel de Formulario Superior
        JPanel pnlForm = new JPanel(new GridLayout(3, 4, 10, 10));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Parámetros de Simulación (CU-13)"));

        pnlForm.add(new JLabel("Monto a Financiar ($):"));
        txtMonto = new JTextField("60000000");
        pnlForm.add(txtMonto);

        pnlForm.add(new JLabel("Plazo (Meses):"));
        txtPlazo = new JTextField("48");
        pnlForm.add(txtPlazo);

        pnlForm.add(new JLabel("Tasa E.A. (% ej: 16.5):"));
        txtTasaEa = new JTextField("16.5");
        pnlForm.add(txtTasaEa);

        JButton btnCalcular = new JButton("Simular Amortización");
        btnCalcular.setBackground(new java.awt.Color(24, 119, 242));
        btnCalcular.setForeground(java.awt.Color.WHITE);
        btnCalcular.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        btnCalcular.addActionListener(e -> ejecutarSimulacion());
        pnlForm.add(btnCalcular);

        add(pnlForm, BorderLayout.NORTH);

        // Tabla de Resultados
        String[] columnas = {"Cuota #", "Vencimiento", "Cuota Fija", "Abono Capital", "Interés", "Saldo Restante"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblAmortizacion = new JTable(modeloTabla);
        tblAmortizacion.setRowHeight(22);
        add(new JScrollPane(tblAmortizacion), BorderLayout.CENTER);

        // Resumen Inferior
        JPanel pnlResumen = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        lblCuotaEstimada = new JLabel("Cuota Estimada: $ 0");
        lblTotalIntereses = new JLabel("Total Intereses: $ 0");
        lblTotalPagar = new JLabel("Total a Pagar: $ 0");

        pnlResumen.add(lblCuotaEstimada);
        pnlResumen.add(lblTotalIntereses);
        pnlResumen.add(lblTotalPagar);

        add(pnlResumen, BorderLayout.SOUTH);
    }

    private void ejecutarSimulacion() {
        try {
            BigDecimal monto = new BigDecimal(txtMonto.getText().trim());
            int plazo = Integer.parseInt(txtPlazo.getText().trim());
            BigDecimal tasaEaPorc = new BigDecimal(txtTasaEa.getText().trim());
            BigDecimal tasaEa = tasaEaPorc.divide(new BigDecimal("100"), 6, java.math.RoundingMode.HALF_UP);

            TablaAmortizacion tabla = CalculadoraAmortizacion.generarTablaAmortizacion(monto, tasaEa, plazo, LocalDate.now().plusMonths(1));

            modeloTabla.setRowCount(0);
            for (CuotaAmortizacion c : tabla.getCuotas()) {
                modeloTabla.addRow(new Object[]{
                    c.getNumeroCuota(),
                    FormatoUtil.formatoFecha(c.getFechaVencimiento()),
                    FormatoUtil.formatoMoneda(c.getCuotaFija()),
                    FormatoUtil.formatoMoneda(c.getAbonoCapital()),
                    FormatoUtil.formatoMoneda(c.getInteres()),
                    FormatoUtil.formatoMoneda(c.getSaldoRestante())
                });
            }

            lblCuotaEstimada.setText("Cuota Mensual: " + FormatoUtil.formatoMoneda(tabla.getCuotaMensualEstimada()));
            lblTotalIntereses.setText("Total Intereses: " + FormatoUtil.formatoMoneda(tabla.getTotalIntereses()));
            lblTotalPagar.setText("Total a Pagar: " + FormatoUtil.formatoMoneda(tabla.getTotalPagar()));

        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Datos de entrada inválidos: " + ex.getMessage(), "Error en Simulación");
        }
    }
}
