"""Add the instrument of each chord to the ``chords`` table.

Why this script exists
----------------------
Chords used to be instrument agnostic, so ``chords.name`` was unique. They now
belong to an instrument (:code:`com.uo289390.toco.model.Chord`): the same name
(e.g. "Cm") can exist once per instrument, so the uniqueness is now
``(name, instrument_id)``.

Hibernate is configured with ``spring.jpa.hibernate.ddl-auto=update``, which
cannot add a mandatory column to a table that already has rows, and never drops
the old unique constraint on ``name``. On startup it fails with::

    ERROR: column "instrument_id" of relation "chords" contains null values

This script adds the column, assigns the existing chords to the guitar (every
chord loaded so far is a guitar chord), makes it mandatory and drops the old
unique constraint on ``name``. Hibernate creates the foreign key and the new
``(name, instrument_id)`` unique constraint on the next startup. It is
idempotent: it can be run as many times as needed.

Usage
-----
    python scripts/migrate_chord_instrument.py [instrument_id]

``instrument_id`` defaults to 1 (Guitarra). Connection settings are read from
``src/main/resources/application.properties`` and can be overridden with the
``DB_URL`` / ``DB_USER`` / ``DB_PASSWORD`` environment variables.
"""

import sys

import psycopg

from console_utils import configure_console
from migrate_preferences_table import build_dsn

configure_console()

DEFAULT_INSTRUMENT_ID = 1


def existing_columns(cur) -> set:
    cur.execute(
        "select column_name from information_schema.columns "
        "where table_schema = 'public' and table_name = 'chords'"
    )
    return {row[0] for row in cur.fetchall()}


def name_only_unique_constraints(cur) -> list:
    """Unique constraints of ``chords`` made only of the ``name`` column."""
    cur.execute(
        """
        select c.conname
        from pg_constraint c
        join pg_class t on t.oid = c.conrelid
        where t.relname = 'chords' and c.contype = 'u'
          and array_length(c.conkey, 1) = 1
          and c.conkey[1] = (
              select a.attnum from pg_attribute a
              where a.attrelid = t.oid and a.attname = 'name'
          )
        """
    )
    return [row[0] for row in cur.fetchall()]


def migrate(cur, instrument_id: int) -> None:
    columns = existing_columns(cur)

    if not columns:
        print("• The 'chords' table does not exist yet; Hibernate will create it. Nothing to do.")
        return

    if 'instrument_id' not in columns:
        cur.execute('alter table chords add column instrument_id bigint')
        print("✓ Added 'chords.instrument_id'.")

    cur.execute('update chords set instrument_id = %s where instrument_id is null', (instrument_id,))
    if cur.rowcount:
        print(f"✓ Assigned {cur.rowcount} chord(s) to instrument {instrument_id}.")

    cur.execute('alter table chords alter column instrument_id set not null')

    for name in name_only_unique_constraints(cur):
        cur.execute(f'alter table chords drop constraint "{name}"')
        print(f"✓ Dropped the unique constraint '{name}' on 'chords.name'.")


def main() -> None:
    instrument_id = int(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_INSTRUMENT_ID
    dsn = build_dsn()
    try:
        with psycopg.connect(dsn) as cn:
            with cn.cursor() as cur:
                migrate(cur, instrument_id)
            cn.commit()
    except psycopg.OperationalError as e:
        print(f"✗ Cannot connect to the database: {e}")
        sys.exit(1)

    print("\nMigration finished. The 'chords' table now matches the JPA mapping.")


if __name__ == '__main__':
    main()
