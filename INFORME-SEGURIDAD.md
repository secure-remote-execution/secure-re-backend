# Informe de seguridad — Laboratorio FDSI (Lab 3/4)

Consolida todo lo realizado sobre `secure-re-backend` y `secure-frontend`, en el
orden de los 4 pasos del laboratorio. Detalle completo y verificable en
`docs/SECURITY.md` de cada repo (hallazgos F-01 a F-04).

## 1. Alcance y línea base

| | Backend | Frontend |
|---|---|---|
| URL | https://github.com/secure-remote-execution/secure-re-backend | https://github.com/secure-remote-execution/secure-frontend |
| Tecnología | Java 21, Spring Boot 3.3, Spring Security + JWT, Spring Data JPA, PostgreSQL 16, Flyway | React 18, TypeScript, Vite, react-router-dom |
| Rama | `security/lab-fdsi` | `security/lab-fdsi` |
| Commit SHA | `eac91a787d56bab110187dc364a4c323da8a7784` | `40c0d53e4ecef8fb1755174eb6400f5fb6e93796` |
| Responsables | jdeltax \<vegavillamiljuanpablo@gmail.com\>, Tomas Olaya \<tomasdi.dev@gmail.com\> | idem |

**Activos identificados**: API REST del backend, base de datos PostgreSQL
(`devices`, `script_simulations`, `users`), secreto de firma JWT, SPA
frontend (sesión en `localStorage`), certificado TLS self-signed de Nginx,
credenciales semilla `admin`/`viewer`.

**Flujo de autenticación**: login (`POST /api/auth/login`) valida contra
`users` (BCrypt) y emite JWT HS256 (claim `role`, 8h de expiración). El
frontend lo guarda y lo manda como `Authorization: Bearer`. Autorización por
rol vía `@PreAuthorize`: `VIEWER` = solo lectura, `ADMIN` = lectura +
simular scripts. Toda operación queda trazada al usuario autenticado
(`performed_by`), nunca anónima.

**APIs**: `POST /api/auth/login` (pública), `GET /api/devices[/{id}]`
(VIEWER|ADMIN), `POST /api/scripts/simulate` (ADMIN), `GET
/api/scripts/simulate/history` (VIEWER|ADMIN), docs OpenAPI (pública).

**Riesgo principal**: gestión de secretos — el `JWT_SECRET` por defecto y las
contraseñas semilla (`Admin123!`, `Viewer123!`) están documentadas en texto
plano en el repo (`application.yml`, README, migración SQL). Si un despliegue
real no sobreescribe `JWT_SECRET`, cualquiera con acceso de lectura puede
forjar un JWT de rol `ADMIN` sin autenticarse legítimamente. Registrado como
**F-01**.

Rama `security/lab-fdsi` creada en ambos repos. `docs/SECURITY.md` completo
en ambos.

## 2. Controles nativos de GitHub

| Control | Estado |
|---|---|
| Dependabot alerts | **Habilitado** (confirmado en `Settings → Advanced Security`), 0 alertas abiertas al 2026-10-21 |
| CodeQL Default setup | No confirmado — pendiente de habilitar |
| Secret scanning (nativo de GitHub) | No confirmado — pendiente de habilitar |

No se compartieron capturas con información sensible; el estado de cada
toggle se registró como texto en `docs/SECURITY.md` sección 2.

## 3. SAST y detección de secretos

Se copió `.github/workflows/seguridad-estatica.yml` (Semgrep + Gitleaks) a
ambos repos, se hizo push a `security/lab-fdsi` y se abrió un PR, lo que
disparó el workflow. Se descargó el artefacto `resultado-semgrep`
(`semgrep.json`) y se analizó:

- **7 hallazgos**: 5 `WARNING` + 2 `ERROR`. **0 hallazgos en el código Java de
  la aplicación** (controllers/services/etc. no dispararon ninguna regla
  OWASP Top Ten).
