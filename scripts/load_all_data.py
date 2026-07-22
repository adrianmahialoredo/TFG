"""
Master script to load all data from JSON files into the application.

This script orchestrates the insertion of all entities in the correct order:
1. Genres (should be loaded first as they are referenced by other entities)
2. Instruments (should be loaded before instrument-genres)
3. InstrumentGenres (relationship between instruments and genres)
4. Chords
5. Songs
6. SongLessons (should be loaded before tab sections)
7. TabSections (references song lessons)

Run this script after ensuring the backend API is running on http://localhost:8080
"""

import subprocess
import sys
from pathlib import Path

# Define the order of scripts to execute
SCRIPTS_ORDER = [
    'insert_genres.py',
    'insert_instruments.py',
    'insert_instrument_genres.py',
    'insert_chords.py',
    'insert_songs.py',
    'insert_song_lessons.py',
    'insert_tab_sections.py',
]

def main():
    """Execute all insert scripts in the correct order."""
    scripts_dir = Path(__file__).parent
    
    print("=" * 70)
    print("Starting data insertion process...")
    print("=" * 70)
    print()
    
    for idx, script_name in enumerate(SCRIPTS_ORDER, 1):
        script_path = scripts_dir / script_name
        
        if not script_path.exists():
            print(f"⚠ Warning: Script '{script_name}' not found. Skipping...")
            continue
        
        print(f"\n[{idx}/{len(SCRIPTS_ORDER)}] Executing {script_name}...")
        print("-" * 70)
        
        try:
            result = subprocess.run(
                [sys.executable, str(script_path)],
                check=True,
                capture_output=False,
                text=True
            )
        except subprocess.CalledProcessError as e:
            print(f"✗ Error executing {script_name}: {e}")
            print("Continuing with next script...")
        except Exception as e:
            print(f"✗ Unexpected error: {e}")
            print("Continuing with next script...")
    
    print()
    print("=" * 70)
    print("Data insertion process completed!")
    print("=" * 70)

if __name__ == '__main__':
    main()

