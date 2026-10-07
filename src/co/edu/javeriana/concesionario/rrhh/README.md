# Módulo de Recursos Humanos (CU-26 a CU-30)

- **Responsable:** Sergio Alejandro Ramírez
- **Área Funcional:** Gestión de Personal, Nómina, Comisiones Comerciales y Turnos

## Casos de Uso del Área y Clases Principales

| Caso de Uso | Nombre del Caso de Uso | Tipo | Clase Principal |
|---|---|---|---|
| **CU-26** | Registrar Empleados y Cargos de Sucursal | CRUD | `persistencia.EmpleadoDAO`<br>`logica.RRHHServicio` |
| **CU-27** | Asignar Horarios y Turnos de Atención | Operativo | `logica.RRHHServicio` |
| **CU-28** | Liquidar Comisiones de Venta y Financiación por Asesor | **No Trivial** | `logica.RRHHServicio` |
| **CU-29** | Gestionar Novedades e Incapacidades | Registro | `persistencia.EmpleadoDAO` |
| **CU-30** | Evaluar Desempeño y Metas Comerciales | Reporte | `vista.RRHHVista` |

## Estructura de Paquetes:
- `modelo/`: `Empleado`.
- `persistencia/`: `EmpleadoDAO` (JDBC H2).
- `logica/`: `RRHHServicio`.
- `vista/`: `RRHHVista` (Java Swing).
