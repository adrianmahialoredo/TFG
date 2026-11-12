"""Widen the token columns of ``spotify_users`` from VARCHAR(255) to VARCHAR(2048).

Why this script exists
----------------------
Spotify access and refresh tokens are longer than 255 characters, so linking a
Spotify account failed with::

    ERROR: value too long for type character varying(255)
    [insert into spotify_users (access_token,refresh_token,spotify_user_id,token_expiration) ...]

:code:`com.uo289390.toco.model.SpotifyUser` now declares both columns with
``@Column(length = TOKEN_MAX_LENGTH)`` (2048), but Hibernate (``ddl-auto=update``)
never alters an existing column, so databases created before the change keep
VARCHAR(255). This script widens them. Widening a VARCHAR keeps the data and is
idempotent: columns that are already 2048 or larger are left alone.

It can be run before or after deploying the new backend.

Usage
-----
    python scripts/migrate_spotify_token_length.py

Connection settings are read from ``src/main/resources/application.properties``
(and the ``.env`` it imports) and can be overridden with the ``DB_URL`` /
``DB_USER`` / ``DB_PASSWORD`` environment variables, as in
``migrate_preferences_table.py``.
"""

import sys

import psycopg

from console_utils import configure_console
from migrate_preferences_table import build_dsn

configure_console()

TABLE = 'spotify_users'
COLUMNS = ('access_token', 'refresh_token')
LENGTH = 2048  # SpotifyUser.TOKEN_MAX_LENGTH


def column_length(cur, column: str):
    """Returns (exists, max length or None when unlimited)."""
    cur.execute(
        "select character_maximum_length from information_schema.columns "
        "where table_schema = 'public' and table_name = %s and column_name = %s",
        (TABLE, column),
    )
    row = cur.fetchone()
    return (row is not None, row[0] if row else None)


def migrate(cur) -> None:
    for column in COLUMNS:
        exists, length = column_length(cur, column)
        if not exists:
            print(f"• '{TABLE}.{column}' does not exist yet; Hibernate will create it with length {LENGTH}.")
        elif length is None or length >= LENGTH:
            print(f"• '{TABLE}.{column}' is already wide enough ({length or 'unlimited'}).")
        else:
            cur.execute(f'alter table {TABLE} alter column {column} type varchar({LENGTH})')
            print(f"✓ Widened '{TABLE}.{column}' from varchar({length}) to varchar({LENGTH}).")


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

    print(f"\nMigration finished. Spotify tokens of up to {LENGTH} characters can be stored.")


if __name__ == '__main__':
    main()
