package co.edu.javeriana.concesionario.clientes.logica;

import co.edu.javeriana.concesionario.clientes.modelo.EstadoPqrs;
import co.edu.javeriana.concesionario.clientes.modelo.Pqrs;
import co.edu.javeriana.concesionario.clientes.modelo.TipoPqrs;
import co.edu.javeriana.concesionario.clientes.persistencia.ClienteDAO;
import co.edu.javeriana.concesionario.clientes.persistencia.PqrsDAO;
import co.edu.javeriana.concesionario.comun.util.ValidadorUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio del CU-03 Gestionar PQRS.
 * Responsable: Andrés Alquerque
 */
public class PqrsServicio {

    public static final int LONGITUD_MAX_DESCRIPCION = 500;
    public static final int LONGITUD_MAX_RESPUESTA = 500;

    private final PqrsDAO pqrsDAO;
    private final ClienteDAO clienteDAO;

    public PqrsServicio() {
        this(new PqrsDAO(), new ClienteDAO());
    }

    public PqrsServicio(PqrsDAO pqrsDAO, ClienteDAO clienteDAO) {
        this.pqrsDAO = pqrsDAO;
        this.clienteDAO = clienteDAO;
    }

    public Pqrs radicarPqrs(Long clienteId, String tipo, String descripcion) throws SQLException {
        if (clienteId == null) {
            throw new IllegalArgumentException("Debe seleccionar el cliente que radica la PQRS.");
        }
        if (clienteDAO.buscarPorId(clienteId).isEmpty()) {
            throw new IllegalArgumentException("No existe un cliente registrado con id " + clienteId + ".");
        }
        TipoPqrs tipoPqrs = validarTipo(tipo);
        String texto = validarTexto(descripcion, "Descripción", LONGITUD_MAX_DESCRIPCION);

        Pqrs pqrs = new Pqrs(null, clienteId, tipoPqrs.name(), texto, EstadoPqrs.ABIERTO.name());
        return pqrsDAO.radicar(pqrs);
    }

    public List<Pqrs> listarPqrs() throws SQLException {
        return pqrsDAO.listarTodas();
    }

    public List<Pqrs> listarPorCliente(Long clienteId) throws SQLException {
        if (clienteId == null) {
            throw new IllegalArgumentException("Debe seleccionar un cliente.");
        }
        return pqrsDAO.listarPorCliente(clienteId);
    }

    public Optional<Pqrs> buscarPqrs(Long id) throws SQLException {
        if (id == null) {
            return Optional.empty();
        }
        return pqrsDAO.buscarPorId(id);
    }

    public void editarPqrs(Long id, String tipo, String descripcion) throws SQLException {
        Pqrs pqrs = obtenerExistente(id);
        if (estadoDe(pqrs) == EstadoPqrs.CERRADO) {
            throw new IllegalArgumentException("La PQRS " + id + " ya está cerrada y no se puede editar.");
        }
        pqrs.setTipo(validarTipo(tipo).name());
        pqrs.setDescripcion(validarTexto(descripcion, "Descripción", LONGITUD_MAX_DESCRIPCION));
        pqrsDAO.actualizar(pqrs);
    }

    public void iniciarGestion(Long id) throws SQLException {
        Pqrs pqrs = obtenerExistente(id);
        if (estadoDe(pqrs) != EstadoPqrs.ABIERTO) {
            throw new IllegalArgumentException("Solo una PQRS abierta puede pasar a En proceso. La PQRS "
                    + id + " está " + estadoDe(pqrs).getEtiqueta().toLowerCase() + ".");
        }
        pqrsDAO.actualizarEstado(id, EstadoPqrs.EN_PROCESO.name(), null, pqrs.getRespuesta());
    }

    public Pqrs cerrarPqrs(Long id, String respuesta) throws SQLException {
        Pqrs pqrs = obtenerExistente(id);
        if (estadoDe(pqrs) == EstadoPqrs.CERRADO) {
            throw new IllegalArgumentException("La PQRS " + id + " ya está cerrada.");
        }
        String texto = validarTexto(respuesta, "Respuesta al cliente", LONGITUD_MAX_RESPUESTA);
        LocalDateTime ahora = LocalDateTime.now();

        pqrsDAO.actualizarEstado(id, EstadoPqrs.CERRADO.name(), ahora, texto);
        pqrs.setEstado(EstadoPqrs.CERRADO.name());
        pqrs.setFechaResolucion(ahora);
        pqrs.setRespuesta(texto);
        return pqrs;
    }

    public void eliminarPqrs(Long id) throws SQLException {
        Pqrs pqrs = obtenerExistente(id);
        if (estadoDe(pqrs) == EstadoPqrs.CERRADO) {
            throw new IllegalArgumentException("No se puede eliminar la PQRS " + id
                    + " porque ya está cerrada y forma parte del índice de satisfacción del cliente (CU-05).");
        }
        pqrsDAO.eliminar(id);
    }

    private Pqrs obtenerExistente(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("Debe seleccionar una PQRS.");
        }
        return pqrsDAO.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe una PQRS con id " + id + "."));
    }

    private static EstadoPqrs estadoDe(Pqrs pqrs) {
        return EstadoPqrs.desdeTexto(pqrs.getEstado());
    }

    private static TipoPqrs validarTipo(String tipo) {
        ValidadorUtil.validarRequerido(tipo, "Tipo");
        return TipoPqrs.desdeTexto(tipo);
    }

    private static String validarTexto(String texto, String campo, int longitudMaxima) {
        ValidadorUtil.validarRequerido(texto, campo);
        String limpio = texto.trim();
        if (limpio.length() > longitudMaxima) {
            throw new IllegalArgumentException("El campo '" + campo + "' no puede tener más de "
                    + longitudMaxima + " caracteres.");
        }
        return limpio;
    }
}
