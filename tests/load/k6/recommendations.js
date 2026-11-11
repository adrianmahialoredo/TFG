// Escenario 2: recomendaciones (el cálculo más costoso del backend).
//   GET /api/recommendations/items/by-user/{id}        carrusel "Para ti" del Inicio
//   GET /api/recommendations/items/by-item-list/{id}   "Para ti" al añadir ítems a una lista
//
// El usuario de pruebas debe tener al menos una lista (mejor con varios ítems).
// k6 run -e BASE_URL=... -e TEST_EMAIL=... -e TEST_PASSWORD=... recommendations.js

import http from 'k6/http';
import { fail, sleep } from 'k6';
import { API, authHeaders, baseOptions, expectOk, loginTestUser, summaryWriter } from './common.js';

export const options = baseOptions();

export function setup() {
  const session = loginTestUser();
  const res = http.get(`${API}/item-lists/by-user/${session.userId}/ids`, authHeaders(session.token));
  const listIds = res.status === 200 ? res.json() : [];
  if (!listIds.length) fail('El usuario de pruebas no tiene listas: crea una con ítems desde la app');
  return { ...session, listId: listIds[0] };
}

export default function (session) {
  const params = authHeaders(session.token);
  expectOk(
    http.get(`${API}/recommendations/items/by-user/${session.userId}`, {
      ...params,
      tags: { name: 'GET /api/recommendations/items/by-user/{id}' },
    }),
    'para ti (inicio)'
  );
  sleep(1);
  expectOk(
    http.get(`${API}/recommendations/items/by-item-list/${session.listId}`, {
      ...params,
      tags: { name: 'GET /api/recommendations/items/by-item-list/{id}' },
    }),
    'para ti (lista)'
  );
  sleep(1);
}

export const handleSummary = summaryWriter('recomendaciones');
