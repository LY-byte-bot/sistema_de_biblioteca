# 📚 Sistema Integrado de Gestión de Biblioteca - UTP

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%20%2F%204.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%20%2F%2021%2B-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL%20(Supabase)-blue.svg)](https://supabase.com/)
[![Thymeleaf](https://img.shields.io/badge/Frontend-Thymeleaf%20%2B%20Bootstrap%205-green.svg)](https://www.thymeleaf.org/)
[![OpenPDF](https://img.shields.io/badge/PDF%20Engine-OpenPDF-red.svg)](https://github.com/LibrePDF/OpenPDF)
[![Apache POI](https://img.shields.io/badge/Excel%20Engine-Apache%20POI-darkgreen.svg)](https://poi.apache.org/)

Sistema integral de gestión bibliotecaria, catálogo digital, control de circulación (préstamos y devoluciones) y generación de reportes oficiales en **PDF** y **Excel** desarrollado para la **Universidad Tecnológica del Perú (UTP)**.

---

## 📌 Descripción General

El **Sistema de Biblioteca** es una aplicación web full-stack construida sobre **Spring Boot** y **Thymeleaf**, conectada a una base de datos **PostgreSQL en la nube (Supabase)**. 

Proporciona tanto una experiencia de usuario para lectores (búsqueda de libros, reservas, visualización de fichas técnicas y descarga de extractos PDF) como un entorno administrativo avanzado para bibliotecarios y administradores (inventario, catalogación, control de devoluciones con cálculo de multas por mora, telemetría del servidor y un **módulo centralizado de reportes tabulares oficiales**).

---

## 🛠️ Tecnologías Utilizadas

* **Backend:** Spring Boot (Spring MVC, Spring Data JPA, Spring Security, Spring Validation).
* **Frontend:** Thymeleaf, Bootstrap 5, Bootstrap Icons, HTML5, CSS3 y JavaScript moderno.
* **Seguridad:** Spring Security con cifrado BCrypt y control de acceso basado en roles (`ADMIN`, `BIBLIOTECARIO`, `LECTOR`).
* **Base de Datos:** PostgreSQL en la nube (Supabase Connection Pooling con SSL).
* **Generación de Reportes:**
  * **OpenPDF (`com.github.librepdf:openpdf`):** Generación de documentos PDF con tablas y cabezal institucional formal.
  * **Apache POI (`poi` / `poi-ooxml`):** Generación de hojas de cálculo `.xlsx` con estilos personalizados, colores corporativos y celdas combinadas.
* **Control de Versiones & Build:** Maven 3.9+ (Maven Wrapper incluido).
* **Librerías Adicionales:** Project Lombok para reducción de código boilerplate.

---

## 🏗️ Estructura del Proyecto

El proyecto sigue una arquitectura en capas limpia y modular bajo `com.utp.biblioteca`:

```text
src/main/java/com/utp/biblioteca/
├── config/             # Inicialización de datos (DataInitializer) y manejo global de excepciones
├── controller/         # Controladores MVC (ViewController, ReporteController) y API REST
├── dto/                # DTOs de transferencia para préstamos, devoluciones y reservas
├── entities/           # 10 entidades JPA mapeadas a las tablas de PostgreSQL
├── reports/            # Exportadores dedicados de PDF y Excel + ReporteHeaderUtil
├── repository/         # Interfaces JpaRepository para acceso a base de datos
├── security/           # Configuración de Spring Security, UserDetails y SuccessHandler
├── service/            # Lógica del negocio y sus implementaciones en service/impl/
└── util/               # Generador de extractos PDF y utilidades del sistema
```

---

## 📊 Módulo de Reportes Oficiales (PDF y Excel)

El sistema cuenta con un generador de reportes tabulares para **todas las tablas de la base de datos**, cumpliendo los lineamientos del curso con **OpenPDF** y **Apache POI**.

### 🏛️ Cabezal Institucional Único
Todos los documentos generados (PDF y Excel) incluyen automáticamente el encabezado oficial de la biblioteca con los datos institucionales:
* **Institución:** Universidad Tecnológica del Perú (UTP)
* **Biblioteca:** Biblioteca Académica del Sur
* **Sede:** Av. Tacna y Arica 160, Arequipa, Perú
* **Contacto:** Tel: (054) 388888 | Email: biblioteca@utp.edu.pe | Web: www.utp.edu.pe
* **Metadatos:** Nombre del reporte, fecha/hora de emisión y total de registros procesados.

> 💡 **Personalización del Encabezado:**  
> Puedes modificar el texto o diseño de los encabezados para todos los reportes desde un solo lugar editando:  
> [`ReporteHeaderUtil.java`](file:///C:/Users/Leiston/Documents/proyecto-marcos-web/sistema_de_biblioteca/src/main/java/com/utp/biblioteca/reports/ReporteHeaderUtil.java)

### 📋 Tablas de Base de Datos Exportables

| # | Tabla en Base de Datos | Módulo / Entidad | Exportar PDF | Exportar Excel |
|---|---|---|---|---|
| 1 | `tbl_libro` | Catálogo de Libros e Inventario | `/reportes/libros/pdf` | `/reportes/libros/excel` |
| 2 | `tbl_usuario` | Padrón de Usuarios del Sistema | `/reportes/usuarios/pdf` | `/reportes/usuarios/excel` |
| 3 | `tbl_prestamo` | Control Histórico de Préstamos | `/reportes/prestamos/pdf` | `/reportes/prestamos/excel` |
| 4 | `tbl_devolucion` | Registro de Devoluciones y Multas | `/reportes/devoluciones/pdf` | `/reportes/devoluciones/excel` |
| 5 | `tbl_reserva` | Solicitudes de Reserva de Libros | `/reportes/reservas/pdf` | `/reportes/reservas/excel` |
| 6 | `tbl_autor` | Padrón de Autores Literarios | `/reportes/autores/pdf` | `/reportes/autores/excel` |
| 7 | `tbl_categoria` | Clasificación de Categorías | `/reportes/categorias/pdf` | `/reportes/categorias/excel` |
| 8 | `tbl_rol` | Roles y Privilegios de Acceso | `/reportes/roles/pdf` | `/reportes/roles/excel` |
| 9 | `tbl_contenido_digital` | Repositorio Digital y Fichas | `/reportes/contenidos/pdf` | `/reportes/contenidos/excel` |
| 10 | `tbl_historial_busqueda` | Auditoría de Búsquedas | `/reportes/historial/pdf` | `/reportes/historial/excel` |

* **Panel General de Reportes:** `http://localhost:8080/admin/reportes`
* **Rutas Dinámicas:** `/reportes/{tabla}/pdf` y `/reportes/{tabla}/excel`
* **Alias de Clase:** `/export/pdf` y `/export/excel` (exporta catálogo de libros directamente).

---

## 💻 Funcionalidades y Vistas del Sistema

### 📖 Vistas Públicas y Lector
* **Login y Registro:** Autenticación segura de lectores y creación de cuentas (`/login`, `/registro`).
* **Catálogo General (`/catalogo`):** Búsqueda en tiempo real por palabras clave, filtro por categorías temáticas y disponibilidad de stock.
* **Detalle del Libro (`/libro/{id}`):** Visualización de ficha técnica completa, autores, disponibilidad y descarga de extracto en PDF generado al vuelo.
* **Mis Préstamos y Reservas (`/prestamos`):** Panel personal del lector para consultar préstamos activos, fechas límite y estado de reservas.

### 🛡️ Panel de Administración (`/admin/**`)
* **Catálogo & Inventario (`/admin/catalogo`):** Control del fondo bibliográfico con botones de descarga rápida de reportes en PDF y Excel.
* **Control de Préstamos & WhatsApp (`/admin/prestamos`):** Monitoreo de préstamos en curso, notificación directa a lectores por WhatsApp y procesamiento de devoluciones con cálculo automático de días de mora y multas.
* **Catalogación & Gestión (`/admin/catalogacion`):** Registro de nuevos títulos, alta de autores y mantenimiento de categorías bibliográficas.
* **Centro de Reportes (`/admin/reportes`):** Panel con conteos en vivo de las 10 tablas de la base de datos y botones de exportación individual.
* **Telemetría Actuator (`/admin/telemetria`):** Diagnóstico de rendimiento, estado de la conexión a la base de datos y salud de la JVM.

---

## 🔑 Credenciales de Acceso Inicial

El sistema inicializa automáticamente los usuarios de prueba en el primer arranque:

| Rol | Correo Electrónico | Contraseña | Acceso |
|---|---|---|---|
| **Administrador** | `admin@biblioteca.edu.pe` | `admin123` | Acceso completo (`/admin/**`) y reportes |
| **Lector** | *(Crear cuenta desde `/registro`)* | *(Definida por el usuario)* | Catálogo, reservas y préstamos personales |

---

## 🚀 Instalación y Ejecución Local

### 1. Prerrequisitos
* Java Development Kit (JDK) 17 o superior instalado.
* Conexión a Internet (para descargar dependencias de Maven y conectar con PostgreSQL en Supabase).

### 2. Clonar el Repositorio
```bash
git clone https://github.com/LY-byte-bot/sistema_de_biblioteca.git
cd sistema_de_biblioteca
```

### 3. Configuración de Base de Datos
El proyecto ya incluye la conexión preconfigurada a **PostgreSQL (Supabase)** en `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://aws-0-us-west-2.pooler.supabase.com:5432/postgres?sslmode=allow
spring.datasource.username=postgres.mxxzidfwkocovrhcjhcj
spring.datasource.password=proyectoUTP
```

### 4. Compilar y Ejecutar

#### En Windows (PowerShell / CMD):
```powershell
.\mvnw.cmd clean spring-boot:run
```

#### En Linux / macOS:
```bash
./mvnw clean spring-boot:run
```

### 5. Acceso al Sistema
Abre tu navegador web en:
```text
http://localhost:8080
```

---

## 🧪 Ejecución de Pruebas Automatizadas

Para validar la correcta compilación y el funcionamiento de la generación de reportes en PDF y Excel:

```powershell
.\mvnw.cmd test
```

---

## 👥 Equipo de Desarrollo
Proyecto desarrollado para el curso de **Marcos de Desarrollo Web** — **Universidad Tecnológica del Perú (UTP)**.
