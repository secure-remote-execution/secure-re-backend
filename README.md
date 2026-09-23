# Secure Product Challenge — Backend

Backend del prototipo de inventario de dispositivos de red y simulación de scripts,
para el **Laboratorio 3/4 (FDSI)**.

> 🔒 Desde el Laboratorio 4, la API requiere autenticación (JWT) y autoriza por rol:
> `VIEWER` (solo lectura) y `ADMIN` (lectura + ejecutar simulaciones de scripts). Todo
> request queda trazado con el usuario autenticado que lo realizó. Todos los dispositivos
> son ficticios. HTTPS se configura a nivel de Nginx (ver `comandosimportantes.txt` /
> `nginx.txt` en la raíz del repo del laboratorio).

## Stack

- Java 21 + Spring Boot 3.3
- Spring Security + JWT (jjwt)
- Spring Data JPA + PostgreSQL
- Flyway (migraciones, sin `ddl-auto: update`)
- springdoc-openapi (Swagger UI)

## Arquitectura por capas

```
controller/   → recibe HTTP, valida DTOs, delega al service
service/      → lógica de negocio (interfaces + impl)
repository/   → JpaRepository, acceso a datos
model/        → entidades JPA
dto/          → objetos de entrada/salida (nunca se exponen entidades)
mapper/       → conversión entidad <-> DTO
config/       → CORS, OpenAPI
security/     → Spring Security: filtro JWT, UserDetailsService, config de la filter chain
filter/       → logging de requests (evidencia para Blue Team, incluye el usuario autenticado)
exception/    → manejo centralizado de errores
```

## Cómo levantarlo

1. Levantar PostgreSQL local:
   ```bash
   docker compose up -d
   ```

2. Variables de entorno (opcional, ya tienen defaults para desarrollo local —
   **cambia `JWT_SECRET` fuera del laboratorio**):
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/secure_product_challenge
   export DB_USER=spc_user
   export DB_PASSWORD=spc_password
   export JWT_SECRET=lab4-fdsi-dev-secret-change-me-please-0123456789abcdef
   export JWT_EXPIRATION_MINUTES=480
   ```

3. Ejecutar la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```

4. La API queda en `http://localhost:8081`, Swagger UI en
   `http://localhost:8081/swagger-ui.html`.

## Autenticación y roles

| Usuario  | Password     | Rol    | Permisos                                         |
|----------|--------------|--------|---------------------------------------------------|
| `admin`  | `Admin123!`  | ADMIN  | Leer inventario/historial + ejecutar simulaciones |
| `viewer` | `Viewer123!` | VIEWER | Solo leer inventario/historial                    |

Usuarios sembrados por la migración Flyway `V2__users_and_audit.sql` (solo para el
laboratorio local; no hay endpoint de registro). El JWT se envía como
`Authorization: Bearer <token>` en cada request protegido.

## Endpoints

| Método | Ruta                          | Auth                | Descripción                                   |
|--------|-------------------------------|----------------------|------------------------------------------------|
| POST   | `/api/auth/login`             | pública              | Autentica y devuelve un JWT                     |
| GET    | `/api/devices`                | VIEWER o ADMIN        | Lista el inventario (filtro opcional `?type=`) |
| GET    | `/api/devices/{id}`           | VIEWER o ADMIN        | Detalle de un dispositivo                       |
| POST   | `/api/scripts/simulate`       | ADMIN                 | Simula ejecución de script (no ejecuta nada real), queda trazado con `performedBy` |
| GET    | `/api/scripts/simulate/history` | VIEWER o ADMIN      | Historial de simulaciones (incluye `performedBy`) |

Ejemplos listos para usar en `requests.http` (incluye los logins).

## Despliegue (servidor Ubuntu + Nginx, según guía del Lab 3)

1. Empaquetar: `./mvnw clean package` → genera el JAR en `target/`.
2. Copiar al servidor: `scp target/*.jar usuario@$TARGET_IP:/opt/app/`
3. Ejecutar como servicio systemd en el puerto **8081** (interno, no público).
4. Nginx en el puerto **80** hace de reverse proxy hacia `localhost:8081` bajo `/api/`,
   `/swagger-ui/`, `/v3/api-docs` y `/webjars/` (ver config de Nginx del repo de infraestructura).
