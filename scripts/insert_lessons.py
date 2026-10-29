import json
import requests
from pathlib import Path

from api_auth import admin_session, api_url
from console_utils import configure_console

configure_console()

# Configuration
API_URL = api_url('/api/lessons')
DATA_FILE = Path(__file__).parent.parent / 'data' / 'lessons.json'

def load_data(file_path):
    """Load JSON data from file with proper encoding."""
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            return json.load(file)
    except FileNotFoundError:
        print(f"Error: File '{file_path}' not found.")
        return []
    except json.JSONDecodeError:
        print(f"Error: Invalid JSON in '{file_path}'.")
        return []

# Fields that link a lesson to its instrument-genre. They are not sent: the
# instrument-genres do not exist yet when the lessons are loaded, and the link is
# made afterwards by insert_instrument_genres.py from each "lessonIds" array
# (which also sets belongsToInstrumentGenre and the index of each lesson).
INSTRUMENT_GENRE_FIELDS = ('instrumentGenreId', 'indexInInstrumentGenre')

def insert_lesson(lesson):
    """Insert a single lesson via API."""
    payload = {k: v for k, v in lesson.items() if k not in INSTRUMENT_GENRE_FIELDS}
    try:
        response = admin_session().post(API_URL, json=payload, timeout=5)

        if response.status_code in [200, 201]:
            print(f"✓ Lesson '{lesson.get('title')}' added successfully.")
            return True
        else:
            print(f"✗ Error adding lesson: {response.status_code} - {response.text}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"✗ Request error for lesson: {e}")
        return False

def main():
    """Main function to insert all lessons."""
    print("Loading lessons from file...")
    lessons = load_data(DATA_FILE)

    if not lessons:
        print("No lessons to insert.")
        return

    print(f"Found {len(lessons)} lessons. Starting insertion...\n")

    successful = 0
    failed = 0

    for lesson in lessons:
        if insert_lesson(lesson):
            successful += 1
        else:
            failed += 1

    print(f"\n{'='*50}")
    print(f"Summary: {successful} successful, {failed} failed out of {len(lessons)}")
    print(f"{'='*50}")

if __name__ == '__main__':
    main()
