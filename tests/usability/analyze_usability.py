"""Analiza los datos de la prueba de usabilidad y genera las tablas.

Entradas (CSV con ; o , como separador, UTF-8; las plantillas están en plantillas/):
  --sus           respuestas del SUS: columnas p1..p10, o un CSV exportado de Google/Microsoft
                  Forms cuyas preguntas empiecen por "1." ... "10."
  --tareas        observación de tareas: participante, tarea, completada (1 / 0.5 / 0),
                  tiempo_s, errores, ayudas, observaciones
  --perfil        perfil de los participantes (opcional)

Salida: results/usabilidad.md (y se imprime por pantalla).

Uso:
    python tests/usability/analyze_usability.py \
        --sus tests/usability/plantillas/respuestas_sus.csv \
        --tareas tests/usability/plantillas/observacion_tareas.csv \
        --perfil tests/usability/plantillas/perfil_participantes.csv

Referencias de la interpretación:
  - SUS: J. Brooke (1996).
  - Notas A+..F: escala curvada de J. Sauro y J. R. Lewis (2016).
  - Adjetivos: A. Bangor, P. Kortum y J. Miller (2009).
"""

import argparse
import csv
import math
import re
import statistics
import sys
from collections import Counter, defaultdict
from pathlib import Path

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

RESULTS_DIR = Path(__file__).parent / 'results'

# Escala curvada de Sauro y Lewis (2016): (mínimo, nota)
SAURO_LEWIS = [(84.1, 'A+'), (80.8, 'A'), (78.9, 'A-'), (77.2, 'B+'), (74.1, 'B'), (72.6, 'B-'),
               (71.1, 'C+'), (65.0, 'C'), (62.7, 'C-'), (51.7, 'D'), (0.0, 'F')]

# Media del SUS asociada a cada adjetivo en Bangor, Kortum y Miller (2009)
BANGOR = [(12.5, 'Lo peor imaginable'), (20.3, 'Horrible'), (35.7, 'Pobre'), (50.9, 'Aceptable (OK)'),
          (71.4, 'Buena'), (85.5, 'Excelente'), (90.9, 'La mejor imaginable')]

# t de Student bilateral al 95 % para gl = 1..30
T95 = [12.706, 4.303, 3.182, 2.776, 2.571, 2.447, 2.365, 2.306, 2.262, 2.228, 2.201, 2.179, 2.160,
       2.145, 2.131, 2.120, 2.110, 2.101, 2.093, 2.086, 2.080, 2.074, 2.069, 2.064, 2.060, 2.056,
       2.052, 2.048, 2.045, 2.042]


def read_csv(path):
    text = Path(path).read_text(encoding='utf-8-sig')
    dialect = csv.Sniffer().sniff(text.splitlines()[0], delimiters=';,\t')
    return list(csv.DictReader(text.splitlines(), dialect=dialect))


def num(value):
    if value is None:
        return None
    value = str(value).strip().replace(',', '.')
    if not value:
        return None
    match = re.match(r'^-?\d+(\.\d+)?', value)
    return float(match.group()) if match else None


def fmt(x, decimals=1):
    return f'{x:.{decimals}f}'.replace('.', ',')


def sus_columns(header):
    """Columnas de las 10 afirmaciones: p1..p10 o las que empiezan por "1." ... "10."."""
    cols = {}
    for name in header:
        key = name.strip().lower()
        m = re.match(r'^p(\d{1,2})$', key) or re.match(r'^(\d{1,2})[.)\-\s]', key)
        if m and 1 <= int(m.group(1)) <= 10 and int(m.group(1)) not in cols:
            cols[int(m.group(1))] = name
    if len(cols) != 10:
        sys.exit(f'No se encuentran las 10 preguntas del SUS en las columnas: {header}')
    return [cols[i] for i in range(1, 11)]


def participant_id(row, index):
    for key in row:
        if key and re.search(r'participante|c[oó]digo', key, re.I) and row[key].strip():
            return row[key].strip()
    return f'P{index}'


def sus_score(answers):
    total = sum((a - 1) if i % 2 == 0 else (5 - a) for i, a in enumerate(answers))
    return total * 2.5


def grade(score):
    return next(g for minimum, g in SAURO_LEWIS if score >= minimum)


def adjective(score):
    return min(BANGOR, key=lambda b: abs(b[0] - score))[1]


def ci95(values):
    n = len(values)
    if n < 2:
        return None
    sd = statistics.stdev(values)
    t = T95[n - 2] if n - 1 <= len(T95) else 1.96
    half = t * sd / math.sqrt(n)
    mean = statistics.mean(values)
    return max(0.0, mean - half), min(100.0, mean + half)


