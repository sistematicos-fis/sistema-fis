package co.edu.javeriana.concesionario.financiacion.vista;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Panel contenedor principal del Módulo de Financiación (CU-11 a CU-15).
 * Responsable: Jerónimo Martin
 */
public class FinanciacionVista extends JPanel {

    public FinanciacionVista() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setFont(new Font("SansSerif", Font.PLAIN, 12));

        pestañas.addTab("Simulador de Crédito (CU-13)", new SimuladorCreditoVista());
        pestañas.addTab("Radicación & Evaluación Bancaria (CU-14 & CU-15)", new SolicitudesCreditoVista());
        pestañas.addTab("Gestión de Pagos de Cuotas", new PagosCreditoVista());

        add(pestañas, BorderLayout.CENTER);
    }
}
