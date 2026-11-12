# Historias Clínicas

Aplicación web de demostración para gestión de historias clínicas, desarrollada con Java, Spring Boot, Spring MVC, Thymeleaf y JPA.

## Requisitos

- Docker Desktop con Docker Compose, o bien JDK 25 y el Maven Wrapper incluido en el proyecto

## Ejecutar localmente

En cualquier sistema operativo, crea `.env` desde `.env.example` solo si aún no existe y configura allí PostgreSQL, MFA y SMTP. En Windows usa `if (-not (Test-Path .env)) { Copy-Item .env.example .env }`; en macOS o Linux, `test -f .env || cp .env.example .env`. `.env` está excluido de Git; no compartas ni subas ese archivo. La aplicación carga sus valores automáticamente al arrancar.

Con Docker Compose (no requiere instalar Java ni PostgreSQL en el equipo):

```shell
docker compose up --build
```

La aplicación y PostgreSQL estarán disponibles en <http://localhost:8080> y `localhost:5432`. Para detenerlos, ejecuta `docker compose down`; para borrar también la base de datos local, usa `docker compose down -v`.

Si prefieres ejecutar la aplicación con Java instalado, necesitas una instancia PostgreSQL accesible.

En Windows, desde PowerShell:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
.\mvnw.cmd spring-boot:run
```

En macOS o Linux:

```shell
test -f .env || cp .env.example .env
./mvnw spring-boot:run
```

La aplicación estará disponible en <http://localhost:8080>. Configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` en `.env`. Completa también las variables SMTP para enviar códigos de verificación. En una instalación nueva, usa valores aleatorios propios para `MFA_ENCRYPTION_PASSWORD` y `MFA_ENCRYPTION_SALT` y guárdalos de forma estable. Si ya hay usuarios con MFA, conserva exactamente las claves que usa esa base de datos; cambiarlas impide validar sus códigos.


## Estado y uso

El registro envía un código al correo y solo crea la cuenta `USER` después de verificarlo. Configura las variables SMTP descritas en `.env.example`. El inicio de sesión y los paneles son demostrativos; las rutas están protegidas por Spring Security.

Este proyecto no debe utilizarse para almacenar información clínica real ni desplegarse como sistema de producción sin implementar y validar controles de seguridad y privacidad adecuados.
