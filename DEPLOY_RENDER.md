# Guia de Despliegue en Render - Clinica Dental SaaS

## Arquitectura

```
                         ┌──────────────┐
                         │  API Gateway  │
                         │   (puerto 8080)│
                         └──────┬───────┘
                                │
            ┌───────────────────┼───────────────────────────┐
            ▼                   ▼                   ▼       ▼
    ┌──────────────┐   ┌──────────────┐   ┌──────────────┐ ┌──────────────┐
    │ auth-service │   │ appointment  │   │  clinical    │ │  catalog     │
    │  (8081)      │   │ -service     │   │ -service     │ │ -service     │
    │              │   │  (8082)      │   │  (8083)      │ │  (8084)      │
    └──────┬───────┘   └──────┬───────┘   └──────┬───────┘ └──────┬───────┘
           │                  │                  │                │
           ▼                  ▼                  ▼                ▼
    ┌──────────────┐   ┌──────────────┐   ┌──────────────┐ ┌──────────────┐
    │  MySQL bd1   │   │  MySQL bd2   │   │  MySQL bd3   │ │  MySQL bd4   │
    │ bgpzojbx...  │   │ byzckqdqz... │   │ bkoffey2x... │ │ b0arfjvp...  │
    └──────────────┘   └──────────────┘   └──────────────┘ └──────────────┘
```

**Cada microservicio tiene su propia base de datos MySQL en Clever Cloud.** Todos comparten la misma clave JWT.

---

## Requisitos Previos

