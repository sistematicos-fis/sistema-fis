package co.edu.javeriana.concesionario.comun.bd;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestor centralizado de la conexión a la base de datos relacional H2 vía JDBC.
 * Única clase autorizada para suministrar conexiones a los DAOs de todas las áreas funcionales.
 */
public final class ConexionBD {

    private static final Logger LOGGER = Logger.getLogger(ConexionBD.class.getName());
    
    // URL por defecto para base de datos H2 embebida persistente
    private static final String DEFAULT_URL_FILE = "jdbc:h2:./data/concesionario_db;DB_CLOSE_DELAY=-1";
    private static final String DEFAULT_URL_MEM = "jdbc:h2:mem:concesionario_db;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static String urlActual = DEFAULT_URL_FILE;
    private static Connection conexionUnica = null;

    private ConexionBD() {
        // Prevenir instanciación
    }

    /**
     * Configura la URL de conexión (útil para pruebas unitarias con BD en memoria).
     */
    public static synchronized void configurarUrlMemoria() {
        urlActual = DEFAULT_URL_MEM;
        cerrarConexion();
    }

    public static synchronized void configurarUrlArchivo() {
        urlActual = DEFAULT_URL_FILE;
        cerrarConexion();
    }

    /**
     * Retorna una conexión activa a la base de datos H2.
     */
    public static synchronized Connection obtenerConexion() throws SQLException {
        if (conexionUnica == null || conexionUnica.isClosed()) {
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Driver H2 no encontrado en el classpath", e);
                throw new SQLException("Driver de H2 no encontrado", e);
            }
            conexionUnica = DriverManager.getConnection(urlActual, USER, PASSWORD);
        }
        return conexionUnica;
    }

    /**
     * Inicializa el esquema ejecutando todos los scripts SQL en db/.
     */
    public static synchronized void inicializarBaseDatos() {
        String[] scripts = {
            "db/00_init_general.sql",
            "db/01_clientes.sql",
            "db/02_inventario.sql",
            "db/03_financiacion.sql",
            "db/04_alquiler.sql",
            "db/05_mantenimiento.sql",
            "db/06_rrhh.sql",
            "db/07_legal.sql"
        };

        try (Connection conn = obtenerConexion()) {
            conn.setAutoCommit(false);
            for (String rutaScript : scripts) {
                ejecutarScript(conn, rutaScript);
            }
            conn.commit();
            conn.setAutoCommit(true);
            LOGGER.info("Base de datos H2 inicializada exitosamente con todos los módulos.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar la base de datos H2", e);
            throw new RuntimeException("Fallo en la inicialización de la base de datos H2: " + e.getMessage(), e);
        }
    }

    private static void ejecutarScript(Connection conn, String rutaScript) throws Exception {
        Path path = Paths.get(rutaScript);
        String contenido;
        if (Files.exists(path)) {
            contenido = Files.readString(path, StandardCharsets.UTF_8);
        } else {
            // Intentar cargarlo desde el classpath
            try (InputStream is = ConexionBD.class.getClassLoader().getResourceAsStream(rutaScript)) {
                if (is == null) {
                    LOGGER.warning("No se encontró el script SQL: " + rutaScript + ". Omitiendo.");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        sb.append(linea).append("\n");
                    }
                }
                contenido = sb.toString();
            }
        }

        // Filtrar líneas de comentarios (--) antes de separar por punto y coma
        StringBuilder sqlSinComentarios = new StringBuilder();
        for (String linea : contenido.split("\\r?\\n")) {
            String lineaTrim = linea.trim();
            if (!lineaTrim.startsWith("--")) {
                sqlSinComentarios.append(linea).append("\n");
            }
        }

        try (Statement stmt = conn.createStatement()) {
            String[] sentencias = sqlSinComentarios.toString().split(";");
            for (String sentencia : sentencias) {
                String sqlLimpio = sentencia.trim();
                if (!sqlLimpio.isEmpty()) {
                    stmt.execute(sqlLimpio);
                }
            }
        }
    }

    /**
     * Cierra de forma segura la conexión si está abierta.
     */
    public static synchronized void cerrarConexion() {
        if (conexionUnica != null) {
            try {
                if (!conexionUnica.isClosed()) {
                    conexionUnica.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error al cerrar conexión H2", e);
            } finally {
                conexionUnica = null;
            }
        }
    }
}
