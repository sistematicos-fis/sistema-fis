package co.edu.javeriana.concesionario.comun.vista;

import co.edu.javeriana.concesionario.comun.seguridad.logica.AutenticacionException;
import co.edu.javeriana.concesionario.comun.seguridad.logica.AutenticacionServicio;
import co.edu.javeriana.concesionario.comun.seguridad.modelo.Usuario;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class VentanaLogin extends JDialog {
    private final AutenticacionServicio servicio;
    private final JTextField txtUsuario;
    private final JPasswordField txtPassword;
    private Usuario usuarioAutenticado;

    public VentanaLogin(AutenticacionServicio servicio) {
        this.servicio = servicio;
        setTitle("Iniciar sesión - Concesionario Integrado");
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        JLabel lblTitulo = new JLabel("Sistema para Venta y Alquiler de Vehículos y Motocicletas", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JPanel pnlCampos = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlCampos.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        pnlCampos.add(new JLabel("Usuario:"));
        txtUsuario = new JTextField(15);
        pnlCampos.add(txtUsuario);
        pnlCampos.add(new JLabel("Contraseña:"));
        txtPassword = new JPasswordField(15);
        pnlCampos.add(txtPassword);

        JButton btnIngresar = new JButton("Ingresar");
        JButton btnCancelar = new JButton("Cancelar");
        btnIngresar.addActionListener(e -> ingresar());
        btnCancelar.addActionListener(e -> dispose());
        txtPassword.addActionListener(e -> ingresar());

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnIngresar);

        setLayout(new BorderLayout());
        add(lblTitulo, BorderLayout.NORTH);
        add(pnlCampos, BorderLayout.CENTER);
        add(pnlBotones, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnIngresar);
        pack();
        setLocationRelativeTo(null);
    }

    public Optional<Usuario> solicitarCredenciales() {
        setVisible(true);
        return Optional.ofNullable(usuarioAutenticado);
    }

    private void ingresar() {
        char[] password = txtPassword.getPassword();
        try {
            usuarioAutenticado = servicio.iniciarSesion(txtUsuario.getText(), password);
            dispose();
        } catch (AutenticacionException ex) {
            MensajeUtil.mostrarAdvertencia(this, ex.getMessage(), "Inicio de sesión");
            txtPassword.setText("");
            txtPassword.requestFocusInWindow();
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(this, "No se pudo iniciar sesión: falló la conexión con la base de datos. "
                    + "Cierre la aplicación y vuelva a abrirla.", "Error de Base de Datos");
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
