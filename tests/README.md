# Pruebas de sistema, de carga y de usabilidad

Herramientas para obtener los datos **reales** de las pruebas de sistema, de carga y de usabilidad. Las pruebas unitarias están con el código (`src/test`, `frontend/src/__tests__`).

```
tests/
├── system/       pruebas de sistema: script automático contra la API + plantilla para el APK
├── load/         pruebas de carga con k6 (catálogo, recomendaciones, login) y resumen
└── usability/    protocolo, consentimiento, tareas, cuestionario SUS, plantillas y análisis
```

Cada herramienta guarda lo que mide en su carpeta `results/` con la fecha (ignorada por git). Esos ficheros son la evidencia de los resultados: no hay que copiar números a mano.

## Requisitos

| Herramienta | Para qué | Instalación |
|---|---|---|
| Python 3.10+ con `requests` | pruebas de sistema y análisis | `pip install requests` (ya lo usan los scripts de `scripts/`) |
| k6 (v2.3.0 instalado en `C:\Program Files\k6`) | pruebas de carga | `winget install GrafanaLabs.k6` |
| APK de `preview` (EAS) | pruebas manuales y de usabilidad | `eas build -p android --profile preview` (ver README §Despliegue) |

Antes de nada, crea desde la app un **usuario de pruebas** normal (no el admin), con sus favoritos y al menos una lista con varias canciones, lecciones y algún género. Úsalo en todos los scripts.

## 1. Pruebas de sistema

**a) Automáticas contra la API** (la parte de servidor de cada PA):

```powershell
python tests/system/api_system_tests.py --base-url https://<servicio>.onrender.com `
    --email pruebas@correo.com --password '<contraseña>' `
    --admin-email <ADMIN_EMAIL> --admin-password '<ADMIN_PASSWORD>'
```

28 comprobaciones (SIS-01…SIS-28): health check y arranque en frío, login (PA-02), seguridad (RNF-03), catálogo (PA-04), listas (PA-07, PA-08), recomendaciones (PA-09, PA-14, PA-15), permisos de admin (PA-13), registro (PA-01) y recuperación de contraseña (PA-16). Crea una lista temporal y la borra; con el admin crea una canción de prueba (id 990001) y la borra. Guarda `results/sistema_<fecha>.csv` y `.md` (tabla de resultados).

**b) Manuales en el APK** (la parte de interfaz): `system/plan_pruebas_manuales.csv` (se abre con Excel). Una fila por PA-01…PA-16 y por RNF; se rellenan "Resultado obtenido", "Estado", fecha, dispositivo, versión del APK y la captura o vídeo de evidencia. La columna "Prueba API relacionada" enlaza con los SIS-xx.

## 2. Pruebas de carga

```powershell
# Comprobar que todo funciona (1 usuario, 30 s por escenario)
.\tests\load\run_load_tests.ps1 -BaseUrl https://<servicio>.onrender.com -Email pruebas@correo.com -Password '<contraseña>' -Profile smoke

# Carga: 0→20 usuarios virtuales en 1 min, 5 min a 20, bajada en 30 s (por escenario)
.\tests\load\run_load_tests.ps1 -BaseUrl https://<servicio>.onrender.com -Email pruebas@correo.com -Password '<contraseña>' -Profile load -Vus 20

# Estrés: lo anterior y un pico de 50 usuarios durante 1 min
.\tests\load\run_load_tests.ps1 ... -Profile stress -Vus 20 -SpikeVus 50
```

- Escenarios: `catalog.js` (GET /api/items), `recommendations.js` ("Para ti" del Inicio y de una lista), `login.js` (BCrypt + JWT). Cada usuario virtual espera 1 s entre peticiones.
- El script despierta antes el backend y anota cuánto tardó en `results/health_checks.csv` (el arranque en frío de Render).
- Al acabar, `summarize_k6.py` escribe `results/resumen_carga.md` con la tabla de resultados: peticiones, rendimiento (req/s), media, mediana, p95, p99, máximo y tasa de errores.
- Render gratuito tiene 0,1 CPU y 512 MB: empieza con 10–20 usuarios virtuales. Si la tasa de errores pasa del 1 % o el p95 se dispara, ese es el límite del plan, y es un resultado válido. Para comparar, se puede repetir contra el backend en local (`-BaseUrl http://localhost:8080`).
- No ejecutes la prueba de estrés contra producción mientras alguien usa la app.

## 3. Prueba de usabilidad

Se sigue el protocolo de usabilidad (diseño, consentimiento, guion del moderador, 8 tareas, cuestionario de perfil, SUS en español y preguntas abiertas), que se guarda fuera del repositorio junto a los resultados.

1. Por cada participante, rellena `usability/plantillas/perfil_participantes.csv` y `observacion_tareas.csv` (éxito, tiempo, errores, ayudas) mientras hace las tareas.
2. Al final, el SUS: con el [formulario SUS de TOCO](https://claude.ai/artifact/WbtESYnX7vV2F3JSGHcWAQ) en el móvil del moderador ("Copiar CSV" o "Descargar CSV" en "Resultados"), en papel (pasarlo a `respuestas_sus.csv`) o con Google o Microsoft Forms (exportar a CSV).
3. Analiza:

```powershell
python tests/usability/analyze_usability.py `
    --perfil tests/usability/plantillas/perfil_participantes.csv `
    --tareas tests/usability/plantillas/observacion_tareas.csv `
    --sus tests/usability/plantillas/respuestas_sus.csv     # o el CSV exportado del formulario
```

Genera `usability/results/usabilidad.md`: perfil, tabla por tarea (éxito, tiempos, errores, ayudas) y SUS por participante con media, desviación típica, IC 95 %, nota de Sauro y Lewis y adjetivo de Bangor et al.

## 4. Pruebas unitarias

```powershell
./mvnw test                         # backend: informe de cobertura en target/site/jacoco/index.html
cd frontend; npm run test:coverage  # frontend: resumen de cobertura al final
```
