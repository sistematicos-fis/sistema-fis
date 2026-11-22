package co.edu.javeriana.concesionario.clientes.vista;

import co.edu.javeriana.concesionario.clientes.logica.CalculadoraIndiceSatisfaccion;
import co.edu.javeriana.concesionario.clientes.logica.ClienteServicio;
import co.edu.javeriana.concesionario.clientes.logica.EncuestaServicio;
import co.edu.javeriana.concesionario.clientes.logica.IndiceSatisfaccionServicio;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.modelo.EncuestaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.ResultadoIndiceSatisfaccion;
import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * Pantalla JavaFX del CU-04 Gestionar encuestas de satisfacción.
 * Al guardar o eliminar una encuesta recalcula el índice del cliente (CU-05).
 * Responsable: Andrés Alquerque
 */
public class EncuestaVista extends BorderPane {

    private final EncuestaServicio servicio;
    private final ClienteServicio clienteServicio;
    private final IndiceSatisfaccionServicio indiceServicio;
    private final ComboBox<Cliente> cmbCliente;
    private final ToggleGroup grupoAtencion = new ToggleGroup();
    private final ToggleGroup grupoProducto = new ToggleGroup();
    private final ToggleGroup grupoTiempos = new ToggleGroup();
    private final TextArea txtComentarios;
    private final Button btnGuardar;
    private final TableView<Object[]> tblEncuestas;
    private final Map<Long, String> nombresClientes = new HashMap<>();
    private Long idSeleccionado;

