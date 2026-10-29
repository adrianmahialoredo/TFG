import json
import requests
from pathlib import Path

from api_auth import admin_session, api_url
from console_utils import configure_console

configure_console()

# Configuration
API_URL = api_url('/api/instrument-genres')
DATA_FILE = Path(__file__).parent.parent / 'data' / 'instrument_genres.json'
REQUEST_TIMEOUT = 30

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

def insert_instrument_genre(relationship):
    """Insert a single instrument-genre relationship via API."""
    try:
        response = admin_session().post(API_URL, json=relationship, timeout=REQUEST_TIMEOUT)

        if response.status_code in [200, 201]:
            print(f"✓ Instrument-Genre relationship (Genre ID: {relationship.get('genreId')}, Instrument ID: {relationship.get('instrumentId')}) added successfully.")
            return True
        else:
            print(f"✗ Error adding instrument-genre relationship (ID: {relationship.get('id')}): {response.status_code} - {response.text}")
            return False
    except requests.exceptions.ConnectionError:
        print(f"✗ Cannot reach the API at {API_URL}. Is the backend running on port 8080?")
        return False
    except requests.exceptions.RequestException as e:
        print(f"✗ Request error for instrument-genre relationship (ID: {relationship.get('id')}): {e}")
        return False

def main():
    """Main function to insert all instrument-genre relationships."""
    print("Loading instrument-genre relationships from file...")
    relationships = load_data(DATA_FILE)
    
    if not relationships:
        print("No instrument-genre relationships to insert.")
        return
    
    print(f"Found {len(relationships)} instrument-genre relationships. Starting insertion...\n")
    
    successful = 0
    failed = 0
    
    for relationship in relationships:
        if insert_instrument_genre(relationship):
            successful += 1
        else:
            failed += 1
    
    print(f"\n{'='*50}")
    print(f"Summary: {successful} successful, {failed} failed out of {len(relationships)}")
    print(f"{'='*50}")

if __name__ == '__main__':
    main()

