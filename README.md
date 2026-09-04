# Secure Product Challenge — Backend

Backend del prototipo de inventario de dispositivos de red y simulación de scripts,
para el **Laboratorio 3 (FDSI)**.

> ⚠️ Por diseño del Laboratorio 3, esta aplicación **no tiene autenticación, autorización
> ni HTTPS**. Eso se agrega en el Laboratorio 4. Todos los dispositivos son ficticios.

## Stack

- Java 21 + Spring Boot 3.3
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
filter/       → logging de requests (evidencia para Blue Team)
exception/    → manejo centralizado de errores
```

## Cómo levantarlo

1. Levantar PostgreSQL local:
   ```bash
   docker compose up -d
   ```

2. Variables de entorno (opcional, ya tienen defaults para desarrollo local):
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/secure_product_challenge
   export DB_USER=spc_user
   export DB_PASSWORD=spc_password
   ```

3. Ejecutar la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```

4. La API queda en `http://localhost:8081`, Swagger UI en
   `http://localhost:8081/swagger-ui.html`.

## Endpoints

| Método | Ruta                          | Descripción                                   |
|--------|-------------------------------|------------------------------------------------|
| GET    | `/api/devices`                | Lista el inventario (filtro opcional `?type=`) |
| GET    | `/api/devices/{id}`           | Detalle de un dispositivo                       |
| POST   | `/api/scripts/simulate`       | Simula ejecución de script (no ejecuta nada real) |
| GET    | `/api/scripts/simulate/history` | Historial de simulaciones                    |

Ejemplos listos para usar en `requests.http`.

## Despliegue (servidor Ubuntu + Nginx, según guía del Lab 3)

1. Empaquetar: `./mvnw clean package` → genera el JAR en `target/`.
2. Copiar al servidor: `scp target/*.jar usuario@$TARGET_IP:/opt/app/`
3. Ejecutar como servicio systemd en el puerto **8081** (interno, no público).
4. Nginx en el puerto **80** hace de reverse proxy hacia `localhost:8081` bajo `/api/`,
   `/swagger-ui/`, `/v3/api-docs` y `/webjars/` (ver config de Nginx del repo de infraestructura).
