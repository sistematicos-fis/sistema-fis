package co.edu.javeriana.concesionario.financiacion.logica;

import co.edu.javeriana.concesionario.comun.simuladores.SimuladorEntidadFinanciera;
import co.edu.javeriana.concesionario.financiacion.modelo.*;
import co.edu.javeriana.concesionario.financiacion.persistencia.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Servicio principal que implementa la orquestación de la lógica de negocio de Financiación.
 * Controla CU-11, CU-12, CU-13, CU-14 y CU-15.
 * 
 * Responsable: Jerónimo Martin
 */
public class FinanciacionServicio {

    private final EntidadFinancieraDAO entidadDAO;
    private final TipoCreditoDAO tipoCreditoDAO;
    private final SolicitudCreditoDAO solicitudDAO;
    private final CreditoFormalizadoDAO formalizadoDAO;
    private final PagoCreditoDAO pagoDAO;

    public FinanciacionServicio() {
        this.entidadDAO = new EntidadFinancieraDAO();
        this.tipoCreditoDAO = new TipoCreditoDAO();
        this.solicitudDAO = new SolicitudCreditoDAO();
        this.formalizadoDAO = new CreditoFormalizadoDAO();
        this.pagoDAO = new PagoCreditoDAO();
    }

    public FinanciacionServicio(EntidadFinancieraDAO entidadDAO, TipoCreditoDAO tipoCreditoDAO,
                                SolicitudCreditoDAO solicitudDAO, CreditoFormalizadoDAO formalizadoDAO,
                                PagoCreditoDAO pagoDAO) {
        this.entidadDAO = entidadDAO;
        this.tipoCreditoDAO = tipoCreditoDAO;
        this.solicitudDAO = solicitudDAO;
        this.formalizadoDAO = formalizadoDAO;
        this.pagoDAO = pagoDAO;
    }

    // --- CU-11: Entidades Financieras ---
    public List<EntidadFinanciera> listarEntidadesActivas() throws SQLException {
        return entidadDAO.listarActivas();
    }

    public EntidadFinanciera registrarEntidad(EntidadFinanciera entidad) throws SQLException {
        if (entidad.getCodigo() == null || entidad.getCodigo().trim().isEmpty()) {
            throw new ReglaNegocioException("El código de la entidad financiera es obligatorio.");
        }
        if (entidad.getNombre() == null || entidad.getNombre().trim().isEmpty()) {
            throw new ReglaNegocioException("El nombre de la entidad financiera es obligatorio.");
        }
        return entidadDAO.guardar(entidad);
    }

    // --- CU-12: Tipos de Crédito ---
    public List<TipoCredito> listarTiposCreditoActivos() throws SQLException {
        return tipoCreditoDAO.listarActivos();
    }

    public TipoCredito registrarTipoCredito(TipoCredito tipo) throws SQLException {
        if (tipo.getPlazoMinimoMeses() <= 0 || tipo.getPlazoMaximoMeses() < tipo.getPlazoMinimoMeses()) {
            throw new ReglaNegocioException("El rango de plazos especificado es inválido.");
        }
        return tipoCreditoDAO.guardar(tipo);
    }

