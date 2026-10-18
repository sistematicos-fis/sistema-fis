package co.edu.javeriana.concesionario.comun.vista;

import co.edu.javeriana.concesionario.alquiler.vista.AlquilerVista;
import co.edu.javeriana.concesionario.clientes.vista.ClienteVista;
import co.edu.javeriana.concesionario.comun.seguridad.modelo.Modulo;
import co.edu.javeriana.concesionario.comun.seguridad.modelo.Usuario;
import co.edu.javeriana.concesionario.financiacion.logica.FinanciacionServicio;
import co.edu.javeriana.concesionario.financiacion.persistencia.EntidadFinancieraDAO;
import co.edu.javeriana.concesionario.financiacion.vista.FinanciacionVista;
import co.edu.javeriana.concesionario.inventario.vista.InventarioVista;
import co.edu.javeriana.concesionario.legal.vista.LegalVista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class VentanaPrincipal extends JFrame {

    private final Usuario usuario;
    private final Runnable alCerrarSesion;
    private final JTabbedPane tabbedPane;
    private final Map<Modulo, JComponent> vistas = new EnumMap<>(Modulo.class);

    public VentanaPrincipal(Usuario usuario, Runnable alCerrarSesion) {
        this.usuario = usuario;
        this.alCerrarSesion = alCerrarSesion;

        setTitle("Sistema para Venta y Alquiler de Vehículos y Motocicletas - Pontificia Universidad Javeriana");
        setSize(1200, 780);
        setMinimumSize(new java.awt.Dimension(960, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        for (Modulo modulo : Modulo.values()) {
            if (usuario.puedeAcceder(modulo)) {
                JComponent vista = crearVista(modulo);
                vistas.put(modulo, vista);
                tabbedPane.addTab(modulo.getTitulo(), vista);
            }
        }

        setJMenuBar(crearBarraMenu());

        JLabel lblEstado = new JLabel(" Base de datos H2 conectada | Usuario: " + usuario.getUsername()
                + " (" + usuario.getNombreCompleto() + ") | Rol: " + usuario.getRol().getNombre());
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

    private JComponent crearVista(Modulo modulo) {
        switch (modulo) {
            case CLIENTES:
                return new ClienteVista();
            case INVENTARIO:
                return new InventarioVista();
            case FINANCIACION:
                return new FinanciacionVista(new FinanciacionServicio(new EntidadFinancieraDAO()));
            case ALQUILER:
                return new AlquilerVista();
            case LEGAL:
                return new LegalVista();
            default:
                throw new IllegalArgumentException("Módulo no soportado: " + modulo);
        }
    }

    private JMenuBar crearBarraMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuSistema = new JMenu("Sistema");
        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar sesión");
        itemCerrarSesion.addActionListener(e -> {
            dispose();
            alCerrarSesion.run();
        });
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));
        menuSistema.add(itemCerrarSesion);
        menuSistema.add(itemSalir);

        JMenu menuModulos = new JMenu("Módulos");
        for (Map.Entry<Modulo, JComponent> entrada : vistas.entrySet()) {
            JMenuItem item = new JMenuItem(entrada.getKey().getTitulo());
            item.addActionListener(e -> tabbedPane.setSelectedComponent(entrada.getValue()));
            menuModulos.add(item);
        }

        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem itemAcercaDe = new JMenuItem("Acerca del Proyecto");
        itemAcercaDe.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Sistema para Venta y Alquiler de Vehículos y Motocicletas\n" +
                "Pontificia Universidad Javeriana - Fundamentos de Ingeniería de Software\n" +
                "Aplicación de escritorio en Java Swing con base de datos H2\n\n" +
                "Integrantes: Andrés Alquerque, Juan Pablo León, Jerónimo Martin,\n" +
                "Samuel Borras, Samuel Montaña.",
                "Acerca de Concesionario Javeriana",
                JOptionPane.INFORMATION_MESSAGE));
        menuAyuda.add(itemAcercaDe);

        menuBar.add(menuSistema);
        menuBar.add(menuModulos);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(menuAyuda);

        return menuBar;
    }
}
