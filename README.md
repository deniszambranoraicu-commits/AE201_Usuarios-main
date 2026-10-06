# usuarios_sqlite · Primera conexión Java + SQLite con JDBC

Proyecto de la **UD02. Manejo de conectores de acceso a bases de datos relacionales** (Acceso a Datos).

## 1. ¿Qué hace este proyecto?

Es un programa Java de consola que:

1. Se **conecta** a una base de datos SQLite (`prueba.db`).
2. Ejecuta una **consulta SQL** sobre la tabla `usuarios` (los usuarios de una localidad concreta, por ejemplo *Madridejos*).
3. **Recorre el resultado** y lo muestra por pantalla.
4. **Cierra** todos los recursos al terminar.

Es el "Hola Mundo" del acceso a datos: todo lo que hagas después (insertar, borrar, transacciones, DAO...) parte de este esquema.

Ejemplo de salida:

```
Conexión establecida con éxito.
Usuarios de Madridejos:
3 | Ana | 12345 | C/ Mayor 4 | Madridejos
```

## 2. Requisitos

| Herramienta | Versión |
|---|---|
| JDK | La indicada en el `pom.xml` (conviene una LTS: 17 o 21) |
| Maven | 3.8 o superior (o el integrado en IntelliJ) |
| Driver | `org.xerial:sqlite-jdbc` (se descarga solo con Maven) |
| Opcional | DB Browser for SQLite, para ver y editar la base de datos |

## 3. Estructura

```
UD02/
├── prueba.db                     ← base de datos SQLite
└── usuarios_sqlite/
    ├── pom.xml                   ← dependencias (driver de SQLite)
    └── src/main/java/es/iesjuanbosco/
        └── Main.java             ← el programa
```

> **Ojo con la ruta de `prueba.db`.** Está en `UD02/`, una carpeta **por encima** del proyecto. Mira la sección 7.

## 4. La base de datos

**Tabla `usuarios`**

| Columna | Tipo | Notas |
|---|---|---|
| `cod` | INTEGER | Clave primaria, autoincremental |
| `nombre` | TEXT | Obligatorio |
| `apellidos` | INTEGER | Obligatorio |
| `direccion` | TEXT | Opcional |
| `localidad` | TEXT | Obligatoria |

**Tabla `Telefonos`**: un usuario puede tener varios teléfonos. Clave primaria `(cod, telefono)`, clave foránea `cod → usuarios(cod)`.

> **Pregunta para reflexionar:** `apellidos` está definido como `INTEGER`. ¿Tiene sentido? SQLite es muy permisivo con los tipos y lo admite, pero otros gestores (MySQL, Oracle...) no lo harían. Corregir el tipo a `TEXT` sería lo correcto.

## 5. Conceptos clave

### JDBC y el driver
**JDBC** (*Java Database Connectivity*) es la API estándar de Java para hablar con bases de datos. Java define las interfaces (`Connection`, `Statement`, `ResultSet`...) y cada gestor aporta su **driver** que las implementa. Aquí el driver es `sqlite-jdbc`, que se añade en el `pom.xml`. Con JDBC 4 o superior **no hace falta** `Class.forName(...)`: el driver se registra solo al estar en el classpath.

### La URL de conexión
```
jdbc:sqlite:ruta/al/fichero.db
└─┬┘ └──┬─┘ └───────┬───────┘
protocolo subprotocolo   dónde está la base de datos
```
Cada gestor tiene su formato (`jdbc:mysql://host:3306/bd`, etc.). En SQLite la base de datos es simplemente **un fichero**.

> **Trampa de SQLite:** si la ruta es incorrecta, SQLite **no falla**: crea un fichero `.db` vacío nuevo. Entonces el error aparecerá después (`no such table: usuarios`), lo que despista mucho.

### Las piezas del acceso a datos

| Clase / interfaz | Para qué sirve |
|---|---|
| `DriverManager` | Fábrica de conexiones: `getConnection(url)` |
| `Connection` | La conexión abierta con la base de datos |
| `Statement` | Ejecuta SQL fijo, sin parámetros |
| `PreparedStatement` | Ejecuta SQL con parámetros (`?`). **Es el recomendable** |
| `ResultSet` | Las filas que devuelve un `SELECT`, recorridas con `next()` |
| `SQLException` | Cualquier error de base de datos. Es una excepción comprobada: hay que tratarla |

### Flujo del programa

```
abrir Connection ──► preparar SQL ──► asignar parámetros (?)
                                              │
cerrar todo ◄── recorrer ResultSet ◄── executeQuery()
```

### Recorrer un `ResultSet`
```java
while (rs.next()) {                    // avanza a la siguiente fila; false si no hay más
    int cod = rs.getInt("cod");        // se lee cada columna por su nombre
    String nombre = rs.getString("nombre");
}
```
Al principio el cursor está **antes** de la primera fila, por eso siempre hay que llamar a `next()` antes de leer.

