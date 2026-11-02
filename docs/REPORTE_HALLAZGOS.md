# Informe técnico de hallazgos

**Proyecto:** Secure Product Challenge — inventario de dispositivos de red y simulador de scripts (FDSI Lab 3/4/5)  
**Integrantes:** jdeltax, Tomas Olaya  
**Repositorio / commit:**
- Backend: https://github.com/secure-remote-execution/secure-re-backend — `security/lab-fdsi` @ `a49b93f4d7d61ecd94312d1adb6c3743eaa8d066` — PR [#6](https://github.com/secure-remote-execution/secure-re-backend/pull/6)
- Frontend: https://github.com/secure-remote-execution/secure-frontend — `security/lab-fdsi` @ `726c51aea5980fb8081770bbb2805833219ba6c0` — PR [#2](https://github.com/secure-remote-execution/secure-frontend/pull/2)

**Entorno autorizado:** staging local propio. Nginx con HTTPS (certificado self-signed) en `https://localhost`, delante del backend Spring Boot (`localhost:8081`) y de PostgreSQL en Docker. Solo datos y usuarios ficticios de laboratorio. No se tocó producción ni sistemas de terceros.  
**Fecha:** 2026-11-02

> Las credenciales de laboratorio se referencian como `$LAB_USER` / `$LAB_PASS`; no se incluyen en este informe.

## Resumen ejecutivo

Los tres riesgos principales del proyecto son:

1. **Secretos en texto plano en el repositorio (F-01, alto, mitigado).** El secreto de firma JWT por defecto y las contraseñas de los usuarios semilla estaban escritos en la configuración y la documentación. Si un despliegue real no cambiaba `JWT_SECRET`, cualquiera con acceso de lectura al repo podía emitir un token válido con rol `ADMIN`. Era el riesgo más grave porque rompía toda la autenticación.
2. **Driver JDBC de PostgreSQL vulnerable (F-03, alto, corregido).** La versión `42.7.4`, heredada de Spring Boot, tiene tres CVE publicadas. Una de ellas (DoS en la autenticación SCRAM) aplica sin importar la configuración.
3. **Falta del header HSTS (F-07, medio, corregido y reescaneado).** Ni el backend ni el sitio estático le pedían al navegador usar siempre HTTPS. Es el hallazgo con el ciclo completo: DAST → validación manual → corrección → reescaneo.

**Corregidos:** F-02 (vite), F-03 (driver PostgreSQL), F-04 (Actions fijadas por SHA) y F-07 (HSTS), cada uno con evidencia antes y después.  
**Mitigado:** F-01 (contraseñas rotadas al arrancar y secreto JWT obligatorio; los hashes viejos siguen en V2 como contraseñas retiradas).  
**Abierto:** F-05, pendiente de triage.  
**Cerrado como limitación técnica:** F-06 (Gitleaks en Actions requiere licencia por ser un repositorio de organización; se usó el CLI como alternativa).

## Metodología

Identificación del alcance → SAST → detección de secretos → SCA → DAST pasivo → validación manual no destructiva → corrección → reescaneo.

| Etapa | Herramienta | Resultado |
|---|---|---|
| SAST | Semgrep (`p/owasp-top-ten`, `p/secrets`) en Actions | 7 hallazgos (5 WARNING, 2 ERROR); 0 en el código Java de la aplicación |
| SAST (reescaneo) | Semgrep local, mismas reglas | Backend 2 (hashes retirados de V2), frontend 0 |
| SAST | CodeQL Default setup | Habilitado; reporta 1 alerta de severidad alta (F-05) |
| Secretos | Gitleaks Action en Actions | Falla a los 5 s por licencia de organización (F-06) |
| Secretos | Gitleaks CLI local (alternativa del kit) | Historial completo de ambos repos (8 y 7 commits): **sin fugas** |
| SCA | Dependabot alerts | Habilitado; 0 alertas abiertas |
| SCA | `npm audit` + dependency graph + GitHub Advisory Database | 2 dependencias vulnerables (F-02, F-03) |
| DAST pasivo | OWASP ZAP Baseline local | Antes: 0 FAIL, 7 WARN, 61 PASS. Después: 0 FAIL, 6 WARN, 62 PASS |
| Validación manual | `curl` con cuenta de laboratorio | Hipótesis HSTS confirmada (F-07) |

