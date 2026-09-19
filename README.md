# 📚 Sistema de Gestión de Biblioteca (Backend)

> **⚠️ ESTADO DEL PROYECTO: EN CONSTRUCCIÓN / EN DESARROLLO ⚠️**  
> Este backend se encuentra actualmente en fase activa de desarrollo y pruebas para el curso de Marcos de Desarrollo Web (UTP).

---

## 📌 Descripción General

El **Sistema de Gestión de Biblioteca** es una solución backend robusta construida con **Spring Boot** y **Spring Data JPA**, conectada a una base de datos **PostgreSQL en la nube (Supabase)**. 

El sistema está diseñado para gestionar el catálogo bibliográfico completo (libros, autores, categorías y recursos digitales) así como el flujo de circulación operativo (usuarios, control de préstamos con descuento de stock, devoluciones con cálculo de multas por mora y gestión de reservas).

---

## 🛠️ Tecnologías y Herramientas

* **Lenguaje:** Java 21+
* **Framework:** Spring Boot
* **Persistencia:** Spring Data JPA / Hibernate ORM
* **Base de Datos:** PostgreSQL (alojado en Supabase con Connection Pooling)
* **Control de versiones y compilación:** Maven (con Maven Wrapper `mvnw`)
* **Utilidades:** Lombok

---

## 🏗️ Arquitectura y Paquetes

El proyecto sigue una arquitectura en capas limpia y desacoplada bajo el paquete base `com.utp.biblioteca`:

```text
src/main/java/com/utp/biblioteca/
├── controller/         # Controladores REST que exponen los endpoints
├── dto/                # Data Transfer Objects para peticiones HTTP
├── entities/           # Entidades JPA que mapean las tablas en PostgreSQL
├── repository/         # Interfaces que extienden JpaRepository para acceso a datos
├── service/            # Interfaces de la lógica de negocio
└── service/impl/       # Implementaciones de servicios transaccionales (@Service, @Transactional)
```

---

## 📦 Módulos del Sistema

### 1. Módulo de Catálogo, Autores y Búsqueda (Persona 1)
* **Entidades:**
  * `Libro` (`tbl_libro`): Registro bibliográfico con control de stock total y disponible.
  * `Autor` (`tbl_autor`): Autores con relación `@ManyToMany` hacia libros (`tbl_libro_autor`).
  * `Categoria` (`tbl_categoria`): Géneros/clasificaciones con relación `@ManyToMany` hacia libros (`tbl_libro_categoria`).
  * `ContenidoDigital` (`tbl_contenido_digital`): Recursos digitales y archivos adjuntos asociados (`@ManyToOne` a libro).
* **Búsqueda:** Búsqueda flexible y personalizada por palabras clave cruzando título, sinopsis, autores y categorías.

### 2. Módulo de Usuarios, Préstamos y Circulación (Persona 2)
* **Entidades:**
  * `Usuario` (`tbl_usuario`): Cuentas de usuarios con validación única de correo y DNI.
  * `HistorialBusqueda` (`tbl_historial_busqueda`): Auditoría de búsquedas realizadas por usuario.
  * `Prestamo` (`tbl_prestamo`): Préstamos activos y finalizados con fechas de vencimiento.
  * `Devolucion` (`tbl_devolucion`): Registro de retorno con cálculo de días de mora y cobro de penalidades/multas.
  * `Reserva` (`tbl_reserva`): Cola de reservas de libros.
* **Lógica de Negocio:**
  * **Control de Stock:** Al registrar un préstamo se valida y descuenta automáticamente el `stockDisponible`.
  * **Reposición y Multas:** Al procesar una devolución se repone el stock y se calcula automáticamente la multa en función de los días de retraso.

---

## 🚀 Endpoints REST Principales

### 📖 Libros y Catálogo (`/api/libros`)
| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/libros` | Listar todos los libros |
| `GET` | `/api/libros/{id}` | Obtener detalle de un libro |
| `POST` | `/api/libros` | Registrar nuevo libro |
| `PUT` | `/api/libros/{id}` | Actualizar información del libro |
| `GET` | `/api/libros/buscar?query={termino}` | Búsqueda por filtros |

### ✍️ Autores y Categorías
* `GET` / `POST` `/api/autores`: Gestión de autores.
* `GET` / `POST` `/api/categorias`: Gestión de categorías.

### 👤 Usuarios (`/api/usuarios`)
| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/usuarios/registro` | Registrar un nuevo usuario |
| `GET` | `/api/usuarios/{id}` | Obtener usuario por ID |
| `GET` | `/api/usuarios` | Listar todos los usuarios |
| `GET` | `/api/usuarios/correo?correo={email}` | Consultar usuario por email |

### 🔄 Préstamos y Devoluciones (`/api/prestamos`)
| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/prestamos/solicitar` | Solicitar préstamo (descuenta stock) |
| `POST` | `/api/prestamos/devolver` | Registrar devolución (repone stock y calcula multas) |
| `GET` | `/api/prestamos/activos` | Listar préstamos activos |
| `GET` | `/api/prestamos/usuario/{id}` | Historial de préstamos de un usuario |

### 📌 Reservas (`/api/reservas`)
| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/reservas` | Crear una reserva de libro |
| `GET` | `/api/reservas/usuario/{id}` | Consultar reservas de un usuario |
| `PUT` | `/api/reservas/{id}/cancelar` | Cancelar reserva |

---

## ⚙️ Configuración y Ejecución Local

### 1. Clonar el repositorio
```bash
git clone https://github.com/LY-byte-bot/sistema_de_biblioteca.git
cd sistema_de_biblioteca
```

### 2. Base de Datos
El proyecto está preconfigurado para conectarse a **PostgreSQL (Supabase)** en `src/main/resources/application.properties`. Hibernate gestionará automáticamente el esquema con `ddl-auto=update`.

### 3. Compilar y Ejecutar
En Windows (PowerShell / CMD):
```powershell
.\mvnw.cmd spring-boot:run
```

En Linux / macOS:
```bash
./mvnw spring-boot:run
```

El servidor iniciará en:
```
http://localhost:8080
```

---

## 👨‍💻 Autores
Proyecto colaborativo desarrollado para la Universidad Tecnológica del Perú (UTP).

