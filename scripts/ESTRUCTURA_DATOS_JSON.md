# 📋 Estructura de Datos JSON - Guía Completa

Esta guía muestra la estructura exacta que debes seguir para rellenar los archivos JSON vacíos.

## 1. Instruments (Instrumentos)

**Archivo**: `data/instruments.json`

```json
[
  {
    "id": 1,
    "name": "Guitarra Acústica",
    "description": "Guitarra acústica de cuerda de nylon",
    "imageUrl": "https://ejemplo.com/guitarra-acustica.jpg",
    "category": "Cuerda"
  },
  {
    "id": 2,
    "name": "Guitarra Eléctrica",
    "description": "Guitarra eléctrica de 6 cuerdas",
    "imageUrl": "https://ejemplo.com/guitarra-electrica.jpg",
    "category": "Cuerda"
  },
  {
    "id": 3,
    "name": "Batería",
    "description": "Batería estándar con 5 piezas",
    "imageUrl": "https://ejemplo.com/bateria.jpg",
    "category": "Percusión"
  }
]
```

**Campos requeridos**:
- `id` (number) - Identificador único
- `name` (string) - Nombre del instrumento
- `description` (string) - Descripción
- `imageUrl` (string) - URL de la imagen
- `category` (string) - Categoría del instrumento

---

## 2. InstrumentGenres (Relación Instrumento-Género)

**Archivo**: `data/instrument_genres.json`

```json
[
  {
    "id": 1,
    "instrumentId": 1,
    "genreId": 1,
    "difficultyLevel": "BEGINNER",
    "specialNotes": "Perfecto para empezar con pop punk"
  },
  {
    "id": 2,
    "instrumentId": 2,
    "genreId": 2,
    "difficultyLevel": "INTERMEDIATE",
    "specialNotes": "Requiere técnica intermedia"
  },
  {
    "id": 3,
    "instrumentId": 3,
    "genreId": 3,
    "difficultyLevel": "ADVANCED",
    "specialNotes": "Nivel avanzado recomendado"
  }
]
```

**Campos requeridos**:
- `id` (number) - Identificador único
- `instrumentId` (number) - ID del instrumento
- `genreId` (number) - ID del género
- `difficultyLevel` (string) - Niveles: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`
- `specialNotes` (string) - Notas especiales (opcional)

---

## 3. Songs (Canciones)

**Archivo**: `data/songs.json`

```json
[
  {
    "id": 1,
    "title": "Basket Case",
    "artist": "Green Day",
    "genreId": 1,
    "year": 1994,
    "difficulty": "BEGINNER",
    "duration": 2,
    "spotifyUrl": "https://open.spotify.com/track/...",
    "youtubeUrl": "https://youtube.com/watch?v=...",
    "imageUrl": "https://ejemplo.com/basket-case.jpg",
    "description": "Una de las mejores canciones de pop punk",
    "chords": ["G", "D", "A"],
    "isPopular": true,
    "decade": 1990
  },
  {
    "id": 2,
    "title": "Johnny B Goode",
    "artist": "Chuck Berry",
    "genreId": 2,
    "year": 1958,
    "difficulty": "INTERMEDIATE",
    "duration": 3,
    "spotifyUrl": "https://open.spotify.com/track/...",
    "youtubeUrl": "https://youtube.com/watch?v=...",
    "imageUrl": "https://ejemplo.com/johnny-b-goode.jpg",
    "description": "Clásico del rock and roll",
    "chords": ["C", "F", "G"],
    "isPopular": true,
    "decade": 1950
  }
]
```

**Campos requeridos**:
- `id` (number) - Identificador único
- `title` (string) - Título de la canción
- `artist` (string) - Artista/banda
- `genreId` (number) - ID del género
- `year` (number) - Año de lanzamiento
- `difficulty` (string) - Dificultad: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`
- `duration` (number) - Duración en minutos
- `spotifyUrl` (string) - URL de Spotify
- `youtubeUrl` (string) - URL de YouTube
- `imageUrl` (string) - URL de la imagen
- `description` (string) - Descripción
- `chords` (array) - Array de acordes utilizados
- `isPopular` (boolean) - Es popular
- `decade` (number) - Década de lanzamiento

---

## 4. SongLessons (Lecciones de Canciones)

**Archivo**: `data/song_lessons.json`

```json
[
  {
    "id": 1,
    "songId": 1,
    "lessonTitle": "Introducción a Basket Case",
    "description": "Aprenderemos los acordes básicos de Basket Case",
    "difficulty": "BEGINNER",
    "duration": 15,
    "videoUrl": "https://youtube.com/watch?v=...",
    "order": 1,
    "isActive": true
  },
  {
    "id": 2,
    "songId": 1,
    "lessonTitle": "Ritmo y tempo",
    "description": "Trabajaremos el ritmo correcto",
    "difficulty": "BEGINNER",
    "duration": 12,
    "videoUrl": "https://youtube.com/watch?v=...",
    "order": 2,
    "isActive": true
  },
  {
    "id": 3,
    "songId": 2,
    "lessonTitle": "Principios del Rock and Roll",
    "description": "Historia y técnica del rock",
    "difficulty": "INTERMEDIATE",
    "duration": 20,
    "videoUrl": "https://youtube.com/watch?v=...",
    "order": 1,
    "isActive": true
  }
]
```

**Campos requeridos**:
- `id` (number) - Identificador único
- `songId` (number) - ID de la canción asociada
- `lessonTitle` (string) - Título de la lección
- `description` (string) - Descripción
- `difficulty` (string) - Dificultad
- `duration` (number) - Duración en minutos
- `videoUrl` (string) - URL del video tutorial
- `order` (number) - Orden de la lección
- `isActive` (boolean) - Está activa

