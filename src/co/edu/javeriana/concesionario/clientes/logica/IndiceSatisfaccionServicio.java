package co.edu.javeriana.concesionario.clientes.logica;

import co.edu.javeriana.concesionario.clientes.modelo.AlertaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.Cliente;
import co.edu.javeriana.concesionario.clientes.modelo.EncuestaSatisfaccion;
import co.edu.javeriana.concesionario.clientes.modelo.Pqrs;
import co.edu.javeriana.concesionario.clientes.modelo.ResultadoIndiceSatisfaccion;
import co.edu.javeriana.concesionario.clientes.persistencia.IndiceSatisfaccionDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IndiceSatisfaccionServicio {

    public static final String MENSAJE_CLIENTE_EN_RIESGO =
            "El cliente presenta un índice de satisfacción bajo. Se recomienda contacto prioritario.";

    public static final int DIAS_PERIODO_POR_DEFECTO = 90;

    private final IndiceSatisfaccionDAO dao;

    public IndiceSatisfaccionServicio() {
        this(new IndiceSatisfaccionDAO());
    }

    public IndiceSatisfaccionServicio(IndiceSatisfaccionDAO dao) {
        this.dao = dao;
    }

    public ResumenCalculo calcularIndices(LocalDate desde, LocalDate hasta) throws SQLException {
        validarPeriodo(desde, hasta);

        List<Cliente> clientes = dao.listarClientesEvaluables(desde, hasta);
        if (clientes.isEmpty()) {
            return new ResumenCalculo(Collections.emptyList(), 0);
        }

        List<ResultadoIndiceSatisfaccion> resultados = new ArrayList<>();
        for (Cliente cliente : clientes) {
            resultados.add(calcularParaCliente(cliente, desde, hasta));
        }

        int alertas = dao.guardarResultados(resultados, MENSAJE_CLIENTE_EN_RIESGO, LocalDateTime.now());
        return new ResumenCalculo(resultados, alertas);
    }

    public ResumenCalculo calcularIndiceCliente(Long clienteId, LocalDate desde, LocalDate hasta) throws SQLException {
        validarPeriodo(desde, hasta);
        if (clienteId == null) {
            throw new IllegalArgumentException("Debe seleccionar un cliente para calcular su índice.");
        }
        Cliente cliente = dao.buscarClientePorId(clienteId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un cliente registrado con id " + clienteId + "."));

        ResultadoIndiceSatisfaccion resultado = calcularParaCliente(cliente, desde, hasta);
        List<ResultadoIndiceSatisfaccion> resultados = List.of(resultado);
        int alertas = dao.guardarResultados(resultados, MENSAJE_CLIENTE_EN_RIESGO, LocalDateTime.now());
        return new ResumenCalculo(resultados, alertas);
    }

    public List<AlertaSatisfaccion> listarAlertasPendientes() throws SQLException {
        return dao.listarAlertasPendientes();
    }

    public boolean marcarAlertaAtendida(Long alertaId) throws SQLException {
        if (alertaId == null) {
            throw new IllegalArgumentException("Debe seleccionar una alerta.");
        }
        return dao.marcarAlertaAtendida(alertaId);
    }

    private ResultadoIndiceSatisfaccion calcularParaCliente(Cliente cliente, LocalDate desde, LocalDate hasta)
            throws SQLException {
        List<EncuestaSatisfaccion> encuestas = dao.listarEncuestasPorCliente(cliente.getId(), desde, hasta);
        List<Pqrs> pqrs = dao.listarPqrsResueltasPorCliente(cliente.getId(), desde, hasta);
        return CalculadoraIndiceSatisfaccion.calcularIndiceCliente(cliente, encuestas, pqrs);
    }

    private void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe indicar la fecha inicial y la fecha final del período a evaluar.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial (" + desde + ") no puede ser posterior a la fecha final ("
                    + hasta + "). Corrija el período e intente de nuevo.");
        }
    }

    public static class ResumenCalculo {
        private final List<ResultadoIndiceSatisfaccion> resultados;
        private final int alertasGeneradas;

        public ResumenCalculo(List<ResultadoIndiceSatisfaccion> resultados, int alertasGeneradas) {
            this.resultados = resultados;
            this.alertasGeneradas = alertasGeneradas;
        }

        public List<ResultadoIndiceSatisfaccion> getResultados() { return resultados; }
        public int getAlertasGeneradas() { return alertasGeneradas; }

        public long contarEnRiesgo() {
            return resultados.stream().filter(ResultadoIndiceSatisfaccion::requiereAlerta).count();
        }

        public boolean estaVacio() {
            return resultados.isEmpty();
        }
    }
}
