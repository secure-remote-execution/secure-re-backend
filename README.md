# Secure Product Challenge — Laboratorio 3 FDSI (ECI)

Prototipo académico para el Laboratorio 3 de Fundamentos de Seguridad de la Información
(profesor Fabian Sierra, ECI). Simula una plataforma para consultar un inventario
**ficticio** de dispositivos de red y **simular** (nunca ejecutar realmente) scripts
de configuración aprobados sobre esos dispositivos.

## ⚠️ Alcance deliberado del Laboratorio 3

Esta aplicación **NO tiene autenticación, JWT, guards ni HTTPS a propósito**. Es un
ejercicio de "construir, atacar, detectar, corregir y verificar":

1. Se construye la app insegura (este repo).
2. Un **Red Team** la reconoce con Nmap, curl y OWASP ZAP (puerto abierto, Swagger UI
   público, headers de seguridad ausentes, CORS, etc.).
3. Un **Blue Team** aplica *hardening* básico (headers HTTP, reverse proxy, logging),
   **sin** agregar autenticación.
4. La autenticación (Spring Security + JWT + roles) y HTTPS se agregan en el
   **Laboratorio 4** — están marcados con `// TODO: Lab 4` en los controllers.

No uses este código en un entorno real ni con datos reales: el inventario de
dispositivos, IPs, vendors y responsables son **100% ficticios**.

## Arquitectura

```
secured-remote-exe/
├── backend/    Java 21 + Spring Boot 4 + Spring Data JPA + PostgreSQL + Flyway + springdoc-openapi
├── frontend/   React + Vite + TypeScript
├── docker-compose.yml   PostgreSQL para desarrollo local
└── requests.http        Colección de ejemplos de la API (curl / REST Client)
```

### Backend (`backend/`)

Arquitectura por capas:

```
com.eci.secureproductchallenge
├── controller/   Solo HTTP: recibe DTOs, delega al service, responde DTOs
├── service/      Lógica de negocio (interfaces en la raíz, impl/ para las implementaciones)
├── repository/   Interfaces JpaRepository
├── model/        Entidades JPA (Device, SimulationRecord)
├── dto/          Request/Response DTOs (nunca se exponen entidades directamente)
├── mapper/       Conversión entidad <-> DTO
├── config/       CORS y Swagger/OpenAPI (SIN configuración de seguridad, ver Lab 4)
├── exception/    @RestControllerAdvice centralizado
└── filter/       Logging de requests (timestamp, método, ruta, IP, status, duración)
```

### Frontend (`frontend/`)

```
src/
├── pages/        DeviceListPage, DeviceDetailPage, ScriptSimulatorPage
├── components/   DeviceCard, DeviceTable, StatusBadge, SimulationHistoryTable
├── services/     httpClient.ts, deviceService.ts, scriptService.ts
├── types/        Device, SimulationRequest, SimulationResult (reflejan los DTOs del backend)
├── hooks/        useDevices, useSimulationHistory
└── App.tsx       Enrutamiento con react-router-dom
```

## Requisitos previos

- Java 21 (JDK)
- Maven (o usa el wrapper `./mvnw` incluido, no requiere instalación)
- Node.js 20+ y npm
- Docker + Docker Compose (para levantar PostgreSQL) — o un PostgreSQL local propio

## 1. Levantar la base de datos (PostgreSQL)

Desde la raíz del repo:

```bash
docker compose up -d
```

Esto levanta un contenedor `spc-postgres` en el puerto `5432` con:

| Variable            | Default                     |
|---------------------|------------------------------|
| `POSTGRES_DB`       | `secure_product_challenge`  |
| `POSTGRES_USER`     | `spc_user`                  |
| `POSTGRES_PASSWORD` | `spc_password`              |

Puedes personalizarlas copiando `.env.example` a `.env` en la raíz.

## 2. Levantar el backend

```bash
cd backend
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Al arrancar, **Flyway** ejecuta
las migraciones (`src/main/resources/db/migration`) y siembra 5 dispositivos ficticios.

Documentación interactiva (Swagger UI): `http://localhost:8080/swagger-ui.html`
Documento OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

### Variables de entorno del backend

| Variable                     | Default                                                        | Descripción                          |
|-------------------------------|----------------------------------------------------------------|---------------------------------------|
| `SPRING_DATASOURCE_URL`       | `jdbc:postgresql://localhost:5432/secure_product_challenge`   | URL JDBC de PostgreSQL                |
| `SPRING_DATASOURCE_USERNAME`  | `spc_user`                                                     | Usuario de la base de datos           |
| `SPRING_DATASOURCE_PASSWORD`  | `spc_password`                                                 | Password de la base de datos          |
| `APP_CORS_ALLOWED_ORIGIN`     | `http://localhost:5173`                                        | Origen permitido por CORS (frontend)  |

Estas variables tienen defaults coherentes con `docker-compose.yml`, así que
`./mvnw spring-boot:run` funciona sin configurar nada adicional en desarrollo local.

## 3. Levantar el frontend

```bash
cd frontend
npm install
npm run dev
```

Disponible en `http://localhost:5173`. Variable de entorno opcional (`frontend/.env`,
ver `frontend/.env.example`):

| Variable              | Default                  | Descripción              |
|-----------------------|---------------------------|---------------------------|
| `VITE_API_BASE_URL`   | `http://localhost:8080`  | URL base de la API backend |

## Endpoints de la API

Todos **sin autenticación** (deliberado, ver arriba).

| Método | Ruta                              | Descripción                                                |
|--------|------------------------------------|--------------------------------------------------------------|
| GET    | `/api/devices`                    | Lista todo el inventario de dispositivos                     |
| GET    | `/api/devices?type=FIREWALL`      | Filtra por tipo (`FIREWALL`, `ROUTER`, `SWITCH`)              |
| GET    | `/api/devices/{id}`               | Detalle de un dispositivo                                     |
| POST   | `/api/scripts/simulate`           | Simula la ejecución de un script (no aplica cambios reales)   |
| GET    | `/api/scripts/simulate/history`   | Historial de simulaciones registradas                         |

Ver [requests.http](requests.http) para ejemplos completos de cada endpoint
(usable con la extensión "REST Client" de VS Code o copiando cada bloque como `curl`).

Ejemplo rápido con curl:

```bash
curl http://localhost:8080/api/devices

curl -X POST http://localhost:8080/api/scripts/simulate \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"11111111-1111-1111-1111-111111111111","scriptName":"backup-config.sh","parameters":{"dryRun":"true"}}'
```

## Notas para el Red Team / Blue Team

- El puerto **8080** queda expuesto sin TLS a propósito (HTTP plano, ver Lab 4 para HTTPS).
- **Swagger UI** (`/swagger-ui.html`) y el documento OpenAPI (`/v3/api-docs`) están
  públicos: es intencional, sirven como superficie de reconocimiento para el ejercicio.
- El filtro `RequestLoggingFilter` deja evidencia de cada request (timestamp, método,
  ruta, IP de origen, status, duración) en el logger
  `com.eci.secureproductchallenge.access`, pensado para correlacionarse con el
  `access.log` de Nginx cuando la app se ponga detrás de un reverse proxy.
- El endpoint de simulación **nunca** ejecuta comandos reales sobre los dispositivos:
  solo queda registrado en `simulation_records` como evidencia de qué se intentó.
