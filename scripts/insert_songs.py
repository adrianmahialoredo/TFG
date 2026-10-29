import json
import requests
from pathlib import Path

from api_auth import admin_session, api_url
from console_utils import configure_console

configure_console()

# Configuration
API_URL = api_url('/api/songs')
DATA_FILE = Path(__file__).parent.parent / 'data' / 'songs.json'

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

def insert_song(song):
    """Insert a single song via API."""
    try:
        response = admin_session().post(API_URL, json=song, timeout=5)
        
        if response.status_code in [200, 201]:
            print(f"✓ Song '{song['title']}' added successfully.")
            return True
        else:
            print(f"✗ Error adding song '{song['title']}': {response.status_code} - {response.text}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"✗ Request error for song '{song.get('title', 'Unknown')}': {e}")
        return False

def main():
    """Main function to insert all songs."""
    print("Loading songs from file...")
    songs = load_data(DATA_FILE)
    
    if not songs:
        print("No songs to insert.")
        return
    
    print(f"Found {len(songs)} songs. Starting insertion...\n")
    
    successful = 0
    failed = 0
    
    for song in songs:
        if insert_song(song):
            successful += 1
        else:
            failed += 1
    
    print(f"\n{'='*50}")
    print(f"Summary: {successful} successful, {failed} failed out of {len(songs)}")
    print(f"{'='*50}")

if __name__ == '__main__':
    main()
