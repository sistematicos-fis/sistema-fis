package co.edu.javeriana.concesionario.legal.vista;

import co.edu.javeriana.concesionario.comun.simuladores.SimuladorAseguradora;
import co.edu.javeriana.concesionario.legal.logica.AseguradoraServicio;
import co.edu.javeriana.concesionario.legal.logica.MatriculaServicio;
import co.edu.javeriana.concesionario.legal.logica.PolizaSeguroServicio;
import co.edu.javeriana.concesionario.legal.logica.RevisionTecnicoMecanicaServicio;
import co.edu.javeriana.concesionario.legal.logica.SoatServicio;
import co.edu.javeriana.concesionario.legal.persistencia.AseguradoraDAO;
import co.edu.javeriana.concesionario.legal.persistencia.MatriculaDAO;
import co.edu.javeriana.concesionario.legal.persistencia.PolizaSeguroDAO;
import co.edu.javeriana.concesionario.legal.persistencia.RevisionTecnicoMecanicaDAO;
import co.edu.javeriana.concesionario.legal.persistencia.SoatDAO;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * Pantalla principal del módulo de Documentación Legal y Seguros: agrupa en pestañas los
 * casos de uso CU-21 a CU-25.
 * Responsable: Samuel Montaña
 */
public class LegalVista extends BorderPane {

    public LegalVista() {
        AseguradoraDAO aseguradoraDAO = new AseguradoraDAO();

        TabPane pestanas = new TabPane();
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        pestanas.getTabs().add(crearPestana("Matrículas (CU-21)",
                new MatriculaVista(new MatriculaServicio(new MatriculaDAO()))));
        pestanas.getTabs().add(crearPestana("Aseguradoras (CU-22)",
                new AseguradoraVista(new AseguradoraServicio(aseguradoraDAO))));
        pestanas.getTabs().add(crearPestana("Pólizas de seguro (CU-23)",
                new PolizaSeguroVista(new PolizaSeguroServicio(new PolizaSeguroDAO(), aseguradoraDAO))));
        pestanas.getTabs().add(crearPestana("SOAT (CU-24)",
                new SoatVista(new SoatServicio(new SoatDAO(), aseguradoraDAO, new SimuladorAseguradora()))));
        pestanas.getTabs().add(crearPestana("Revisión Técnico-Mecánica (CU-25)",
                new RevisionTecnicoMecanicaVista(new RevisionTecnicoMecanicaServicio(new RevisionTecnicoMecanicaDAO()))));
        setCenter(pestanas);
    }

    private Tab crearPestana(String titulo, Node contenido) {
        return new Tab(titulo, contenido);
    }
}
