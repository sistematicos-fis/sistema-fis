package co.edu.javeriana.concesionario.comun.vista;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Piezas de interfaz que comparten las pantallas de todos los módulos.
 */
public final class ComponentesFX {

    private ComponentesFX() {
    }

    public static TableView<Object[]> crearTabla(String... columnas) {
        TableView<Object[]> tabla = new TableView<>();
        for (int i = 0; i < columnas.length; i++) {
            final int indice = i;
            TableColumn<Object[], Object> columna = new TableColumn<>(columnas[i]);
            columna.setCellValueFactory(fila -> new SimpleObjectProperty<>(fila.getValue()[indice]));
            columna.setSortable(false);
            tabla.getColumns().add(columna);
        }
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPlaceholder(new Label("No hay registros para mostrar."));
        return tabla;
    }

    public static GridPane crearFormulario(int columnas, Node... nodos) {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        for (int c = 0; c < columnas; c++) {
            ColumnConstraints restriccion = new ColumnConstraints();
            restriccion.setHgrow(Priority.ALWAYS);
            restriccion.setFillWidth(true);
            restriccion.setPercentWidth(100.0 / columnas);
            grid.getColumnConstraints().add(restriccion);
        }
        for (int i = 0; i < nodos.length; i++) {
            Node nodo = nodos[i];
            if (nodo instanceof javafx.scene.control.Control control) {
                control.setMaxWidth(Double.MAX_VALUE);
            }
            grid.add(nodo, i % columnas, i / columnas);
        }
        return grid;
    }

    public static TitledPane crearSeccion(String titulo, Node contenido) {
        TitledPane seccion = new TitledPane(titulo, contenido);
        seccion.setCollapsible(false);
        return seccion;
    }

    public static HBox crearBarraBotones(Pos alineacion, Node... botones) {
        HBox barra = new HBox(10, botones);
        barra.setAlignment(alineacion);
        barra.setPadding(new Insets(10, 0, 0, 0));
        return barra;
    }
}
