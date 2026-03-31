import json
import requests
from pathlib import Path

# Configuration
API_URL = 'http://localhost:8080/tab-sections'
DATA_FILE = Path(__file__).parent.parent / 'data' / 'tab_sections.json'

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

def insert_tab_section(tab_section):
    """Insert a single tab section via API."""
    try:
        response = requests.post(API_URL, json=tab_section, timeout=5)
        
        if response.status_code in [200, 201]:
            print(f"✓ Tab section for song lesson ID '{tab_section.get('songLessonId')}' added successfully.")
            return True
        else:
            print(f"✗ Error adding tab section: {response.status_code} - {response.text}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"✗ Request error for tab section: {e}")
        return False

def main():
    """Main function to insert all tab sections."""
    print("Loading tab sections from file...")
    tab_sections = load_data(DATA_FILE)
    
    if not tab_sections:
        print("No tab sections to insert.")
        return
    
    print(f"Found {len(tab_sections)} tab sections. Starting insertion...\n")
    
    successful = 0
    failed = 0
    
    for tab_section in tab_sections:
        if insert_tab_section(tab_section):
            successful += 1
        else:
            failed += 1
    
    print(f"\n{'='*50}")
    print(f"Summary: {successful} successful, {failed} failed out of {len(tab_sections)}")
    print(f"{'='*50}")

if __name__ == '__main__':
    main()

