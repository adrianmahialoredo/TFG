import json
import requests
import os
from pathlib import Path

from api_auth import admin_session, api_url
from console_utils import configure_console

configure_console()

# Configuration
API_URL = api_url('/api/tunes')
DATA_FILE = Path(__file__).parent.parent / 'data' / 'tunes.json'

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

def insert_tune(tune):
    """Insert a single tuning via API."""
    try:
        response = admin_session().post(API_URL, json=tune, timeout=5)
        
        if response.status_code in [200, 201]:
            print(f"✓ Tune '{tune['title']}' ({tune['instrumentId']}) added successfully.")
            return True
        else:
            print(f"✗ Error adding tune '{tune['title']}' ({tune['instrumentId']}): {response.status_code} - {response.text}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"✗ Request error for tune '{tune['title']}' ({tune['instrumentId']}): {e}")
        return False

def main():
    """Main function to insert all tunings."""
    print("Loading tunes from file...")
    tunes = load_data(DATA_FILE)
    
    if not tunes:
        print("No tunes to insert.")
        return
    
    print(f"Found {len(tunes)} tunes. Starting insertion...\n")
    
    successful = 0
    failed = 0
    
    for tune in tunes:
        if insert_tune(tune):
            successful += 1
        else:
            failed += 1
    
    print(f"\n{'='*50}")
    print(f"Summary: {successful} successful, {failed} failed out of {len(tunes)}")
    print(f"{'='*50}")

if __name__ == '__main__':
    main()