### Cerrar recursos: `try-with-resources`
`Connection`, `Statement` y `ResultSet` consumen recursos y **hay que cerrarlos**. Lo moderno es declararlos entre paréntesis del `try`: Java los cierra solos, en el orden correcto, incluso si hay una excepción.

```java
try (Connection con = DriverManager.getConnection(url);
     PreparedStatement ps = con.prepareStatement(sql)) {
    // usar con y ps
}   // aquí se cierran automáticamente
```

### `PreparedStatement` y la inyección SQL
**Nunca** construyas SQL concatenando valores:

```java
// MAL: vulnerable a inyección SQL
String sql = "SELECT * FROM usuarios WHERE localidad = '" + localidad + "'";

// BIEN: el valor viaja separado del SQL
String sql = "SELECT cod, nombre FROM usuarios WHERE localidad = ?";
ps.setString(1, localidad);
```
Si `localidad` contuviera `' OR '1'='1`, la primera versión devolvería **toda la tabla**.

### Otras buenas prácticas
- Escribe las columnas en el `SELECT` en lugar de usar `SELECT *`.
- Gestiona el caso de **cero resultados**.
- Muestra los errores por `System.err` con un mensaje claro, no solo `printStackTrace()`.
- No dejes rutas absolutas (`D:/...`) escritas en el código.

## 6. Parámetros del programa (`args`)

La ruta de la base de datos se puede pasar como **argumento** al ejecutar:

```java
String rutaDB = args.length > 0 ? args[0] : RUTA_DB_POR_DEFECTO;
```
Equivale a: *"si me pasan una ruta, la uso; si no, uso la de por defecto"*.

- **IntelliJ:** *Run → Edit Configurations → Program arguments*.
- **Terminal:** `java -cp target/classes es.iesjuanbosco.Main ../prueba.db`

## 7. Cómo ejecutarlo

1. Clona el repositorio y abre la carpeta `usuarios_sqlite` como proyecto Maven.
2. Deja que Maven descargue el driver.
3. Indica dónde está `prueba.db`. Elige una opción:
    - Copiarla a la raíz del proyecto y usar `"prueba.db"`.
    - Usar la ruta relativa `"../prueba.db"` (funciona si ejecutas desde `usuarios_sqlite/`).
    - Pasar la ruta completa como argumento del programa.
4. Ejecuta `Main`.

**Comprobación rápida** de qué ruta está usando realmente:
```java
System.out.println(new java.io.File(rutaDB).getAbsolutePath());
```

## 8. Errores típicos

| Síntoma | Causa probable |
|---|---|
| `no such table: usuarios` | La ruta es incorrecta y SQLite ha creado una base de datos vacía. Borra ese `.db` vacío y revisa la ruta |
| `No suitable driver found` | Falta la dependencia `sqlite-jdbc` o Maven no ha terminado de descargarla |
| `no such column: ...` | El nombre de la columna en el código no coincide con el de la tabla |
| `database is locked` | DB Browser tiene cambios sin guardar (*Write Changes*) o la base de datos abierta |
| La consulta no devuelve nada | Mayúsculas o tildes distintas en el valor buscado (`'Madridejos'` ≠ `'madridejos'`), o no hay datos de esa localidad |
| `UnsupportedClassVersionError` / error de compilación | La versión del JDK no coincide con la del `pom.xml` |

## 9. Ejercicios propuestos

1. **Localidad por parámetro:** que la localidad se pida por teclado (`Scanner`) en lugar de estar fija.
2. **Contar:** muestra cuántos usuarios hay por localidad (`GROUP BY`).
3. **Teléfonos:** muestra cada usuario junto con sus teléfonos (`JOIN` con `Telefonos`).
4. **Clase `Usuario`:** crea un POJO con sus atributos, getters y `toString()`, y guarda cada fila en un objeto en vez de imprimirla directamente.
5. **Clase `UsuarioDAO`:** mueve la consulta a un método `List<Usuario> buscarPorLocalidad(String localidad)`. Es el **patrón DAO**, último punto de la unidad.
6. **Escritura:** añade `INSERT`, `UPDATE` y `DELETE` con `PreparedStatement` y `executeUpdate()`.
7. **Transacciones:** inserta un usuario y su teléfono en la misma transacción (`setAutoCommit(false)`, `commit()`, `rollback()`).

## 10. Relación con los criterios de evaluación (RA2)

- **c)** Se ha utilizado el conector idóneo en la aplicación y se ha establecido la conexión.
- **g)** Se han desarrollado aplicaciones que efectúan consultas.

Los ejercicios 4 a 7 conectan con los criterios **e), f) e i)**.