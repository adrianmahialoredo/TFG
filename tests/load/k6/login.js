// Escenario 3: inicio de sesión (BCrypt + firma del JWT), CPU intensivo a propósito.
// POST /api/auth/login con el usuario de pruebas.
//
// k6 run -e BASE_URL=... -e TEST_EMAIL=... -e TEST_PASSWORD=... login.js

import http from 'k6/http';
import { check, sleep } from 'k6';
import { API, baseOptions, summaryWriter } from './common.js';

export const options = baseOptions();

const body = JSON.stringify({ identifier: __ENV.TEST_EMAIL, password: __ENV.TEST_PASSWORD });
const params = { headers: { 'Content-Type': 'application/json' }, tags: { name: 'POST /api/auth/login' } };

export default function () {
  const res = http.post(`${API}/auth/login`, body, params);
  check(res, {
    'login: 200': (r) => r.status === 200,
    'login: devuelve token': (r) => r.status === 200 && Boolean(r.json('token')),
  });
  sleep(1);
}

export const handleSummary = summaryWriter('login');