def analyze_sus(path, out):
    rows = read_csv(path)
    cols = sus_columns(list(rows[0].keys()))
    scores = []
    out.append('### Cuestionario SUS\n')
    out.append('| Participante | ' + ' | '.join(f'A{i}' for i in range(1, 11)) + ' | Puntuación SUS |')
    out.append('|---|' + '---|' * 11)
    for i, row in enumerate(rows, start=1):
        answers = [num(row[c]) for c in cols]
        if any(a is None for a in answers):
            continue  # fila de la plantilla sin rellenar
        if any(a < 1 or a > 5 for a in answers):
            sys.exit(f'Respuesta fuera de 1..5 en la fila {i}: {answers}')
        score = sus_score([int(a) for a in answers])
        scores.append(score)
        out.append(f'| {participant_id(row, i)} | ' + ' | '.join(str(int(a)) for a in answers) + f' | {fmt(score)} |')

    if not scores:
        out.append('\n*(Sin respuestas todavía.)*\n')
        return
    mean = statistics.mean(scores)
    out.append('')
    out.append(f'- Participantes: **{len(scores)}**')
    out.append(f'- Media SUS: **{fmt(mean)}** sobre 100 (mediana {fmt(statistics.median(scores))}, '
               f'mín. {fmt(min(scores))}, máx. {fmt(max(scores))})')
    if len(scores) > 1:
        low, high = ci95(scores)
        out.append(f'- Desviación típica: {fmt(statistics.stdev(scores))}; IC 95 %: [{fmt(low)}, {fmt(high)}]')
    out.append(f'- Nota según la escala curvada de Sauro y Lewis: **{grade(mean)}** '
               f'({"por encima" if mean > 68 else "por debajo"} de la media de referencia, 68)')
    out.append(f'- Adjetivo más cercano según Bangor, Kortum y Miller: **{adjective(mean)}**')
    comments = [row.get('comentarios', '').strip() for row in rows if row.get('comentarios', '').strip()]
    if comments:
        out.append('\nComentarios:\n')
        out.extend(f'- "{c}"' for c in comments)
    out.append('')


def analyze_tasks(path, out):
    rows = read_csv(path)
    by_task = defaultdict(list)
    for row in rows:
        done = num(row.get('completada'))
        if done is None:
            continue  # tarea sin rellenar o "no aplica"
        by_task[row['tarea'].strip()].append({
            'done': done, 'time': num(row.get('tiempo_s')),
            'errors': num(row.get('errores')) or 0, 'helps': num(row.get('ayudas')) or 0,
        })
    out.append('### Métricas por tarea\n')
    if not by_task:
        out.append('*(Sin observaciones todavía.)*\n')
        return
    out.append('| Tarea | n | Éxito sin ayuda | Éxito (incl. parcial) | Tiempo medio | Tiempo mediano | Errores medios | Ayudas medias |')
    out.append('|---|---|---|---|---|---|---|---|')
    for task in sorted(by_task, key=lambda t: (len(t), t)):
        items = by_task[task]
        n = len(items)
        full = sum(1 for x in items if x['done'] >= 1) / n * 100
        partial = sum(x['done'] for x in items) / n * 100
        times = [x['time'] for x in items if x['time'] is not None]
        mean_t = f'{fmt(statistics.mean(times), 0)} s' if times else '—'
        med_t = f'{fmt(statistics.median(times), 0)} s' if times else '—'
        out.append(f'| {task} | {n} | {fmt(full, 0)} % | {fmt(partial, 0)} % | {mean_t} | {med_t} | '
                   f'{fmt(statistics.mean(x["errors"] for x in items))} | {fmt(statistics.mean(x["helps"] for x in items))} |')
    out.append('\n"Éxito sin ayuda": completada = 1. "Incl. parcial": completada = 0,5 cuenta como media tarea.\n')


def analyze_profile(path, out):
    rows = [r for r in read_csv(path) if (r.get('nivel') or '').strip()]
    out.append('### Perfil de los participantes\n')
    if not rows:
        out.append('*(Sin datos todavía.)*\n')
        return
    out.append(f'- Participantes: {len(rows)}')
    for field, label in [('nivel', 'Nivel'), ('anos_tocando', 'Años tocando'), ('edad', 'Edad'),
                         ('formacion_reglada', 'Formación reglada'), ('usa_spotify', 'Usa Spotify')]:
        counts = Counter((r.get(field) or '').strip() or 'sin dato' for r in rows)
        out.append(f'- {label}: ' + ', '.join(f'{k} ({v})' for k, v in counts.most_common()))
    instruments = Counter(i.strip().lower() for r in rows for i in re.split(r'[,/+]', r.get('instrumentos') or '') if i.strip())
    if instruments:
        out.append('- Instrumentos: ' + ', '.join(f'{k} ({v})' for k, v in instruments.most_common()))
    out.append('')


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument('--sus')
    p.add_argument('--tareas')
    p.add_argument('--perfil')
    args = p.parse_args()
    if not (args.sus or args.tareas or args.perfil):
        p.error('indica al menos --sus, --tareas o --perfil')

    out = ['## Resultados de la prueba de usabilidad\n']
    if args.perfil:
        analyze_profile(args.perfil, out)
    if args.tareas:
        analyze_tasks(args.tareas, out)
    if args.sus:
        analyze_sus(args.sus, out)

    RESULTS_DIR.mkdir(exist_ok=True)
    report = '\n'.join(out)
    (RESULTS_DIR / 'usabilidad.md').write_text(report + '\n', encoding='utf-8')
    print(report)
    print(f'\nGuardado en {RESULTS_DIR / "usabilidad.md"}')


if __name__ == '__main__':
    main()
