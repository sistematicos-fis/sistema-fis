# Módulo de Inventario y Bodega (CU-06 a CU-10)

- **Responsable:** Juan Pablo León
- **Área Funcional:** Control de Stock, Vehículos, Motocicletas y Bodegas

## Casos de Uso del Área y Clases Principales

| Caso de Uso | Nombre del Caso de Uso | Tipo | Clase Principal |
|---|---|---|---|
| **CU-06** | Registrar Vehículo o Motocicleta en Catálogo | CRUD | `persistencia.VehiculoDAO`<br>`logica.InventarioServicio` |
| **CU-07** | Gestionar Bodegas y Patios de Almacenamiento | CRUD | `persistencia.VehiculoDAO`<br>`modelo.Bodega` |
| **CU-08** | Trasladar Vehículos entre Bodegas | Transaccional | `logica.InventarioServicio` |
| **CU-09** | Consultar Disponibilidad y Filtro Multicriterio de Flota | Consulta Avanzada | `persistencia.VehiculoDAO`<br>`vista.InventarioVista` |
| **CU-10** | Controlar existencias en tiempo real para ventas | **No Trivial** | `logica.VerificacionInventarioServicio` |

## Estructura de Paquetes:
- `modelo/`: `Vehiculo`, `Bodega`, `CategoriaVehiculo`, `EstadoVehiculo`, `ConteoFisico`, `VerificacionInventario`, `ResultadoVerificacion`, `IncidenciaInventario`, `EstadoIncidencia`.
- `persistencia/`: `VehiculoDAO`, `BodegaDAO`, `VerificacionInventarioDAO` (JDBC con H2).
- `logica/`: `InventarioServicio`, `VerificacionInventarioServicio`.
- `vista/`: `InventarioVista`, `BodegaVista`, `VerificacionInventarioVista` (JavaFX).

## CU-10 — Controlar existencias en tiempo real para ventas

Antes de confirmar una venta, el caso de uso contrasta tres fuentes que pueden contradecirse:
el estado registrado del vehículo, los contratos de alquiler vigentes y el último conteo
físico de la bodega. Si concuerdan, bloquea la unidad para la venta en curso; si no,
deja constancia del motivo y, cuando el desacuerdo es entre el físico y el sistema,
abre una incidencia que solo el Administrador de Bodega puede cerrar.

### Reglas de negocio

| Regla | Descripción |
|---|---|
| RN-10-01 | El vehículo debe existir en el catálogo. |
| RN-10-02 | Sin conteo físico previo no se puede confirmar disponibilidad. |
| RN-10-03 | Un alquiler `RESERVADO` o `EN_CURSO` impide la venta (flujo alterno 3A). |
| RN-10-04 | Solo un vehículo `DISPONIBLE` puede pasar a venta. |
| RN-10-05 | Si el físico no coincide con el sistema se abre incidencia y la unidad pasa a `EN_VERIFICACION` (flujo alterno 4A). |
| RN-10-06 | Con una incidencia abierta no se confirma nada hasta resolverla. |
| RN-10-07 | La confirmación deja la unidad `RESERVADA` con un bloqueo vigente por 30 minutos. |
| RN-10-08 | Toda verificación queda registrada, sea cual sea su resultado. |
| RN-10-09 | El resultado, la incidencia y el cambio de estado se escriben en una sola transacción (RF-28). |

### Resultados posibles

`DISPONIBLE_CONFIRMADO`, `OPERACION_ACTIVA`, `ESTADO_NO_VENDIBLE`, `SIN_CONTEO_FISICO`,
`DISCREPANCIA_DETECTADA` e `INCIDENCIA_PENDIENTE`. Solo el primero habilita la venta.

### Tablas

`conteos_fisicos` (lo observado en bodega), `verificaciones_inventario` (traza de cada
verificación) e `incidencias_inventario` (discrepancias pendientes de resolver).

Las pruebas están en `test/co/edu/javeriana/concesionario/inventario/VerificacionInventarioServicioTest.java`.
