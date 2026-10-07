package co.edu.javeriana.concesionario.financiacion.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.financiacion.logica.FinanciacionServicio;
import co.edu.javeriana.concesionario.financiacion.modelo.CuotaAmortizacion;
import co.edu.javeriana.concesionario.financiacion.modelo.PagoCredito;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
 * Vista Swing para la consulta del plan de pagos y registro de abonos a cuotas.
 * Responsable: Jerónimo Martin
 */
public class PagosCreditoVista extends JPanel {

    private final FinanciacionServicio servicio;
    private final JTextField txtCreditoId;
    private final JTable tblCuotas;
    private final DefaultTableModel modeloTabla;

    public PagosCreditoVista() {
        this.servicio = new FinanciacionServicio();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Barra Superior de Búsqueda
        JPanel pnlBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlBusqueda.setBorder(BorderFactory.createTitledBorder("Consultar Crédito Formalizado"));

        pnlBusqueda.add(new JLabel("ID Crédito Formalizado:"));
        txtCreditoId = new JTextField("1", 8);
        pnlBusqueda.add(txtCreditoId);

        JButton btnBuscar = new JButton("Cargar Cuotas");
        btnBuscar.addActionListener(e -> cargarCuotas());
        pnlBusqueda.add(btnBuscar);

        add(pnlBusqueda, BorderLayout.NORTH);

        // Tabla de Cuotas
        String[] columnas = {"Cuota #", "Vencimiento", "Valor Cuota", "Abono Capital", "Interés", "Saldo Restante", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCuotas = new JTable(modeloTabla);
        tblCuotas.setRowHeight(22);
        add(new JScrollPane(tblCuotas), BorderLayout.CENTER);

        // Barra Inferior de Pago
        JPanel pnlPago = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnPagar = new JButton("Registrar Pago de Cuota Seleccionada");
        btnPagar.setBackground(new java.awt.Color(34, 139, 34));
        btnPagar.setForeground(java.awt.Color.WHITE);
        btnPagar.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        btnPagar.addActionListener(e -> registrarPago());
        pnlPago.add(btnPagar);

        add(pnlPago, BorderLayout.SOUTH);
    }

    private void cargarCuotas() {
        try {
            Long credId = Long.parseLong(txtCreditoId.getText().trim());
            List<CuotaAmortizacion> cuotas = servicio.consultarCuotasDeCredito(credId);
            modeloTabla.setRowCount(0);

            for (CuotaAmortizacion c : cuotas) {
                modeloTabla.addRow(new Object[]{
                    c.getNumeroCuota(),
                    FormatoUtil.formatoFecha(c.getFechaVencimiento()),
                    FormatoUtil.formatoMoneda(c.getCuotaFija()),
                    FormatoUtil.formatoMoneda(c.getAbonoCapital()),
                    FormatoUtil.formatoMoneda(c.getInteres()),
                    FormatoUtil.formatoMoneda(c.getSaldoRestante()),
                    c.getEstado()
                });
            }

            if (cuotas.isEmpty()) {
                MensajeUtil.mostrarAdvertencia(this, "No se encontraron cuotas para el crédito ID: " + credId, "Sin Registros");
            }
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al cargar cuotas: " + ex.getMessage(), "Error");
        }
    }

    private void registrarPago() {
        int fila = tblCuotas.getSelectedRow();
        if (fila == -1) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione la cuota a pagar en la tabla.", "Aviso");
            return;
        }

        try {
            Long credId = Long.parseLong(txtCreditoId.getText().trim());
            int numCuota = (Integer) modeloTabla.getValueAt(fila, 0);
            String estado = (String) modeloTabla.getValueAt(fila, 6);

            if ("PAGADA".equalsIgnoreCase(estado)) {
                MensajeUtil.mostrarAdvertencia(this, "La cuota #" + numCuota + " ya fue pagada previamente.", "Ya Pagada");
                return;
            }

            // Obtener monto
            String strValor = (String) modeloTabla.getValueAt(fila, 2);
            BigDecimal monto = new BigDecimal(strValor.replaceAll("[^0-9]", "")).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);

            PagoCredito pago = servicio.registrarPagoCuota(credId, numCuota, monto, "TRANSFERENCIA_SIMULADA");
            MensajeUtil.mostrarInfo(this, "¡Pago registrado exitosamente!\nRecibo N°: " + pago.getNumeroRecibo() +
                    "\nCuota #" + numCuota + " marcada como PAGADA en H2.", "Pago Exitoso");

            cargarCuotas();
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al registrar pago: " + ex.getMessage(), "Error de Pago");
        }
    }
}
