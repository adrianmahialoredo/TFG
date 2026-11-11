"""Add the order column of ``itemlist_items`` and fill it for the existing rows.

Why this script exists
----------------------
:code:`com.uo289390.toco.model.ItemList` keeps its items in a ``List`` so that
they are shown in the order in which they were added. Without an order column
Hibernate does not persist that order and the database returns the rows of the
join table ``itemlist_items`` in any order (after removing an item, Hibernate
rewrites the rows and the order changes).

The mapping now has ``@OrderColumn(name = "item_position")``. Hibernate
(``ddl-auto=update``) adds the column on startup, but leaves it empty on the
existing rows, and a list with an empty position cannot be loaded::

    HibernateException: null index column for collection: ...ItemList.items

This script adds the column if it is missing and numbers the rows of each list
from 0, keeping the order in which they are stored (the closest thing to the
insertion order that is left). It is idempotent: it only touches lists with
missing positions, so it can be run as many times as needed.

Run it **before and right after deploying** the new backend: the old backend,
still running during the deployment, may add rows without position.

Usage
-----
    python scripts/migrate_itemlist_order.py

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

TABLE = 'itemlist_items'
COLUMN = 'item_position'


def table_exists(cur) -> bool:
    cur.execute(
        "select 1 from information_schema.tables "
        "where table_schema = 'public' and table_name = %s",
        (TABLE,),
    )
    return cur.fetchone() is not None


def column_exists(cur) -> bool:
    cur.execute(
        "select 1 from information_schema.columns "
        "where table_schema = 'public' and table_name = %s and column_name = %s",
        (TABLE, COLUMN),
    )
    return cur.fetchone() is not None


def migrate(cur) -> None:
    if not table_exists(cur):
        print(f"• The '{TABLE}' table does not exist yet; Hibernate will create it. Nothing to do.")
        return

    if not column_exists(cur):
        cur.execute(f'alter table {TABLE} add column {COLUMN} integer')
        print(f"✓ Added column '{TABLE}.{COLUMN}'.")

    # Renumber (from 0) every list that has at least one row without position. The rows that
    # already have one keep their relative order; the new ones go after them, in storage order.
    cur.execute(
        f"""
        with pending as (
            select distinct itemlist_id from {TABLE} where {COLUMN} is null
        ),
        numbered as (
            select t.ctid as row_id,
                   row_number() over (
                       partition by t.itemlist_id
                       order by t.{COLUMN} nulls last, t.ctid
                   ) - 1 as position
            from {TABLE} t
            join pending p on p.itemlist_id = t.itemlist_id
        )
        update {TABLE} t
        set {COLUMN} = n.position
        from numbered n
        where t.ctid = n.row_id
        """
    )
    if cur.rowcount:
        print(f"✓ Numbered {cur.rowcount} row(s) of '{TABLE}'.")
    else:
        print(f"• Every row of '{TABLE}' already has a position.")


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

    print(f"\nMigration finished. '{TABLE}' keeps the order of the items of each list.")


if __name__ == '__main__':
    main()