**Alternativas documentadas:**
- **DAST:** el workflow del kit `dast-staging.yml` está en el repo, pero no se ejecutó. Corre en un runner de GitHub en la nube que no puede alcanzar nuestro staging, que solo existe en `localhost` / IP de LAN. Se ejecutó el mismo baseline local con Docker, como permite el enunciado: `zap-baseline.py -t https://localhost` con la imagen `zaproxy/zap-stable` y `--network host`.
- **Secretos:** como indica el kit, ante la limitación de licencia de Gitleaks en organizaciones, se corrió el CLI: `docker run zricethezav/gitleaks detect --source <repo> --redact`.

## Hallazgo F-07 — Falta del header Strict-Transport-Security (HSTS)
- **Título:** El backend y el sitio estático no envían `Strict-Transport-Security`.
- **Categoría OWASP/CWE:** OWASP A05:2021 Security Misconfiguration; CWE-319 (Cleartext Transmission of Sensitive Information). Regla ZAP 10035.
- **Severidad y justificación:** Media. Sin HSTS, un atacante en la red puede bajar la conexión a HTTP. En este despliegue Nginx no escucha en el puerto 80, lo que reduce la exposición, por eso no es alta. Es defensa en profundidad.
- **Evidencia minimizada:** ZAP Baseline marcó la regla 10035 como `WARN-NEW` en 4 URLs servidas por Nginx (`docs/evidence/zap-baseline-report.html`). Al investigar la causa en el backend:
  ```bash
  curl -sk -D - -o /dev/null -X POST https://localhost/api/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$LAB_USER\",\"password\":\"$LAB_PASS\"}" | grep -i strict-transport
  # (sin salida: header ausente)
  ```
- **Impacto en C/I/D:** Confidencialidad e integridad. Una conexión degradada a HTTP expone el JWT y las credenciales, y permite modificar respuestas.
- **Pasos de validación segura en entorno propio:** Hipótesis: *"el backend no envía HSTS aunque siempre está detrás de Nginx con HTTPS"*. Se validó con una sola petición de login con la cuenta de laboratorio, solo leyendo los headers de la respuesta. No se modificaron datos ni se hizo ningún ataque. Es reproducible de forma determinista.
  Causa raíz: Spring Security ya incluye `HstsHeaderWriter`, pero solo agrega el header si `request.isSecure()` es verdadero. Nginx habla con el backend por HTTP plano, y Spring no confiaba en el `X-Forwarded-Proto: https` que Nginx reenvía.
