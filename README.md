# Historias Clínicas

Aplicación web de demostración para gestión de historias clínicas, desarrollada con Java, Spring Boot, Spring MVC, Thymeleaf y JPA.

## Requisitos

- Java 17 o superior
- Maven Wrapper incluido en el proyecto

## Ejecutar localmente

En Windows, desde la carpeta del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación estará disponible en <http://localhost:8080>. Por defecto se conecta a PostgreSQL en `localhost:5432`, a la base `hwd`, con el usuario `postgres`. Configura `DB_PASSWORD` en el entorno antes de iniciar la aplicación. Puedes cambiar la URL, el usuario y el puerto mediante `DB_URL`, `DB_USERNAME` y `SERVER_PORT`, respectivamente.


## Estado y uso

El registro envía un código al correo y solo crea la cuenta `USER` después de verificarlo. Configura las variables SMTP descritas en `.env.example`. El inicio de sesión y los paneles son demostrativos; las rutas están protegidas por Spring Security.

Este proyecto no debe utilizarse para almacenar información clínica real ni desplegarse como sistema de producción sin implementar y validar controles de seguridad y privacidad adecuados.
