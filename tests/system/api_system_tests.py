"""Pruebas de sistema automáticas contra la API REST desplegada (Render + Neon) o local.

Ejecutan, de extremo a extremo y sin mocks, la parte de servidor de las pruebas de aceptación
(PA-xx de la memoria) y guardan el resultado REAL de cada una (código HTTP, tiempo, observación)
en tests/system/results/sistema_<fecha>.csv y .md, listo para la tabla 7.2.

La parte de interfaz de cada PA (pantallas, modales, afinador con micrófono...) se prueba a mano
en el APK con la plantilla plan_pruebas_sistema.csv.

Uso (PowerShell):
    python tests/system/api_system_tests.py --base-url https://toco-backend.onrender.com `
        --email pruebas@correo.com --password 'Secreta1!' `
        [--admin-email admin@... --admin-password ...]

El usuario de pruebas debe ser un USER normal con sus favoritos (lo normal tras registrarse en la
app). Las pruebas crean una lista temporal y la borran al final. Con credenciales de admin se
prueba también la creación de una canción (PA-13), que se borra después.
"""

import argparse
import csv
import json
import sys
import time
import uuid
from datetime import datetime, timezone
from pathlib import Path

import requests

RESULTS_DIR = Path(__file__).parent / 'results'

# La consola de Windows no es UTF-8 por defecto (✓, ✗, tildes)
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')


class Runner:
    def __init__(self, base_url: str, timeout: float):
        self.api = base_url.rstrip('/') + '/api'
        self.base = base_url.rstrip('/')
        self.timeout = timeout
        self.results = []

    def call(self, method, path, token=None, body=None, base=False):
        headers = {'Content-Type': 'application/json'}
        if token:
            headers['Authorization'] = f'Bearer {token}'
        url = (self.base if base else self.api) + path
        started = time.perf_counter()
        res = requests.request(method, url, headers=headers,
                               data=json.dumps(body) if body is not None else None, timeout=self.timeout)
        elapsed_ms = (time.perf_counter() - started) * 1000
        return res, elapsed_ms

    def record(self, test_id, pa, description, expected, ok, obtained, ms=None):
        status = 'Superada' if ok else 'Fallida'
        self.results.append({
            'id': test_id, 'pa': pa, 'descripcion': description, 'esperado': expected,
            'obtenido': obtained, 'estado': status, 'tiempo_ms': '' if ms is None else round(ms),
            'fecha_utc': datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M:%S'),
        })
        mark = '✓' if ok else '✗'
        print(f'{mark} {test_id} [{pa}] {description}: {obtained}' + ('' if ms is None else f' ({ms:.0f} ms)'))

    def check(self, test_id, pa, description, expected, fn):
        """Ejecuta fn() -> (ok, obtenido, ms); una excepción cuenta como fallo."""
        try:
            ok, obtained, ms = fn()
        except Exception as e:  # noqa: BLE001 - se registra cualquier fallo
            ok, obtained, ms = False, f'Excepción: {e}', None
        self.record(test_id, pa, description, expected, ok, obtained, ms)
        return ok


def login(r: Runner, identifier, password):
    res, ms = r.call('POST', '/auth/login', body={'identifier': identifier, 'password': password})
    return res, ms


