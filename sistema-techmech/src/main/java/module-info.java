module co.edu.javeriana.techmech.sistematechmech {
    requires javafx.controls;
    requires javafx.fxml;


    opens co.edu.javeriana.techmech.sistematechmech to javafx.fxml;
    exports co.edu.javeriana.techmech.sistematechmech;
}