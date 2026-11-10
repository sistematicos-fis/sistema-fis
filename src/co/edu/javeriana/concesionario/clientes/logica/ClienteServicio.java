package co.edu.javeriana.concesionario.clientes.logica;

import co.edu.javeriana.concesionario.clientes.modelo.*;
import co.edu.javeriana.concesionario.clientes.persistencia.ClienteDAO;
import co.edu.javeriana.concesionario.clientes.persistencia.PqrsDAO;
import co.edu.javeriana.concesionario.comun.util.ValidadorUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de lógica de negocio para Clientes y PQRS (CU-01 a CU-05).
 * Responsable: Andrés Alquerque
 */
public class ClienteServicio {

    public static final List<String> TIPOS_DOCUMENTO = List.of("CC", "NIT", "CE", "PASAPORTE");
    public static final int LONGITUD_MAX_DOCUMENTO = 20;
    public static final int LONGITUD_MAX_NOMBRE = 80;
    public static final int LONGITUD_MAX_EMAIL = 100;

    private static final String SQLSTATE_REGISTROS_ASOCIADOS = "23503";

    private final ClienteDAO clienteDAO;
    private final PqrsDAO pqrsDAO;

    public ClienteServicio() {
        this.clienteDAO = new ClienteDAO();
        this.pqrsDAO = new PqrsDAO();
    }

    public ClienteServicio(ClienteDAO clienteDAO, PqrsDAO pqrsDAO) {
        this.clienteDAO = clienteDAO;
        this.pqrsDAO = pqrsDAO;
    }

