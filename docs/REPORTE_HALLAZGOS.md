# Reporte de hallazgos — DAST, validación manual y corrección

Cubre los pasos 5-7 del laboratorio: DAST pasivo, validación manual de un
hallazgo con hipótesis formal, y corrección con evidencia antes/después.
Contexto de alcance, activos y herramientas en `docs/SECURITY.md`.

## 1. DAST pasivo (OWASP ZAP Baseline)

**Staging usado**: entorno local propio, Nginx HTTPS (self-signed) en
`https://localhost` frente al backend (`localhost:8081`) y Postgres en
Docker — datos ficticios, ningún dato real.

**Por qué no se usó `dast-staging.yml` + `STAGING_URL` en GitHub Actions**:
ese workflow corre en un runner de GitHub en la nube, que no puede alcanzar
`localhost` ni la IP de LAN de esta máquina. El enunciado contempla
explícitamente esta situación ("si no hay staging, ejecute baseline local
sobre localhost con Docker y documente la alternativa") — esa es la ruta que
se siguió.

**Comando ejecutado**:
```bash
docker run --network host -v "$(pwd):/zap/wrk/:rw" -t zaproxy/zap-stable \
  zap-baseline.py -t https://localhost -r zap-report.html -J zap-report.json -I
```

**Resultado**: `0 FAIL-NEW · 7 WARN-NEW · 61 PASS` sobre 5 URLs. Reporte
completo en `docs/evidence/zap-baseline-report.html` (y `.json`).

**Limitación de cobertura (honesta, no se oculta)**: el spider pasivo de ZAP
solo descubrió 5 URLs (`/`, `/robots.txt`, `/sitemap.xml`, el bundle JS, y
una variante) porque esta es una SPA con rutas de React Router — ZAP no
ejecuta el JS del cliente para navegar a `/login`, `/devices`, `/scripts`, ni
conoce las rutas de la API (`/api/...`) que solo se invocan vía `fetch` desde
el bundle. El scan cubre la superficie estática servida por Nginx, no las
rutas internas de la SPA ni los endpoints del backend.

**Los 7 WARN**:
| Regla ZAP | Hallazgo | Prioridad |
|---|---|---|
| 10035 | Strict-Transport-Security Header Not Set | **Alta — elegido para validar/corregir (ver abajo, F-07)** |
| 10055 | CSP: Failure to Define Directive with No Fallback | Media — nuestra CSP ya tiene `default-src 'self'` pero faltan directivas explícitas (`img-src`, `frame-ancestors`, etc.) |
| 10063 | Permissions Policy Header Not Set | Media |
| 90004 | Cross-Origin-Embedder-Policy Header Missing or Invalid | Baja (no servimos recursos cross-origin embebidos) |
| 10015 | Re-examine Cache-control Directives | Baja |
| 10049 | Storable and Cacheable Content | Baja (assets estáticos versionados, es esperado) |
| 10109 | Modern Web Application | Informativo, no accionable |

## 2. Hipótesis de seguridad y validación manual (paso 6)

**Hipótesis**: *"El backend no envía el header `Strict-Transport-Security`
aun estando siempre detrás de Nginx con HTTPS."* — mismo tipo de hallazgo que
ZAP marcó en el frontend (regla 10035), pero investigado directamente en el
backend por su causa de código.

**Validación** (petición inocua, sin tocar datos ni terceros):
```bash
curl -sk -D - -o /dev/null -X POST https://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123!"}' | grep -i strict-transport
# (sin salida → header ausente, confirmado)
```

**Causa raíz**: Spring Security trae por defecto un `HstsHeaderWriter` que
solo agrega el header si `request.isSecure()` es `true`. Nginx habla con el
backend por `http://localhost:8081` (plano) y reenvía `X-Forwarded-Proto:
https`, pero Spring no confía en ese header a menos que se le diga
explícitamente — por eso `isSecure()` daba `false` y el writer nunca se
activaba, pese a que el usuario final sí navega por HTTPS.

**Reproducible**: sí, 100% determinista, confirmado dos veces.

## 3. Corrección y prueba de regresión (paso 7)

**Cambio** (pequeño, rastreable, un archivo): `secure-re-backend/src/main/resources/application.yml`
```yaml
server:
  port: 8081
  forward-headers-strategy: framework
```
Con esto Spring interpreta `X-Forwarded-Proto`/`X-Forwarded-For` del proxy,
reconoce la conexión como HTTPS, y el `HstsHeaderWriter` ya incluido por
Spring Security se activa solo — no hace falta escribir el header a mano.

**Regresión (después del fix, mismo comando)**:
```bash
curl -sk -D - -o /dev/null -X POST https://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123!"}' | grep -i strict-transport
# Strict-Transport-Security: max-age=31536000 ; includeSubDomains
```
✅ Header presente. Verificado reiniciando el backend con el cambio aplicado.

**Lo que falta para cerrar el hallazgo completo**: ZAP escaneó las páginas
*estáticas* servidas directamente por Nginx (no pasan por el backend), que
necesitan el mismo header a nivel de Nginx. Ese archivo (`/etc/nginx/nginx.conf`
en el servidor) no está versionado en ningún repo de este laboratorio, así
que no se puede "commitear" — se aplicó manualmente:
```bash
sudo sed -i '/X-Content-Type-Options "nosniff" always;/a\        add_header Strict-Transport-Security "max-age=63072000; includeSubDomains" always;' /etc/nginx/nginx.conf
sudo nginx -t && sudo systemctl reload nginx
```
y se actualizó `nginx.txt` (la plantilla de referencia del laboratorio) con
el mismo header para que quede documentado para el próximo despliegue.

**Aplicado y verificado en el servidor real** (2026-11-02):
```bash
curl -sk -D - -o /dev/null https://localhost/ | grep -i strict-transport
# Strict-Transport-Security: max-age=63072000; includeSubDomains
```
✅ Header presente tanto en el backend (API) como en el sitio estático
(Nginx).

**Re-escaneo con ZAP (evidencia antes/después completa)**:

| | Antes | Después |
|---|---|---|
| Regla 10035 (HSTS) | `WARN-NEW` | `PASS` |
| Totales | 0 FAIL · 7 WARN · 61 PASS | 0 FAIL · **6 WARN** · **62 PASS** |
| Reporte | `evidence/zap-baseline-report.html` | `evidence/zap-baseline-report-after.html` |

**F-07 cerrado por completo** en `docs/SECURITY.md`, con prueba de regresión
real (curl + reescaneo ZAP), no solo el fix sin verificar.

## Limitaciones de este laboratorio

- El spider pasivo de ZAP no cubre rutas SPA ni endpoints de API (ver nota en
  sección 1) — para cobertura completa haría falta un spider autenticado/AJAX
  o apuntar ZAP directo a cada endpoint conocido.
- No se corrió DAST activo (fuera del alcance autorizado del laboratorio).
- El fix de Nginx no quedó en un PR porque la config de Nginx no vive en
  ningún repo de este proyecto — es una limitación de infraestructura, no
  del código de la app.
