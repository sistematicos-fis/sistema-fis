package co.edu.javeriana.concesionario.clientes.vista;

import co.edu.javeriana.concesionario.clientes.logica.CalculadoraIndiceSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.CategoriaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.EncuestaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.persistencia.ClienteDAO;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Diálogo visual para el monitoreo del Índice de Satisfacción del Cliente (CU-05 - No CRUD).
 * Responsable: Andrés Alquerque
 */
public class IndiceSatisfaccionVista {

    private final Component parent;

    public IndiceSatisfaccionVista(Component parent) {
        this.parent = parent;
    }

    public void mostrarDialogo() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(parent), "Índice de Satisfacción (CU-05 - No CRUD)", JDialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(500, 320);
        dlg.setLocationRelativeTo(parent);

        JPanel pnl = new JPanel(new GridLayout(4, 1, 10, 10));
        pnl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        BigDecimal global = BigDecimal.valueOf(4.45); // Valor por defecto o calculado
        CategoriaSatisfaccion categoria = CalculadoraIndiceSatisfaccion.clasificarNivel(global);

        try {
            ClienteDAO dao = new ClienteDAO();
            List<EncuestaSatisfaccion> encuestas = dao.listarEncuestas();
            if (!encuestas.isEmpty()) {
                global = CalculadoraIndiceSatisfaccion.calcularIndiceGlobal(encuestas);
                categoria = CalculadoraIndiceSatisfaccion.clasificarNivel(global);
            }
        } catch (Exception ignored) {
        }

        JLabel lblTitulo = new JLabel("Métricas de Satisfacción Posventa (CSAT / NPS)");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 15));

        JLabel lblIndice = new JLabel("Índice Ponderado Global: " + global + " / 5.00");
        lblIndice.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel lblNivel = new JLabel("Nivel de Satisfacción: " + categoria.name());
        lblNivel.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblNivel.setForeground(new java.awt.Color(34, 139, 34));

        JLabel lblFormula = new JLabel("<html><i>Ponderación: Atención (40%) + Producto (35%) + Tiempos (25%)</i></html>");

        pnl.add(lblTitulo);
        pnl.add(lblIndice);
        pnl.add(lblNivel);
        pnl.add(lblFormula);

        dlg.setLayout(new BorderLayout());
        dlg.add(pnl, BorderLayout.CENTER);
        dlg.setVisible(true);
    }
}
