"""Read the backend configuration the same way Spring does.

``application.properties`` no longer holds secrets: values such as
``spring.datasource.password=${DB_PASSWORD}`` are placeholders resolved from
the environment or from the ``.env`` file in the project root. The database
scripts use :func:`load_properties` so they get the real values instead of the
literal ``${...}`` text.
"""

import os
import re
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
PROPERTIES_FILE = PROJECT_ROOT / 'src' / 'main' / 'resources' / 'application.properties'
ENV_FILE = PROJECT_ROOT / '.env'

_PLACEHOLDER = re.compile(r'\$\{([^}:]+)(?::([^}]*))?\}')


def _read_key_values(path: Path) -> dict:
    """Read a KEY=VALUE file (.properties or .env), skipping comments and blank lines."""
    values = {}
    if not path.exists():
        return values
    for raw_line in path.read_text(encoding='utf-8').splitlines():
        line = raw_line.strip()
        if not line or line.startswith('#') or '=' not in line:
            continue
        key, value = line.split('=', 1)
        values[key.strip()] = value.strip()
    return values


def load_env() -> dict:
    """Variables from .env, overridden by the real environment (same precedence as Spring)."""
    return {**_read_key_values(ENV_FILE), **os.environ}


def load_properties(path: Path = PROPERTIES_FILE) -> dict:
    """Read application.properties resolving ``${VAR}`` / ``${VAR:default}`` placeholders."""
    env = load_env()

    def resolve(match: re.Match) -> str:
        name, default = match.group(1), match.group(2)
        return env.get(name, default if default is not None else '')

    return {key: _PLACEHOLDER.sub(resolve, value) for key, value in _read_key_values(path).items()}
