package co.edu.javeriana.concesionario.comun.vista;

import co.edu.javeriana.concesionario.alquiler.vista.AlquilerVista;
import co.edu.javeriana.concesionario.clientes.vista.ClienteVista;
import co.edu.javeriana.concesionario.comun.seguridad.modelo.Modulo;
import co.edu.javeriana.concesionario.comun.seguridad.modelo.Usuario;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.financiacion.logica.FinanciacionServicio;
import co.edu.javeriana.concesionario.financiacion.persistencia.EntidadFinancieraDAO;
import co.edu.javeriana.concesionario.financiacion.vista.FinanciacionVista;
import co.edu.javeriana.concesionario.inventario.vista.InventarioVista;
import co.edu.javeriana.concesionario.legal.vista.LegalVista;

import java.util.EnumMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class VentanaPrincipal extends Stage {

    private final Usuario usuario;
    private final Runnable alCerrarSesion;
    private final TabPane tabPane;
    private final Map<Modulo, Tab> pestanas = new EnumMap<>(Modulo.class);

    public VentanaPrincipal(Usuario usuario, Runnable alCerrarSesion) {
        this.usuario = usuario;
        this.alCerrarSesion = alCerrarSesion;

        setTitle("Sistema para Venta y Alquiler de Vehículos y Motocicletas - Pontificia Universidad Javeriana");
        setMinWidth(960);
        setMinHeight(600);
        setOnCloseRequest(e -> salir());

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        for (Modulo modulo : Modulo.values()) {
            if (usuario.puedeAcceder(modulo)) {
                Node vista = crearVista(modulo);
                vista.setStyle("-fx-font-weight: normal; -fx-font-size: 12px;");
                Tab pestana = new Tab(modulo.getTitulo(), vista);
                pestanas.put(modulo, pestana);
                tabPane.getTabs().add(pestana);
            }
        }

        Label lblEstado = new Label("Base de datos H2 conectada | Usuario: " + usuario.getUsername()
                + " (" + usuario.getNombreCompleto() + ") | Rol: " + usuario.getRol().getNombre());
        HBox pnlInferior = new HBox(lblEstado);
        pnlInferior.setPadding(new Insets(6, 12, 6, 12));
        pnlInferior.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #d2d2d2; -fx-border-width: 1 0 0 0;");

        BorderPane raiz = new BorderPane(tabPane, crearBarraMenu(), null, pnlInferior, null);
        setScene(new Scene(raiz, 1200, 780));
        centerOnScreen();
    }

    private Node crearVista(Modulo modulo) {
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

    private MenuBar crearBarraMenu() {
        MenuItem itemCerrarSesion = new MenuItem("Cerrar sesión");
        itemCerrarSesion.setOnAction(e -> {
            close();
            alCerrarSesion.run();
        });
        MenuItem itemSalir = new MenuItem("Salir");
        itemSalir.setOnAction(e -> salir());
        Menu menuSistema = new Menu("Sistema", null, itemCerrarSesion, itemSalir);

        Menu menuModulos = new Menu("Módulos");
        for (Map.Entry<Modulo, Tab> entrada : pestanas.entrySet()) {
            MenuItem item = new MenuItem(entrada.getKey().getTitulo());
            item.setOnAction(e -> tabPane.getSelectionModel().select(entrada.getValue()));
            menuModulos.getItems().add(item);
        }

        MenuItem itemAcercaDe = new MenuItem("Acerca del Proyecto");
        itemAcercaDe.setOnAction(e -> MensajeUtil.mostrarInfo(tabPane,
                "Sistema para Venta y Alquiler de Vehículos y Motocicletas\n" +
                "Pontificia Universidad Javeriana - Fundamentos de Ingeniería de Software\n" +
                "Aplicación de escritorio en JavaFX con base de datos H2\n\n" +
                "Integrantes: Andrés Alquerque, Juan Pablo León, Jerónimo Martin,\n" +
                "Samuel Borras, Samuel Montaña.",
                "Acerca de Concesionario Javeriana"));
        Menu menuAyuda = new Menu("Ayuda", null, itemAcercaDe);

        return new MenuBar(menuSistema, menuModulos, menuAyuda);
    }

    private void salir() {
        Platform.exit();
        System.exit(0);
    }
}
