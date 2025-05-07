import json
import requests

# API Endpoint for inserting songs
api_url = 'http://localhost:8080/api/items/songs'

# Load songs from JSON file
with open('../data/songs.json', 'r') as file:  
    songs = json.load(file)

# Función para insertar cada acorde
def insert_songs(song):
    response = requests.post(api_url, json=song)
    
    if response.status_code == 200:  # HTTP 200: OK
        print(f"Acorde '{song['name']}' insertado correctamente.")
    else:
        print(f"Error al insertar el acorde '{song['name']}': {response.status_code}, {response.text}")

# Insertar todos los acordes en el archivo JSON
for song in songs:
    insert_songs(song)
