# Módulo de Clientes y PQRS (CU-01 a CU-05)

- **Responsable:** Andrés Alquerque
- **Área funcional:** clientes, categoría comercial, PQRS, encuestas e índice de satisfacción

## Casos de uso y clases principales

| Caso de uso | Nombre | Tipo | Clases principales | Estado |
|---|---|---|---|---|
| CU-01 | Registrar y gestionar clientes | CRUD | `vista.ClienteVista`, `logica.ClienteServicio`, `persistencia.ClienteDAO` | Implementado |
| CU-02 | Clasificar clientes por categoría comercial | Regla de negocio | `logica.ClienteServicio`, `persistencia.ClienteDAO`, `modelo.CategoriaComercial` | Implementado |
| CU-03 | Gestionar PQRS | CRUD | `vista.PqrsVista`, `logica.PqrsServicio`, `persistencia.PqrsDAO`, `modelo.EstadoPqrs` | Implementado |
| CU-04 | Gestionar encuestas de satisfacción | CRUD | `vista.EncuestaVista`, `logica.EncuestaServicio`, `persistencia.EncuestaDAO` | Implementado |
| CU-05 | Calcular índice combinado de satisfacción | No trivial | `vista.IndiceSatisfaccionVista`, `logica.IndiceSatisfaccionServicio`, `logica.CalculadoraIndiceSatisfaccion`, `persistencia.IndiceSatisfaccionDAO` | Implementado |

## Pantallas

La pestaña **Clientes y PQRS** de `VentanaPrincipal` es un `ClientesModuloVista` con tres sub-pestañas. `VentanaPrincipal` crea los DAO y servicios del área y se los pasa a cada pantalla por el constructor.

- **Clientes** (`ClienteVista`): registrar, editar y eliminar clientes; asignar la categoría comercial a mano o volver a la automática; botón para abrir la ventana del índice de satisfacción (`IndiceSatisfaccionVista`).
- **PQRS** (`PqrsVista`): radicar, editar, iniciar gestión, cerrar con respuesta y eliminar PQRS.
- **Encuestas** (`EncuestaVista`): cuestionario de tres preguntas con opciones de 1 a 5 y comentarios; registrar, editar y eliminar encuestas. Cada cambio recalcula el índice del cliente y muestra el valor anterior y el nuevo.

## Reglas por caso de uso

### CU-01: clientes

- Tipo de documento: CC, NIT, CE o PASAPORTE. CC y CE solo números (5 a 15 dígitos); NIT números con dígito de verificación opcional (`900123456-7`); pasaporte letras y números (5 a 20).
- Nombres y apellidos obligatorios (máximo 80). Correo y teléfono opcionales, con formato válido (teléfono de 7 a 10 dígitos). Ingresos obligatorios y no negativos.
- No puede haber dos clientes con el mismo documento.
- No se elimina un cliente con registros asociados (PQRS, encuestas, alertas, contratos de alquiler, pólizas o actas de traspaso); el mensaje indica cuáles tiene.

### CU-02: categoría comercial

- Automática según ingresos mensuales: ESTANDAR (< 5 M), PREFERENCIAL (5 M a < 15 M), CORPORATIVO (15 M a < 30 M) y VIP (≥ 30 M).
- El analista puede asignarla a mano; queda marcada en la columna `categoria_manual` y se respeta aunque se editen los ingresos.
- «Usar categoría automática» quita la asignación manual y recalcula la categoría con los ingresos.

### CU-03: PQRS

- Radicar: cliente existente, tipo (PETICION, QUEJA, RECLAMO o SUGERENCIA) y descripción obligatoria (máximo 500). Queda Abierta.
- Estados: Abierta → En proceso → Cerrada. Para cerrar se exige la respuesta al cliente; se guarda la fecha de resolución.
- Una PQRS cerrada no se edita ni se elimina, porque cuenta en el índice del CU-05.

### CU-04: encuestas

- Cliente existente, tres puntuaciones de 1 a 5 (atención, vehículo o producto y tiempos de entrega) y comentarios opcionales (máximo 300).
- Al editar se cambian las puntuaciones y los comentarios, no el cliente ni la fecha.

### CU-05: índice combinado de satisfacción

- Encuesta: nota = 0,40·atención + 0,35·producto + 0,25·tiempos (de 1 a 5), llevada a 0-100 con (nota − 1) × 25.
- PQRS resuelta: 50 % puntaje base del tipo + 50 % oportunidad frente a su SLA (PETICION 15 días/100, QUEJA 10/60, RECLAMO 10/40, SUGERENCIA 15/100).
- Índice = 60 % encuestas + 40 % PQRS. Si falta una de las dos se usa solo la otra (flujos 2A y 3A).
- Satisfecho ≥ 70, Neutral 50 a 69,99, En riesgo < 50. En riesgo genera una alerta para el asesor (flujo 5A), sin duplicar alertas pendientes.
- Índices y alertas se guardan en una sola transacción (RF-28) en `IndiceSatisfaccionDAO.guardarResultados`.

## Paquetes

- `modelo/`: `Cliente`, `Pqrs`, `EncuestaSatisfaccion`, `AlertaSatisfaccion`, `ResultadoIndiceSatisfaccion` y los enum `CategoriaComercial`, `CategoriaSatisfaccion`, `BaseCalculoIndice`, `TipoPqrs`, `EstadoPqrs`.
- `persistencia/`: `ClienteDAO`, `PqrsDAO`, `EncuestaDAO`, `IndiceSatisfaccionDAO` (JDBC sobre H2).
- `logica/`: `ClienteServicio`, `PqrsServicio`, `EncuestaServicio`, `IndiceSatisfaccionServicio`, `CalculadoraIndiceSatisfaccion`.
- `vista/`: `ClientesModuloVista`, `ClienteVista`, `PqrsVista`, `EncuestaVista`, `IndiceSatisfaccionVista` (JavaFX).

## Base de datos (`db/01_clientes.sql`)

Tablas `clientes`, `pqrs`, `encuestas_satisfaccion` y `alertas_satisfaccion`. Datos semilla: 3 clientes, 3 encuestas y 4 PQRS cerradas, que cubren los flujos básico, 2A, 3A y 5A del CU-05.

## Pruebas

90 pruebas JUnit en `test/co/edu/javeriana/concesionario/clientes/`:

| Caso de uso | Unitarias | Integración (H2 en memoria) |
|---|---|---|
| CU-01 | `ClienteServicioTest` (13) | `ClienteIntegracionTest` (4) |
| CU-02 | `CategoriaComercialTest` (8) | `CategoriaComercialIntegracionTest` (3) |
| CU-03 | `PqrsServicioTest` (17) | `PqrsIntegracionTest` (3) |
| CU-04 | `EncuestaServicioTest` (10) | `EncuestaIntegracionTest` (3) |
| CU-05 | `CalculadoraIndiceSatisfaccionTest` (19), `IndiceSatisfaccionServicioTest` (6) | `IndiceSatisfaccionIntegracionTest` (4) |

Se ejecutan con `./mvnw clean test`.
