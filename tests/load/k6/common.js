// Configuración común de las pruebas de carga (k6).
//
// Variables de entorno (se pasan con -e NOMBRE=valor o las pone run_load_tests.ps1):
//   BASE_URL       URL del backend, sin /api (por defecto http://localhost:8080)
//   TEST_EMAIL     email de un usuario de pruebas (NO el admin) con al menos una lista con ítems
//   TEST_PASSWORD  su contraseña
//   PROFILE        smoke | load | stress   (por defecto: load)
//   VUS            usuarios virtuales de la meseta (por defecto 20)
//   SPIKE_VUS      usuarios virtuales del pico en el perfil stress (por defecto 50)
//   HOLD           duración de la meseta (por defecto 5m)
//   RESULTS_DIR    carpeta donde se guarda el JSON del resumen (por defecto ../results)

import http from 'k6/http';
import { check, fail } from 'k6';

export const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
export const API = `${BASE_URL}/api`;
const RESULTS_DIR = __ENV.RESULTS_DIR || '../results';

const VUS = Number(__ENV.VUS || 20);
const SPIKE_VUS = Number(__ENV.SPIKE_VUS || 50);
const HOLD = __ENV.HOLD || '5m';

/**
 * Perfiles de carga:
 * - smoke: 1 usuario 30 s, para comprobar que el script funciona antes de cargar el servidor.
 * - load: subida de 0 a VUS en 1 min (calentamiento de la JVM y del pool de conexiones),
 *   meseta de VUS durante HOLD y bajada en 30 s.
 * - stress: como load y además un pico de SPIKE_VUS durante 1 min.
 */
export function stagesFor(profile = __ENV.PROFILE || 'load') {
  switch (profile) {
    case 'smoke':
      return [{ duration: '30s', target: 1 }];
    case 'stress':
      return [
        { duration: '1m', target: VUS },
        { duration: HOLD, target: VUS },
        { duration: '10s', target: SPIKE_VUS },
        { duration: '1m', target: SPIKE_VUS },
        { duration: '30s', target: 0 },
      ];
    default:
      return [
        { duration: '1m', target: VUS },
        { duration: HOLD, target: VUS },
        { duration: '30s', target: 0 },
      ];
  }
}

/** Opciones comunes: percentiles que se guardan en el resumen y umbrales informativos. */
export function baseOptions() {
  return {
    stages: stagesFor(),
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    thresholds: {
      // No paran la prueba: marcan en el resumen si se cumplen los objetivos
      http_req_failed: ['rate<0.01'],
      http_req_duration: ['p(95)<1000'],
    },
  };
}

/** Inicia sesión con el usuario de pruebas y devuelve { token, userId }. */
export function loginTestUser() {
  const email = __ENV.TEST_EMAIL;
  const password = __ENV.TEST_PASSWORD;
  if (!email || !password) fail('Faltan TEST_EMAIL y TEST_PASSWORD');

  const res = http.post(`${API}/auth/login`, JSON.stringify({ identifier: email, password }), {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'setup-login' },
    timeout: '120s', // Render free: la primera petición puede despertar el contenedor (~1 min)
  });
  if (res.status !== 200) fail(`Login del usuario de pruebas falló: ${res.status} ${res.body}`);
  const body = res.json();
  return { token: body.token, userId: body.user.id };
}

export function authHeaders(token) {
  return { headers: { Authorization: `Bearer ${token}` } };
}

export function expectOk(res, name) {
  return check(res, { [`${name}: 200`]: (r) => r.status === 200 });
}

/**
 * Guarda el resumen completo en JSON (lo lee summarize_k6.py) y escribe por consola
 * las cifras principales. `scenario` da nombre al fichero.
 */
export function summaryWriter(scenario) {
  return function handleSummary(data) {
    const stamp = new Date().toISOString().replace(/[:.]/g, '-');
    const m = data.metrics;
    const d = m.http_req_duration ? m.http_req_duration.values : {};
    const reqs = m.http_reqs ? m.http_reqs.values : {};
    const failed = m.http_req_failed ? m.http_req_failed.values.rate : 0;
    const lines = [
      `Escenario: ${scenario}  (perfil ${__ENV.PROFILE || 'load'}, BASE_URL ${BASE_URL})`,
      `Peticiones: ${reqs.count}   Rendimiento: ${(reqs.rate || 0).toFixed(1)} req/s`,
      `Tiempo de respuesta (ms): media ${(d.avg || 0).toFixed(0)} | mediana ${(d.med || 0).toFixed(0)} | p95 ${(d['p(95)'] || 0).toFixed(0)} | p99 ${(d['p(99)'] || 0).toFixed(0)} | máx ${(d.max || 0).toFixed(0)}`,
      `Errores HTTP: ${(failed * 100).toFixed(2)} %   VUs máx: ${m.vus_max ? m.vus_max.values.max : '?'}`,
      '',
    ];
    return {
      [`${RESULTS_DIR}/k6_${scenario}_${stamp}.json`]: JSON.stringify(
        { scenario, profile: __ENV.PROFILE || 'load', baseUrl: BASE_URL, finishedAt: new Date().toISOString(), ...data },
        null,
        2
      ),
      stdout: lines.join('\n'),
    };
  };
}
