import json
import requests
from pathlib import Path

from console_utils import configure_console

configure_console()

# Configuration
API_URL = 'http://localhost:8080/api/instruments'
DATA_FILE = Path(__file__).parent.parent / 'data' / 'instruments.json'

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

def insert_instrument(instrument):
    """Insert a single instrument via API."""
    try:
        response = requests.post(API_URL, json=instrument, timeout=5)
        
        if response.status_code in [200, 201]:
            print(f"✓ Instrument '{instrument['name']}' added successfully.")
            return True
        else:
            print(f"✗ Error adding instrument '{instrument['name']}': {response.status_code} - {response.text}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"✗ Request error for instrument '{instrument['name']}': {e}")
        return False

def main():
    """Main function to insert all instruments."""
    print("Loading instruments from file...")
    instruments = load_data(DATA_FILE)
    
    if not instruments:
        print("No instruments to insert.")
        return
    
    print(f"Found {len(instruments)} instruments. Starting insertion...\n")
    
    successful = 0
    failed = 0
    
    for instrument in instruments:
        if insert_instrument(instrument):
            successful += 1
        else:
            failed += 1
    
    print(f"\n{'='*50}")
    print(f"Summary: {successful} successful, {failed} failed out of {len(instruments)}")
    print(f"{'='*50}")

if __name__ == '__main__':
    main()
