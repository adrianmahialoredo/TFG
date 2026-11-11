"""Export the catalog of a database to the JSON files of ``data/``.

Why this script exists
----------------------
The JSON files of ``data/`` are the source the ``insert_*.py`` scripts load
through the API. When the catalog is edited directly in a deployed database
(e.g. production in Neon), this script brings those changes back to ``data/``
so that the repository and the database hold the same catalog.

It only **reads** the database (read-only transaction) and writes, in the same
format the insert scripts expect (see ``data.md``):

    chords.json, genres.json, instrument_genres.json, instruments.json,
    lessons.json, songs.json (with their song lessons and tab sections),
    tunes.json

Users, lists, preferences and Spotify tokens are not exported (they are not
catalog data and contain personal information).

Usage
-----
    python scripts/export_db_to_json.py              # writes data/*.json
    python scripts/export_db_to_json.py --out otra/  # another folder

Connection settings as in ``migrate_preferences_table.py``: application
properties and ``.env``, overridable with ``DB_URL`` / ``DB_USER`` /
``DB_PASSWORD`` (a Neon URL without port and with ``?sslmode=require`` works).
"""

import argparse
import json
import re
import sys
from pathlib import Path

import psycopg

from console_utils import configure_console
from migrate_preferences_table import build_dsn

configure_console()

DATA_DIR = Path(__file__).resolve().parents[1] / 'data'

# Files written with one record per line (as they were written by hand)
ONE_LINE_FILES = {'chords', 'tunes'}
# A list holding only strings, numbers, booleans or null (after json.dumps with indent)
_SCALAR_LIST = re.compile(r'\[\s*((?:(?:"(?:[^"\\]|\\.)*"|-?\d+(?:\.\d+)?|true|false|null)\s*,?\s*)*)\]')


def to_json(name, data):
    """JSON in the style of data/: short lists of values on one line."""
    if name in ONE_LINE_FILES:
        lines = ['  { ' + json.dumps(record, ensure_ascii=False)[1:-1] + ' }' for record in data]
        return '[\n' + ',\n'.join(lines) + '\n]\n'
    text = json.dumps(data, ensure_ascii=False, indent=2)

    def collapse(match):
        values = [v.strip() for v in re.split(r',\s*\n\s*', match.group(1).strip()) if v.strip()]
        return '[' + ', '.join(values) + ']'

    return _SCALAR_LIST.sub(collapse, text) + '\n'


def rows(cur, query, params=()):
    cur.execute(query, params)
    names = [d.name for d in cur.description]
    return [dict(zip(names, r)) for r in cur.fetchall()]


def grouped(cur, query, key, value):
    """Map key -> list of values, keeping the order of the query."""
    result = {}
    for row in rows(cur, query):
        result.setdefault(row[key], []).append(row[value])
    return result


def text_of(cur, value):
    """Text of a @Lob String column.

    Hibernate stores a @Lob String in PostgreSQL as a large object and keeps its oid in the
    column (type ``oid``, or ``text`` holding the number when the column was created as text).
    """
    if value is None:
        return None
    if isinstance(value, str) and not value.isdigit():
        return value
    cur.execute('select convert_from(lo_get(%s::oid), %s)', (str(value), 'UTF8'))
    return cur.fetchone()[0]


def export_instruments(cur):
    return [{'id': r['id'], 'name': r['name'], 'imageUrl': r['image_url']}
            for r in rows(cur, 'select id, name, image_url from instruments order by id')]


def export_chords(cur):
    notes = grouped(cur, 'select chord_id, note from chord_notes order by chord_id, ctid', 'chord_id', 'note')
    return [{
        'id': r['id'], 'name': r['name'], 'notes': notes.get(r['id'], []), 'imageUrl': r['image_url'],
        'open': r['open'], 'barre': r['barre'], 'powerChord': r['power_chord'], 'minor': r['minor'],
        'major': r['major'], 'seventh': r['seventh'], 'capo': r['capo'], 'capoFret': r['capo_fret'],
        'instrumentId': r['instrument_id'],
    } for r in rows(cur, 'select * from chords order by id')]


def export_tunes(cur):
    notes = grouped(cur, 'select tune_id, note from tune_notes order by tune_id, string_index', 'tune_id', 'note')
    return [{'id': r['id'], 'title': r['title'], 'instrumentId': r['instrument_id'], 'notes': notes.get(r['id'], [])}
            for r in rows(cur, 'select id, title, instrument_id from tunes order by id')]


def export_genres(cur):
    return [{
        'id': r['id'], 'title': r['title'], 'level': r['level'], 'bannerUrl': r['banner_url'],
        'imageUrl': r['image_url'], 'welcomeIntroVideoId': r['welcome_intro_video'],
        'welcomeSongVideoId': r['welcome_song_video'], 'description': text_of(cur, r['description']),
    } for r in rows(cur, 'select g.*, i.title, i.level, i.image_url from genres g join item i on i.id = g.id order by g.id')]