- **WARNING ×5 — tags mutables en Actions** (`actions/checkout@v4`,
  `actions/setup-python@v5`, `actions/upload-artifact@v4`,
  `gitleaks/gitleaks-action@v3`, en `.github/workflows/seguridad-estatica.yml`):
  usar un tag en vez de un commit SHA permite que un maintainer comprometido
  re-apunte el tag a código malicioso (mismo vector que el incidente real de
  `tj-actions/changed-files`, marzo 2025). **No es falsa alarma** — 100%
  accionable tal cual. Registrado como **F-04**, con los SHA de reemplazo ya
  verificados vía la API de GitHub.
- **ERROR ×2 — hash BCrypt hardcodeado** (`V2__users_and_audit.sql`): confirma
  con herramienta automática el riesgo ya identificado a mano en **F-01**.
  Matiz importante (no es un falso positivo, pero sí hay que calibrar el
  alcance): Semgrep marca `confidence: LOW` porque solo ve el *hash*
  (unidireccional, no explotable por sí solo); no puede ver que las
  *contraseñas en texto plano* que generaron esos hashes también están
  documentadas aparte (README, comentario SQL, `requests.http`) — eso es lo
  que realmente eleva el riesgo a Alto, no el hash en sí.
- Ningún resultado del scan se descartó como falso positivo: los 7 son
  verdaderos positivos sobre el estado actual del repo.

Gitleaks corrió en el mismo workflow pero su resultado no se exportó como
artefacto independiente, así que no se revisó en este informe.

## 4. Componentes y dependencias (SCA)

Dependabot no mostró alertas abiertas, así que se hizo revisión manual sobre
el dependency graph de cada repo, seleccionando una dependencia por
componente y validando si el riesgo aplicaba de verdad al código (no se
actualizó nada a ciegas):

### F-02 — `vite@6.4.3` (frontend, directa) + `source-map-js@1.2.1` (transitiva)
- 5 advisories del módulo `launch-editor` de Vite + 1 DoS de `source-map-js`.
- **Validación de alcance**: ambas son herramientas de *build/dev-time*
  (dev server de Vite, PostCSS en `npm run build`) — no viajan en el bundle
  servido a usuarios (`dist/`). No afectan producción; sí a un dev corriendo
  `npm run dev` que visite un sitio malicioso.
- **Corrección**: `npm audit fix` — bump de **parche** dentro del rango ya
  declarado en `package.json` (`^6.2.0`), sin breaking changes: `vite`
  6.4.3→6.4.5, `source-map-js` 1.2.1→1.2.2. Verificado con `npm run build`
  (mismo bundle de salida). **Estado: Cerrado.**

### F-03 — `org.postgresql:postgresql@42.7.4` (backend, transitiva vía BOM de Spring Boot)
- 3 advisories reales (GitHub Advisory Database): CVE-2025-49146,
  CVE-2026-42198, CVE-2026-54291 — todas con `42.7.4` dentro del rango
  vulnerable.
- **Validación de alcance**: 2 de los 3 (downgrade de channel-binding) no
  aplican hoy porque el proyecto no usa `channelBinding=require` ni TLS en la
  URL JDBC; la tercera (DoS por iteraciones PBKDF2 sin límite en SCRAM) sí
  aplica en cualquier escenario, aunque requiere un servidor Postgres
  malicioso/comprometido para explotarse.
- **Corrección**: override de `postgresql.version` a `42.7.14` (última en
  Maven Central, misma rama 42.7.x) en `pom.xml`, en vez de forzar un bump
  de la versión de Spring Boot completa (que sí sería un cambio de mayor
  riesgo/incompatibilidad). Verificado con `mvn dependency:tree` y `mvn
  package` (compila y empaqueta OK). Pendiente de probar contra Postgres real
  (Docker no disponible en el entorno de trabajo). **Estado: Corregido,
  pendiente verificación runtime.**

## Pendientes

- Habilitar y confirmar CodeQL Default setup y GitHub secret scanning nativo.
- Revisar el resultado de Gitleaks del mismo PR.
- F-01 (secretos en texto plano) y F-04 (tags mutables de Actions) siguen
  **Abiertos** — correcciones propuestas en `docs/SECURITY.md`, no aplicadas
  aún.
- Verificar F-03 contra una instancia real de Postgres.
- Secciones 4 y 5 de `docs/SECURITY.md` (validación antes/después, pentest
  autorizado) siguen pendientes — no se fabricó evidencia que no existe.
