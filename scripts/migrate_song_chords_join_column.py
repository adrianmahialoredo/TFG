"""Rename the join column of ``song_chords`` to the current JPA mapping.

Why this script exists
----------------------
Chords used to belong to a song, so the join table ``song_chords`` had the
columns ``(song_id, chord_id)``. They now belong to a song lesson
(:code:`com.uo289390.toco.model.SongLesson`), which maps the same table as
``(song_lesson_id, chord_id)``. The foreign key of ``song_id`` already points at
``song_lessons(id)``; only the column name is outdated.

Hibernate is configured with ``spring.jpa.hibernate.ddl-auto=update``, which
never renames columns. On startup it tries to add ``song_lesson_id`` as a new
mandatory column and fails because the table already has rows::

    ERROR: column "song_lesson_id" of relation "song_chords" contains null values

This script renames the column, keeping the data. It is idempotent: it can be
run as many times as needed and does nothing if the schema is already fine.

Usage
-----
    python scripts/migrate_song_chords_join_column.py

Connection settings are read from ``src/main/resources/application.properties``
and can be overridden with the ``DB_URL`` / ``DB_USER`` / ``DB_PASSWORD``
environment variables.
"""

import sys

import psycopg

from console_utils import configure_console
from migrate_preferences_table import build_dsn

configure_console()


def existing_columns(cur) -> set:
    cur.execute(
        "select column_name from information_schema.columns "
        "where table_schema = 'public' and table_name = 'song_chords'"
    )
    return {row[0] for row in cur.fetchall()}


def migrate(cur) -> None:
    columns = existing_columns(cur)

    if not columns:
        print("• The 'song_chords' table does not exist yet; Hibernate will create it. Nothing to do.")
        return

    if 'song_lesson_id' in columns:
        print("• 'song_chords.song_lesson_id' already exists. Nothing to do.")
        return

    if 'song_id' in columns:
        cur.execute('alter table song_chords rename column song_id to song_lesson_id')
        print("✓ Renamed 'song_chords.song_id' to 'song_lesson_id'.")


def main() -> None:
    dsn = build_dsn()
    try:
        with psycopg.connect(dsn) as cn:
            with cn.cursor() as cur:
                migrate(cur)
            cn.commit()
    except psycopg.OperationalError as e:
        print(f"✗ Cannot connect to the database: {e}")
        sys.exit(1)

    print("\nMigration finished. The 'song_chords' table now matches the JPA mapping.")


if __name__ == '__main__':
    main()
