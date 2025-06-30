import json
import requests

# API URL
api_url = 'http://localhost:8080/api/chords'

# Load chords from JSON file
with open('../data/chords.json', 'r') as file: 
    chords = json.load(file)

# Function for inserting each chord
def insert_chord(chord):
    response = requests.post(api_url, json=chord)
    
    if response.status_code == 201:  # HTTP 201: Created
        print(f"Chord '{chord['name']}' added succsesfully.")
    else:
        print(f"Error when adding chord: '{chord['name']}': {response.status_code}, {response.text}")

# Insert every chord from the file
for chord in chords:
    insert_chord(chord)


