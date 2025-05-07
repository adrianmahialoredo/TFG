import json
import requests

# URL de la API de Spring Boot donde quieres insertar los acordes
api_url = 'http://localhost:8080/chords'

# Cargar los acordes desde el archivo JSON
with open('../data/chords.json', 'r') as file:  # Ajusta la ruta relativa
    chords = json.load(file)

# Función para insertar cada acorde
def insert_chord(chord):
    response = requests.post(api_url, json=chord)
    
    if response.status_code == 200:  # HTTP 201: Created
        print(f"Acorde '{chord['name']}' insertado correctamente.")
    else:
        print(f"Error al insertar el acorde '{chord['name']}': {response.status_code}, {response.text}")

# Insertar todos los acordes en el archivo JSON
for chord in chords:
    insert_chord(chord)
