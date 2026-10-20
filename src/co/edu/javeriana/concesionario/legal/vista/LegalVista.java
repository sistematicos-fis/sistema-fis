package co.edu.javeriana.concesionario.legal.vista;

import co.edu.javeriana.concesionario.legal.persistencia.AseguradoraDAO;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * Pantalla principal del módulo de Documentación Legal: agrupa en pestañas la gestión de
 * aseguradoras (CU-22), la verificación de la revisión técnico-mecánica (CU-25).
 * Responsable: Samuel Montaña
 */
public class LegalVista extends BorderPane {

    public LegalVista() {
        TabPane pestanas = new TabPane();
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        pestanas.getTabs().add(crearPestana("Aseguradoras (CU-22)",
                new AseguradoraVista(new AseguradoraServicio(new AseguradoraDAO()))));
        pestanas.getTabs().add(crearPestana("Revisión Técnico-Mecánica (CU-25)",
                new RevisionTecnicoMecanicaVista(new RevisionTecnicoMecanicaServicio(new RevisionTecnicoMecanicaDAO()))));
        pestanas.getTabs().add(crearPestana("Pólizas", new PolizasVista()));
        setCenter(pestanas);
    }

    private Tab crearPestana(String titulo, Node contenido) {
        return new Tab(titulo, contenido);
    }
}
