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
| **CU-10** | Valorizar Inventario y Reporte de Depreciación | **No Trivial** | `logica.InventarioServicio` |

## Estructura de Paquetes:
- `modelo/`: `Vehiculo`, `Bodega`, `CategoriaVehiculo`.
- `persistencia/`: `VehiculoDAO` (JDBC con H2).
- `logica/`: `InventarioServicio`.
- `vista/`: `InventarioVista` (JavaFX).
