# Módulo de Documentación Legal (CU-31 a CU-35)

- **Responsable:** Samuel Montaña
- **Área Funcional:** Trámites de Traspaso, RUNT, Matrículas y Pólizas de Seguro (SOAT / Todo Riesgo)

## Casos de Uso del Área y Clases Principales

| Caso de Uso | Nombre del Caso de Uso | Tipo | Clase Principal |
|---|---|---|---|
| **CU-31** | Radicar Acta de Traspaso y Venta | CRUD | `persistencia.LegalDAO`<br>`logica.LegalServicio` |
| **CU-32** | Gestionar Registro Inicial de Matrícula (RUNT) | Trámite | `logica.LegalServicio` |
| **CU-33** | Configurar Convenios con Aseguradoras | CRUD | `modelo.Aseguradora`<br>`persistencia.LegalDAO` |
| **CU-34** | Cotizar y Emitir Póliza con Aseguradora (Simulador) | **No Trivial** / Integración Simulada | `comun.simuladores.SimuladorAseguradora`<br>`persistencia.LegalDAO` |
| **CU-35** | Auditar y Archivar Expediente Legal de Vehículo | Auditoría / Cierre | `vista.LegalVista` |

## Estructura de Paquetes:
- `modelo/`: `PolizaSeguro`, `Aseguradora`.
- `persistencia/`: `LegalDAO` (JDBC H2).
- `logica/`: `LegalServicio`.
- `vista/`: `LegalVista` (Java Swing).
