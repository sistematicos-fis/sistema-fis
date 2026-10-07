# Módulo de Financiación (CU-11 a CU-15)

- **Responsable:** Jerónimo Martin
- **Área Funcional:** Financiación de Vehículos y Motocicletas
- **Paradigma:** Programación Orientada a Objetos en 4 capas (Modelo, Persistencia, Lógica, Vista).

## Casos de Uso del Área y Clases Principales

| Caso de Uso | Nombre del Caso de Uso | Tipo | Clase Principal (Lógica / Persistencia) |
|---|---|---|---|
| **CU-11** | Registrar y Gestionar Entidades Financieras Aliadas | CRUD | `persistencia.EntidadFinancieraDAO`<br>`logica.FinanciacionServicio` |
| **CU-12** | Configurar Parámetros y Tipos de Crédito | CRUD | `persistencia.TipoCreditoDAO`<br>`logica.FinanciacionServicio` |
| **CU-13** | Simular Plan de Financiación y Tabla de Amortización | **No Trivial** | `logica.CalculadoraAmortizacion`<br>`modelo.TablaAmortizacion` |
| **CU-14** | Radicar y Evaluar Solicitud de Crédito | CRUD Extendido | `persistencia.SolicitudCreditoDAO`<br>`logica.FinanciacionServicio` |
| **CU-15** | Consultar Scoring Crediticio y Formalizar Aprobación Externa (Simulador) | Transaccional / Integración Simulada | `comun.simuladores.SimuladorEntidadFinanciera`<br>`persistencia.CreditoFormalizadoDAO` |

## Reglas de Negocio Implementadas:
1. **Regla del 30% de Endeudamiento**: La cuota mensual estimada no puede exceder el 30% de los ingresos mensuales netos certificados del cliente.
2. **Cálculo Financiero No Trivial (CU-13)**: Sistema de amortización Francés. Conversión de tasa nominal/EA a mes vencido, cálculo de cuota fija con redondeo matemático exacto a 2 decimales y ajuste de residual en la última cuota para garantizar saldo cero exacto.
3. **Simulador Externo Obligatorio (CU-15)**: Consulta de scoring y decisión de aprobación bancaria sin conexiones reales externas, mediante simulación determinista controlada.
4. **Persistencia H2 JDBC**: Todos los datos se almacenan en tablas relacionales con transacciones ACID atómicas.