    // --- CU-13: Simulación (No Trivial) ---
    public TablaAmortizacion simularCredito(BigDecimal monto, BigDecimal tasaEa, int plazoMeses) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El monto a financiar debe ser mayor a cero.");
        }
        if (plazoMeses <= 0) {
            throw new ReglaNegocioException("El plazo en meses debe ser mayor a cero.");
        }
        return CalculadoraAmortizacion.generarTablaAmortizacion(monto, tasaEa, plazoMeses, LocalDate.now().plusMonths(1));
    }

    // --- CU-14: Radicación y Evaluación de Solicitud de Crédito ---
    public SolicitudCredito radicarSolicitud(
            Long clienteId, Long vehiculoId, Long tipoCreditoId,
            BigDecimal montoSolicitado, int plazoMeses, BigDecimal cuotaInicial,
            BigDecimal ingresosCertificados, BigDecimal gastosDeclarados,
            String documentoCliente) throws SQLException {

        if (montoSolicitado == null || montoSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El monto solicitado debe ser mayor a cero.");
        }
        if (ingresosCertificados == null || ingresosCertificados.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("Los ingresos certificados deben ser mayores a cero.");
        }

        TipoCredito tc = tipoCreditoDAO.buscarPorId(tipoCreditoId)
                .orElseThrow(() -> new ReglaNegocioException("Tipo de crédito no encontrado."));

        if (plazoMeses < tc.getPlazoMinimoMeses() || plazoMeses > tc.getPlazoMaximoMeses()) {
            throw new ReglaNegocioException("El plazo solicitado (" + plazoMeses + " meses) está fuera del rango permitido [" 
                    + tc.getPlazoMinimoMeses() + " - " + tc.getPlazoMaximoMeses() + "].");
        }

        // Calcular cuota estimada y ratio de endeudamiento
        BigDecimal tasaMensual = CalculadoraAmortizacion.convertirEaAMensualVencida(tc.getTasaInteresEa());
        BigDecimal cuotaEstimada = CalculadoraAmortizacion.calcularCuotaFija(montoSolicitado, tasaMensual, plazoMeses);
        BigDecimal ratioEndeudamiento = cuotaEstimada.divide(ingresosCertificados, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);

        // REGLA DEL 30%: Si la cuota supera el 30% del ingreso, se rechaza directamente
        boolean excedeCapacidad = ratioEndeudamiento.compareTo(new BigDecimal("30.00")) > 0;

        SolicitudCredito s = new SolicitudCredito();
        s.setNumeroRadicado("RAD-" + System.currentTimeMillis() % 100000000);
        s.setClienteId(clienteId);
        s.setVehiculoId(vehiculoId);
        s.setTipoCreditoId(tipoCreditoId);
        s.setMontoSolicitado(montoSolicitado);
        s.setPlazoMeses(plazoMeses);
        s.setCuotaInicial(cuotaInicial != null ? cuotaInicial : BigDecimal.ZERO);
        s.setIngresosCertificados(ingresosCertificados);
        s.setGastosDeclarados(gastosDeclarados != null ? gastosDeclarados : BigDecimal.ZERO);
        s.setCuotaEstimada(cuotaEstimada);
        s.setPorcentajeEndeudamiento(ratioEndeudamiento);

        if (excedeCapacidad) {
            s.setEstado("RECHAZADA");
            s.setObservaciones("Rechazada: Capacidad de endeudamiento excedida (" + ratioEndeudamiento + "% > 30.00%).");
        } else {
            s.setEstado("EN_EVALUACION");
            s.setObservaciones("Radicación exitosa. Lista para simulación de scoring bancario.");
        }

        return solicitudDAO.guardar(s);
    }

    // --- CU-15: Evaluación de Scoring y Formalización Simulada ---
    public CreditoFormalizado formalizarSolicitudConBanco(Long solicitudId, String documentoCliente) throws SQLException {
        SolicitudCredito sol = solicitudDAO.buscarPorId(solicitudId)
                .orElseThrow(() -> new ReglaNegocioException("Solicitud no encontrada con ID: " + solicitudId));

        if ("RECHAZADA".equals(sol.getEstado())) {
            throw new ReglaNegocioException("No se puede formalizar una solicitud que se encuentra rechazada.");
        }
        if ("FORMALIZADA".equals(sol.getEstado())) {
            throw new ReglaNegocioException("La solicitud ya fue formalizada previamente.");
        }

        TipoCredito tc = tipoCreditoDAO.buscarPorId(sol.getTipoCreditoId())
                .orElseThrow(() -> new ReglaNegocioException("Tipo de crédito asociado no encontrado."));

        EntidadFinanciera entidad = entidadDAO.buscarPorId(tc.getEntidadFinancieraId())
                .orElseThrow(() -> new ReglaNegocioException("Entidad bancaria asociada no encontrada."));

        // Invocar el simulador bancario interno (Restricción de simuladores para servicios externos)
        SimuladorEntidadFinanciera.RespuestaSimulacionBanco respuestaBanco = SimuladorEntidadFinanciera.evaluarCreditoExterno(
                documentoCliente,
                sol.getIngresosCertificados(),
                sol.getCuotaEstimada(),
                entidad.getScoringMinimo(),
                tc.getTasaInteresEa()
        );

        if (!respuestaBanco.isAprobado()) {
            solicitudDAO.actualizarEstado(sol.getId(), "RECHAZADA", respuestaBanco.getScoreCalculado(), respuestaBanco.getObservacion());
            throw new ReglaNegocioException("Entidad bancaria simulada denegó el crédito: " + respuestaBanco.getObservacion());
        }

        // Si fue aprobada por el simulador, se formaliza atómicamente y se genera la tabla de cuotas
        TablaAmortizacion tabla = CalculadoraAmortizacion.generarTablaAmortizacion(
                sol.getMontoSolicitado(),
                tc.getTasaInteresEa(),
                sol.getPlazoMeses(),
                LocalDate.now().plusMonths(1)
        );

        BigDecimal tasaMensual = CalculadoraAmortizacion.convertirEaAMensualVencida(tc.getTasaInteresEa());

        CreditoFormalizado cf = new CreditoFormalizado();
        cf.setSolicitudId(sol.getId());
        cf.setCodigoAprobacionBanco(respuestaBanco.getCodigoAprobacion());
        cf.setEntidadId(entidad.getId());
        cf.setMontoAprobado(sol.getMontoSolicitado());
        cf.setTasaMensualAplicada(tasaMensual);
        cf.setPlazoMeses(sol.getPlazoMeses());
        cf.setSaldoCapitalPendiente(sol.getMontoSolicitado());
        cf.setEstado("ACTIVO");

        return formalizadoDAO.formalizarCreditoConCuotas(cf, tabla);
    }

    // --- Pagos de Cuotas ---
    public PagoCredito registrarPagoCuota(Long creditoId, int numeroCuota, BigDecimal monto, String metodo) throws SQLException {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El monto a pagar debe ser positivo.");
        }

        PagoCredito pago = new PagoCredito();
        pago.setCreditoFormalizadoId(creditoId);
        pago.setNumeroCuota(numeroCuota);
        pago.setMontoPagado(monto);
        pago.setNumeroRecibo("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        pago.setMetodoPago(metodo != null ? metodo : "TRANSFERENCIA_SIMULADA");

        return pagoDAO.registrarPago(pago);
    }

    public List<SolicitudCredito> listarSolicitudes() throws SQLException {
        return solicitudDAO.listarTodas();
    }

    public List<CuotaAmortizacion> consultarCuotasDeCredito(Long creditoId) throws SQLException {
        return formalizadoDAO.listarCuotasPorCredito(creditoId);
    }
}
