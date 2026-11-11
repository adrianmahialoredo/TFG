// Escenario 1: descarga del resumen del catálogo (GET /api/items).
// Es la petición más pesada en bytes: la app la hace al iniciar sesión para precargar el catálogo.
//
// k6 run -e BASE_URL=... -e TEST_EMAIL=... -e TEST_PASSWORD=... catalog.js

import http from 'k6/http';
import { sleep } from 'k6';
import { API, authHeaders, baseOptions, expectOk, loginTestUser, summaryWriter } from './common.js';

export const options = baseOptions();

export function setup() {
  return loginTestUser();
}

export default function (session) {
  const res = http.get(`${API}/items`, { ...authHeaders(session.token), tags: { name: 'GET /api/items' } });
  expectOk(res, 'catálogo');
  sleep(1); // tiempo de "lectura" del usuario entre peticiones
}

export const handleSummary = summaryWriter('catalogo');
