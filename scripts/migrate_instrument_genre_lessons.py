"""Move the InstrumentGenre - Lesson relationship to the ``lessons`` table.

Why this script exists
----------------------
The relationship between instrument-genres and lessons used to be a many-to-many
stored in the ``instrument_genre_lesson`` join table.

Now the :code:`com.demo.demo.model.Lesson` entity owns it through three columns in
``lessons``:

* ``instrument_genre_id`` (FK to ``instrument_genres``)
* ``belongs_to_instrument_genre`` (boolean, default false)
* ``index_in_instrument_genre`` (integer, default 0)

and :code:`InstrumentGenre.lessons` is only its inverse side (``mappedBy``).

Hibernate (``spring.jpa.hibernate.ddl-auto=update``) adds the new columns but does
not copy the existing relationships, so without this script every instrument-genre
would appear without lessons. The script:

1. Adds the new columns if Hibernate has not created them yet.
2. Copies each row of ``instrument_genre_lesson`` to its lesson. The position inside
   the instrument-genre follows the lesson id (the join table stored no order).
   A lesson linked to several instrument-genres keeps the one with the lowest id.

It is idempotent: lessons that already have an instrument-genre are not touched.
The legacy join table is kept; it can be dropped by hand once the data is checked.

Usage
-----
    python scripts/migrate_instrument_genre_lessons.py

Connection settings are read from ``src/main/resources/application.properties``
and can be overridden with the ``DB_URL`` / ``DB_USER`` / ``DB_PASSWORD``
environment variables.
"""

import sys

import psycopg

from console_utils import configure_console
from migrate_preferences_table import build_dsn

configure_console()

JOIN_TABLE = 'instrument_genre_lesson'


def table_exists(cur, table: str) -> bool:
    cur.execute(
        "select 1 from information_schema.tables "
        "where table_schema = 'public' and table_name = %s",
        (table,)
    )
    return cur.fetchone() is not None


def existing_columns(cur, table: str) -> set:
    cur.execute(
        "select column_name from information_schema.columns "
        "where table_schema = 'public' and table_name = %s",
        (table,)
    )
    return {row[0] for row in cur.fetchall()}


def migrate(cur) -> None:
    if not table_exists(cur, 'lessons'):
        print("• The 'lessons' table does not exist yet; Hibernate will create it. Nothing to do.")
        return

    # 1. New columns (same definition Hibernate generates, with defaults for existing rows).
    columns = existing_columns(cur, 'lessons')
    if 'instrument_genre_id' not in columns:
        cur.execute('alter table lessons add column instrument_genre_id bigint')
        print("✓ Added column 'lessons.instrument_genre_id'.")
    if 'belongs_to_instrument_genre' not in columns:
        cur.execute('alter table lessons add column belongs_to_instrument_genre boolean default false not null')
        print("✓ Added column 'lessons.belongs_to_instrument_genre'.")
    if 'index_in_instrument_genre' not in columns:
        cur.execute('alter table lessons add column index_in_instrument_genre integer default 0 not null')
        print("✓ Added column 'lessons.index_in_instrument_genre'.")

    # 2. Copy the legacy many-to-many rows.
    if not table_exists(cur, JOIN_TABLE):
        print(f"• The legacy '{JOIN_TABLE}' table does not exist. Nothing to copy.")
        return

    cur.execute(
        f"select lesson_id, array_agg(instrument_genre_id order by instrument_genre_id) "
        f"from {JOIN_TABLE} group by lesson_id having count(*) > 1"
    )
    for lesson_id, instrument_genre_ids in cur.fetchall():
        print(f"! Lesson {lesson_id} was linked to several instrument-genres {instrument_genre_ids}; "
              f"it keeps {instrument_genre_ids[0]}.")

    cur.execute(
        f"""
        with chosen as (
            select lesson_id, min(instrument_genre_id) as instrument_genre_id
            from {JOIN_TABLE}
            group by lesson_id
        ),
        ordered as (
            select lesson_id, instrument_genre_id,
                   row_number() over (partition by instrument_genre_id order by lesson_id) - 1 as position
            from chosen
        )
        update lessons l
        set instrument_genre_id = o.instrument_genre_id,
            belongs_to_instrument_genre = true,
            index_in_instrument_genre = o.position
        from ordered o
        where l.id = o.lesson_id
          and l.instrument_genre_id is null
        """
    )
    print(f"✓ Linked {cur.rowcount} lesson(s) to their instrument-genre.")


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

    print("\nMigration finished. Lessons now own their instrument-genre relationship.")


if __name__ == '__main__':
    main()
