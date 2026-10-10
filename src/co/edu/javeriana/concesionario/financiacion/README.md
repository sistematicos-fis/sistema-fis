# Módulo de Financiación (CU-11 a CU-15)

- **Responsable:** Jerónimo Martin
- **Área Funcional:** Financiación de Vehículos y Motocicletas
- **Paradigma:** Programación Orientada a Objetos en 4 capas (Modelo, Persistencia, Lógica, Vista).

## Casos de Uso del Área (numeración según SRS v2.1)

| Caso de Uso | Nombre del Caso de Uso | Tipo | Estado |
|---|---|---|---|
| **CU-11** | Gestionar entidades financieras aliadas | CRUD | **Implementado** |
| CU-12 | Gestionar parámetros y tipos de crédito | CRUD | Pendiente |
| CU-13 | Gestionar solicitudes de crédito | CRUD | Pendiente |
| CU-14 | Registrar pagos y abonos de crédito | CRUD | Pendiente |
| CU-15 | Simular, evaluar y formalizar crédito (amortización) | No CRUD (no trivial) | Pendiente |

## CU-11: Clases por capa

| Capa | Clase | Responsabilidad |
|---|---|---|
| Modelo | `modelo.EntidadFinanciera` | Datos del banco aliado: código, nombre, NIT, tasa base E.A., scoring mínimo y estado. |
| Persistencia | `persistencia.EntidadFinancieraDAO` | Crear, consultar, actualizar y eliminar en la tabla `entidades_financieras` de H2 vía JDBC. |
| Lógica | `logica.FinanciacionServicio` | Validaciones y reglas de negocio del CU-11. |
| Lógica | `logica.ReglaNegocioException` | Error que se muestra al usuario cuando se viola una regla. |
| Vista | `vista.FinanciacionVista` | Formulario y tabla en Java Swing. |

## Reglas de Negocio del CU-11 (RNF-04 / RNF-06)
1. **Campos obligatorios:** código, nombre y NIT.
2. **Formato de NIT:** dígitos con guion y dígito de verificación (ej: `890.903.938-8`).
3. **Código y NIT únicos:** no puede haber dos entidades con el mismo código o NIT.
4. **Tasa base E.A.:** mayor a 0 % y menor a 100 %. Se escribe en porcentaje (16.5) y se guarda como fracción (0.165).
5. **Scoring mínimo:** entre 300 y 900.
6. **Eliminación:** si la entidad tiene registros asociados en otros módulos, el sistema rechaza la eliminación y sugiere marcarla como inactiva.
