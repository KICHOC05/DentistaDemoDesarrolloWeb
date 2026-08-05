# Catalog Service

Microservicio de catálogo de servicios dentales para **ClinicaDentalSaaS**.

## Objetivo

Administra el catálogo de servicios dentales disponibles en la clínica. Permite crear, consultar, actualizar, activar/desactivar y eliminar servicios dentales.

## Estructura

```
catalog-service/
├── pom.xml
├── Dockerfile
├── README.md
├── .env.example
└── src/
    ├── main/
    │   ├── java/com/dnt/catalog/
    │   │   ├── CatalogServiceApplication.java
    │   │   ├── config/
    │   │   ├── controller/DentalServiceController.java
    │   │   ├── dto/
    │   │   │   ├── CreateDentalServiceRequest.java
    │   │   │   ├── UpdateDentalServiceRequest.java
    │   │   │   ├── ChangeStatusRequest.java
    │   │   │   └── DentalServiceResponse.java
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   └── BusinessRuleException.java
    │   │   ├── model/DentalService.java
    │   │   ├── repository/DentalServiceRepository.java
    │   │   ├── security/
    │   │   │   ├── CatalogSecurityConfig.java
    │   │   │   ├── JwtCookieFilter.java
    │   │   │   └── JwtTokenValidator.java
    │   │   ├── service/
    │   │   │   ├── DentalServiceService.java
    │   │   │   └── DentalServiceServiceImpl.java
    │   │   └── seeder/DataSeeder.java
    │   └── resources/application.properties
    └── test/
```

## Endpoints

| Método | Ruta | Rol requerido | Descripción |
|--------|------|--------------|-------------|
| `GET` | `/actuator/health` | Público | Health check |
| `GET` | `/api/catalog/services/active` | Autenticado | Listar servicios activos |
| `GET` | `/api/catalog/services/{publicId}` | Autenticado | Obtener servicio por ID |
| `GET` | `/api/catalog/services` | ADMIN, RECEPTIONIST | Listar todos los servicios |
| `POST` | `/api/catalog/services` | ADMIN, RECEPTIONIST | Crear servicio |
| `PUT` | `/api/catalog/services/{publicId}` | ADMIN, RECEPTIONIST | Actualizar servicio |
| `PATCH` | `/api/catalog/services/{publicId}/status` | ADMIN, RECEPTIONIST | Cambiar estado (activar/desactivar) |
| `DELETE` | `/api/catalog/services/{publicId}` | ADMIN | Eliminar servicio |

### Ejemplos de request/response

#### Crear servicio

```http
POST /api/catalog/services
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Limpieza dental",
  "description": "Limpieza general y eliminacion de sarro",
  "price": 650.00,
  "durationMinutes": 45
}
```

Respuesta: `201 Created`

#### Consultar activos

```http
GET /api/catalog/services/active
Authorization: Bearer <token>
```

Respuesta: `200 OK` - Array de servicios activos ordenados por nombre.

#### Cambiar estado

```http
PATCH /api/catalog/services/{publicId}/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "active": false
}
```

Respuesta: `200 OK` - Servicio actualizado.

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `PORT` | Puerto HTTP | `8084` |
| `DB_URL` | URL de conexión MySQL | Clever Cloud |
| `DB_USER` | Usuario MySQL | - |
| `DB_PASS` | Contraseña MySQL | - |
| `JWT_SECRET` | Clave secreta JWT | `ClinicaDentalSaaS2026SecretKey...` |
| `JPA_DDL_AUTO` | Estrategia DDL de JPA | `update` |
| `JPA_SHOW_SQL` | Mostrar SQL en logs | `false` |

## Ejecución local

```bash
cd catalog-service
copy .env.example .env
# Editar .env con las credenciales reales
mvn spring-boot:run
```

El servicio inicia en `http://localhost:8084`.

## Conexión con Clever Cloud

Base de datos: `b0arfjvp1vhkdjcl6bmq` en `b0arfjvp1vhkdjcl6bmq-mysql.services.clever-cloud.com:3306`.

Configurar las variables `DB_URL`, `DB_USER` y `DB_PASS` en el archivo `.env`.

## Pruebas con Postman

1. Obtener un token JWT desde el endpoint `POST /api/auth/login` del auth-service.
2. Incluir el token en el header `Authorization: Bearer <token>`.
3. Probar los endpoints listados arriba.

### Colección de ejemplo

```
POST localhost:8084/api/catalog/services
GET  localhost:8084/api/catalog/services
GET  localhost:8084/api/catalog/services/active
GET  localhost:8084/api/catalog/services/{publicId}
PUT  localhost:8084/api/catalog/services/{publicId}
PATCH localhost:8084/api/catalog/services/{publicId}/status
DELETE localhost:8084/api/catalog/services/{publicId}
```

A través del API Gateway, reemplazar `localhost:8084` por `localhost:8080`.

## Integración con API Gateway

El API Gateway enruta `GET /api/catalog/**` al catalog-service:

```properties
CATALOG_SERVICE_URL=${CATALOG_SERVICE_URL:http://localhost:8084}
spring.cloud.gateway.routes[6].id=catalog-service
spring.cloud.gateway.routes[6].uri=${CATALOG_SERVICE_URL}
spring.cloud.gateway.routes[6].predicates[0]=Path=/api/catalog/**
```

## Despliegue con Docker

```bash
cd catalog-service
docker build -t dental-catalog-service .
docker run -p 8084:8084 --env-file .env dental-catalog-service
```

## Despliegue en Render

Configuración recomendada:

- **Name:** `dental-catalog-service`
- **Root Directory:** `catalog-service`
- **Runtime:** Docker
- **Dockerfile Path:** `Dockerfile`
- **Instance Type:** Free
- **Health Check Path:** `/actuator/health`

### Variables de entorno en Render

```
DB_URL
DB_USER
DB_PASS
JWT_SECRET
PORT
JPA_DDL_AUTO
JPA_SHOW_SQL
```

### Variable del API Gateway en Render

```
CATALOG_SERVICE_URL=https://dental-catalog-service.onrender.com
```