- Cuenta en [Render](https://render.com)
- Repositorio en GitHub conectado a Render
- Bases de datos MySQL en Clever Cloud (ya creadas)

---

## Bases de Datos MySQL (Clever Cloud)

Cada servicio se conecta a su propia base de datos. Las URLs JDBC ya estan como valores por defecto en cada `application.properties`.

| Servicio | BD (Clever Cloud) | Host | Usuario |
|----------|------------------|------|---------|
| auth-service | `bgpzojbxcg5wuh0dgnzr` | `bgpzojbxcg5wuh0dgnzr-mysql.services.clever-cloud.com` | `utw9dr3fkgw3x5tu` |
| appointment-service | `byzckqdqz5kfjmu1mlor` | `byzckqdqz5kfjmu1mlor-mysql.services.clever-cloud.com` | `um0aytfzzqye8rbg` |
| clinical-service | `bkoffey2xrpmnnvsgmyk` | `bkoffey2xrpmnnvsgmyk-mysql.services.clever-cloud.com` | `uw2oz4pvq65rubov` |
| catalog-service | `b0arfjvp1vhkdjcl6bmq` | `b0arfjvp1vhkdjcl6bmq-mysql.services.clever-cloud.com` | `uhxge6yizzgqi8ov` |

Las contraseñas estan configuradas como valores por defecto en los archivos `application.properties`. Para produccion, **configuralas como variables de entorno en Render** para no exponerlas en el codigo.

---

## Clave JWT Compartida

Genera una clave secreta de al menos 256 bits:

```powershell
$bytes = [byte[]]::new(32)
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

Guarda este valor como `JWT_SECRET`. **Debe ser el mismo en los 5 servicios.**

---

## Despliegue de los Servicios

En Render, crea **5 Web Services** apuntando al mismo repositorio de GitHub.

---

### Servicio 1: auth-service

| Configuracion | Valor |
|---------------|-------|
| **Name** | `dental-auth-service` |
| **Region** | `Frankfurt` o `US East` |
| **Branch** | `main` |
| **Root Directory** | `auth-service` |
| **Runtime** | `Docker` |
| **Dockerfile Path** | `Dockerfile` |
| **Plan** | `Free` o `Starter` |

**Variables de Entorno:**

| Variable | Valor |
|----------|-------|
| `SERVER_PORT` | `8081` |
| `JWT_SECRET` | `<tu clave secreta>` |
| `DB_URL` | `jdbc:mysql://bgpzojbxcg5wuh0dgnzr-mysql.services.clever-cloud.com:3306/bgpzojbxcg5wuh0dgnzr?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `DB_USER` | `utw9dr3fkgw3x5tu` |
| `DB_PASS` | `<password de bd1>` |
| `DB_DRIVER` | `com.mysql.cj.jdbc.Driver` |
| `DB_DIALECT` | `org.hibernate.dialect.MySQLDialect` |
| `APP_GATEWAY_URL` | `https://dental-gateway.onrender.com` |
| `APPOINTMENT_SERVICE_URL` | `https://dental-appointment-service.onrender.com` |

---

### Servicio 2: appointment-service

| Configuracion | Valor |
|---------------|-------|
| **Name** | `dental-appointment-service` |
| **Root Directory** | `appointment-service` |
| **Runtime** | `Docker` |
| **Dockerfile Path** | `Dockerfile` |

| **Plan** | `Free` o `Starter` |

**Variables de Entorno:**

| Variable | Valor |
|----------|-------|
| `SERVER_PORT` | `8082` |
| `JWT_SECRET` | `<mismo que auth-service>` |
| `DB_URL` | `jdbc:mysql://byzckqdqz5kfjmu1mlor-mysql.services.clever-cloud.com:3306/byzckqdqz5kfjmu1mlor?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `DB_USER` | `um0aytfzzqye8rbg` |
| `DB_PASS` | `<password de bd2>` |
| `DB_DRIVER` | `com.mysql.cj.jdbc.Driver` |
| `DB_DIALECT` | `org.hibernate.dialect.MySQLDialect` |
| `APP_GATEWAY_URL` | `https://dental-gateway.onrender.com` |
| `AUTH_SERVICE_URL` | `https://dental-auth-service.onrender.com` |
| `CLINICAL_SERVICE_URL` | `https://dental-clinical-service.onrender.com` |

---

### Servicio 3: clinical-service

| Configuracion | Valor |
|---------------|-------|
| **Name** | `dental-clinical-service` |
| **Root Directory** | `clinical-service` |
| **Runtime** | `Docker` |
| **Dockerfile Path** | `Dockerfile` |

**Variables de Entorno:**

| Variable | Valor |
|----------|-------|
| `SERVER_PORT` | `8083` |
| `JWT_SECRET` | `<mismo que auth-service>` |
| `DB_URL` | `jdbc:mysql://bkoffey2xrpmnnvsgmyk-mysql.services.clever-cloud.com:3306/bkoffey2xrpmnnvsgmyk?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `DB_USER` | `uw2oz4pvq65rubov` |
| `DB_PASS` | `<password de bd3>` |
| `DB_DRIVER` | `com.mysql.cj.jdbc.Driver` |
| `DB_DIALECT` | `org.hibernate.dialect.MySQLDialect` |
| `APP_GATEWAY_URL` | `https://dental-gateway.onrender.com` |

---

### Servicio 4: catalog-service

| Configuracion | Valor |
|---------------|-------|
| **Name** | `dental-catalog-service` |
| **Root Directory** | `catalog-service` |
| **Runtime** | `Docker` |
| **Dockerfile Path** | `Dockerfile` |

**Variables de Entorno:**

| Variable | Valor |
|----------|-------|
| `SERVER_PORT` | `8084` |
| `JWT_SECRET` | `<mismo que auth-service>` |
| `DB_URL` | `jdbc:mysql://b0arfjvp1vhkdjcl6bmq-mysql.services.clever-cloud.com:3306/b0arfjvp1vhkdjcl6bmq?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `DB_USER` | `uhxge6yizzgqi8ov` |
| `DB_PASS` | `<password de bd4>` |
| `JPA_DDL_AUTO` | `update` |
| `JPA_SHOW_SQL` | `false` |
| `APP_GATEWAY_URL` | `https://dental-gateway.onrender.com` |

---

### Servicio 5: api-gateway

| Configuracion | Valor |
|---------------|-------|
| **Name** | `dental-gateway` |
| **Root Directory** | `api-gateway` |
| **Runtime** | `Docker` |
| **Dockerfile Path** | `Dockerfile` |

**Variables de Entorno:**

| Variable | Valor |
|----------|-------|
| `SERVER_PORT` | `8080` |
| `AUTH_SERVICE_URL` | `https://dental-auth-service.onrender.com` |
| `APPOINTMENT_SERVICE_URL` | `https://dental-appointment-service.onrender.com` |
| `CLINICAL_SERVICE_URL` | `https://dental-clinical-service.onrender.com` |
| `CATALOG_SERVICE_URL` | `https://dental-catalog-service.onrender.com` |

---

## Orden de Despliegue

```
1. auth-service         (esperar a que este verde)
2. appointment-service  ┐
3. clinical-service     ├─ en paralelo
4. catalog-service      ┘
5. api-gateway          (ultimo, necesita las 4 URLs del paso anterior)
```

Despues de desplegar el gateway, **actualiza `APP_GATEWAY_URL`** en los 4 servicios backend con `https://dental-gateway.onrender.com`.

---

## Health Checks

| Servicio | Health Check Path |
|----------|-------------------|
| auth-service | `/web/login` |
| appointment-service | `/web/appointments` |
| clinical-service | `/web/clinical` |
| catalog-service | `/actuator/health` |
| api-gateway | `/web/login` |

---

## Mantener Servicios Gratuitos Despiertos

En el plan Free, Render duerme los servicios tras 15 min de inactividad. Usa [cron-job.org](https://cron-job.org):

```
https://dental-gateway.onrender.com               cada 10 min
https://dental-auth-service.onrender.com           cada 10 min
https://dental-appointment-service.onrender.com    cada 10 min
https://dental-clinical-service.onrender.com       cada 10 min
https://dental-catalog-service.onrender.com        cada 10 min
```

---

## Verificacion

```bash
# 1. Login
curl -X POST https://dental-auth-service.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. Gateway
curl -L https://dental-gateway.onrender.com

# 3. Catalogo via Gateway
curl https://dental-gateway.onrender.com/api/catalog/services/active \
  -H "Authorization: Bearer <token>"
```

### Credenciales por defecto

| Usuario | Password | Rol |
|---------|----------|-----|
| `admin` | `admin123` | ADMIN |
| `recepcion` | `recepcion123` | RECEPTIONIST |
| `doctor1` | `doctor123` | DOCTOR |

Se crean automaticamente al iniciar `auth-service` por primera vez.

---

## Logs y Debugging

| Error | Causa | Solucion |
|-------|-------|----------|
| `Connection refused` | MySQL no accesible | Verifica DB_URL, DB_USER, DB_PASS |
| `JWT signature does not match` | JWT_SECRET diferente | Usa el mismo JWT_SECRET en los 5 servicios |
| `Table 'xxx' doesn't exist` | ddl-auto=update no ejecutado | Verifica `spring.jpa.hibernate.ddl-auto=update` |
| `404 Not Found` en gateway | Rutas mal configuradas | Revisa las *_SERVICE_URL en el gateway |

---

## Despliegue Local con Docker Compose

```bash
# Archivo .env en la raiz:
DB_ROOT_PASS=rootpassword123
DB_NAME=clinicadental
JWT_SECRET=ClinicaDentalSaaS2026SecretKeyForJWTTokenGenerationHS256

# Ejecutar:
docker-compose up -d

# Acceder: http://localhost:8080
```

O sin Docker (Java 21+):

```powershell
.\start-all.ps1
```
