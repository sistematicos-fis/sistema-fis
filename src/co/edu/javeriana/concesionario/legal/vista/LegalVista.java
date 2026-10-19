package co.edu.javeriana.concesionario.legal.vista;

import co.edu.javeriana.concesionario.comun.util.FormatoUtil;
import co.edu.javeriana.concesionario.comun.util.MensajeUtil;
import co.edu.javeriana.concesionario.comun.vista.ComponentesFX;
import co.edu.javeriana.concesionario.legal.logica.LegalServicio;
import co.edu.javeriana.concesionario.legal.modelo.PolizaSeguro;

import java.math.BigDecimal;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * Pantalla JavaFX para la gestión legal y emisión de pólizas con aseguradora simulada (CU-31 a CU-35).
 * Responsable: Samuel Montaña
 */
public class LegalVista extends BorderPane {

    private final LegalServicio servicio;
    private final TextField txtVin;
    private final TextField txtValorComercial;
    private final ComboBox<String> cmbTipoPoliza;
    private final TableView<Object[]> tblPolizas;

    public LegalVista() {
        this.servicio = new LegalServicio();
        setPadding(new Insets(15));

        // Formulario
        txtVin = new TextField("93HBL4820P1029381");
        txtValorComercial = new TextField("115000000");
        cmbTipoPoliza = new ComboBox<>();
        cmbTipoPoliza.getItems().addAll("TODO_RIESGO_RESPONSABILIDAD", "SOAT", "EXTRACONTRACTUAL");
        cmbTipoPoliza.getSelectionModel().selectFirst();

        Button btnEmitir = new Button("Emitir Póliza (Simulador)");
        btnEmitir.setStyle("-fx-background-color: #228b22; -fx-text-fill: white; -fx-font-weight: bold;");
        btnEmitir.setOnAction(e -> emitirPoliza());

        setTop(ComponentesFX.crearSeccion("Emitir Póliza con Aseguradora Simulada (CU-34)", ComponentesFX.crearFormulario(4,
                new Label("VIN Vehículo:"), txtVin,
                new Label("Valor Comercial ($):"), txtValorComercial,
                new Label("Tipo de Póliza:"), cmbTipoPoliza,
                btnEmitir)));

        // Tabla
        tblPolizas = ComponentesFX.crearTabla("ID", "N° Póliza", "Tipo", "Prima Anual", "Cobertura Total", "Estado", "Cód. Autorización");
        BorderPane.setMargin(tblPolizas, new Insets(10, 0, 0, 0));
        setCenter(tblPolizas);

        // Barra Sur
        Button btnRefrescar = new Button("Actualizar Pólizas");
        btnRefrescar.setOnAction(e -> cargarPolizas());
        setBottom(ComponentesFX.crearBarraBotones(Pos.CENTER_RIGHT, btnRefrescar));

        cargarPolizas();
    }

    private void emitirPoliza() {
        try {
            String vin = txtVin.getText().trim();
            BigDecimal valor = new BigDecimal(txtValorComercial.getText().trim());
            String tipo = cmbTipoPoliza.getValue();

            PolizaSeguro poliza = servicio.emitirPolizaConSimulador(1L, 1L, 1L, vin, valor, tipo);
            MensajeUtil.mostrarInfo(this, "¡Póliza emitida exitosamente!\nN°: " + poliza.getNumeroPoliza() +
                    "\nCód. Autorización Aseguradora: " + poliza.getCodigoAprobacionSimulada() +
                    "\nValor Prima Anual: " + FormatoUtil.formatoMoneda(poliza.getValorPrima()) +
                    "\nCobertura Total: " + FormatoUtil.formatoMoneda(poliza.getValorCobertura()), "Emisión Exitosa");

            cargarPolizas();
        } catch (Exception ex) {
            MensajeUtil.mostrarError(this, "Error al emitir: " + ex.getMessage(), "Error");
        }
    }

    private void cargarPolizas() {
        try {
            List<PolizaSeguro> lista = servicio.listarPolizas();
            tblPolizas.getItems().clear();
            for (PolizaSeguro p : lista) {
                tblPolizas.getItems().add(new Object[]{
                    p.getId(),
                    p.getNumeroPoliza(),
                    p.getTipoPoliza(),
                    FormatoUtil.formatoMoneda(p.getValorPrima()),
                    FormatoUtil.formatoMoneda(p.getValorCobertura()),
                    p.getEstado(),
                    p.getCodigoAprobacionSimulada()
                });
            }
        } catch (Exception ignored) {
        }
    }
}
