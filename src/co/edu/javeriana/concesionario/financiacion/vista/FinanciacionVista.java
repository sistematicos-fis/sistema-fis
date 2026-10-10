package co.edu.javeriana.concesionario.financiacion.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.financiacion.logica.FinanciacionServicio;
import co.edu.javeriana.concesionario.financiacion.logica.ReglaNegocioException;
import co.edu.javeriana.concesionario.financiacion.modelo.EntidadFinanciera;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class FinanciacionVista extends JPanel {
    private final FinanciacionServicio servicio;

    private final JTextField txtCodigo;
    private final JTextField txtNombre;
    private final JTextField txtNit;
    private final JTextField txtTasaEa;
    private final JTextField txtScoring;
    private final JCheckBox chkActivo;
    private final JTable tblEntidades;
    private final DefaultTableModel modeloTabla;

    private Long idSeleccionado;

    public FinanciacionVista() {
        this.servicio = new FinanciacionServicio();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel pnlForm = new JPanel(new GridLayout(3, 4, 10, 10));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Entidad Financiera Aliada (CU-11)"));

        pnlForm.add(new JLabel("Código (ej: BANCOL):"));
        txtCodigo = new JTextField();
        pnlForm.add(txtCodigo);

        pnlForm.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        pnlForm.add(txtNombre);

        pnlForm.add(new JLabel("NIT (ej: 890.903.938-8):"));
        txtNit = new JTextField();
        pnlForm.add(txtNit);

        pnlForm.add(new JLabel("Tasa base E.A. (% ej: 16.5):"));
        txtTasaEa = new JTextField();
        pnlForm.add(txtTasaEa);

        pnlForm.add(new JLabel("Scoring mínimo (300 - 900):"));
        txtScoring = new JTextField();
        pnlForm.add(txtScoring);

        pnlForm.add(new JLabel("Estado:"));
        chkActivo = new JCheckBox("Activa", true);
        pnlForm.add(chkActivo);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnNuevo = new JButton("Nuevo");
        btnNuevo.addActionListener(e -> limpiarFormulario());
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardarEntidad());
        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(e -> eliminarEntidad());
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarEntidades());
        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnGuardar);
        pnlBotones.add(btnEliminar);
        pnlBotones.add(btnRefrescar);

        JPanel pnlNorte = new JPanel(new BorderLayout());
        pnlNorte.add(pnlForm, BorderLayout.CENTER);
        pnlNorte.add(pnlBotones, BorderLayout.SOUTH);
        add(pnlNorte, BorderLayout.NORTH);

        String[] columnas = {"ID", "Código", "Nombre", "NIT", "Tasa E.A.", "Scoring Mín.", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblEntidades = new JTable(modeloTabla);
        tblEntidades.setRowHeight(22);
        tblEntidades.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblEntidades.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });
        JScrollPane scroll = new JScrollPane(tblEntidades);
        scroll.setBorder(BorderFactory.createTitledBorder("Entidades registradas (seleccione una fila para editarla o eliminarla)"));
        add(scroll, BorderLayout.CENTER);

        cargarEntidades();
    }

    private void cargarEntidades() {
        try {
            List<EntidadFinanciera> lista = servicio.listarEntidades();
            modeloTabla.setRowCount(0);
            for (EntidadFinanciera e : lista) {
                modeloTabla.addRow(new Object[]{
                    e.getId(),
                    e.getCodigo(),
                    e.getNombre(),
                    e.getNit(),
                    FormatoUtil.formatoPorcentaje(e.getTasaBaseEa()),
                    e.getScoringMinimo(),
                    e.isActivo() ? "Activa" : "Inactiva"
                });
            }
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudieron consultar las entidades financieras: " + ex.getMessage()
                    + "\nVerifique la conexión con la base de datos e intente de nuevo.", "Error de Consulta");
        }
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tblEntidades.getSelectedRow();
        if (fila == -1) {
            return;
        }
        Long id = (Long) modeloTabla.getValueAt(fila, 0);
        try {
            servicio.buscarEntidad(id).ifPresent(e -> {
                idSeleccionado = e.getId();
                txtCodigo.setText(e.getCodigo());
                txtNombre.setText(e.getNombre());
                txtNit.setText(e.getNit());
                txtTasaEa.setText(e.getTasaBaseEa().multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                txtScoring.setText(String.valueOf(e.getScoringMinimo()));
                chkActivo.setSelected(e.isActivo());
            });
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo cargar la entidad seleccionada: " + ex.getMessage(), "Error de Consulta");
        }
    }

    private void guardarEntidad() {
        EntidadFinanciera entidad;
        try {
            entidad = leerFormulario();
        } catch (NumberFormatException ex) {
            MensajeUtil.mostrarAdvertencia(this, "No se pudo guardar la entidad: la Tasa E.A. y el Scoring mínimo deben ser números "
                    + "(ej: 16.5 y 650).", "Datos Inválidos");
            return;
        }

        try {
            if (idSeleccionado == null) {
                servicio.registrarEntidad(entidad);
                MensajeUtil.mostrarInfo(this, "Entidad financiera " + entidad.getNombre() + " registrada correctamente.", "Registro Exitoso");
            } else {
                servicio.actualizarEntidad(entidad);
                MensajeUtil.mostrarInfo(this, "Entidad financiera " + entidad.getNombre() + " actualizada correctamente.", "Actualización Exitosa");
            }
            limpiarFormulario();
            cargarEntidades();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Datos Inválidos");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo guardar la entidad en la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
    }

    private void eliminarEntidad() {
        if (idSeleccionado == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione una entidad de la tabla para eliminarla.", "Aviso");
            return;
        }
        if (!MensajeUtil.confirmar(this, "¿Desea eliminar la entidad financiera " + txtNombre.getText() + "?", "Confirmar Eliminación")) {
            return;
        }
        try {
            servicio.eliminarEntidad(idSeleccionado);
            MensajeUtil.mostrarInfo(this, "Entidad financiera eliminada correctamente.", "Eliminación Exitosa");
            limpiarFormulario();
            cargarEntidades();
        } catch (ReglaNegocioException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "No se puede eliminar");
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo eliminar la entidad de la base de datos: " + ex.getMessage(), "Error de Base de Datos");
        }
    }

    private EntidadFinanciera leerFormulario() {
        EntidadFinanciera entidad = new EntidadFinanciera();
        entidad.setId(idSeleccionado);
        entidad.setCodigo(txtCodigo.getText());
        entidad.setNombre(txtNombre.getText());
        entidad.setNit(txtNit.getText());
        BigDecimal tasaPorcentaje = new BigDecimal(txtTasaEa.getText().trim());
        entidad.setTasaBaseEa(tasaPorcentaje.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        entidad.setScoringMinimo(Integer.parseInt(txtScoring.getText().trim()));
        entidad.setActivo(chkActivo.isSelected());
        return entidad;
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        txtCodigo.setText("");
        txtNombre.setText("");
        txtNit.setText("");
        txtTasaEa.setText("");
        txtScoring.setText("");
        chkActivo.setSelected(true);
        tblEntidades.clearSelection();
    }
}
