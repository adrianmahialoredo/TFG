"""Authenticated access to the backend API for the data loading scripts.

Creating catalog content (genres, songs, lessons...) requires the ADMIN role, so the
insertion scripts log in with the administrator configured in ``.env``
(``ADMIN_EMAIL`` / ``ADMIN_PASSWORD``, the same values the backend uses to create it at
startup) and send the JWT on every request.
"""

import sys
from functools import lru_cache

import requests

from env_utils import load_env

BASE_URL = 'http://localhost:8080'
LOGIN_URL = f'{BASE_URL}/api/auth/login'
LOGIN_TIMEOUT = 10


@lru_cache(maxsize=1)
def admin_session() -> requests.Session:
    """Log in as the administrator once and return a session that sends its JWT.

    Exits the script with a clear message if the credentials are missing or rejected,
    since every insertion would fail with 401/403 anyway.
    """
    env = load_env()
    email, password = env.get('ADMIN_EMAIL', ''), env.get('ADMIN_PASSWORD', '')
    if not email or not password:
        print('✗ ADMIN_EMAIL and ADMIN_PASSWORD must be set in .env to load data (see .env.example).')
        sys.exit(1)

    try:
        response = requests.post(LOGIN_URL, json={'email': email, 'password': password}, timeout=LOGIN_TIMEOUT)
    except requests.exceptions.ConnectionError:
        print(f'✗ Cannot reach the API at {BASE_URL}. Is the backend running on port 8080?')
        sys.exit(1)

    if response.status_code != 200:
        print(f'✗ Admin login failed ({response.status_code}): {response.text}')
        print('  Check ADMIN_EMAIL / ADMIN_PASSWORD in .env and restart the backend so it creates the admin.')
        sys.exit(1)

    session = requests.Session()
    session.headers['Authorization'] = f"Bearer {response.json()['token']}"
    return session
