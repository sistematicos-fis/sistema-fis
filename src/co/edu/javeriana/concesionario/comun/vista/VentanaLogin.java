package co.edu.javeriana.concesionario.comun.vista;

import co.edu.javeriana.concesionario.comun.seguridad.logica.AutenticacionException;
import co.edu.javeriana.concesionario.comun.seguridad.logica.AutenticacionServicio;
import co.edu.javeriana.concesionario.comun.seguridad.modelo.Usuario;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VentanaLogin extends Stage {
    private final AutenticacionServicio servicio;
    private final TextField txtUsuario;
    private final PasswordField txtPassword;
    private Usuario usuarioAutenticado;

    public VentanaLogin(AutenticacionServicio servicio) {
        this.servicio = servicio;
        setTitle("Iniciar sesión - Concesionario Integrado");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        Label lblTitulo = new Label("Sistema para Venta y Alquiler de Vehículos y Motocicletas");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 14));
        BorderPane.setAlignment(lblTitulo, Pos.CENTER);
        BorderPane.setMargin(lblTitulo, new Insets(15, 15, 5, 15));

        txtUsuario = new TextField();
        txtUsuario.setPrefColumnCount(15);
        txtPassword = new PasswordField();
        txtPassword.setPrefColumnCount(15);

        GridPane pnlCampos = new GridPane();
        pnlCampos.setHgap(10);
        pnlCampos.setVgap(10);
        pnlCampos.setPadding(new Insets(10, 20, 10, 20));
        pnlCampos.addRow(0, new Label("Usuario:"), txtUsuario);
        pnlCampos.addRow(1, new Label("Contraseña:"), txtPassword);

        Button btnIngresar = new Button("Ingresar");
        Button btnCancelar = new Button("Cancelar");
        btnIngresar.setDefaultButton(true);
        btnCancelar.setCancelButton(true);
        btnIngresar.setOnAction(e -> ingresar());
        btnCancelar.setOnAction(e -> close());

        HBox pnlBotones = new HBox(10, btnCancelar, btnIngresar);
        pnlBotones.setAlignment(Pos.CENTER_RIGHT);
        pnlBotones.setPadding(new Insets(5, 20, 15, 20));

        BorderPane raiz = new BorderPane(pnlCampos, lblTitulo, null, pnlBotones, null);
        setScene(new Scene(raiz));
    }

    public Optional<Usuario> solicitarCredenciales() {
        showAndWait();
        return Optional.ofNullable(usuarioAutenticado);
    }

    private void ingresar() {
        char[] password = txtPassword.getText().toCharArray();
        try {
            usuarioAutenticado = servicio.iniciarSesion(txtUsuario.getText(), password);
            close();
        } catch (AutenticacionException ex) {
            MensajeUtil.mostrarAdvertencia(txtPassword, ex.getMessage(), "Inicio de sesión");
            txtPassword.clear();
            txtPassword.requestFocus();
        } catch (SQLException ex) {
            MensajeUtil.mostrarError(txtPassword, "No se pudo iniciar sesión: falló la conexión con la base de datos. "
                    + "Cierre la aplicación y vuelva a abrirla.", "Error de Base de Datos");
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