- **Corrección (commit / pull request):**
  - Backend: `server.forward-headers-strategy: framework` en `src/main/resources/application.yml`. Commit `a49b93f`, PR [#6](https://github.com/secure-remote-execution/secure-re-backend/pull/6).
  - Nginx (sitio estático): `add_header Strict-Transport-Security "max-age=63072000; includeSubDomains" always;` aplicado en `/etc/nginx/nginx.conf` y en la plantilla `nginx.txt`. La configuración de Nginx no está versionada en ninguno de los dos repos, por lo que este cambio no forma parte del PR.
- **Resultado del reescaneo:**
  - `curl` al backend: `Strict-Transport-Security: max-age=31536000 ; includeSubDomains`
  - `curl` a `https://localhost/`: `Strict-Transport-Security: max-age=63072000; includeSubDomains`
  - ZAP: la regla 10035 pasó de `WARN-NEW` a `PASS`. Totales de 7 WARN / 61 PASS a 6 WARN / 62 PASS (`docs/evidence/zap-baseline-report-after.html`).
  - Prueba de regresión: los dos `curl` anteriores más el reescaneo de ZAP.
  - Observación: en las respuestas de `/api` llegan dos headers HSTS, uno del backend y otro de Nginx, porque su `add_header` también aplica a lo proxeado. El navegador procesa solo el primero, así que no hay riesgo. Se puede limpiar con `proxy_hide_header Strict-Transport-Security;` en el `location` de la API.

## Hallazgo F-03 — Driver JDBC de PostgreSQL vulnerable
- **Título:** `org.postgresql:postgresql@42.7.4`, versión heredada del BOM de Spring Boot 3.3.4, afectada por tres CVE.
- **Categoría OWASP/CWE:** OWASP A06:2021 Vulnerable and Outdated Components; CWE-1395 (Dependency on Vulnerable Third-Party Component).
- **Severidad y justificación:** Alta. Advisories reales del GitHub Advisory Database con `42.7.4` dentro del rango vulnerable:
  - CVE-2025-49146 / GHSA-hq9p-pm7w-8p54: bypass de `channelBinding=require`.
  - CVE-2026-42198 / GHSA-98qh-xjc8-98pq: DoS por iteraciones PBKDF2 sin límite en SCRAM.
  - CVE-2026-54291 / GHSA-j92g-9f8w-j867: degradación silenciosa de channel binding.
- **Evidencia minimizada:** `mvn dependency:tree` → `org.postgresql:postgresql:jar:42.7.4:runtime`.
- **Impacto en C/I/D:** Disponibilidad (el DoS en SCRAM aplica siempre, pero requiere un servidor PostgreSQL malicioso o comprometido). Confidencialidad e integridad para las dos CVE de channel binding, que **no aplican** hoy porque la URL JDBC no usa `channelBinding=require` ni TLS.
- **Pasos de validación segura en entorno propio:** Se comparó la versión resuelta contra los rangos de cada advisory, y se revisó la URL JDBC de `application.yml` para decidir cuáles CVE aplican al código real. No se intentó explotar nada.
- **Corrección (commit / pull request):** Override de `postgresql.version` a `42.7.14` en `pom.xml`: misma rama 42.7.x, sin cambios de API. Se descartó subir toda la versión de Spring Boot, que sería un cambio incompatible y "a ciegas". Commit `8041375`, PR [#6](https://github.com/secure-remote-execution/secure-re-backend/pull/6).
- **Resultado del reescaneo:** `mvn dependency:tree` → `42.7.14`; `mvn package` OK; backend arriba y login funcionando contra PostgreSQL con el driver nuevo.

## Hallazgo F-02 — vite y source-map-js vulnerables (frontend)
- **Título:** `vite@6.4.3` (directa) y `source-map-js@1.2.1` (transitiva) con advisories altos.
- **Categoría OWASP/CWE:** OWASP A06:2021 Vulnerable and Outdated Components; CWE-1395.
- **Severidad y justificación:** Media. `npm audit` las reporta como altas, pero ambas son herramientas de build y desarrollo: no viajan en el bundle `dist/` que se sirve a los usuarios.
- **Evidencia minimizada:** `npm audit` → 2 vulnerabilidades altas (GHSA-r825-w8mm-5pxw y otras 4 del módulo `launch-editor` de vite; GHSA-68fv-2mgg-jv7q en source-map-js).
- **Impacto en C/I/D:** Integridad y disponibilidad del entorno del desarrollador (ejecución de comandos contra el dev server si se visita un sitio malicioso con `npm run dev` activo). No afecta producción.
- **Pasos de validación segura en entorno propio:** Se verificó con `npm ls` que ambas solo participan en `npm run dev` / `npm run build`.
- **Corrección (commit / pull request):** `npm audit fix`, con bump de parche dentro del rango ya declarado (`^6.2.0`): vite 6.4.3 → 6.4.5, source-map-js 1.2.1 → 1.2.2. Commit `a62ff50`, PR [#2](https://github.com/secure-remote-execution/secure-frontend/pull/2).
- **Resultado del reescaneo:** `npm audit` → 0 vulnerabilidades; `npm run build` produce el mismo bundle.

## Hallazgo F-01 — Secretos en texto plano en el repositorio
- **Título:** Secreto de firma JWT por defecto y contraseñas de usuarios semilla escritos en el repo.
- **Categoría OWASP/CWE:** OWASP A07:2021 Identification and Authentication Failures; CWE-798 (Hard-coded Credentials) y CWE-321 (Hard-coded Cryptographic Key).
- **Severidad y justificación:** Alta. Con el secreto JWT por defecto se pueden emitir tokens de cualquier rol sin autenticarse.
- **Evidencia minimizada:** Semgrep (`generic.secrets.security.detected-bcrypt-hash`, ERROR ×2) detectó los hashes BCrypt en `db/migration/V2__users_and_audit.sql`. Las contraseñas en claro estaban en el comentario de V2, `README.md` y `requests.http`, y el valor por defecto de `JWT_SECRET` en `application.yml` (no se reproducen aquí).
- **Impacto en C/I/D:** Confidencialidad e integridad: acceso de administrador y ejecución de simulaciones a nombre de otro usuario.
- **Pasos de validación segura en entorno propio:** Revisión de los archivos señalados por Semgrep. Antes del fix, un login de `admin` con la contraseña versionada en V2 respondía `HTTP 200`. No se forjó ningún token.
- **Corrección (commit / pull request):** en la rama `security/lab-fdsi`:
  1. `application.yml`: `secret: ${JWT_SECRET}` sin valor por defecto; la app no arranca si falta.
  2. Nuevo `SeedUserPasswordInitializer`: al arrancar, toma las contraseñas de `LAB_ADMIN_PASSWORD` / `LAB_VIEWER_PASSWORD` (obligatorias) y reemplaza los hashes que sembró V2.
  3. Contraseñas en claro eliminadas de `README.md` (backend y frontend) y de `requests.http` (ahora usa `{{$processEnv ...}}`).
  4. Prueba de regresión automatizada: `SeedUserPasswordInitializerTest`, con 3 casos: rota el hash, no reescribe si ya coincide, falla si la contraseña llega vacía.

  V2 **no se modificó**: Flyway valida el checksum de cada migración aplicada, y editarla impediría arrancar contra la base existente.
- **Resultado del reescaneo:**
  - Login de `admin` con la contraseña versionada en V2: `HTTP 200` → **`HTTP 401`**. Con las contraseñas nuevas: `HTTP 200`.
  - El viewer sigue recibiendo `403` al simular (los roles no cambiaron).
  - Arranque sin `JWT_SECRET`: falla con `Could not resolve placeholder 'JWT_SECRET'`.
  - `mvn test`: 3 tests, 0 fallos.
  - Semgrep sigue reportando los 2 hashes de V2 (`docs/evidence/semgrep-despues.json`). Es riesgo residual aceptado: siguen en el repo y en el historial de git, pero ya no son credenciales válidas.

## Hallazgo F-04 — GitHub Actions referenciadas por tag mutable
- **Título:** Los 5 `uses:` de `seguridad-estatica.yml` apuntan a tags (`@v4`, `@v5`, `@v3`), no a un commit fijo.
- **Categoría OWASP/CWE:** OWASP A08:2021 Software and Data Integrity Failures; CWE-1357 y CWE-353.
- **Severidad y justificación:** Media. Requiere que la cuenta del mantenedor de la Action sea comprometida, el mismo vector del incidente de `tj-actions/changed-files` (marzo 2025).
- **Evidencia minimizada:** Semgrep `github-actions-mutable-action-tag`, WARNING ×5 (líneas 14, 15, 24, 33 y 37).
- **Impacto en C/I/D:** Integridad: código ajeno se ejecutaría en el pipeline con los permisos del workflow.
- **Pasos de validación segura en entorno propio:** Lectura del workflow y consulta de los SHA actuales de cada tag en la API pública de GitHub.
- **Corrección (commit / pull request):** cada `uses:` fijado a su SHA, con el tag como comentario, en `seguridad-estatica.yml` y también en `dast-staging.yml` del kit, en ambos repos (rama `security/lab-fdsi`): `actions/checkout@11d5960a…` (v4), `actions/setup-python@a26af69b…` (v5), `actions/upload-artifact@ea165f8d…` (v4), `gitleaks/gitleaks-action@e0c47f4f…` (v3). SHA completos en `docs/SECURITY.md`.
- **Resultado del reescaneo:** Semgrep local con las mismas reglas: 0 hallazgos `github-actions-mutable-action-tag` (antes 5), en backend y frontend (`docs/evidence/semgrep-despues.json`).

## Hallazgo F-06 — Gitleaks falla en Actions (limitación técnica)
- **Título:** El job `Gitleaks (secret scanning)` del PR falla a los 5 s.
- **Categoría OWASP/CWE:** No es una vulnerabilidad; es una limitación de la herramienta.
- **Severidad y justificación:** Informativa. El CLI no encontró fugas en todo el historial. Que el job falle en 5 s, antes de escanear, es consistente con que `gitleaks-action` exige `GITLEAKS_LICENSE` en repositorios de organización, como advierten el kit y el propio workflow.
- **Evidencia minimizada:** Check rojo en `docs/evidence/pr-checks-codeql-gitleaks-semgrep.png`; CLI: `8 commits scanned … no leaks found` (backend), `7 commits scanned … no leaks found` (frontend); reporte vacío en `docs/evidence/gitleaks-cli-report.json`.
- **Impacto en C/I/D:** Ninguno directo. El riesgo es perder la detección automática de secretos en cada PR.
- **Pasos de validación segura en entorno propio:** Gitleaks CLI en Docker, en modo lectura y con `--redact`.
- **Corrección (commit / pull request):** Limitación documentada. Alternativas: GitHub secret scanning nativo, o un `GITLEAKS_LICENSE` gratuito para uso académico.
- **Resultado del reescaneo:** CLI sin fugas.

## Hallazgo F-05 — Alerta de CodeQL (pendiente de triage)
- **Título:** El check `Code scanning results / CodeQL` del PR reporta "1 new alert including 1 high severity security vulnerability".
- **Categoría OWASP/CWE:** Por determinar.
- **Severidad y justificación:** Alta según GitHub. Aplicabilidad real sin confirmar.
- **Evidencia minimizada:** `docs/evidence/pr-checks-codeql-gitleaks-semgrep.png`.
- **Impacto en C/I/D:** Por determinar.
- **Pasos de validación segura en entorno propio:** Pendiente: abrir el detalle en `Security → Code scanning`.
- **Corrección (commit / pull request):** Pendiente.
- **Resultado del reescaneo:** Pendiente.

## Reflexión final

**Qué identificaron las herramientas:** cada una vio una capa distinta. Semgrep encontró problemas de configuración del pipeline (F-04) y de secretos (F-01). `npm audit` y el GitHub Advisory Database encontraron las dependencias vulnerables (F-02, F-03). ZAP encontró la falta de headers en lo que se sirve (F-07).

**Qué omitieron:**
- Ninguna detectó directamente el valor por defecto de `JWT_SECRET` en `application.yml`: ni Semgrep ni Gitleaks lo marcaron. Semgrep marcó los hashes BCrypt (con `confidence: LOW`), pero no las contraseñas en claro que los generan.
- Dependabot mostró 0 alertas mientras el driver de PostgreSQL tenía tres CVE. Hubo que buscarlas a mano.
- ZAP solo recorrió 5 URLs: como es una SPA de React, no navega las rutas internas ni llama a la API.
- La causa raíz de F-07 en el backend (Spring no confiaba en el proxy) solo apareció al investigar a mano el hallazgo de ZAP.

**Cómo integrarlo al ciclo de desarrollo:**
- Correr SAST y SCA en cada PR (ya está en `seguridad-estatica.yml`) y pasar de `|| true` a bloquear el merge cuando el triage esté limpio.
- Mantener las Actions fijadas por SHA (F-04) y actualizarlas a propósito, no por el tag.
- No sembrar credenciales en migraciones: inyectarlas por entorno, como quedó con `SeedUserPasswordInitializer` (F-01).
- Agregar un test de integración que verifique los headers de seguridad, para que F-07 no vuelva (hoy la regresión de F-07 es manual; la de F-01 ya es automatizada).
- Usar `docs/CHECKLIST_PR.md` en cada PR.
- Mover la configuración de Nginx a un repo para que también pase por revisión.
