# Concesionario Integrado

Sistema de escritorio para la venta y el alquiler de vehículos y motocicletas. Proyecto de Fundamentos de Ingeniería de Software, Pontificia Universidad Javeriana.

- Profesor: Jaime Andrés Pavlich Mariscal, Ph.D.
- Grupo Sistemáticos: Andrés Alquerque, Juan Pablo León, Jerónimo Martin, Samuel Borras y Samuel Montaña

Hecho en Java 21 con JavaFX para la interfaz, H2 como base de datos (por JDBC) y JUnit 5 para las pruebas.

## Cómo está organizado

El código está dividido en cinco áreas, una por integrante, y un paquete `comun` con lo que comparten todas. Cada área tiene las mismas cuatro capas:

- `modelo/`: las clases de datos.
- `persistencia/`: los DAO, que hacen las consultas SQL con JDBC.
- `logica/`: las reglas de negocio.
- `vista/`: las pantallas en JavaFX.

```text
sistema-fis/
├── pom.xml
├── mvnw, mvnw.cmd
├── db/                  scripts SQL que crean las tablas al arrancar
├── docs/
├── src/co/edu/javeriana/concesionario/
│   ├── Main.java
│   ├── comun/           conexión a H2, login y roles, utilidades, ventanas principales
│   ├── clientes/
│   ├── inventario/
│   ├── financiacion/
│   ├── alquiler/
│   └── legal/
└── test/                pruebas JUnit, con la misma estructura de paquetes que src/
```

Hay un script SQL por área en `db/`:

| Script | Tablas |
|---|---|
| `00_init_general.sql` | usuarios |
| `01_clientes.sql` | clientes, pqrs, encuestas y alertas de satisfacción |
| `02_inventario.sql` | bodegas, vehículos y movimientos de inventario |
| `03_financiacion.sql` | entidades financieras |
| `04_alquiler.sql` | puntos de entrega, tarifas y contratos de alquiler |
| `07_legal.sql` | aseguradoras, pólizas, actas de traspaso y revisiones técnico-mecánicas |

Cada área tiene su propio README con el detalle de sus casos de uso.

## Integrantes y casos de uso

| Integrante | Área | Casos de uso | Caso no CRUD |
|---|---|---|---|
| Andrés Alquerque | Clientes y PQRS | CU-01 a CU-05 | CU-05 Calcular índice combinado de satisfacción del cliente |
| Juan Pablo León | Inventario y bodega | CU-06 a CU-10 | CU-10 Realizar control y conciliación de inventario |
| Jerónimo Martin | Financiación | CU-11 a CU-15 | CU-15 Simular, evaluar y formalizar crédito |
| Samuel Borras | Alquiler | CU-16 a CU-20 | CU-20 Realizar alquiler de un vehículo |
| Samuel Montaña | Documentación legal | CU-21 a CU-25 | CU-24 Validar y renovar SOAT, CU-25 Verificar vigencia de la revisión técnico-mecánica |

## Restricciones del curso

- Aplicación de escritorio en Java con interfaz JavaFX. Nada web.
- Los datos se guardan en H2 por JDBC, no en archivos de texto.
- No hay conexiones reales con bancos ni aseguradoras. Para eso está `comun/simuladores/SimuladorAseguradora.java`.
- Inicio de sesión con usuario y contraseña (RF-26) y pestañas según el rol (RF-27). Las contraseñas se guardan como hash SHA-256.
- Cada cambio entra por una rama `feature/...` y un pull request.
- Los documentos (PMP, SRS, SDD) van en Word o LibreOffice y los diagramas en Lucidchart.

## Cómo correrlo

Se necesita el JDK 21 o una versión más nueva. No hace falta instalar Maven: `./mvnw` lo descarga la primera vez, junto con H2, JavaFX y JUnit, así que esa primera vez se necesita internet.

