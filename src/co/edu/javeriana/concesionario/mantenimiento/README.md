# Módulo de Mantenimiento y Flota (CU-21 a CU-25)

- **Responsable:** Jaime Andrés Molina
- **Área Funcional:** Taller de Servicio, Mantenimiento Preventivo/Correctivo y Alistamiento de Flota

## Casos de Uso del Área y Clases Principales

| Caso de Uso | Nombre del Caso de Uso | Tipo | Clase Principal |
|---|---|---|---|
| **CU-21** | Registrar Ingreso a Taller y Orden de Trabajo | CRUD | `persistencia.MantenimientoDAO`<br>`logica.MantenimientoServicio` |
| **CU-22** | Asignar Bahías de Servicio y Mecánicos | Operativo | `modelo.Taller` |
| **CU-23** | Cargar Repuestos e Insumos a Orden | Transaccional | `persistencia.MantenimientoDAO` |
| **CU-24** | Calcular Costo Total de Reparación y Diagnóstico | **No Trivial** | `logica.MantenimientoServicio` |
| **CU-25** | Registrar Control de Calidad y Cierre de Orden | Auditoría / Entrega | `vista.MantenimientoVista` |

## Estructura de Paquetes:
- `modelo/`: `OrdenMantenimiento`, `Taller`.
- `persistencia/`: `MantenimientoDAO` (JDBC H2).
- `logica/`: `MantenimientoServicio`.
- `vista/`: `MantenimientoVista` (Java Swing).
