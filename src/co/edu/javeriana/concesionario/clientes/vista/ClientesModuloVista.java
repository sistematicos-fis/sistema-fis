package co.edu.javeriana.concesionario.clientes.vista;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Pestaña del área Clientes y PQRS: agrupa la gestión de clientes y la de PQRS.
 * Responsable: Andrés Alquerque
 */
public class ClientesModuloVista extends TabPane {

    public ClientesModuloVista(ClienteVista clienteVista, PqrsVista pqrsVista) {
        setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab tabClientes = new Tab("Clientes", clienteVista);
        Tab tabPqrs = new Tab("PQRS", pqrsVista);
        getTabs().addAll(tabClientes, tabPqrs);

        getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual == tabPqrs) {
                pqrsVista.recargar();
            }
        });
    }
}
