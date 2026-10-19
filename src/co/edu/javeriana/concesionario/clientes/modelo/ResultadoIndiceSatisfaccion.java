package co.edu.javeriana.concesionario.clientes.modelo;

import java.math.BigDecimal;

public class ResultadoIndiceSatisfaccion {

    private final Long clienteId;
    private final String documentoCliente;
    private final String nombreCliente;
    private final Long asesorId;
    private final int cantidadEncuestas;
    private final int cantidadPqrs;
    private final BigDecimal componenteEncuestas;
    private final BigDecimal componentePqrs;
    private final BigDecimal indice;
    private final BaseCalculoIndice baseCalculo;
    private final CategoriaSatisfaccion categoria;

    public ResultadoIndiceSatisfaccion(Long clienteId, String documentoCliente, String nombreCliente,
                                       Long asesorId, int cantidadEncuestas, int cantidadPqrs,
                                       BigDecimal componenteEncuestas, BigDecimal componentePqrs,
                                       BigDecimal indice, BaseCalculoIndice baseCalculo,
                                       CategoriaSatisfaccion categoria) {
        this.clienteId = clienteId;
        this.documentoCliente = documentoCliente;
        this.nombreCliente = nombreCliente;
        this.asesorId = asesorId;
        this.cantidadEncuestas = cantidadEncuestas;
        this.cantidadPqrs = cantidadPqrs;
        this.componenteEncuestas = componenteEncuestas;
        this.componentePqrs = componentePqrs;
        this.indice = indice;
        this.baseCalculo = baseCalculo;
        this.categoria = categoria;
    }

    public Long getClienteId() { return clienteId; }
    public String getDocumentoCliente() { return documentoCliente; }
    public String getNombreCliente() { return nombreCliente; }
    public Long getAsesorId() { return asesorId; }
    public int getCantidadEncuestas() { return cantidadEncuestas; }
    public int getCantidadPqrs() { return cantidadPqrs; }
    public BigDecimal getComponenteEncuestas() { return componenteEncuestas; }
    public BigDecimal getComponentePqrs() { return componentePqrs; }
    public BigDecimal getIndice() { return indice; }
    public BaseCalculoIndice getBaseCalculo() { return baseCalculo; }
    public CategoriaSatisfaccion getCategoria() { return categoria; }

    public boolean requiereAlerta() {
        return categoria == CategoriaSatisfaccion.EN_RIESGO;
    }

    @Override
    public String toString() {
        return nombreCliente + " -> " + indice + " (" + categoria + ")";
    }
}
