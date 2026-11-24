package co.edu.javeriana.concesionario.clientes.vista;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Pestaña del área Clientes y PQRS: agrupa la gestión de clientes, PQRS y encuestas.
 * Responsable: Andrés Alquerque
 */
public class ClientesModuloVista extends TabPane {

    public ClientesModuloVista(ClienteVista clienteVista, PqrsVista pqrsVista, EncuestaVista encuestaVista) {
        setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab tabClientes = new Tab("Clientes", clienteVista);
        Tab tabPqrs = new Tab("PQRS", pqrsVista);
        Tab tabEncuestas = new Tab("Encuestas", encuestaVista);
        getTabs().addAll(tabClientes, tabPqrs, tabEncuestas);

        getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual == tabPqrs) {
                pqrsVista.recargar();
            } else if (actual == tabEncuestas) {
                encuestaVista.recargar();
            }
        });
    }
}
