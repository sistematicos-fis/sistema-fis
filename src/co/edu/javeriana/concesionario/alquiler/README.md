# Módulo de Alquiler de Vehículos (CU-16 a CU-20)

- **Responsable:** Samuel Borras
- **Área Funcional:** Renta de Vehículos Corto y Largo Plazo, Gestión de Sedes y Flota en Renta

## Casos de Uso del Área y Clases Principales

| Caso de Uso | Nombre del Caso de Uso | Tipo | Clase Principal |
|---|---|---|---|
| **CU-16** | Gestionar Puntos de Entrega y Devolución | CRUD | `persistencia.PuntoEntregaRepository`<br>`logica.PuntoEntregaService` |
| **CU-17** | Configurar Tarifas de Alquiler por Categoría | CRUD | `logica.PuntoEntregaService` |
| **CU-18** | Reservar y Formalizar Contrato de Alquiler | Transaccional | `logica.PuntoEntregaService` |
| **CU-19** | Liquidar Alquiler con Cálculo de Penalidad por Retraso o Combustible | **No Trivial** | `logica.PuntoEntregaService` |
| **CU-20** | Registrar Devolución e Inspección Técnica del Vehículo | Transaccional | `vista.AlquilerVista` |

## Estructura de Paquetes:
- `modelo/`: `PuntoEntrega`.
- `persistencia/`: `PuntoEntregaRepository`, `InMemoryPuntoEntregaRepository`.
- `logica/`: `PuntoEntregaService`.
- `vista/`: `AlquilerVista` (JavaFX).