    public Cliente registrarCliente(String documento, String tipoDoc, String nombres, String apellidos,
                                    String email, String telefono, BigDecimal ingresos) throws SQLException {
        Cliente c = armarCliente(null, documento, tipoDoc, nombres, apellidos, email, telefono, ingresos);

        if (clienteDAO.buscarPorDocumento(c.getDocumento()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente con el documento: " + c.getDocumento());
        }

        return clienteDAO.guardar(c);
    }

    public Cliente editarCliente(Long id, String documento, String tipoDoc, String nombres, String apellidos,
                                 String email, String telefono, BigDecimal ingresos) throws SQLException {
        buscarExistente(id);
        Cliente c = armarCliente(id, documento, tipoDoc, nombres, apellidos, email, telefono, ingresos);

        Optional<Cliente> mismoDocumento = clienteDAO.buscarPorDocumento(c.getDocumento());
        if (mismoDocumento.isPresent() && !mismoDocumento.get().getId().equals(id)) {
            throw new IllegalArgumentException("Ya existe otro cliente con el documento: " + c.getDocumento());
        }

        clienteDAO.actualizar(c);
        return c;
    }

    public void eliminarCliente(Long id) throws SQLException {
        buscarExistente(id);

        List<String> asociados = new ArrayList<>();
        for (Map.Entry<String, Integer> registro : clienteDAO.contarRegistrosAsociados(id).entrySet()) {
            if (registro.getValue() > 0) {
                asociados.add(registro.getValue() + " " + registro.getKey());
            }
        }
        if (!asociados.isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar el cliente porque tiene registros asociados: "
                    + String.join(", ", asociados) + ".");
        }

        try {
            clienteDAO.eliminar(id);
        } catch (SQLException e) {
            if (SQLSTATE_REGISTROS_ASOCIADOS.equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                        "No se puede eliminar el cliente porque tiene registros asociados en otros módulos.");
            }
            throw e;
        }
    }

    public CategoriaComercial clasificarCategoriaPorIngresos(BigDecimal ingresos) {
        if (ingresos == null || ingresos.compareTo(new BigDecimal("5000000")) < 0) {
            return CategoriaComercial.ESTANDAR;
        } else if (ingresos.compareTo(new BigDecimal("15000000")) < 0) {
            return CategoriaComercial.PREFERENCIAL;
        } else if (ingresos.compareTo(new BigDecimal("30000000")) < 0) {
            return CategoriaComercial.CORPORATIVO;
        } else {
            return CategoriaComercial.VIP;
        }
    }

    public Optional<Cliente> buscarPorDocumento(String doc) throws SQLException {
        return clienteDAO.buscarPorDocumento(doc);
    }

    public Optional<Cliente> buscarPorId(Long id) throws SQLException {
        return clienteDAO.buscarPorId(id);
    }

    public List<Cliente> listarClientes() throws SQLException {
        return clienteDAO.listarTodos();
    }

    public Pqrs radicarPqrs(Long clienteId, String tipo, String descripcion) throws SQLException {
        ValidadorUtil.validarRequerido(descripcion, "Descripción");
        Pqrs p = new Pqrs(null, clienteId, tipo, descripcion, "ABIERTO");
        return pqrsDAO.radicar(p);
    }

    public List<Pqrs> listarPqrs() throws SQLException {
        return pqrsDAO.listarTodas();
    }

    private Cliente buscarExistente(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("Debe seleccionar un cliente.");
        }
        return clienteDAO.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el cliente con id " + id + "."));
    }

    private Cliente armarCliente(Long id, String documento, String tipoDoc, String nombres, String apellidos,
                                 String email, String telefono, BigDecimal ingresos) {
        String tipo = tipoDoc != null ? tipoDoc.trim().toUpperCase() : null;
        if (!TIPOS_DOCUMENTO.contains(tipo)) {
            throw new IllegalArgumentException("El tipo de documento debe ser CC, NIT, CE o PASAPORTE.");
        }

        ValidadorUtil.validarRequerido(documento, "Documento");
        String doc = documento.trim();
        validarDocumento(doc, tipo);

        ValidadorUtil.validarRequerido(nombres, "Nombres");
        ValidadorUtil.validarRequerido(apellidos, "Apellidos");
        validarLongitud(nombres.trim(), LONGITUD_MAX_NOMBRE, "Nombres");
        validarLongitud(apellidos.trim(), LONGITUD_MAX_NOMBRE, "Apellidos");

        String correo = textoOpcional(email);
        if (correo != null) {
            if (!ValidadorUtil.esEmailValido(correo)) {
                throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
            }
            validarLongitud(correo, LONGITUD_MAX_EMAIL, "Correo Electrónico");
        }

        String tel = textoOpcional(telefono);
        if (tel != null && !tel.matches("\\d{7,10}")) {
            throw new IllegalArgumentException("El teléfono debe tener solo números, entre 7 y 10 dígitos.");
        }

        if (!ValidadorUtil.esMontoNoNegativo(ingresos)) {
            throw new IllegalArgumentException("Los ingresos mensuales son obligatorios y no pueden ser negativos.");
        }

        return new Cliente(id, doc, tipo, nombres.trim(), apellidos.trim(), correo, tel, ingresos,
                clasificarCategoriaPorIngresos(ingresos));
    }

    private static void validarDocumento(String documento, String tipo) {
        validarLongitud(documento, LONGITUD_MAX_DOCUMENTO, "Documento");
        boolean valido;
        String mensaje;
        switch (tipo) {
            case "NIT":
                valido = documento.matches("\\d{6,15}(-\\d)?");
                mensaje = "El NIT debe tener solo números y, si se incluye, el dígito de verificación después de un guion.";
                break;
            case "PASAPORTE":
                valido = documento.matches("[A-Za-z0-9]{5,20}");
                mensaje = "El pasaporte debe tener entre 5 y 20 letras o números, sin espacios.";
                break;
            default:
                valido = documento.matches("\\d{5,15}");
                mensaje = "El documento " + tipo + " debe tener solo números, entre 5 y 15 dígitos.";
        }
        if (!valido) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private static void validarLongitud(String texto, int maximo, String campo) {
        if (texto.length() > maximo) {
            throw new IllegalArgumentException("El campo '" + campo + "' admite máximo " + maximo + " caracteres.");
        }
    }

    private static String textoOpcional(String texto) {
        return ValidadorUtil.esTextoValido(texto) ? texto.trim() : null;
    }
}
