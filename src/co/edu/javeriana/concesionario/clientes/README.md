# Módulo de Clientes y PQRS (CU-01 a CU-05)

- **Responsable:** Andrés Alquerque
- **Área Funcional:** Gestión de Clientes, Categorización y Servicio Posventa

## Casos de Uso del Área y Clases Principales

| Caso de Uso | Nombre del Caso de Uso | Tipo | Clase Principal |
|---|---|---|---|
| **CU-01** | Registrar y Gestionar Clientes | CRUD | `persistencia.ClienteDAO`<br>`logica.ClienteServicio` |
| **CU-02** | Clasificar Clientes por Categoría Comercial | Regla Negocio | `modelo.CategoriaComercial`<br>`logica.ClienteServicio` |
| **CU-03** | Radicar Peticiones, Quejas, Reclamos y Sugerencias (PQRS) | CRUD | `persistencia.PqrsDAO`<br>`logica.ClienteServicio` |
| **CU-04** | Gestionar Encuestas de Satisfacción Posventa | Transaccional | `modelo.EncuestaSatisfaccion`<br>`persistencia.ClienteDAO` |
| **CU-05** | Calcular Índice de Satisfacción del Cliente (CSAT / NPS) | **No Trivial** | `logica.CalculadoraIndiceSatisfaccion`<br>`vista.IndiceSatisfaccionVista` |

## Estructura de Paquetes:
- `modelo/`: Entidades de dominio (`Cliente`, `Pqrs`, `EncuestaSatisfaccion`, `CategoriaSatisfaccion`, `CategoriaComercial`).
- `persistencia/`: Acceso a datos con JDBC y H2 (`ClienteDAO`, `PqrsDAO`).
- `logica/`: Reglas de negocio y cálculo (`CalculadoraIndiceSatisfaccion`, `ClienteServicio`).
- `vista/`: Formularios y tableros Swing (`ClienteVista`, `IndiceSatisfaccionVista`).
