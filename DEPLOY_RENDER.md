# Guia de Despliegue en Render - Clinica Dental SaaS

## Arquitectura

```
                         ┌──────────────┐
                         │  API Gateway  │
                         │   (puerto 8080)│
                         └──────┬───────┘
                                │
            ┌───────────────────┼───────────────────┐
            ▼                   ▼                   ▼
    ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
    │ auth-service │   │ appointment  │   │  clinical    │
    │  (8081)      │   │ -service     │   │ -service     │
    │              │   │  (8082)      │   │  (8083)      │
    └──────┬───────┘   └──────┬───────┘   └──────┬───────┘
           │                  │                  │
           └──────────────────┼──────────────────┘
                              ▼
                     ┌──────────────┐
                     │    MySQL DB  │
                     └──────────────┘
```

Cada servicio comparte la misma base de datos MySQL y la misma clave JWT secreta.

---

## Requisitos Previos

- Cuenta en [Render](https://render.com)
- Repositorio en GitHub conectado a Render
- Base de datos MySQL externa (ver seccion "Base de Datos" abajo)

---

## 1. Base de Datos MySQL

Render no ofrece MySQL como servicio gestionado. Usa una de estas alternativas gratuitas:

| Proveedor | URL | Plan Gratuito |
|-----------|-----|---------------|
| [Aiven](https://aiven.io) | aiven.io | MySQL 1GB RAM, 5GB disco |
| [freesqldatabase.com](https://freesqldatabase.com) | freesqldatabase.com | 5MB, 2 conexiones simultaneas |
| [Railway](https://railway.app) | railway.app | Trial con $5 de credito |

**Crea la base de datos y toma nota de estos valores:**
```
DB_HOST=<hostname>
DB_PORT=3306
DB_NAME=<nombre_base_datos>
DB_USER=<usuario>
DB_PASS=<password>
```

Construye la URL de conexion JDBC:
```
jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME>?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

---

## 2. Clave JWT Compartida

Genera una clave secreta de al menos 256 bits (32 caracteres). Puedes usar PowerShell:

```powershell
# Generar clave aleatoria de 64 caracteres hex
$bytes = [byte[]]::new(32)
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

Guarda este valor como `JWT_SECRET`. **Debe ser el mismo en los 4 servicios.**

---

## 3. Despliegue de los Servicios

En Render, crea 4 **Web Services** apuntando al mismo repositorio de GitHub.

---

### Servicio 1: auth-service

| Configuracion | Valor |
|---------------|-------|
| **Name** | `dental-auth-service` |
| **Region** | `Frankfurt` o `US East` |
| **Branch** | `main` |
| **Root Directory** | `auth-service` |
| **Runtime** | `Docker` |
| **Dockerfile Path** | `Dockerfile.auth-service` |
| **Plan** | `Free` o `Starter` |

**Variables de Entorno:**

| Variable | Valor | Ejemplo |
|----------|-------|---------|
| `SERVER_PORT` | `8081` | |
| `JWT_SECRET` | `<tu_clave_secreta>` | |
| `DB_URL` | URL JDBC de MySQL | `jdbc:mysql://host:3306/dentaldb?useSSL=true&serverTimezone=UTC` |
| `DB_USER` | Usuario MySQL | |
| `DB_PASS` | Password MySQL | |
| `DB_DRIVER` | `com.mysql.cj.jdbc.Driver` | |
| `DB_DIALECT` | `org.hibernate.dialect.MySQLDialect` | |
| `APP_GATEWAY_URL` | URL del gateway (se setea despues) | `https://dental-gateway.onrender.com` |

> **Nota**: `APP_GATEWAY_URL` se puede dejar vacio temporalmente y actualizar despues de crear el gateway.

---

### Servicio 2: appointment-service

| Configuracion | Valor |
|---------------|-------|
| **Name** | `dental-appointment-service` |
| **Root Directory** | `appointment-service` |
| **Runtime** | `Docker` |
| **Dockerfile Path** | `Dockerfile.appointment-service` |

**Variables de Entorno:**

| Variable | Valor |
|----------|-------|
| `SERVER_PORT` | `8082` |
| `JWT_SECRET` | **(mismo que auth-service)** |
| `DB_URL` | **(misma URL que auth-service)** |
| `DB_USER` | **(mismo usuario)** |
| `DB_PASS` | **(misma password)** |
| `DB_DRIVER` | `com.mysql.cj.jdbc.Driver` |
| `DB_DIALECT` | `org.hibernate.dialect.MySQLDialect` |
| `APP_GATEWAY_URL` | `https://dental-gateway.onrender.com` |

---

### Servicio 3: clinical-service

| Configuracion | Valor |
|---------------|-------|
| **Name** | `dental-clinical-service` |
| **Root Directory** | `clinical-service` |
| **Runtime** | `Docker` |
| **Dockerfile Path** | `Dockerfile.clinical-service` |

**Variables de Entorno:** (las mismas que appointment-service, con `SERVER_PORT=8083`)

---

### Servicio 4: api-gateway

| Configuracion | Valor |
|---------------|-------|
| **Name** | `dental-gateway` |
| **Root Directory** | `api-gateway` |
| **Runtime** | `Docker` |
| **Dockerfile Path** | `Dockerfile.api-gateway` |

**Variables de Entorno:**

| Variable | Valor |
|----------|-------|
| `SERVER_PORT` | `8080` |
| `AUTH_SERVICE_URL` | `https://dental-auth-service.onrender.com` |
| `APPOINTMENT_SERVICE_URL` | `https://dental-appointment-service.onrender.com` |
| `CLINICAL_SERVICE_URL` | `https://dental-clinical-service.onrender.com` |

---

## 4. Orden de Despliegue

```
1. auth-service         (esperar a que este verde)
2. appointment-service  (en paralelo con clinical-service)
3. clinical-service     (en paralelo con appointment-service)
4. api-gateway          (ultimo, necesita las 3 URLs anteriores)
```

Despues de desplegar el gateway, **actualiza `APP_GATEWAY_URL`** en auth-service, appointment-service y clinical-service con la URL del gateway.

---

## 5. Health Checks

Para evitar que Render marque el servicio como "unhealthy", configura en cada Web Service:

**Settings > Health Check Path**:

| Servicio | Health Check Path |
|----------|-------------------|
| auth-service | `/web/login` |
| appointment-service | `/web/appointments` |
| clinical-service | `/web/clinical` |
| api-gateway | `/web/login` |

---

## 6. Mantener Servicios Gratuitos Despiertos

En el plan Free, Render duerme los servicios tras 15 minutos de inactividad. Usa [cron-job.org](https://cron-job.org) para hacer ping cada 10 minutos:

Crea 4 cron jobs:
```
https://dental-gateway.onrender.com      # cada 10 min
https://dental-auth-service.onrender.com  # cada 10 min
https://dental-appointment-service.onrender.com  # cada 10 min
https://dental-clinical-service.onrender.com     # cada 10 min
```

---

## 7. Verificacion

Una vez desplegados, prueba el flujo completo:

```bash
# 1. Login como admin
curl -X POST https://dental-auth-service.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
# Deberia devolver un token JWT

# 2. Verificar que el gateway funciona
curl -L https://dental-gateway.onrender.com
# Deberia redirigir a /web/login

# 3. Verificar citas a traves del gateway
curl https://dental-gateway.onrender.com/web/appointments \
  -H "Cookie: token=<token_del_login>"
```

### Credenciales por defecto

| Usuario | Password | Rol |
|---------|----------|-----|
| `admin` | `admin123` | ADMIN |
| `recepcion` | `recepcion123` | RECEPTIONIST |
| `doctor1` | `doctor123` | DOCTOR |

Estos usuarios se crean automaticamente al iniciar `auth-service` por primera vez.

---

## 8. Logs y Debugging

Cada servicio en Render tiene una pestana **Logs** en tiempo real. Los errores comunes:

| Error | Causa | Solucion |
|-------|-------|----------|
| `Connection refused` | MySQL no accesible | Verifica DB_HOST y que MySQL acepte conexiones externas |
| `JWT signature does not match` | JWT_SECRET diferente entre servicios | Usa el mismo JWT_SECRET en los 4 servicios |
| `Table 'xxx' doesn't exist` | `ddl-auto=update` no ejecutado | Verifica que `spring.jpa.hibernate.ddl-auto=update` este en las variables |
| `404 Not Found` en gateway | Rutas mal configuradas | Revisa `SERVER_PORT` y las URLs en las variables del gateway |

---

## 9. Variables de Entorno - Resumen

### auth-service, appointment-service, clinical-service
```
SERVER_PORT=<puerto>
JWT_SECRET=<misma_clave_en_los_3>
DB_URL=jdbc:mysql://<host>:<port>/<db>?useSSL=true&serverTimezone=UTC
DB_USER=<user>
DB_PASS=<pass>
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_DIALECT=org.hibernate.dialect.MySQLDialect
APP_GATEWAY_URL=https://dental-gateway.onrender.com
```

### api-gateway
```
SERVER_PORT=8080
AUTH_SERVICE_URL=https://dental-auth-service.onrender.com
APPOINTMENT_SERVICE_URL=https://dental-appointment-service.onrender.com
CLINICAL_SERVICE_URL=https://dental-clinical-service.onrender.com
```

---

## 10. Despliegue Local con Docker Compose

Si prefieres probar localmente con MySQL:

```bash
# 1. Asegurate de tener Docker instalado
# 2. Crea un archivo .env en la raiz con:

DB_ROOT_PASS=rootpassword123
DB_NAME=clinicadental
JWT_SECRET=ClinicaDentalSaaS2026SecretKeyForJWTTokenGenerationHS256

# 3. Ejecuta:
docker-compose up -d

# 4. Accede a:
#    http://localhost:8080
```

O usando H2 (sin Docker, solo Java 21+):

```powershell
.\start-all.ps1
# Cada servicio se inicia en su propia ventana
# Logs en .\logs\
# Gateway en http://localhost:8080
```
