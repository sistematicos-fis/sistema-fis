package co.edu.javeriana.concesionario.financiacion.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.financiacion.logica.FinanciacionServicio;
import co.edu.javeriana.concesionario.financiacion.modelo.CreditoFormalizado;
import co.edu.javeriana.concesionario.financiacion.modelo.SolicitudCredito;

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
 * Vista Swing para la Radicación (CU-14) y Formalización con Simulador Bancario (CU-15).
 * Responsable: Jerónimo Martin
 */
public class SolicitudesCreditoVista extends JPanel {

    private final FinanciacionServicio servicio;
    private final JTextField txtDocCliente;
    private final JTextField txtMonto;
    private final JTextField txtPlazo;
    private final JTextField txtIngresos;
    private final JTable tblSolicitudes;
    private final DefaultTableModel modeloTabla;

    public SolicitudesCreditoVista() {
        this.servicio = new FinanciacionServicio();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulario Superior para radicar
        JPanel pnlForm = new JPanel(new GridLayout(2, 5, 8, 8));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Radicar Nueva Solicitud (CU-14 - Regla del 30%)"));

        pnlForm.add(new JLabel("Doc. Cliente:"));
        txtDocCliente = new JTextField("1018456789");
        pnlForm.add(txtDocCliente);

        pnlForm.add(new JLabel("Monto Solicitado ($):"));
        txtMonto = new JTextField("50000000");
        pnlForm.add(txtMonto);

        pnlForm.add(new JLabel("Plazo (meses):"));
        txtPlazo = new JTextField("48");
        pnlForm.add(txtPlazo);

        pnlForm.add(new JLabel("Ingresos Mensuales ($):"));
        txtIngresos = new JTextField("6500000");
        pnlForm.add(txtIngresos);

        JButton btnRadicar = new JButton("Radicar Solicitud");
        btnRadicar.addActionListener(e -> radicarSolicitud());
        pnlForm.add(btnRadicar);

        add(pnlForm, BorderLayout.NORTH);

        // Tabla de Solicitudes
        String[] columnas = {"ID", "Radicado", "Monto", "Plazo", "Cuota Estimada", "% Endeudamiento", "Estado", "Score"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblSolicitudes = new JTable(modeloTabla);
        tblSolicitudes.setRowHeight(22);
        add(new JScrollPane(tblSolicitudes), BorderLayout.CENTER);

        // Barra inferior con botón de formalización con banco simulado (CU-15)
        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRefrescar = new JButton("Actualizar Lista");
        btnRefrescar.addActionListener(e -> cargarSolicitudes());
        pnlAcciones.add(btnRefrescar);

        JButton btnEvaluarBanco = new JButton("Formalizar con Simulador Bancario (CU-15)");
        btnEvaluarBanco.setBackground(new java.awt.Color(34, 139, 34));
        btnEvaluarBanco.setForeground(java.awt.Color.WHITE);
        btnEvaluarBanco.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        btnEvaluarBanco.addActionListener(e -> formalizarSolicitudSeleccionada());
        pnlAcciones.add(btnEvaluarBanco);

        add(pnlAcciones, BorderLayout.SOUTH);

        cargarSolicitudes();
    }

    private void radicarSolicitud() {
        try {
            String doc = txtDocCliente.getText().trim();
            BigDecimal monto = new BigDecimal(txtMonto.getText().trim());
            int plazo = Integer.parseInt(txtPlazo.getText().trim());
            BigDecimal ingresos = new BigDecimal(txtIngresos.getText().trim());

            SolicitudCredito s = servicio.radicarSolicitud(
                    1L, 1L, 1L, monto, plazo, BigDecimal.ZERO, ingresos, BigDecimal.ZERO, doc
            );

            MensajeUtil.mostrarInfo(this, "Solicitud radicada con éxito.\nRadicado: " + s.getNumeroRadicado() + 
                    "\nEstado: " + s.getEstado() + "\n% Endeudamiento: " + s.getPorcentajeEndeudamiento() + "%", "Radicación Exitosa");

            cargarSolicitudes();
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, ex.getMessage(), "Error al Radicar");
        }
    }

    private void formalizarSolicitudSeleccionada() {
        int fila = tblSolicitudes.getSelectedRow();
        if (fila == -1) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione una solicitud de la tabla para evaluarla con el simulador.", "Aviso");
            return;
        }

        Long idSol = (Long) modeloTabla.getValueAt(fila, 0);
        String doc = txtDocCliente.getText().trim();

        try {
            CreditoFormalizado cf = servicio.formalizarSolicitudConBanco(idSol, doc);
            MensajeUtil.mostrarInfo(this, "¡Crédito Formalizado Exitosamente!\n" +
                    "Código Aprobación Bancaria (Simulada): " + cf.getCodigoAprobacionBanco() + "\n" +
                    "Monto Aprobado: " + FormatoUtil.formatoMoneda(cf.getMontoAprobado()) + "\n" +
                    "Plazo: " + cf.getPlazoMeses() + " meses\n" +
                    "Tabla de Amortización generada e insertada en H2.", "Aprobación Bancaria Exitosa");
            cargarSolicitudes();
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Resultado: " + ex.getMessage(), "Evaluación Simulador Bancario");
            cargarSolicitudes();
        }
    }

    private void cargarSolicitudes() {
        try {
            List<SolicitudCredito> lista = servicio.listarSolicitudes();
            modeloTabla.setRowCount(0);
            for (SolicitudCredito s : lista) {
                modeloTabla.addRow(new Object[]{
                    s.getId(),
                    s.getNumeroRadicado(),
                    FormatoUtil.formatoMoneda(s.getMontoSolicitado()),
                    s.getPlazoMeses() + "m",
                    FormatoUtil.formatoMoneda(s.getCuotaEstimada()),
                    s.getPorcentajeEndeudamiento() + "%",
                    s.getEstado(),
                    s.getScoreCrediticioSimulado() != null ? s.getScoreCrediticioSimulado() : "-"
                });
            }
        } catch (Exception e) {
            // Manejo silencioso en carga inicial si la tabla aún no tiene datos
        }
    }
}