Los comandos se corren desde la carpeta raíz del proyecto, porque la aplicación busca los scripts en `db/` y guarda la base en `data/` a partir de ahí.

```bash
git clone https://github.com/sistematicos-fis/sistema-fis.git
cd sistema-fis
chmod +x mvnw          # solo en macOS o Linux, si bajaste el ZIP
```

Pruebas:

```bash
./mvnw clean test
```

Deben pasar las 121. Para correr solo una clase: `./mvnw test -Dtest=FinanciacionServicioTest`.

Aplicación:

```bash
./mvnw clean compile exec:java
```

También se puede correr la clase `co.edu.javeriana.concesionario.Main` desde IntelliJ o Eclipse.

### Usuarios de prueba

| Usuario | Contraseña | Rol | Pestañas que ve |
|---|---|---|---|
| admin | admin123 | Administrador | Todas |
| asesor | asesor123 | Asesor Comercial | Clientes, Financiación, Alquiler |
| analista | credito123 | Analista de Crédito | Financiación |
| servicio | servicio123 | Analista de Servicio al Cliente | Clientes |
| bodega | bodega123 | Administrador de Bodega | Inventario |
| legal | legal123 | Analista de Documentación Legal | Documentación Legal |

### Ver la base de datos con la consola de H2

La URL de la conexión tiene `AUTO_SERVER=TRUE`, así que la consola puede abrir la base mientras la aplicación sigue abierta.

Con la aplicación corriendo, en otra terminal y desde la raíz del proyecto:

```bash
java -cp ~/.m2/repository/com/h2database/h2/2.3.232/h2-2.3.232.jar org.h2.tools.Console
```

En Windows el jar está en `%USERPROFILE%\.m2\repository\com\h2database\h2\2.3.232\h2-2.3.232.jar`.

En la página que se abre:

- JDBC URL: `jdbc:h2:<ruta del proyecto>/data/concesionario_db;AUTO_SERVER=TRUE`. En macOS o Linux, `echo "jdbc:h2:$(pwd)/data/concesionario_db;AUTO_SERVER=TRUE"` la imprime lista para copiar.
- User Name: `sa`
- Password: vacío

Después de guardar, editar o borrar algo en la aplicación, se vuelve a consultar la tabla, por ejemplo `SELECT * FROM ENTIDADES_FINANCIERAS;`, y el cambio aparece.

## Administración

**Dónde están los datos.** En `data/concesionario_db.mv.db`, dentro de la carpeta del proyecto. Esa carpeta está en el `.gitignore`, así que cada quien tiene su propia base.

**Cómo se crean las tablas.** Al arrancar, `ConexionBD.inicializarBaseDatos()` corre los scripts de `db/` en orden. Usan `CREATE TABLE IF NOT EXISTS` y `MERGE`, por lo que se pueden ejecutar muchas veces sin duplicar datos.

**Empezar de cero.** Cerrar la aplicación y la consola, borrar la carpeta `data/` y volver a abrir la aplicación. La base se crea otra vez con los datos iniciales.

**Copia de seguridad.** Con la aplicación cerrada, copiar la carpeta `data/`. Para restaurar, se pone esa copia en su lugar.

**Usuarios.** No hay pantalla para administrarlos; se hace desde la consola de H2:

```sql
INSERT INTO usuarios (username, password_hash, nombre_completo, rol, activo)
VALUES ('nuevo', LOWER(RAWTOHEX(HASH('SHA-256', STRINGTOUTF8('clave123')))), 'Nombre Apellido', 'ASESOR_COMERCIAL', TRUE);

UPDATE usuarios SET activo = FALSE WHERE username = 'nuevo';
```

Roles válidos: `ADMINISTRADOR`, `ASESOR_COMERCIAL`, `ANALISTA_CREDITO`, `ANALISTA_SERVICIO_CLIENTE`, `ADMINISTRADOR_BODEGA` y `ANALISTA_DOCUMENTACION_LEGAL`.
