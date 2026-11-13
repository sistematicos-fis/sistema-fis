package co.edu.javeriana.concesionario.clientes.logica;

import co.edu.javeriana.concesionario.clientes.modelo.EncuestaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.persistencia.ClienteDAO;
import co.edu.javeriana.concesionario.clientes.persistencia.EncuestaDAO;
import co.edu.javeriana.concesionario.comun.util.ValidadorUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Lógica de negocio del CU-04 Gestionar encuestas de satisfacción.
 * Responsable: Andrés Alquerque
 */
public class EncuestaServicio {

    public static final int PUNTUACION_MINIMA = 1;
    public static final int PUNTUACION_MAXIMA = 5;
    public static final int LONGITUD_MAX_COMENTARIOS = 300;

    private final EncuestaDAO encuestaDAO;
    private final ClienteDAO clienteDAO;

    public EncuestaServicio() {
        this(new EncuestaDAO(), new ClienteDAO());
    }

    public EncuestaServicio(EncuestaDAO encuestaDAO, ClienteDAO clienteDAO) {
        this.encuestaDAO = encuestaDAO;
        this.clienteDAO = clienteDAO;
    }

    public EncuestaSatisfaccion registrarEncuesta(Long clienteId, int atencion, int producto, int tiempos,
                                                  String comentarios) throws SQLException {
        if (clienteId == null) {
            throw new IllegalArgumentException("Debe seleccionar el cliente que responde la encuesta.");
        }
        if (clienteDAO.buscarPorId(clienteId).isEmpty()) {
            throw new IllegalArgumentException("No existe el cliente con id " + clienteId + ".");
        }
        validarPuntuaciones(atencion, producto, tiempos);

        EncuestaSatisfaccion e = new EncuestaSatisfaccion(null, clienteId, atencion, producto, tiempos,
                normalizarComentarios(comentarios));
        return encuestaDAO.registrar(e);
    }

    public List<EncuestaSatisfaccion> listarEncuestas() throws SQLException {
        return encuestaDAO.listarTodas();
    }

    public List<EncuestaSatisfaccion> listarPorCliente(Long clienteId) throws SQLException {
        return encuestaDAO.listarPorCliente(clienteId);
    }

    public Optional<EncuestaSatisfaccion> buscarEncuesta(Long id) throws SQLException {
        return encuestaDAO.buscarPorId(id);
    }

    public EncuestaSatisfaccion editarEncuesta(Long id, int atencion, int producto, int tiempos,
                                               String comentarios) throws SQLException {
        EncuestaSatisfaccion e = buscarExistente(id);
        validarPuntuaciones(atencion, producto, tiempos);

        e.setPuntuacionAtencion(atencion);
        e.setPuntuacionProducto(producto);
        e.setPuntuacionTiempos(tiempos);
        e.setComentarios(normalizarComentarios(comentarios));
        encuestaDAO.actualizar(e);
        return e;
    }

    public void eliminarEncuesta(Long id) throws SQLException {
        buscarExistente(id);
        encuestaDAO.eliminar(id);
    }

    private EncuestaSatisfaccion buscarExistente(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("Debe seleccionar una encuesta.");
        }
        return encuestaDAO.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la encuesta con id " + id + "."));
    }

    private static void validarPuntuaciones(int atencion, int producto, int tiempos) {
        validarPuntuacion(atencion, "Atención");
        validarPuntuacion(producto, "Producto");
        validarPuntuacion(tiempos, "Tiempos de entrega");
    }

    private static void validarPuntuacion(int valor, String pregunta) {
        if (valor < PUNTUACION_MINIMA || valor > PUNTUACION_MAXIMA) {
            throw new IllegalArgumentException("La puntuación de '" + pregunta + "' debe estar entre "
                    + PUNTUACION_MINIMA + " y " + PUNTUACION_MAXIMA + ".");
        }
    }

    private static String normalizarComentarios(String comentarios) {
        if (!ValidadorUtil.esTextoValido(comentarios)) {
            return null;
        }
        String texto = comentarios.trim();
        if (texto.length() > LONGITUD_MAX_COMENTARIOS) {
            throw new IllegalArgumentException("Los comentarios admiten máximo " + LONGITUD_MAX_COMENTARIOS
                    + " caracteres.");
        }
        return texto;
    }
}