def export_instrument_genres(cur):
    # The lessons of an itinerary are those of the join table plus those that point at it
    # (lessons.instrument_genre_id), in itinerary order. The join table only exists in databases
    # created with an older mapping.
    cur.execute("select to_regclass('public.instrument_genre_lesson') is not null")
    legacy = ('select igl.instrument_genre_id as ig_id, igl.lesson_id from instrument_genre_lesson igl union '
              if cur.fetchone()[0] else '')
    lessons = grouped(cur,
                      'select ig_id, lesson_id from (' + legacy +
                      '  select l.instrument_genre_id as ig_id, l.id as lesson_id from lessons l'
                      '  where l.instrument_genre_id is not null'
                      ') x join lessons l on l.id = x.lesson_id '
                      'order by ig_id, l.index_in_instrument_genre, l.id',
                      'ig_id', 'lesson_id')
    return [{
        'id': r['id'], 'title': r['title'], 'imageUrl': r['image_url'], 'level': r['level'],
        'instrumentId': r['instrument_id'], 'genreId': r['genre_id'], 'lessonIds': lessons.get(r['id'], []),
    } for r in rows(cur, 'select ig.*, i.title, i.level, i.image_url from instrument_genres ig '
                         'join item i on i.id = ig.id order by ig.id')]


def export_lessons(cur):
    sections = {}
    for s in rows(cur, 'select * from lesson_sections order by lesson_id, order_index, id'):
        sections.setdefault(s['lesson_id'], []).append({
            'title': s['title'], 'htmlContent': text_of(cur, s['html_content']),
            'videoUrl': s['video_url'], 'orderIndex': s['order_index'],
        })
    result = []
    for r in rows(cur, 'select l.*, i.title, i.level, i.image_url from lessons l join item i on i.id = l.id order by l.id'):
        lesson = {'id': r['id'], 'level': r['level'], 'title': r['title']}
        if r['image_url']:
            lesson['imageUrl'] = r['image_url']
        lesson.update({
            'belongsToGenre': r['belongs_to_genre'], 'genreId': r['genre_id'],
            'belongsToInstrumentGenre': r['belongs_to_instrument_genre'],
            'instrumentGenreId': r['instrument_genre_id'],
        })
        if r['belongs_to_instrument_genre']:
            lesson['indexInInstrumentGenre'] = r['index_in_instrument_genre']
        if r['instrument_id'] is not None:
            lesson['instrumentId'] = r['instrument_id']
        lesson.update({'genrePopular': r['genre_popular'], 'popular': r['popular'],
                       'sections': sections.get(r['id'], [])})
        result.append(lesson)
    return result


def export_songs(cur):
    genres = grouped(cur, 'select song_id, genre_id from song_genre order by song_id, genre_id', 'song_id', 'genre_id')
    instruments = grouped(cur, 'select song_id, instruments_id from songs_instruments order by song_id, instruments_id',
                          'song_id', 'instruments_id')
    chords = grouped(cur, 'select song_lesson_id, chord_id from song_chords order by song_lesson_id, ctid',
                     'song_lesson_id', 'chord_id')
    tabs = {}
    for t in rows(cur, 'select * from tab_sections order by song_lesson_id, id'):
        tabs.setdefault(t['song_lesson_id'], []).append(
            {'identifier': t['identifier'], 'content': text_of(cur, t['content'])})
    lessons = {}
    for sl in rows(cur, 'select * from song_lessons order by song_id, id'):
        lessons.setdefault(sl['song_id'], []).append({
            'id': sl['id'], 'difficulty': sl['difficulty'], 'chordIds': chords.get(sl['id'], []),
            'chordedLyrics': text_of(cur, sl['chorded_lyrics']), 'videoUrl': sl['video_url'],
            'instrumentId': sl['instrument_id'], 'tabSections': tabs.get(sl['id'], []),
        })
    return [{
        'id': r['id'], 'title': r['title'], 'artist': r['artist'], 'imageUrl': r['image_url'],
        'genreIds': genres.get(r['id'], []), 'instrumentIds': instruments.get(r['id'], []),
        'decade': r['decade'], 'popular': r['popular'], 'newSong': r['new_song'],
        'genreClassic': r['genre_classic'], 'genrePopular': r['genre_popular'], 'genreNew': r['genre_new'],
        'artistClassic': r['artist_classic'], 'artistPopular': r['artist_popular'], 'artistNew': r['artist_new'],
        'lessons': lessons.get(r['id'], []),
    } for r in rows(cur, 'select s.*, i.title, i.image_url from songs s join item i on i.id = s.id order by s.id')]


EXPORTS = {
    'instruments': export_instruments,
    'chords': export_chords,
    'tunes': export_tunes,
    'genres': export_genres,
    'instrument_genres': export_instrument_genres,
    'lessons': export_lessons,
    'songs': export_songs,
}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__.split('\n\n')[0])
    parser.add_argument('--out', type=Path, default=DATA_DIR, help='carpeta de salida (por defecto data/)')
    args = parser.parse_args()
    args.out.mkdir(parents=True, exist_ok=True)

    try:
        with psycopg.connect(build_dsn()) as cn:
            cn.read_only = True
            with cn.cursor() as cur:
                for name, export in EXPORTS.items():
                    data = export(cur)
                    path = args.out / f'{name}.json'
                    path.write_text(to_json(name, data), encoding='utf-8')
                    print(f'✓ {path.name}: {len(data)}')
    except psycopg.OperationalError as e:
        print(f'✗ Cannot connect to the database: {e}')
        sys.exit(1)


if __name__ == '__main__':
    main()