---

## 5. TabSections (Secciones de Tabs)

**Archivo**: `data/tab_sections.json`

```json
[
  {
    "id": 1,
    "songLessonId": 1,
    "sectionName": "Verso",
    "tabContent": "e|---3---3---3---3---|---3---3---3---3---|\nB|---0---0---0---0---|---0---0---0---0---|\nG|---0---0---0---0---|---0---0---0---0---|\nD|---2---2---2---2---|---2---2---2---2---|\nA|---2---2---2---2---|---2---2---2---2---|\nE|---0---0---0---0---|---0---0---0---0---|",
    "description": "Tab para el verso de la canción",
    "order": 1,
    "difficulty": "BEGINNER"
  },
  {
    "id": 2,
    "songLessonId": 1,
    "sectionName": "Estribillo",
    "tabContent": "e|---5---5---5---5---|---5---5---5---5---|\nB|---5---5---5---5---|---5---5---5---5---|\nG|---6---6---6---6---|---6---6---6---6---|\nD|---7---7---7---7---|---7---7---7---7---|\nA|---7---7---7---7---|---7---7---7---7---|\nE|---5---5---5---5---|---5---5---5---5---|",
    "description": "Tab para el estribillo",
    "order": 2,
    "difficulty": "BEGINNER"
  }
]
```

**Campos requeridos**:
- `id` (number) - Identificador único
- `songLessonId` (number) - ID de la lección asociada
- `sectionName` (string) - Nombre de la sección (Verso, Estribillo, etc.)
- `tabContent` (string) - Contenido de la tablatura en formato ASCII
- `description` (string) - Descripción
- `order` (number) - Orden dentro de la lección
- `difficulty` (string) - Dificultad

---

## 6. ItemLists (Listas de Elementos)

**Archivo**: `data/item_lists.json`

```json
[
  {
    "id": 1,
    "userId": 1,
    "name": "Mis Acordes Favoritos",
    "description": "Colección de mis acordes favoritos",
    "type": "CHORD",
    "createdDate": "2024-01-15",
    "items": [1, 5, 10, 15],
    "isPublic": false
  },
  {
    "id": 2,
    "userId": 1,
    "name": "Canciones para Practicar",
    "description": "Canciones que quiero aprender",
    "type": "SONG",
    "createdDate": "2024-01-20",
    "items": [1, 2, 3],
    "isPublic": true
  },
  {
    "id": 3,
    "userId": 2,
    "name": "Rock Clásico",
    "description": "Mis canciones de rock clásico favoritas",
    "type": "SONG",
    "createdDate": "2024-02-01",
    "items": [2, 5, 8],
    "isPublic": true
  }
]
```

**Campos requeridos**:
- `id` (number) - Identificador único
- `userId` (number) - ID del usuario propietario
- `name` (string) - Nombre de la lista
- `description` (string) - Descripción
- `type` (string) - Tipo: `CHORD`, `SONG`, `LESSON`, `INSTRUMENT`
- `createdDate` (string) - Fecha de creación (YYYY-MM-DD)
- `items` (array) - Array de IDs de elementos en la lista
- `isPublic` (boolean) - Es pública

---

## 7. Referencia de Datos Existentes

### Géneros (de genres.json)

| ID | Nombre | ID | Nombre |
|---|---|---|---|
| 1 | Pop Punk | 4 | Funk |
| 2 | Rock & Roll | 5 | ... |
| 3 | Pop Rock | ... | ... |

### Acordes (de chords.json)

Contiene 289 acordes diferentes con propiedades como:
- `name` - Nombre del acorde (C, G, D, Am, etc.)
- `notes` - Notas musicales
- `imageUrl` - Imagen del acorde
- `open`, `barre`, `powerChord` - Tipos
- `minor`, `major`, `seventh` - Variantes

---

## ⚠️ Notas Importantes

1. **IDs únicos**: Cada `id` debe ser único dentro de su tipo
2. **Referencias válidas**: Los `Id` que referencian otras entidades deben existir
3. **Formato de fechas**: Usar formato `YYYY-MM-DD`
4. **URLs**: Deben ser URLs válidas o strings vacíos
5. **Enums**: Usar exactamente los valores indicados (BEGINNER, INTERMEDIATE, ADVANCED)
6. **UTF-8**: El contenido del archivo debe ser válido UTF-8

---

## 🔗 Ejemplo Completo de Flujo

```
1. Crear Instrumento (instruments.json)
   → ID: 1, nombre: "Guitarra Acústica"

2. Crear Género (genres.json) ✓ Ya existe
   → ID: 1, nombre: "Pop Punk"

3. Crear relación Instrumento-Género (instrument_genres.json)
   → instrumentId: 1, genreId: 1

4. Crear Canción (songs.json)
   → ID: 1, title: "Basket Case", genreId: 1

5. Crear Lección de Canción (song_lessons.json)
   → ID: 1, songId: 1

6. Crear Sección de Tab (tab_sections.json)
   → ID: 1, songLessonId: 1

7. Crear Lista de Elementos (item_lists.json)
   → ID: 1, items: [1] (referencia a la canción)
```

---

## 📞 Validación

Asegúrate de que:
- ✅ JSON es válido (usa https://jsonlint.com/)
- ✅ Todas las referencias (IDs) existen
- ✅ Codificación UTF-8
- ✅ Array con al menos `[]`
- ✅ Tipos de datos correctos (number, string, boolean, array)

---

**Última actualización**: 2026-03-22

