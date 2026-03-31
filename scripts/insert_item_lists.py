import json
import requests
from pathlib import Path

# Configuration
API_URL = 'http://localhost:8080/item-lists'
DATA_FILE = Path(__file__).parent.parent / 'data' / 'item_lists.json'

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

def insert_item_list(item_list):
    """Insert a single item list via API."""
    try:
        response = requests.post(API_URL, json=item_list, timeout=5)
        
        if response.status_code in [200, 201]:
            print(f"✓ Item list '{item_list.get('name', 'Unnamed')}' added successfully.")
            return True
        else:
            print(f"✗ Error adding item list: {response.status_code} - {response.text}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"✗ Request error for item list: {e}")
        return False

def main():
    """Main function to insert all item lists."""
    print("Loading item lists from file...")
    item_lists = load_data(DATA_FILE)
    
    if not item_lists:
        print("No item lists to insert.")
        return
    
    print(f"Found {len(item_lists)} item lists. Starting insertion...\n")
    
    successful = 0
    failed = 0
    
    for item_list in item_lists:
        if insert_item_list(item_list):
            successful += 1
        else:
            failed += 1
    
    print(f"\n{'='*50}")
    print(f"Summary: {successful} successful, {failed} failed out of {len(item_lists)}")
    print(f"{'='*50}")

if __name__ == '__main__':
    main()

