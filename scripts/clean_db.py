
"""Clean PostgreSQL database tables for the configured schema."""

import argparse
import os
from pathlib import Path

import psycopg


def _load_properties(path: Path) -> dict:
    props = {}
    if not path.exists():
        return props
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        props[key.strip()] = value.strip()
    return props


def _get_defaults() -> dict:
    project_root = Path(__file__).resolve().parents[1]
    props = _load_properties(project_root / "src" / "main" / "resources" / "application.properties")
    jdbc_url = props.get("spring.datasource.url", "")

    host = os.getenv("DB_HOST") or "localhost"
    port = int(os.getenv("DB_PORT") or 5432)
    database = os.getenv("DB_NAME") or "postgres"
    if jdbc_url.startswith("jdbc:postgresql://"):
        trimmed = jdbc_url.replace("jdbc:postgresql://", "", 1)
        host_port, _, dbname = trimmed.partition("/")
        if host_port:
            if ":" in host_port:
                host, port_str = host_port.split(":", 1)
                if port_str.isdigit():
                    port = int(port_str)
            else:
                host = host_port
        if dbname:
            database = dbname

    user = os.getenv("DB_USER") or props.get("spring.datasource.username") or "postgres"
    password = os.getenv("DB_PASSWORD") or props.get("spring.datasource.password") or ""

    return {
        "host": host,
        "port": port,
        "dbname": database,
        "user": user,
        "password": password,
    }


def _fetch_tables(conn: psycopg.Connection, schema: str) -> list[str]:
    with conn.cursor() as cur:
        cur.execute(
            """
            SELECT tablename
            FROM pg_catalog.pg_tables
            WHERE schemaname = %s
            ORDER BY tablename;
            """,
            (schema,),
        )
        return [row[0] for row in cur.fetchall()]


def _truncate_tables(conn: psycopg.Connection, schema: str, tables: list[str], dry_run: bool) -> None:
    if not tables:
        print(f"No tables found in schema '{schema}'.")
        return

    qualified = ", ".join(f"\"{schema}\".\"{table}\"" for table in tables)
    statement = f"TRUNCATE TABLE {qualified} RESTART IDENTITY CASCADE;"

    if dry_run:
        print(statement)
        return

    with conn.cursor() as cur:
        cur.execute(statement)
    conn.commit()


def main() -> int:
    defaults = _get_defaults()

    parser = argparse.ArgumentParser(description="Clean PostgreSQL tables by truncating a schema.")
    parser.add_argument("--host", default=defaults["host"])
    parser.add_argument("--port", type=int, default=defaults["port"])
    parser.add_argument("--dbname", default=defaults["dbname"])
    parser.add_argument("--user", default=defaults["user"])
    parser.add_argument("--password", default=defaults["password"])
    parser.add_argument("--schema", default=os.getenv("DB_SCHEMA") or "public")
    parser.add_argument("--dry-run", action="store_true", help="Print SQL without executing.")
    parser.add_argument("--confirm", action="store_true", help="Actually execute the cleanup.")

    args = parser.parse_args()

    if not args.confirm:
        print("Refusing to run without --confirm. Use --dry-run to preview SQL.")
        return 2

    conn = psycopg.connect(
        host=args.host,
        port=args.port,
        dbname=args.dbname,
        user=args.user,
        password=args.password,
    )
    try:
        tables = _fetch_tables(conn, args.schema)
        _truncate_tables(conn, args.schema, tables, args.dry_run)
    finally:
        conn.close()

    return 0


if __name__ == "__main__":
    raise SystemExit(main())

