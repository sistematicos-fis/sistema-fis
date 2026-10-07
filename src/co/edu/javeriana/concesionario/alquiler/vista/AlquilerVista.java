package co.edu.javeriana.concesionario.alquiler.vista;

import co.edu.javeriana.concesionario.alquiler.logica.PuntoEntregaService;
import co.edu.javeriana.concesionario.alquiler.modelo.PuntoEntrega;
import co.edu.javeriana.concesionario.alquiler.persistencia.InMemoryPuntoEntregaRepository;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
 * Panel Swing para la gestión del módulo de Alquiler y Puntos de Entrega (CU-16 a CU-20).
 * Responsable: Samuel Borras
 */
public class AlquilerVista extends JPanel {

    private final PuntoEntregaService service;
    private final JTextField txtId;
    private final JTextField txtNombre;
    private final JTextField txtDireccion;
    private final JTextField txtCiudad;
    private final JTable tblPuntos;
    private final DefaultTableModel modeloTabla;

    public AlquilerVista() {
        this.service = new PuntoEntregaService(new InMemoryPuntoEntregaRepository());
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulario
        JPanel pnlForm = new JPanel(new GridLayout(2, 5, 8, 8));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Gestionar Puntos de Entrega / Devolución (CU-16)"));

        pnlForm.add(new JLabel("Código Sede:"));
        txtId = new JTextField("SEDE-01");
        pnlForm.add(txtId);

        pnlForm.add(new JLabel("Nombre Sede:"));
        txtNombre = new JTextField("Sede Aeropuerto");
        pnlForm.add(txtNombre);

        pnlForm.add(new JLabel("Dirección:"));
        txtDireccion = new JTextField("Av. El Dorado # 103-09");
        pnlForm.add(txtDireccion);

        pnlForm.add(new JLabel("Ciudad:"));
        txtCiudad = new JTextField("Bogotá");
        pnlForm.add(txtCiudad);

        JButton btnRegistrar = new JButton("Registrar Punto");
        btnRegistrar.addActionListener(e -> registrarPunto());
        pnlForm.add(btnRegistrar);

        add(pnlForm, BorderLayout.NORTH);

        // Tabla Central
        String[] columnas = {"ID", "Nombre de Sede", "Dirección", "Ciudad", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblPuntos = new JTable(modeloTabla);
        tblPuntos.setRowHeight(22);
        add(new JScrollPane(tblPuntos), BorderLayout.CENTER);

        // Barra Inferior
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnDeshabilitar = new JButton("Deshabilitar Sede Seleccionada");
        btnDeshabilitar.addActionListener(e -> deshabilitarPunto());
        pnlSur.add(btnDeshabilitar);

        add(pnlSur, BorderLayout.SOUTH);

        // Cargar sedes demo
        service.registrarPunto("SEDE-01", "Sede Norte Autopista", "Auto Norte # 170-20", "Bogotá");
        service.registrarPunto("SEDE-02", "Sede Aeropuerto El Dorado", "Avenida El Dorado # 103-09", "Bogotá");
        actualizarTabla();
    }

    private void registrarPunto() {
        try {
            String id = txtId.getText().trim();
            String nom = txtNombre.getText().trim();
            String dir = txtDireccion.getText().trim();
            String ciu = txtCiudad.getText().trim();

            service.registrarPunto(id, nom, dir, ciu);
            MensajeUtil.mostrarInfo(this, "Punto de entrega registrado exitosamente.", "Éxito");
            actualizarTabla();

            txtId.setText("");
            txtNombre.setText("");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, ex.getMessage(), "Error al Registrar Sede");
        }
    }

    private void deshabilitarPunto() {
        int fila = tblPuntos.getSelectedRow();
        if (fila == -1) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione un punto de entrega en la tabla.", "Aviso");
            return;
        }
        String id = (String) modeloTabla.getValueAt(fila, 0);
        service.deshabilitarPunto(id);
        actualizarTabla();
    }

    private void actualizarTabla() {
        List<PuntoEntrega> lista = service.listarPuntosActivos();
        modeloTabla.setRowCount(0);
        for (PuntoEntrega p : lista) {
            modeloTabla.addRow(new Object[]{
                p.getId(),
                p.getNombre(),
                p.getDireccion(),
                p.getCiudad(),
                p.isActivo() ? "ACTIVO" : "INACTIVO"
            });
        }
    }
}
