import json
import requests
from pathlib import Path

# Configuration
API_URL = 'http://localhost:8080/song-lessons'
DATA_FILE = Path(__file__).parent.parent / 'data' / 'song_lessons.json'

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

def insert_song_lesson(lesson):
    """Insert a single song lesson via API."""
    try:
        response = requests.post(API_URL, json=lesson, timeout=5)
        
        if response.status_code in [200, 201]:
            print(f"✓ Song lesson for song ID '{lesson.get('songId')}' added successfully.")
            return True
        else:
            print(f"✗ Error adding song lesson: {response.status_code} - {response.text}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"✗ Request error for song lesson: {e}")
        return False

def main():
    """Main function to insert all song lessons."""
    print("Loading song lessons from file...")
    lessons = load_data(DATA_FILE)
    
    if not lessons:
        print("No song lessons to insert.")
        return
    
    print(f"Found {len(lessons)} song lessons. Starting insertion...\n")
    
    successful = 0
    failed = 0
    
    for lesson in lessons:
        if insert_song_lesson(lesson):
            successful += 1
        else:
            failed += 1
    
    print(f"\n{'='*50}")
    print(f"Summary: {successful} successful, {failed} failed out of {len(lessons)}")
    print(f"{'='*50}")

if __name__ == '__main__':
    main()