    public EncuestaVista(EncuestaServicio servicio, ClienteServicio clienteServicio,
                         IndiceSatisfaccionServicio indiceServicio) {
        this.servicio = servicio;
        this.clienteServicio = clienteServicio;
        this.indiceServicio = indiceServicio;
        setPadding(new Insets(15));

        cmbCliente = new ComboBox<>();
        cmbCliente.setPromptText("Seleccione el cliente que responde");
        txtComentarios = new TextArea();
        txtComentarios.setPromptText("Comentarios del cliente (opcional, máximo 300 caracteres)");
        txtComentarios.setPrefRowCount(2);
        txtComentarios.setWrapText(true);

        btnGuardar = new Button("Registrar encuesta");
        btnGuardar.setOnAction(e -> guardarEncuesta());
        Button btnNuevo = new Button("Nueva encuesta");
        btnNuevo.setOnAction(e -> limpiarFormulario());

        setTop(ComponentesFX.crearSeccion("Encuesta de satisfacción posventa", ComponentesFX.crearFormulario(2,
                new Label("Cliente:"), cmbCliente,
                new Label("1. ¿Cómo califica la atención recibida?"), crearOpciones(grupoAtencion),
                new Label("2. ¿Qué tan satisfecho está con el vehículo o producto?"),
                crearOpciones(grupoProducto),
                new Label("3. ¿Cómo califica los tiempos de entrega?"), crearOpciones(grupoTiempos),
                new Label("Comentarios:"), txtComentarios,
                btnGuardar, btnNuevo)));

        tblEncuestas = ComponentesFX.crearTabla("ID", "Cliente", "Atención", "Producto", "Tiempos",
                "Puntaje (0-100)", "Comentarios", "Fecha");
        tblEncuestas.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, fila) -> cargarSeleccion(fila));
        BorderPane.setMargin(tblEncuestas, new Insets(10, 0, 0, 0));
        setCenter(tblEncuestas);

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarEncuesta());
        Button btnRefrescar = new Button("Actualizar Lista");
        btnRefrescar.setOnAction(e -> recargar());

        setBottom(ComponentesFX.crearBarraBotones(Pos.CENTER_RIGHT, btnEliminar, btnRefrescar));

        recargar();
    }

    public void recargar() {
        cargarClientes();
        cargarEncuestas();
    }

    private static HBox crearOpciones(ToggleGroup grupo) {
        HBox opciones = new HBox(15);
        opciones.setAlignment(Pos.CENTER_LEFT);
        int minimo = EncuestaServicio.PUNTUACION_MINIMA;
        int maximo = EncuestaServicio.PUNTUACION_MAXIMA;
        for (int valor = minimo; valor <= maximo; valor++) {
            RadioButton opcion = new RadioButton(String.valueOf(valor));
            opcion.setUserData(valor);
            opcion.setToggleGroup(grupo);
            opciones.getChildren().add(opcion);
        }
        return opciones;
    }

    private void cargarClientes() {
        try {
            Cliente seleccionado = cmbCliente.getValue();
            List<Cliente> clientes = clienteServicio.listarClientes();
            nombresClientes.clear();
            for (Cliente c : clientes) {
                nombresClientes.put(c.getId(), c.getNombreCompleto());
            }
            cmbCliente.getItems().setAll(clientes);
            if (seleccionado != null) {
                seleccionarCliente(seleccionado.getId());
            }
        } catch (Exception e) {
            MensajeUtil.mostrarError(this, "No se pudieron cargar los clientes: " + e.getMessage(), "Error");
        }
    }

    private void cargarEncuestas() {
        try {
            tblEncuestas.getItems().clear();
            for (EncuestaSatisfaccion e : servicio.listarEncuestas()) {
                tblEncuestas.getItems().add(new Object[]{
                    e.getId(),
                    nombresClientes.getOrDefault(e.getClienteId(), "Cliente " + e.getClienteId()),
                    e.getPuntuacionAtencion(),
                    e.getPuntuacionProducto(),
                    e.getPuntuacionTiempos(),
                    CalculadoraIndiceSatisfaccion.calcularPuntajeEncuesta(e),
                    e.getComentarios() != null ? e.getComentarios() : "",
                    e.getFecha() != null ? FormatoUtil.formatoFecha(e.getFecha().toLocalDate()) : ""
                });
            }
        } catch (Exception e) {
            MensajeUtil.mostrarError(this, "No se pudieron cargar las encuestas: " + e.getMessage(), "Error");
        }
    }

    private void cargarSeleccion(Object[] fila) {
        if (fila == null) {
            return;
        }
        try {
            Optional<EncuestaSatisfaccion> encuesta = servicio.buscarEncuesta((Long) fila[0]);
            if (encuesta.isEmpty()) {
                return;
            }
            EncuestaSatisfaccion e = encuesta.get();
            idSeleccionado = e.getId();
            seleccionarCliente(e.getClienteId());
            cmbCliente.setDisable(true);
            marcar(grupoAtencion, e.getPuntuacionAtencion());
            marcar(grupoProducto, e.getPuntuacionProducto());
            marcar(grupoTiempos, e.getPuntuacionTiempos());
            txtComentarios.setText(e.getComentarios() != null ? e.getComentarios() : "");
            btnGuardar.setText("Guardar cambios");
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "No se pudo cargar la encuesta: " + ex.getMessage(), "Error");
        }
    }

    private void guardarEncuesta() {
        try {
            Cliente cliente = cmbCliente.getValue();
            if (cliente == null) {
                throw new IllegalArgumentException("Debe seleccionar el cliente que responde la encuesta.");
            }
            int atencion = respuesta(grupoAtencion, "1. Atención recibida");
            int producto = respuesta(grupoProducto, "2. Vehículo o producto");
            int tiempos = respuesta(grupoTiempos, "3. Tiempos de entrega");

            String antes = indiceActual(cliente.getId());
            String accion;
            if (idSeleccionado == null) {
                servicio.registrarEncuesta(cliente.getId(), atencion, producto, tiempos,
                        txtComentarios.getText());
                accion = "Encuesta registrada para " + cliente.getNombreCompleto() + ".";
            } else {
                servicio.editarEncuesta(idSeleccionado, atencion, producto, tiempos, txtComentarios.getText());
                accion = "Encuesta " + idSeleccionado + " actualizada.";
            }
            String despues = indiceActual(cliente.getId());

            limpiarFormulario();
            cargarEncuestas();
            MensajeUtil.mostrarInfo(this, accion + "\n\nÍndice de satisfacción del cliente:\n"
                    + antes + "  →  " + despues, "Encuesta guardada");
        } catch (IllegalArgumentException e) {
            MensajeUtil.mostrarAdvertencia(this, e.getMessage(), "Encuesta incompleta");
        } catch (Exception e) {
            MensajeUtil.mostrarError(this, "No se pudo guardar la encuesta: " + e.getMessage(), "Error");
        }
    }

    private void eliminarEncuesta() {
        Object[] fila = tblEncuestas.getSelectionModel().getSelectedItem();
        if (fila == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione una encuesta en la tabla.",
                    "Seleccione una encuesta");
            return;
        }
        Long id = (Long) fila[0];
        if (!MensajeUtil.confirmar(this, "¿Eliminar la encuesta " + id + " de " + fila[1] + "?",
                "Confirmar eliminación")) {
            return;
        }
        try {
            Long clienteId = servicio.buscarEncuesta(id)
                    .map(EncuestaSatisfaccion::getClienteId).orElse(null);
            String antes = indiceActual(clienteId);
            servicio.eliminarEncuesta(id);
            String despues = indiceActual(clienteId);

            limpiarFormulario();
            cargarEncuestas();
            MensajeUtil.mostrarInfo(this, "Encuesta " + id + " eliminada.\n\n"
                    + "Índice de satisfacción del cliente:\n" + antes + "  →  " + despues,
                    "Encuesta eliminada");
        } catch (IllegalArgumentException e) {
            MensajeUtil.mostrarAdvertencia(this, e.getMessage(), "No se puede eliminar la encuesta");
        } catch (Exception e) {
            MensajeUtil.mostrarError(this, "No se pudo eliminar la encuesta: " + e.getMessage(), "Error");
        }
    }

    private String indiceActual(Long clienteId) throws Exception {
        if (clienteId == null) {
            return "sin índice";
        }
        LocalDate hasta = LocalDate.now();
        LocalDate desde = hasta.minusDays(IndiceSatisfaccionServicio.DIAS_PERIODO_POR_DEFECTO);
        try {
            IndiceSatisfaccionServicio.ResumenCalculo resumen =
                    indiceServicio.calcularIndiceCliente(clienteId, desde, hasta);
            ResultadoIndiceSatisfaccion r = resumen.getResultados().get(0);
            String texto = r.getIndice() + " (" + r.getCategoria().getEtiqueta() + ")";
            if (resumen.getAlertasGeneradas() > 0) {
                texto += ". Se generó una alerta para contacto prioritario";
            }
            return texto;
        } catch (IllegalStateException e) {
            return "sin índice (no tiene encuestas ni PQRS cerradas en los últimos "
                    + IndiceSatisfaccionServicio.DIAS_PERIODO_POR_DEFECTO + " días)";
        }
    }

    private static int respuesta(ToggleGroup grupo, String pregunta) {
        Toggle elegida = grupo.getSelectedToggle();
        if (elegida == null) {
            throw new IllegalArgumentException("Responda la pregunta '" + pregunta + "' (de 1 a 5).");
        }
        return (Integer) elegida.getUserData();
    }

    private static void marcar(ToggleGroup grupo, int valor) {
        for (Toggle opcion : grupo.getToggles()) {
            if (Integer.valueOf(valor).equals(opcion.getUserData())) {
                grupo.selectToggle(opcion);
                return;
            }
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = null;
        cmbCliente.setDisable(false);
        cmbCliente.getSelectionModel().clearSelection();
        grupoAtencion.selectToggle(null);
        grupoProducto.selectToggle(null);
        grupoTiempos.selectToggle(null);
        txtComentarios.clear();
        btnGuardar.setText("Registrar encuesta");
        tblEncuestas.getSelectionModel().clearSelection();
    }

    private void seleccionarCliente(Long clienteId) {
        for (Cliente c : cmbCliente.getItems()) {
            if (c.getId().equals(clienteId)) {
                cmbCliente.setValue(c);
                return;
            }
        }
    }
}
