"""Resume los JSON de k6 (tests/load/results/k6_*.json) en una tabla Markdown y CSV.

Para cada escenario coge la ejecución más reciente (o todas con --all) y saca las mismas
métricas que la tabla 7.3.3 de la memoria: peticiones, rendimiento, media, p95, p99,
máximo y tasa de errores. Escribe resumen_carga.md y resumen_carga.csv en la misma carpeta.

Uso:
    python tests/load/summarize_k6.py [carpeta_resultados] [--all]
"""

import csv
import json
import sys
from pathlib import Path

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

NAMES = {'catalogo': 'Catálogo (GET /api/items)',
         'recomendaciones': 'Recomendaciones (Para ti)',
         'login': 'Login / JWT'}


def load_runs(folder: Path):
    runs = []
    for path in sorted(folder.glob('k6_*.json')):
        try:
            data = json.loads(path.read_text(encoding='utf-8'))
        except (OSError, json.JSONDecodeError) as e:
            print(f'Se ignora {path.name}: {e}')
            continue
        runs.append((path, data))
    return runs


def metric(data, name, stat, default=0.0):
    return data.get('metrics', {}).get(name, {}).get('values', {}).get(stat, default)


def row(path: Path, data: dict) -> dict:
    duration_s = data.get('state', {}).get('testRunDurationMs', 0) / 1000
    return {
        'escenario': NAMES.get(data.get('scenario'), data.get('scenario')),
        'perfil': data.get('profile'),
        'fecha': data.get('finishedAt', '')[:19].replace('T', ' '),
        'vus_max': int(metric(data, 'vus_max', 'max')),
        'duracion_s': round(duration_s),
        'peticiones': int(metric(data, 'http_reqs', 'count')),
        'req_s': round(metric(data, 'http_reqs', 'rate'), 1),
        'media_ms': round(metric(data, 'http_req_duration', 'avg')),
        'mediana_ms': round(metric(data, 'http_req_duration', 'med')),
        'p95_ms': round(metric(data, 'http_req_duration', 'p(95)')),
        'p99_ms': round(metric(data, 'http_req_duration', 'p(99)')),
        'max_ms': round(metric(data, 'http_req_duration', 'max')),
        'errores_pct': round(100 * metric(data, 'http_req_failed', 'rate'), 2),
        'fichero': path.name,
    }


def main() -> int:
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    folder = Path(args[0]) if args else Path(__file__).parent / 'results'
    runs = load_runs(folder)
    if not runs:
        print(f'No hay resultados k6_*.json en {folder}')
        return 1

    rows = [row(p, d) for p, d in runs]
    if '--all' not in sys.argv:
        latest = {}
        for r in rows:  # ordenados por nombre (incluye la fecha): gana el último
            latest[(r['escenario'], r['perfil'])] = r
        rows = list(latest.values())
    order = list(NAMES.values())
    rows.sort(key=lambda r: (order.index(r['escenario']) if r['escenario'] in order else 99, r['perfil'], r['fecha']))

    with open(folder / 'resumen_carga.csv', 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=list(rows[0].keys()), delimiter=';')
        writer.writeheader()
        writer.writerows(rows)

    fmt = lambda n: f'{n:,}'.replace(',', '.')
    lines = ['| Métrica | ' + ' | '.join(r['escenario'] for r in rows) + ' |',
             '|---|' + '---|' * len(rows)]
    for label, key, unit in [
        ('Perfil / usuarios virtuales máx.', None, ''),
        ('Duración de la prueba', 'duracion_s', ' s'),
        ('Peticiones totales', 'peticiones', ''),
        ('Rendimiento medio', 'req_s', ' req/s'),
        ('Tiempo de respuesta medio', 'media_ms', ' ms'),
        ('Mediana', 'mediana_ms', ' ms'),
        ('Percentil 95', 'p95_ms', ' ms'),
        ('Percentil 99', 'p99_ms', ' ms'),
        ('Máximo', 'max_ms', ' ms'),
        ('Tasa de errores HTTP', 'errores_pct', ' %'),
    ]:
        if key is None:
            cells = [f"{r['perfil']} / {r['vus_max']}" for r in rows]
        else:
            cells = [(fmt(r[key]) if isinstance(r[key], int) else str(r[key]).replace('.', ',')) + unit for r in rows]
        lines.append(f'| {label} | ' + ' | '.join(cells) + ' |')
    lines.append('')
    lines.append('Fechas de ejecución: ' + '; '.join(f"{r['escenario']}: {r['fecha']} UTC" for r in rows))

    table = '\n'.join(lines)
    (folder / 'resumen_carga.md').write_text(table + '\n', encoding='utf-8')
    print(table)
    print(f'\nGuardado en {folder / "resumen_carga.md"} y resumen_carga.csv')
    return 0


if __name__ == '__main__':
    sys.exit(main())
