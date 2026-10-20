package co.edu.javeriana.concesionario.alquiler.vista;

import co.edu.javeriana.concesionario.alquiler.logica.PuntoEntregaService;
import co.edu.javeriana.concesionario.alquiler.modelo.PuntoEntrega;
import co.edu.javeriana.concesionario.alquiler.persistencia.InMemoryPuntoEntregaRepository;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * Pantalla JavaFX para la gestión del módulo de Alquiler y Puntos de Entrega (CU-16 a CU-20).
 * Responsable: Samuel Borras
 */
public class AlquilerVista extends BorderPane {

    private final PuntoEntregaService service;
    private final TextField txtId;
    private final TextField txtNombre;
    private final TextField txtDireccion;
    private final TextField txtCiudad;
    private final TableView<Object[]> tblPuntos;

    public AlquilerVista() {
        this.service = new PuntoEntregaService(new InMemoryPuntoEntregaRepository());
        setPadding(new Insets(15));

        // Formulario
        txtId = new TextField("SEDE-01");
        txtNombre = new TextField("Sede Aeropuerto");
        txtDireccion = new TextField("Av. El Dorado # 103-09");
        txtCiudad = new TextField("Bogotá");

        Button btnRegistrar = new Button("Registrar Punto");
        btnRegistrar.setOnAction(e -> registrarPunto());

        setTop(ComponentesFX.crearSeccion("Gestionar Puntos de Entrega / Devolución (CU-16)", ComponentesFX.crearFormulario(5,
                new Label("Código Sede:"), txtId,
                new Label("Nombre Sede:"), txtNombre,
                new Label("Dirección:"), txtDireccion,
                new Label("Ciudad:"), txtCiudad,
                btnRegistrar)));

        // Tabla Central
        tblPuntos = ComponentesFX.crearTabla("ID", "Nombre de Sede", "Dirección", "Ciudad", "Estado");
        BorderPane.setMargin(tblPuntos, new Insets(10, 0, 0, 0));
        setCenter(tblPuntos);

        // Barra Inferior
        Button btnDeshabilitar = new Button("Deshabilitar Sede Seleccionada");
        btnDeshabilitar.setOnAction(e -> deshabilitarPunto());
        setBottom(ComponentesFX.crearBarraBotones(Pos.CENTER_RIGHT, btnDeshabilitar));

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

            txtId.clear();
            txtNombre.clear();
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, ex.getMessage(), "Error al Registrar Sede");
        }
    }

    private void deshabilitarPunto() {
        Object[] fila = tblPuntos.getSelectionModel().getSelectedItem();
        if (fila == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione un punto de entrega en la tabla.", "Aviso");
            return;
        }
        String id = (String) fila[0];
        service.deshabilitarPunto(id);
        actualizarTabla();
    }

    private void actualizarTabla() {
        List<PuntoEntrega> lista = service.listarPuntosActivos();
        tblPuntos.getItems().clear();
        for (PuntoEntrega p : lista) {
            tblPuntos.getItems().add(new Object[]{
                p.getId(),
                p.getNombre(),
                p.getDireccion(),
                p.getCiudad(),
                p.isActivo() ? "ACTIVO" : "INACTIVO"
            });
        }
    }
}
