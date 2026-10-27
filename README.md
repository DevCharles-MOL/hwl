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

La aplicación estará disponible en <http://localhost:8080>. De forma predeterminada usa una base de datos H2 en memoria, por lo que no requiere instalar PostgreSQL.

Para conectarla a PostgreSQL, configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` en el entorno antes de iniciar la aplicación.

## Estado y uso

El registro crea usuarios con rol `USER`. El inicio de sesión y los paneles de usuario y administración son todavía demostrativos; las páginas no están protegidas por autenticación.

Este proyecto no debe utilizarse para almacenar información clínica real ni desplegarse como sistema de producción sin implementar y validar controles de seguridad y privacidad adecuados.
