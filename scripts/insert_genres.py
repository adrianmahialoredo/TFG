import json
import requests
from pathlib import Path

# Configuration
API_URL = 'http://localhost:8080/genres'
DATA_FILE = Path(__file__).parent.parent / 'data' / 'genres.json'

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

def insert_genre(genre):
    """Insert a single genre via API."""
    try:
        response = requests.post(API_URL, json=genre, timeout=5)
        
        if response.status_code in [200, 201]:
            print(f"✓ Genre '{genre['name']}' added successfully.")
            return True
        else:
            print(f"✗ Error adding genre '{genre['name']}': {response.status_code} - {response.text}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"✗ Request error for genre '{genre['name']}': {e}")
        return False

def main():
    """Main function to insert all genres."""
    print("Loading genres from file...")
    genres = load_data(DATA_FILE)
    
    if not genres:
        print("No genres to insert.")
        return
    
    print(f"Found {len(genres)} genres. Starting insertion...\n")
    
    successful = 0
    failed = 0
    
    for genre in genres:
        if insert_genre(genre):
            successful += 1
        else:
            failed += 1
    
    print(f"\n{'='*50}")
    print(f"Summary: {successful} successful, {failed} failed out of {len(genres)}")
    print(f"{'='*50}")

if __name__ == '__main__':
    main()