def main() -> int:
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument('--base-url', default='http://localhost:8080')
    p.add_argument('--email', required=True, help='usuario de pruebas (rol USER)')
    p.add_argument('--password', required=True)
    p.add_argument('--admin-email')
    p.add_argument('--admin-password')
    p.add_argument('--timeout', type=float, default=120, help='segundos (Render puede tardar ~1 min en despertar)')
    args = p.parse_args()

    r = Runner(args.base_url, args.timeout)
    print(f'Pruebas de sistema contra {args.base_url}\n')

    # ---------- Disponibilidad y arranque en frío ----------
    def health():
        res, ms = r.call('GET', '/actuator/health', base=True)
        state = res.json().get('status') if res.ok else res.status_code
        return res.ok and state == 'UP', f'{res.status_code} {state} (1.ª petición: incluye el arranque en frío si estaba dormido)', ms
    r.check('SIS-01', 'Despliegue', 'Health check /actuator/health', '200 UP', health)

    def health_warm():
        res, ms = r.call('GET', '/actuator/health', base=True)
        return res.ok, f'{res.status_code} en caliente', ms
    r.check('SIS-02', 'Despliegue', 'Health check ya despierto', '200 en < 1 s', health_warm)

    # ---------- CU-02 / PA-02: inicio de sesión ----------
    def wrong_password():
        res, ms = login(r, args.email, 'Contraseña-Incorrecta-1!')
        return res.status_code == 401, f'{res.status_code}', ms
    r.check('SIS-03', 'PA-02', 'Login con contraseña errónea', '401 sin revelar si la cuenta existe', wrong_password)

    def unknown_user():
        res, ms = login(r, f'no-existe-{uuid.uuid4().hex[:8]}@correo.com', 'Cualquiera1!')
        return res.status_code == 401, f'{res.status_code} (mismo código que contraseña errónea)', ms
    r.check('SIS-04', 'PA-02', 'Login con usuario inexistente', '401, igual que contraseña errónea', unknown_user)

    session = {}

    def good_login():
        res, ms = login(r, args.email, args.password)
        if res.status_code == 200:
            body = res.json()
            session.update(token=body['token'], user=body['user'])
        return res.status_code == 200 and 'token' in session, f'{res.status_code}, JWT recibido: {"token" in session}', ms
    if not r.check('SIS-05', 'PA-02', 'Login correcto con email', '200 con JWT y usuario', good_login):
        print('\nSin sesión no se pueden ejecutar el resto de pruebas.')
        return save(r)

    token, user = session['token'], session['user']
    uid = user['id']

    def login_username():
        res, ms = login(r, user['username'], args.password)
        return res.status_code == 200, f'{res.status_code}', ms
    r.check('SIS-06', 'PA-02 / RF-USR-01', 'Login con nombre de usuario', '200', login_username)

    # ---------- RNF-03: seguridad ----------
    def no_token():
        res, ms = r.call('GET', f'/users/{uid}')
        return res.status_code == 401, f'{res.status_code}', ms
    r.check('SIS-07', 'RNF-03', 'Recurso protegido sin token', '401', no_token)

    def other_user():
        res, ms = r.call('GET', f'/users/{uid + 100000}', token=token)
        return res.status_code == 403, f'{res.status_code}', ms
    r.check('SIS-08', 'RNF-03', 'Leer el perfil de otro usuario', '403', other_user)

    # ---------- CU-04: catálogo ----------
    catalog = []

    def get_catalog():
        res, ms = r.call('GET', '/items', token=token)
        if res.ok:
            catalog.extend(res.json())
        size_kb = len(res.content) / 1024
        return res.ok and len(catalog) > 0, f'{res.status_code}, {len(catalog)} ítems, {size_kb:.0f} KB', ms
    r.check('SIS-09', 'PA-04 / RF-CAT-09', 'Descarga del resumen del catálogo (precarga)', '200 con todos los ítems', get_catalog)

    by_type = {}
    for item in catalog:
        by_type.setdefault(str(item.get('type', '')).upper().replace('_', ''), []).append(item)

    # ---------- CU-07 / PA-07: crear lista ----------
    list_id = None

    def create_list():
        nonlocal list_id
        res, ms = r.call('POST', '/item-lists', token=token, body={'name': 'Rutina Diaria (prueba de sistema)', 'userId': uid})
        if res.status_code == 201:
            list_id = res.json()['id']
        return res.status_code == 201, f'{res.status_code}, id {list_id}', ms
    r.check('SIS-10', 'PA-07 / RF-LIS-01', 'Crear la lista "Rutina Diaria"', '201', create_list)

    def create_list_for_other():
        res, ms = r.call('POST', '/item-lists', token=token, body={'name': 'Ajena', 'userId': uid + 100000})
        return res.status_code == 403, f'{res.status_code}', ms
    r.check('SIS-11', 'PA-07 / RNF-03', 'Crear una lista a nombre de otro usuario', '403', create_list_for_other)

    # ---------- CU-08 / PA-08: añadir ítems heterogéneos ----------
    picks = [by_type.get(t, [None])[0] for t in ('SONG', 'LESSON', 'GENRE')]
    picks = [p for p in picks if p]
    if list_id and len(picks) == 3:
        def add_items():
            last, total = None, 0
            for item in picks:
                last, ms = r.call('POST', f'/item-lists/{list_id}/items/{item["id"]}', token=token)
                total += ms
                if last.status_code != 200:
                    return False, f'{last.status_code} al añadir {item["id"]}', total
            ids = last.json()['itemIds']
            expected = [p['id'] for p in picks]
            return ids == expected, f'200 x3, orden {ids}', total / 3
        r.check('SIS-12', 'PA-08 / RF-LIS-02', 'Añadir canción, lección y género', '200 y orden de inserción', add_items)

        def add_duplicate():
            res, ms = r.call('POST', f'/item-lists/{list_id}/items/{picks[0]["id"]}', token=token)
            ids = res.json().get('itemIds', []) if res.ok else []
            return res.ok and ids.count(picks[0]['id']) == 1 and len(ids) == 3, f'{res.status_code}, {len(ids)} ítems', ms
        r.check('SIS-13', 'PA-08 / RF-LIS-02', 'Añadir otra vez la canción', '200, sin duplicarse', add_duplicate)

        # ---------- CU-09 / PA-09: recomendaciones de la lista ----------
        def list_recs():
            res, ms = r.call('GET', f'/recommendations/items/by-item-list/{list_id}', token=token)
            recs = res.json() if res.ok else []
            in_list = {p['id'] for p in picks} & {x['id'] for x in recs}
            types = sorted({str(x.get('type')) for x in recs})
            ok = res.ok and 0 < len(recs) <= 20 and not in_list
            return ok, f'{res.status_code}, {len(recs)} ítems ({", ".join(types)}), repetidos de la lista: {len(in_list)}', ms
        r.check('SIS-14', 'PA-09 / RF-REC-01', '"Para ti" de la lista', '200, ≤ 20 ítems, ninguno de la lista', list_recs)

        def remove_item():
            res, ms = r.call('DELETE', f'/item-lists/{list_id}/items/{picks[1]["id"]}', token=token)
            ids = res.json().get('itemIds', []) if res.ok else []
            return res.ok and ids == [picks[0]['id'], picks[2]['id']], f'{res.status_code}, quedan {ids}', ms
        r.check('SIS-15', 'PA-08 / CU-08', 'Quitar la lección de la lista', '200 y el resto en orden', remove_item)
    else:
        r.record('SIS-12', 'PA-08', 'Añadir ítems a la lista', '200', False,
                 'No se pudo crear la lista o el catálogo no tiene canción, lección y género')

    # ---------- CU-09: Inicio ----------
    def home_recs():
        res, ms = r.call('GET', f'/recommendations/items/by-user/{uid}', token=token)
        recs = res.json() if res.ok else []
        return res.ok and len(recs) <= 12, f'{res.status_code}, {len(recs)} ítems', ms
    r.check('SIS-16', 'CU-09 / RF-REC-02', '"Para ti" del Inicio', '200, ≤ 12 ítems', home_recs)

    def top_artists():
        res, ms = r.call('GET', f'/users/{uid}/top-artists', token=token)
        artists = res.json() if res.ok else []
        session['artists'] = artists
        return res.ok and len(artists) <= 5, f'{res.status_code}, {len(artists)} artistas', ms
    r.check('SIS-17', 'PA-14 / RF-REC-06', 'Artistas más repetidos en las listas', '200, ≤ 5 artistas', top_artists)

    def other_top_artists():
        res, ms = r.call('GET', f'/users/{uid + 100000}/top-artists', token=token)
        return res.status_code == 403, f'{res.status_code}', ms
    r.check('SIS-18', 'PA-14 / RNF-03', 'Artistas de otro usuario', '403', other_top_artists)

    def spotify_items():
        res, ms = r.call('GET', f'/spotify/recommendations/items/{uid}', token=token)
        n = len(res.json()) if res.ok else 0
        return res.status_code in (200, 204), f'{res.status_code}, {n} ítems (vinculado: {user.get("spotifyConnected")})', ms
    r.check('SIS-19', 'PA-15 / RF-REC-05', '"Basado en tu Spotify" del Inicio', '200 (vacío si no está vinculado)', spotify_items)

    # ---------- RF-ADM-01 / PA-13 ----------
    def song_as_user():
        res, ms = r.call('POST', '/songs', token=token, body={'id': 990001, 'title': 'Prueba', 'level': 1})
        return res.status_code == 403, f'{res.status_code}', ms
    r.check('SIS-20', 'PA-13 / RF-ADM-01', 'Crear canción con un USER', '403', song_as_user)

    def song_without_token():
        res, ms = r.call('POST', '/songs', body={'id': 990001, 'title': 'Prueba', 'level': 1})
        return res.status_code == 401, f'{res.status_code}', ms
    r.check('SIS-21', 'PA-13 / RF-ADM-01', 'Crear canción sin token', '401', song_without_token)

    if args.admin_email and args.admin_password:
        def song_as_admin():
            res, _ = login(r, args.admin_email, args.admin_password)
            admin_token = res.json()['token']
            genre = by_type.get('GENRE', [{}])[0].get('id')
            song = {'id': 990001, 'title': 'Prueba de sistema PA-13', 'artist': 'TOCO', 'level': 1,
                    'decade': '2020s', 'genreIds': [genre] if genre else [], 'instrumentIds': [], 'lessonIds': []}
            res, ms = r.call('POST', '/songs', token=admin_token, body=song)
            created = res.status_code
            r.call('DELETE', '/songs/990001', token=admin_token)
            return created == 201, f'{created} (borrada después)', ms
        r.check('SIS-22', 'PA-13 / RF-ADM-01', 'Crear canción con un ADMIN', '201', song_as_admin)

    # ---------- Registro y recuperación de contraseña ----------
    def registered_email_code():
        res, ms = r.call('POST', '/auth/verification-code', body={'email': user['email']})
        return res.status_code == 400, f'{res.status_code} {res.json().get("message", "")}', ms
    r.check('SIS-23', 'PA-01 / CU-01', 'Código de registro para un email ya registrado', '400 "Email already registered"', registered_email_code)

    unknown = f'nadie-{uuid.uuid4().hex[:8]}@correo.com'

    def reset_unknown():
        res, ms = r.call('POST', '/auth/password-reset/code', body={'email': unknown})
        return res.status_code == 204, f'{res.status_code} (no revela si existe la cuenta)', ms
    r.check('SIS-24', 'PA-16 / CU-13', 'Recuperar contraseña de un email sin cuenta', '204', reset_unknown)

    def reset_too_soon():
        res, ms = r.call('POST', '/auth/password-reset/code', body={'email': unknown})
        return res.status_code == 429, f'{res.status_code}', ms
    r.check('SIS-25', 'PA-16 / CU-13', 'Pedir otro código antes de 60 s', '429', reset_too_soon)

    def reset_wrong_code():
        res, ms = r.call('POST', '/auth/password-reset/verify', body={'email': unknown, 'code': '000000'})
        return res.status_code == 400, f'{res.status_code} {res.json().get("message", "")}', ms
    r.check('SIS-26', 'PA-16 / CU-13', 'Comprobar un código que no se pidió', '400', reset_wrong_code)

    def weak_new_password():
        res, ms = r.call('POST', '/auth/password-reset', body={'email': unknown, 'code': '000000', 'newPassword': 'debil'})
        return res.status_code == 400, f'{res.status_code}', ms
    r.check('SIS-27', 'PA-16 / CU-13', 'Contraseña nueva que no cumple la política', '400', weak_new_password)

    # ---------- Limpieza ----------
    if list_id:
        def delete_list():
            res, ms = r.call('DELETE', f'/item-lists/{list_id}', token=token)
            return res.status_code == 204, f'{res.status_code}', ms
        r.check('SIS-28', 'PA-07 / CU-07', 'Borrar la lista de prueba', '204', delete_list)

    return save(r)


