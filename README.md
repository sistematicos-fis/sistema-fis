# Concesionario Integrado - Sistema para Venta y Alquiler de Vehículos y Motocicletas

**Pontificia Universidad Javeriana**  
**Facultad de Ingeniería - Departamento de Ingeniería de Sistemas**  
**Asignatura:** Fundamentos de Ingeniería de Software (FIS)  
**Profesor:** Dr. Jaime Pavlich-Mariscal  
**Equipo de Proyecto:** Grupo Sistemáticos  

---

## 1. Estructura Oficial del Proyecto

El proyecto está diseñado bajo una arquitectura modular desacoplada en cuatro capas (**Modelo, Persistencia, Lógica y Vista**), asegurando la separación de responsabilidades y facilitando el trabajo concurrente del equipo sin conflictos de fusión (*merge conflicts*).

```text
concesionario/
├── README.md               # Explica toda la estructura y guía de desarrollo
├── .gitignore              # Reglas de exclusión para Maven, IDEs y base de datos local H2
├── pom.xml                 # Configuración de compilación Maven, dependencias H2 y JUnit 5
├── mvnw / mvnw.cmd         # Maven Wrapper para compilación sin requerir Maven global
├── docs/                   # Documentación oficial (PMP, SRS, SDD en formato .docx / .odt)
│   ├── PMP/
│   ├── SRS/
│   └── SDD/
├── db/                     # Un script SQL por área funcional para inicialización de H2
│   ├── 00_init_general.sql # Tablas transversales: usuarios, roles y autenticación
│   ├── 01_clientes.sql     # Clientes, categorías comerciales, encuestas y PQRS
│   ├── 02_inventario.sql   # Catálogo de vehículos, motocicletas y bodegas
│   ├── 03_financiacion.sql # Entidades financieras aliadas (CU-11)
│   ├── 04_alquiler.sql     # Puntos de entrega, sedes, tarifas y contratos de renta
│   └── 07_legal.sql        # Trámites de traspaso, RUNT y pólizas de seguro
├── test/                   # Pruebas automatizadas JUnit 5 (espejo exacto del árbol src/)
│   └── co/edu/javeriana/concesionario/
│       ├── comun/          # Pruebas de conexión a H2
│       ├── clientes/       # Pruebas unitarias de clientes y CSAT
│       ├── inventario/     # Pruebas de stock y depreciación
│       ├── financiacion/   # Pruebas del CRUD de entidades financieras (CU-11)
│       ├── alquiler/       # Pruebas de gestión de sedes y puntos de entrega
│       └── legal/          # Pruebas de emisión con simulador de aseguradora
└── src/co/edu/javeriana/concesionario/
    ├── Main.java           # Punto de entrada que arranca la aplicación de escritorio Swing
    │
    ├── comun/              # Componentes compartidos por 2 o más áreas
    │   ├── bd/             # ConexionBD.java (Única conexión autorizada a H2 con JDBC)
    │   ├── simuladores/    # Simuladores internos obligatorios (Prohibidas conexiones reales)
    │   │   └── SimuladorAseguradora.java (CU-34)
    │   ├── vista/          # VentanaPrincipal.java (Menú principal y navegación Swing)
    │   └── util/           # Validaciones, formateadores de moneda/fecha y diálogos
    │
    ├── clientes/           # CU-01 a CU-05 · Clientes y PQRS ......... Andrés Alquerque
    ├── inventario/         # CU-06 a CU-10 · Inventario y Bodega ...... Juan Pablo León
    ├── financiacion/       # CU-11 a CU-15 · Financiación ............ Jerónimo Martin
    ├── alquiler/           # CU-16 a CU-20 · Alquiler ................ Samuel Borras
    └── legal/              # CU-21 a CU-25 · Documentación Legal ...... Samuel Montaña
```

---

## 2. Asignación de Integrantes y Casos de Uso (25 Casos de Uso - 5 Integrantes)

Cada integrante del equipo es dueño de **5 casos de uso**, incluyendo al menos **1 caso de uso no trivial (no CRUD)**, cumpliendo con la regla de grupos de 5 a 7 integrantes:

| Integrante | Área Funcional | Casos de Uso Asignados | Caso No Trivial (No CRUD) |
|---|---|---|---|
| **Andrés Alquerque** | Clientes y PQRS | **CU-01 a CU-05** | **CU-05**: Cálculo Ponderado del Índice de Satisfacción (CSAT / NPS) |
| **Juan Pablo León** | Inventario y Bodega | **CU-06 a CU-10** | **CU-10**: Valoración de Inventario y Depreciación Anual Comercial |
| **Jerónimo Martin** | Financiación | **CU-11 a CU-15** | **CU-15**: Simular, Evaluar y Formalizar Crédito (Amortización) |
| **Samuel Borras** | Alquiler | **CU-16 a CU-20** | **CU-19**: Liquidación de Alquiler con Penalidades por Retraso/Combustible |
| **Samuel Montaña** | Documentación Legal | **CU-21 a CU-25** | **CU-24 / CU-34**: Cotización y Emisión con Simulador Interno de Aseguradora |

---

## 3. Arquitectura Interna de Cada Área Funcional

Cada área funcional sigue estrictamente el siguiente patrón modular:

```text
<area>/
├── README.md               # Detalle de los 5 casos de uso y sus clases asociadas
├── modelo/                 # Entidades y objetos de valor (Datos)
├── persistencia/           # DAOs con JDBC puro sobre el motor relacional H2
├── logica/                 # Reglas de negocio y algoritmos de cálculo (No CRUD)
└── vista/                  # Formularios, diálogos y paneles en Java Swing
```

---

## 4. Cumplimiento de Restricciones Técnicas y del Curso

- **Tipo de Aplicación**: Estrictamente de escritorio Java. Prohibido desarrollo web o híbrido.
- **GUI**: Exclusivamente implementada con **Java Swing**. Prohibido HTML/CSS.
- **Base de Datos y Persistencia**: Motor relacional embebido **H2 Database** accedido exclusivamente mediante **JDBC** y sentencias SQL estándar. *Prohibido el uso de archivos planos de texto.*
- **Simulación de Servicios Externos**: Prohibidas las conexiones externas reales con bancos o aseguradoras. Se implementan simuladores internos en `comun.simuladores` (`SimuladorAseguradora`).
- **Seguridad**: Autenticación multiusuario obligatoria y control de acceso basado en roles (RBAC) en `00_init_general.sql`.
- **Control de Versiones**: Flujo con ramas `feature/CU-XX-descripcion` e integración obligatoria vía Pull Requests (PR) aprobados por pares. Push directo a `main` prohibido.
- **Documentación**: Todos los entregables (PMP, SRS, SDD) se redactan exclusivamente en formatos **MS Word (`.docx`)** o **LibreOffice (`.odt`)**. Diagramas UML diseñados con herramientas visuales (draw.io, Enterprise Architect).

---

## 5. Instrucciones de Compilación y Ejecución

### Requisitos:
- **JDK 17 o 21 LTS**
- **Apache Maven 3.8+** (o usar el wrapper `./mvnw`)

### Ejecutar Pruebas Automatizadas:
```bash
./mvnw clean test
```

### Ejecutar la Aplicación:
```bash
./mvnw clean compile exec:java
```
O directamente ejecutando la clase principal `co.edu.javeriana.concesionario.Main` desde IntelliJ IDEA o Eclipse.
