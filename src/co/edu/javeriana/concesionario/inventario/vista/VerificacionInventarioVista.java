package co.edu.javeriana.concesionario.inventario.vista;

import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;
import co.edu.javeriana.concesionario.inventario.logica.VerificacionInventarioServicio;
import co.edu.javeriana.concesionario.inventario.modelo.ConteoFisico;
import co.edu.javeriana.concesionario.inventario.modelo.EstadoVehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.IncidenciaInventario;
import co.edu.javeriana.concesionario.inventario.modelo.Vehiculo;
import co.edu.javeriana.concesionario.inventario.modelo.VerificacionInventario;
import co.edu.javeriana.concesionario.inventario.persistencia.VehiculoDAO;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Pantalla JavaFX del CU-10 (controlar existencias en tiempo real para ventas).
 *
 * Tiene dos mitades: arriba el asesor comercial pide la verificación de una unidad,
 * abajo el Administrador de Bodega registra conteos físicos y cierra las incidencias
 * que la verificación haya abierto. No ejecuta SQL ni aplica reglas: todo pasa por
 * VerificacionInventarioServicio.
 *
 * Responsable: Juan Pablo León
 */
public class VerificacionInventarioVista extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(VerificacionInventarioVista.class.getName());
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VerificacionInventarioServicio servicio;
    private final VehiculoDAO vehiculoDAO;

    private final ComboBox<Vehiculo> cmbVehiculo = new ComboBox<>();
    private final TextField txtSolicitante = new TextField("asesor");
    private final Label lblVeredicto = new Label("Seleccione un vehículo y solicite la verificación.");
    private final Label lblUltimoConteo = new Label("-");

    private final ComboBox<EstadoVehiculo> cmbEstadoFisico = new ComboBox<>();
    private final CheckBox chkPresente = new CheckBox("La unidad está físicamente en la bodega");
    private final TextField txtResponsable = new TextField("bodega");
    private final TextField txtObservaciones = new TextField();

    private final TableView<Object[]> tblIncidencias;
    private final ComboBox<EstadoVehiculo> cmbEstadoConfirmado = new ComboBox<>();
    private final TextField txtResolutor = new TextField("bodega");
    private final TextField txtObservacionResolucion = new TextField();

    /** Incidencia elegida en la tabla; null cuando no hay ninguna seleccionada. */
    private Long idIncidenciaSeleccionada;

    public VerificacionInventarioVista() {
        this(new VerificacionInventarioServicio(), new VehiculoDAO());
    }

    public VerificacionInventarioVista(VerificacionInventarioServicio servicio, VehiculoDAO vehiculoDAO) {
        this.servicio = servicio;
        this.vehiculoDAO = vehiculoDAO;
        setPadding(new Insets(15));

        cmbEstadoFisico.getItems().addAll(EstadoVehiculo.values());
        cmbEstadoFisico.getSelectionModel().select(EstadoVehiculo.DISPONIBLE);
        cmbEstadoConfirmado.getItems().addAll(EstadoVehiculo.values());
        cmbEstadoConfirmado.getSelectionModel().select(EstadoVehiculo.DISPONIBLE);
        chkPresente.setSelected(true);

        lblVeredicto.setWrapText(true);
        lblUltimoConteo.setWrapText(true);

        cmbVehiculo.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> mostrarUltimoConteo());

        tblIncidencias = ComponentesFX.crearTabla("ID", "Vehículo", "Sistema", "Físico",
                "Descripción", "Detectada por", "Fecha");
        tblIncidencias.getSelectionModel().selectedIndexProperty()
                .addListener((obs, anterior, actual) -> cargarIncidenciaSeleccionada());

        setTop(construirPanelVerificacion());
        setCenter(construirPanelIncidencias());

        cargarVehiculos();
        cargarIncidencias();
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------

    private VBox construirPanelVerificacion() {
        Button btnVerificar = new Button("Verificar disponibilidad");
        btnVerificar.setOnAction(e -> verificar());
        Button btnRefrescar = new Button("Refrescar flota");
        btnRefrescar.setOnAction(e -> cargarVehiculos());

        VBox panel = new VBox(8,
                ComponentesFX.crearSeccion("Verificación de existencias para venta (CU-10)",
                        new VBox(8,
                                ComponentesFX.crearFormulario(4,
                                        new Label("Vehículo:"), cmbVehiculo,
                                        new Label("Solicitado por:"), txtSolicitante),
                                new Label("Último conteo físico:"), lblUltimoConteo,
                                new Label("Resultado:"), lblVeredicto,
                                ComponentesFX.crearBarraBotones(Pos.CENTER_LEFT, btnVerificar, btnRefrescar))));
        panel.setPadding(new Insets(0, 0, 10, 0));
        return panel;
    }

    private SplitPane construirPanelIncidencias() {
        Button btnRegistrarConteo = new Button("Registrar conteo físico");
        btnRegistrarConteo.setOnAction(e -> registrarConteo());

        BorderPane panelConteo = new BorderPane();
        panelConteo.setCenter(ComponentesFX.crearSeccion("Conteo físico de bodega",
                new VBox(8,
                        ComponentesFX.crearFormulario(4,
                                new Label("Estado físico:"), cmbEstadoFisico,
                                new Label("Responsable:"), txtResponsable,
                                new Label("Observaciones:"), txtObservaciones),
                        chkPresente,
                        ComponentesFX.crearBarraBotones(Pos.CENTER_LEFT, btnRegistrarConteo))));

        Button btnResolver = new Button("Resolver incidencia");
        btnResolver.setOnAction(e -> resolverIncidencia());
        Button btnRefrescarIncidencias = new Button("Refrescar");
        btnRefrescarIncidencias.setOnAction(e -> cargarIncidencias());

        BorderPane panelIncidencias = new BorderPane();
        panelIncidencias.setTop(new Label("Incidencias abiertas"));
        panelIncidencias.setCenter(tblIncidencias);
        panelIncidencias.setBottom(new VBox(8,
                ComponentesFX.crearFormulario(4,
                        new Label("Estado real confirmado:"), cmbEstadoConfirmado,
                        new Label("Resuelta por:"), txtResolutor,
                        new Label("Observación:"), txtObservacionResolucion),
                ComponentesFX.crearBarraBotones(Pos.CENTER_LEFT, btnResolver, btnRefrescarIncidencias)));
        BorderPane.setMargin(tblIncidencias, new Insets(5, 0, 5, 0));

        SplitPane split = new SplitPane(panelConteo, panelIncidencias);
        split.setDividerPositions(0.4);
        return split;
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    private void verificar() {
        Vehiculo vehiculo = cmbVehiculo.getValue();
        if (vehiculo == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione el vehículo a verificar.", "CU-10");
            return;
        }
        try {
            VerificacionInventario resultado = servicio.verificarDisponibilidadParaVenta(
                    vehiculo.getId(), txtSolicitante.getText());

            String texto = resultado.getResultado().getDescripcion() + ". " + resultado.getMotivo();
            if (resultado.getBloqueoExpira() != null) {
                texto += " Bloqueo vigente hasta las "
                        + resultado.getBloqueoExpira().format(FORMATO_HORA) + ".";
            }
            lblVeredicto.setText(texto);

            if (resultado.confirmoDisponibilidad()) {
                MensajeUtil.mostrarInfo(this, texto, "Disponibilidad confirmada");
            } else {
                MensajeUtil.mostrarAdvertencia(this, texto, "Venta no habilitada");
            }

            cargarVehiculos();
            cargarIncidencias();
            mostrarUltimoConteo();
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "No se pudo verificar: " + ex.getMessage(), "Error");
        }
    }

    private void registrarConteo() {
        Vehiculo vehiculo = cmbVehiculo.getValue();
        if (vehiculo == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione el vehículo contado.", "CU-10");
            return;
        }
        try {
            servicio.registrarConteoFisico(vehiculo.getId(), cmbEstadoFisico.getValue(),
                    chkPresente.isSelected(), txtResponsable.getText(), txtObservaciones.getText());
            txtObservaciones.clear();
            MensajeUtil.mostrarInfo(this, "Conteo físico registrado.", "CU-10");
            mostrarUltimoConteo();
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "No se pudo registrar el conteo: " + ex.getMessage(), "Error");
        }
    }

    private void resolverIncidencia() {
        if (idIncidenciaSeleccionada == null) {
            MensajeUtil.mostrarAdvertencia(this, "Seleccione la incidencia a resolver.", "CU-10");
            return;
        }
        try {
            servicio.resolverIncidencia(idIncidenciaSeleccionada, cmbEstadoConfirmado.getValue(),
                    txtResolutor.getText(), txtObservacionResolucion.getText());
            idIncidenciaSeleccionada = null;
            txtObservacionResolucion.clear();
            MensajeUtil.mostrarInfo(this, "Incidencia resuelta; el vehículo quedó en el estado confirmado.",
                    "CU-10");
            cargarIncidencias();
            cargarVehiculos();
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "No se pudo resolver: " + ex.getMessage(), "Error");
        }
    }

    // ------------------------------------------------------------------
    // Carga de datos
    // ------------------------------------------------------------------

    private void cargarVehiculos() {
        try {
            Vehiculo seleccionado = cmbVehiculo.getValue();
            List<Vehiculo> flota = vehiculoDAO.listarTodos();
            cmbVehiculo.getItems().setAll(flota);
            if (seleccionado != null) {
                flota.stream()
                        .filter(v -> v.getId().equals(seleccionado.getId()))
                        .findFirst()
                        .ifPresent(cmbVehiculo.getSelectionModel()::select);
            } else if (!flota.isEmpty()) {
                cmbVehiculo.getSelectionModel().selectFirst();
            }
        } catch (Exception ex) {
            // Se llama desde el constructor, antes de que exista la ventana: no se puede
            // abrir un diálogo modal aquí, así que el error se registra y se avisa en pantalla.
            LOGGER.log(Level.SEVERE, "No se pudo cargar la flota para la verificación", ex);
            lblVeredicto.setText("No se pudo cargar la flota: " + ex.getMessage());
        }
    }

    private void mostrarUltimoConteo() {
        Vehiculo vehiculo = cmbVehiculo.getValue();
        if (vehiculo == null) {
            lblUltimoConteo.setText("-");
            return;
        }
        try {
            Optional<ConteoFisico> conteo = servicio.ultimoConteo(vehiculo.getId());
            if (conteo.isEmpty()) {
                lblUltimoConteo.setText("Sin conteo físico registrado. Registre uno antes de verificar.");
                return;
            }
            ConteoFisico c = conteo.get();
            lblUltimoConteo.setText("Estado físico " + c.getEstadoFisico()
                    + (c.isPresente() ? "" : " (ausente en bodega)")
                    + " | registrado por " + c.getResponsable()
                    + (c.getFechaConteo() != null ? " el " + c.getFechaConteo().format(FORMATO_HORA) : "")
                    + " | estado en sistema: " + vehiculo.getEstado());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "No se pudo consultar el último conteo físico", ex);
            lblUltimoConteo.setText("No se pudo consultar el conteo: " + ex.getMessage());
        }
    }

    private void cargarIncidencias() {
        try {
            List<IncidenciaInventario> lista = servicio.listarIncidenciasAbiertas();
            tblIncidencias.getItems().clear();
            for (IncidenciaInventario i : lista) {
                tblIncidencias.getItems().add(new Object[]{
                    i.getId(),
                    i.getVehiculoId(),
                    i.getEstadoSistema(),
                    i.getEstadoFisico(),
                    i.getDescripcion(),
                    i.getDetectadaPor(),
                    i.getFechaDeteccion() != null ? i.getFechaDeteccion().format(FORMATO_HORA) : "-"
                });
            }
            idIncidenciaSeleccionada = null;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "No se pudieron cargar las incidencias de inventario", ex);
            tblIncidencias.setPlaceholder(new Label("No se pudieron cargar las incidencias: " + ex.getMessage()));
        }
    }

    private void cargarIncidenciaSeleccionada() {
        Object[] fila = tblIncidencias.getSelectionModel().getSelectedItem();
        idIncidenciaSeleccionada = fila != null ? (Long) fila[0] : null;
    }
}