def save(r: Runner) -> int:
    RESULTS_DIR.mkdir(exist_ok=True)
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S')
    fields = ['id', 'pa', 'descripcion', 'esperado', 'obtenido', 'estado', 'tiempo_ms', 'fecha_utc']
    with open(RESULTS_DIR / f'sistema_{stamp}.csv', 'w', newline='', encoding='utf-8-sig') as f:
        w = csv.DictWriter(f, fieldnames=fields, delimiter=';')
        w.writeheader()
        w.writerows(r.results)

    lines = ['| ID | PA / RF | Prueba | Resultado esperado | Resultado obtenido | Tiempo | Estado |',
             '|---|---|---|---|---|---|---|']
    for x in r.results:
        t = f"{x['tiempo_ms']} ms" if x['tiempo_ms'] != '' else ''
        lines.append(f"| {x['id']} | {x['pa']} | {x['descripcion']} | {x['esperado']} | {x['obtenido']} | {t} | {x['estado']} |")
    passed = sum(1 for x in r.results if x['estado'] == 'Superada')
    lines.append(f'\n{passed} de {len(r.results)} superadas. Ejecutado el {r.results[0]["fecha_utc"] if r.results else ""} UTC contra {r.base}.')
    (RESULTS_DIR / f'sistema_{stamp}.md').write_text('\n'.join(lines) + '\n', encoding='utf-8')
    print(f'\n{passed}/{len(r.results)} superadas. Resultados en {RESULTS_DIR / f"sistema_{stamp}.md"}')
    return 0 if passed == len(r.results) else 1


if __name__ == '__main__':
    sys.exit(main())
