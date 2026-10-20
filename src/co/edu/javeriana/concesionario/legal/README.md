# Módulo de Documentación Legal (CU-21 a CU-25)

- **Responsable:** Samuel Montaña
- **Área Funcional:** Trámites de Traspaso, RUNT, Matrículas y Pólizas de Seguro (SOAT / Todo Riesgo)

## Casos de Uso del Área y Clases Principales

| Caso de Uso | Nombre del Caso de Uso | Tipo | Clase Principal |
|---|---|---|---|
| **CU-21** | Radicar Acta de Traspaso y Venta | CRUD | `persistencia.LegalDAO`<br>`logica.LegalServicio` |
| **CU-22** | Gestionar Registro Inicial de Matrícula (RUNT) | Trámite | `logica.LegalServicio` |
| **CU-23** | Configurar Convenios con Aseguradoras | CRUD | `modelo.Aseguradora`<br>`persistencia.LegalDAO` |
| **CU-24** | Cotizar y Emitir Póliza con Aseguradora (Simulador) | **No Trivial** / Integración Simulada | `comun.simuladores.SimuladorAseguradora`<br>`persistencia.LegalDAO` |
| **CU-25** | Auditar y Archivar Expediente Legal de Vehículo | Auditoría / Cierre | `vista.LegalVista` |

## Estructura de Paquetes:
- `modelo/`: `PolizaSeguro`, `Aseguradora`.
- `persistencia/`: `LegalDAO` (JDBC H2).
- `logica/`: `LegalServicio`.
- `vista/`: `LegalVista` (JavaFX).
