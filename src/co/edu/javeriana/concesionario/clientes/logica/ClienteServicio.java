package co.edu.javeriana.concesionario.clientes.logica;

import co.edu.javeriana.concesionario.clientes.modelo.*;
import co.edu.javeriana.concesionario.clientes.persistencia.ClienteDAO;
import co.edu.javeriana.concesionario.clientes.persistencia.PqrsDAO;
import co.edu.javeriana.concesionario.comun.util.ValidadorUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de lógica de negocio para Clientes y PQRS (CU-01 a CU-05).
 * Responsable: Andrés Alquerque
 */
public class ClienteServicio {

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
        ValidadorUtil.validarRequerido(documento, "Documento");
        ValidadorUtil.validarRequerido(nombres, "Nombres");
        ValidadorUtil.validarRequerido(apellidos, "Apellidos");

        if (clienteDAO.buscarPorDocumento(documento).isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente con el documento: " + documento);
        }

        // Clasificación automática de categoría comercial según ingresos (CU-02)
        CategoriaComercial cat = clasificarCategoriaPorIngresos(ingresos);

        Cliente c = new Cliente(null, documento, tipoDoc, nombres, apellidos, email, telefono, ingresos, cat);
        return clienteDAO.guardar(c);
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
}
