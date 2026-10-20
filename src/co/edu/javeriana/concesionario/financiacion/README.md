# Financiación

Responsable: Jerónimo Martin

Casos de uso del área: CU-11 a CU-15. Por ahora está el CU-11.

## CU-11 Gestionar entidades financieras aliadas

Permite crear, consultar, editar y eliminar los bancos con los que trabaja el concesionario.

Clases:
- modelo/EntidadFinanciera
- persistencia/EntidadFinancieraDAO
- logica/FinanciacionServicio
- logica/ReglaNegocioException
- vista/FinanciacionVista

Validaciones:
- Código, nombre y NIT son obligatorios
- El NIT va con el formato 890.903.938-8
- No se puede repetir el código ni el NIT
- La tasa E.A. tiene que estar entre 0 y 100 %
- El scoring mínimo va de 300 a 900
- No se deja eliminar una entidad si otro módulo la está usando

Pruebas: test/.../financiacion/FinanciacionServicioTest.java
