# Módulo de Documentación Legal (CU-21 a CU-25)

- **Responsable:** Samuel Montaña
- **Área Funcional:** Trámites de Traspaso, RUNT, Matrículas y Pólizas de Seguro (SOAT / Todo Riesgo)

## Casos de Uso del Área y Clases Principales

| Caso de Uso | Nombre del Caso de Uso | Tipo | Clase Principal |
|---|---|---|---|
| **CU-21** | Gestionar matrículas de vehículos | CRUD | `persistencia.LegalDAO`<br>`logica.LegalServicio` |
| **CU-22** | Gestionar aseguradoras | CRUD | `logica.LegalServicio` |
| **CU-23** | Gestionar pólizas de seguro | CRUD | `modelo.Aseguradora`<br>`persistencia.LegalDAO` |
| **CU-24** | Validar y renovar SOAT del vehículo | NO CRUD | `comun.simuladores.SimuladorAseguradora`<br>`persistencia.LegalDAO` |
| **CU-25** | Verificar vigencia de revisión técnico-mecánica antes de alquiler o venta | NO CRUD | `vista.LegalVista` |

## Estructura de Paquetes:
- `modelo/`: `PolizaSeguro`, `Aseguradora`.
- `persistencia/`: `LegalDAO` (JDBC H2).
- `logica/`: `LegalServicio`.
- `vista/`: `LegalVista` (JavaFX).
