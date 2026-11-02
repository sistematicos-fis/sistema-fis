# Módulo de Clientes y PQRS (CU-01 a CU-05)

- **Responsable:** Andrés Alquerque
- **Área funcional:** clientes, categoría comercial, PQRS, encuestas e índice de satisfacción

## Casos de uso y clases principales

| Caso de uso | Nombre | Tipo | Clases principales | Estado |
|---|---|---|---|---|
| CU-01 | Registrar y gestionar clientes | CRUD | `vista.ClienteVista`, `logica.ClienteServicio`, `persistencia.ClienteDAO` | Implementado |
| CU-02 | Clasificar clientes por categoría comercial | Regla de negocio | `logica.ClienteServicio.clasificarCategoriaPorIngresos`, `modelo.CategoriaComercial` | Implementado |
| CU-03 | Gestionar PQRS | CRUD | `logica.ClienteServicio.radicarPqrs`, `persistencia.PqrsDAO` | Servicio y DAO; pantalla en la próxima entrega |
| CU-04 | Gestionar encuestas de satisfacción | CRUD | `persistencia.ClienteDAO.guardarEncuesta`, `modelo.EncuestaSatisfaccion` | DAO; pantalla en la próxima entrega |
| CU-05 | Calcular índice combinado de satisfacción | No trivial | `vista.IndiceSatisfaccionVista`, `logica.IndiceSatisfaccionServicio`, `logica.CalculadoraIndiceSatisfaccion`, `persistencia.IndiceSatisfaccionDAO` | Implementado |

## CU-05: índice combinado de satisfacción

- Encuesta: nota = 0,40·atención + 0,35·producto + 0,25·tiempos (de 1 a 5), llevada a 0-100 con (nota − 1) × 25.
- PQRS resuelta: 50 % puntaje base del tipo + 50 % oportunidad frente a su SLA (PETICION 15 días/100, QUEJA 10/60, RECLAMO 10/40, SUGERENCIA 15/100).
- Índice = 60 % encuestas + 40 % PQRS. Si falta una de las dos se usa solo la otra (flujos 2A y 3A).
- Satisfecho ≥ 70, Neutral 50 a 69,99, En riesgo < 50. En riesgo genera una alerta para el asesor (flujo 5A), sin duplicar alertas pendientes.
- Índices y alertas se guardan en una sola transacción (RF-28) en `IndiceSatisfaccionDAO.guardarResultados`.

## Paquetes

- `modelo/`: `Cliente`, `Pqrs`, `EncuestaSatisfaccion`, `AlertaSatisfaccion`, `ResultadoIndiceSatisfaccion` y los enum `CategoriaComercial`, `CategoriaSatisfaccion`, `BaseCalculoIndice`, `TipoPqrs`.
- `persistencia/`: `ClienteDAO`, `PqrsDAO`, `IndiceSatisfaccionDAO` (JDBC sobre H2).
- `logica/`: `ClienteServicio`, `IndiceSatisfaccionServicio`, `CalculadoraIndiceSatisfaccion`.
- `vista/`: `ClienteVista`, `IndiceSatisfaccionVista` (JavaFX).

## Base de datos (`db/01_clientes.sql`)

Tablas `clientes`, `pqrs`, `encuestas_satisfaccion` y `alertas_satisfaccion`, con datos semilla que cubren los flujos básico, 2A, 3A y 5A del CU-05.

## Pruebas

29 pruebas JUnit en `test/co/edu/javeriana/concesionario/clientes/`: `CalculadoraIndiceSatisfaccionTest`, `IndiceSatisfaccionServicioTest` e `IndiceSatisfaccionIntegracionTest` (H2 en memoria).
