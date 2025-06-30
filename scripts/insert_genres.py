import json
import requests

# API Endpoint for inserting genres
api_url = 'http://localhost:8080/genres'

# Load genres from JSON file
with open('../data/genres.json', 'r') as file:  
    genres = json.load(file)

# Función para insertar cada acorde
def insert_genres(genre):
    response = requests.post(api_url, json=genre)
    
    if response.status_code == 201:  # HTTP 201: Created
        print(f"Genero '{genre['name']}' insertado correctamente.")
    else:
        print(f"Error al insertar el genero '{genre['name']}': {response.status_code}, {response.text}")

# Insertar todos los acordes en el archivo JSON
for genre in genres:
    insert_genres(genre)