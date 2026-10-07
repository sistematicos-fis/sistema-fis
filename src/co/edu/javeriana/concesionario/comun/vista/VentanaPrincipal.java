package co.edu.javeriana.concesionario.comun.vista;

import co.edu.javeriana.concesionario.alquiler.vista.AlquilerVista;
import co.edu.javeriana.concesionario.clientes.vista.ClienteVista;
import co.edu.javeriana.concesionario.financiacion.vista.FinanciacionVista;
import co.edu.javeriana.concesionario.inventario.vista.InventarioVista;
import co.edu.javeriana.concesionario.legal.vista.LegalVista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Ventana Principal del Sistema Concesionario Integrado.
 * Implementada con Java Swing (Cumple restricción GUI: exclusivamente Swing o JavaFX).
 * Conecta los 5 módulos funcionales del equipo.
 */
public class VentanaPrincipal extends JFrame {

    private final JTabbedPane tabbedPane;
    private final JLabel lblEstado;

    public VentanaPrincipal() {
        setTitle("Sistema para Venta y Alquiler de Vehículos y Motocicletas - Pontificia Universidad Javeriana");
        setSize(1200, 780);
        setMinimumSize(new java.awt.Dimension(960, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Barra de menú superior
        setJMenuBar(crearBarraMenu());

        // Contenedor principal con pestañas para cada módulo
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Inicializar vistas de las 5 áreas activas
        tabbedPane.addTab("Clientes y PQRS", new ClienteVista());
        tabbedPane.addTab("Inventario y Bodega", new InventarioVista());
        tabbedPane.addTab("Financiación (CU-11 a CU-15)", new FinanciacionVista());
        tabbedPane.addTab("Alquiler", new AlquilerVista());
        tabbedPane.addTab("Documentación Legal", new LegalVista());

        // Por defecto, enfocar Financiación (responsabilidad de Jerónimo)
        tabbedPane.setSelectedIndex(2);

        // Barra de estado inferior
        lblEstado = new JLabel(" Sistema Concesionario Inicializado | H2 Database Conectada | Usuario: admin (Administrador General)");
        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JPanel pnlInferior = new JPanel(new BorderLayout());
        pnlInferior.setBackground(new Color(245, 245, 245));
        pnlInferior.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 210, 210)));
        pnlInferior.add(lblEstado, BorderLayout.WEST);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        add(pnlInferior, BorderLayout.SOUTH);
    }

    private JMenuBar crearBarraMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Menú Sistema
        JMenu menuSistema = new JMenu("Sistema");
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));
        menuSistema.add(itemSalir);

        // Menú Módulos
        JMenu menuModulos = new JMenu("Módulos");
        String[] modulos = {
            "Clientes y PQRS (Andrés Alquerque)",
            "Inventario y Bodega (Juan Pablo León)",
            "Financiación (Jerónimo Martin)",
            "Alquiler (Samuel Borras)",
            "Documentación Legal (Samuel Montaña)"
        };
        for (int i = 0; i < modulos.length; i++) {
            final int index = i;
            JMenuItem item = new JMenuItem(modulos[i]);
            item.addActionListener(e -> tabbedPane.setSelectedIndex(index));
            menuModulos.add(item);
        }

        // Menú Ayuda
        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem itemAcercaDe = new JMenuItem("Acerca del Proyecto");
        itemAcercaDe.addActionListener(e -> {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Sistema para Venta y Alquiler de Vehículos y Motocicletas\n" +
                "Pontificia Universidad Javeriana - Fundamentos de Ingeniería de Software\n" +
                "Arquitectura Modular en Capas - Java Swing & H2 Relacional Embebido\n\n" +
                "Integrantes: Andrés Alquerque, Juan Pablo León, Jerónimo Martin,\n" +
                "Samuel Borras, Samuel Montaña.",
                "Acerca de Concesionario Javeriana",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });
        menuAyuda.add(itemAcercaDe);

        menuBar.add(menuSistema);
        menuBar.add(menuModulos);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(menuAyuda);

        return menuBar;
    }
}
