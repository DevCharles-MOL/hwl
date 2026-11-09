# Despliegue en AWS ECS Express Mode

Este proyecto es una aplicación Spring Boot con vistas Thymeleaf. Necesita un servidor Java persistente y una base de datos PostgreSQL; AWS Amplify Hosting no ejecuta directamente este servidor.

App Runner dejó de aceptar clientes nuevos; AWS recomienda ECS Express Mode. Consulta el [aviso de disponibilidad de App Runner](https://docs.aws.amazon.com/apprunner/latest/dg/apprunner-availability-change.html) y la [guía de ECS Express Mode](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/express-service-overview.html).

El repositorio incluye un `Dockerfile` de Java 25 y un flujo de GitHub Actions que construye la imagen, la publica en Amazon ECR y solicita a ECS una nueva implementación al enviar cambios a `main`.

## Requisitos

- Una cuenta de AWS y una región elegida. Crea ECR, ECS y RDS en la misma región.
- Una base de datos Amazon RDS para PostgreSQL y un servicio ECS Express Mode.
- GitHub Actions habilitado en el repositorio.
- No despliegues datos clínicos reales: este proyecto es una demostración y no está preparado ni validado para producción con información sanitaria.

Los recursos de AWS generan costos. Revisa precios y configura presupuestos/alertas antes de crearlos.

## 1. Crear la imagen inicial en ECR

1. Crea un repositorio privado de Amazon ECR llamado `hdw`.
2. Configura el proveedor OIDC y agrega las variables `AWS_ROLE_TO_ASSUME`, `AWS_REGION` y `ECR_REPOSITORY` descritas en **GitHub Actions**.
3. Envía un commit a `main`. El flujo probará, construirá y publicará las etiquetas `latest` y el SHA del commit en ECR. El paso de despliegue se omitirá hasta que exista el servicio ECS.

El rol de GitHub debe usar OpenID Connect (OIDC), limitado al repositorio y a la rama `main`; no guardes claves de acceso de AWS de larga duración en GitHub.

## 2. Crear PostgreSQL en RDS

1. Crea una instancia Amazon RDS para PostgreSQL en la misma región y VPC que ECS. Usa el nombre de base `hwd` (o ajusta la URL más adelante).
2. Mantén la base **privada**, sin acceso público.
3. Crea o elige un grupo de seguridad de RDS. Permite entrada TCP `5432` solo desde el grupo de seguridad de las tareas ECS.
4. Guarda el endpoint de RDS, el nombre de usuario y la contraseña en un gestor de secretos. No pongas la contraseña en el repositorio.

## 3. Crear el servicio ECS Express Mode

1. En Amazon ECS, crea el servicio usando la imagen `latest` del repositorio ECR y selecciona **Express Mode**.
2. Usa el puerto de contenedor `8080` y un health check HTTP en `/`.
3. Configura la conectividad de salida a la VPC/subredes que alcanzan RDS y asigna a las tareas el grupo de seguridad autorizado por RDS. Mantén la base fuera de las subredes públicas.
4. Configura las variables de entorno:

   | Nombre | Valor |
   | --- | --- |
   | `DB_URL` | `jdbc:postgresql://<endpoint-rds>:5432/hwd?sslmode=require` |
   | `DB_USERNAME` | Usuario PostgreSQL de RDS |
   | `PORT` | `8080` |

5. Inyecta `DB_PASSWORD` como secreto de AWS Secrets Manager en vez de texto plano. Da al rol de ejecución de tareas permiso para leer ese secreto (y la clave KMS si usas una clave propia).
6. Define también `MFA_ENCRYPTION_PASSWORD` y `MFA_ENCRYPTION_SALT` como secretos/variables estables: usa una contraseña aleatoria fuerte y un salt hexadecimal aleatorio de 16 caracteres. **No los cambies después de guardar secretos MFA cifrados**, pues podrían quedar ilegibles.
7. Si la aplicación debe crear un administrador inicial, configura `ADMIN_EMAIL` y `ADMIN_PASSWORD`; guarda la contraseña como secreto. Si no defines ambos, no se crea ese administrador.

Después de crear el servicio desde `latest`, añade las variables `ECS_CLUSTER` y `ECS_SERVICE` en GitHub. Las siguientes ejecuciones del flujo actualizarán la imagen.

## 4. Preparar GitHub Actions con OIDC

Configura un proveedor de identidad OIDC para GitHub Actions en IAM y crea un rol con una política mínima:

- Confianza limitada a `repo:<OWNER>/<REPOSITORY>:ref:refs/heads/main` con audiencia `sts.amazonaws.com`.
- Permisos ECR para obtener el token y publicar imágenes **solo** en el repositorio `hdw`.
- `ecs:UpdateService` para el servicio ECS objetivo. Agrega únicamente los permisos de lectura de ECS necesarios para la consola/validación.

En **Settings → Secrets and variables → Actions → Variables** del repositorio, crea:

| Variable | Valor |
| --- | --- |
| `AWS_ROLE_TO_ASSUME` | ARN del rol IAM que confía en OIDC |
| `AWS_REGION` | Región AWS elegida, por ejemplo `us-east-1` |
| `ECR_REPOSITORY` | `hdw` |
| `ECS_CLUSTER` | Nombre o ARN del clúster ECS |
| `ECS_SERVICE` | Nombre o ARN del servicio Express Mode |

No agregues `DB_PASSWORD` ni otros secretos de aplicación a estas variables; configúralos en el servicio ECS mediante Secrets Manager.

## 5. Publicar

Envía el proyecto a GitHub y confirma que la rama predeterminada se llame `main`. Una vez creados ECR y el servicio ECS, cada push a `main` ejecutará `.github/workflows/deploy-aws.yml`. Revisa **Actions** en GitHub y los eventos del servicio en ECS para confirmar el resultado.

Si tu rama predeterminada tiene otro nombre, actualiza el filtro `branches` del workflow antes de publicarlo.

## Prueba local del contenedor

Con Docker instalado, construye la imagen con `docker build -t hdw .`. Antes de ejecutarla, proporciona `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` para una instancia PostgreSQL accesible; no uses las credenciales de producción en pruebas locales.
