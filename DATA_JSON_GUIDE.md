# Guia unica de JSON de datos (TFG)

Este documento resume la estructura de los JSON en `data/` para generar datos validos y coherentes con el backend y el frontend. Se basa en los DTOs y entidades actuales.

Checklist rapido
- Cada archivo es un array JSON (minimo `[]`).
- IDs numericos, unicos dentro de su tipo cuando se usen.
- Referencias entre entidades deben existir.
- URLs deben ser validas (HTTP/HTTPS) cuando el modelo lo exige.

---

## 1) `data/chords.json`
Entidad/DTO: `Chord` / `ChordDto`

Estructura (por item):
- `id` (number, opcional)
- `name` (string, obligatorio, unico)
- `notes` (array string, obligatorio, no vacio)
- `imageUrl` (string, obligatorio, URL http/https)
- `open` (boolean)
- `barre` (boolean)
- `powerChord` (boolean)
- `minor` (boolean)
- `major` (boolean)
- `seventh` (boolean)
- `capo` (boolean)
- `capoFret` (number, 0-12)

Notas:
- `capoFret` esta validado entre 0 y 12 en el backend.

Ejemplo real (ya hay datos):
```json
{
  "name": "Gmaj7",
  "notes": ["G", "B", "D", "F#"],
  "imageUrl": "https://...",
  "open": true,
  "barre": false,
  "powerChord": false,
  "minor": false,
  "major": true,
  "seventh": true,
  "capo": false,
  "capoFret": 0
}
```

---

## 2) `data/genres.json`
Entidad/DTO: `Genre` / `GenreDTO`

Estructura (por item):
- `id` (number, opcional)
- `name` (string, obligatorio)
- `description` (string, max 1000)
- `bannerUrl` (string, URL http/https o vacio en DTO)
- `imageUrl` (string, URL http/https o vacio en DTO)
- `welcomeIntroVideoId` (string)
- `welcomeSongVideoId` (string)
- `lessonIds` (array number, opcional)

Validaciones sensibles:
- En la entidad `Genre` los campos `bannerUrl`, `welcomeIntroVideo` y `welcomeSongVideo` son obligatorios y deben ser URL completas.
- En los JSON actuales se guardan IDs (ej. `"NstT7n8KzZQ"`). Si falla la validacion, usar URL completa.

Ejemplo real (ya hay datos):
```json
{
  "name": "Pop Punk",
  "bannerUrl": "https://...",
  "imageUrl": "https://...",
  "welcomeIntroVideoId": "NstT7n8KzZQ",
  "welcomeSongVideoId": "hTWKbfoikeg",
  "description": "..."
}
```

---

## 3) `data/instruments.json`
Entidad/DTO: `Instrument` / `InstrumentDTO`

Estructura (por item):
- `id` (number, opcional)
- `name` (string, obligatorio, 1-100)
- `imageUrl` (string, obligatorio, URL http/https)

Estado actual: `[]` (vacio).

---

## 4) `data/instrument_genres.json`
Entidad/DTO: `InstrumentGenre` / `InstrumentGenreDTO`

Estructura (por item):
- `id` (number, opcional)
- `instrumentId` (number, obligatorio)
- `genreId` (number, obligatorio)
- `lessonIds` (array number, opcional)

Validacion sensible:
- `InstrumentGenre` hereda de `Item` (requiere `title` e `imageUrl` en entidad), pero el DTO no los incluye. Si el backend valida entidad, puede fallar.

Estado actual: `[]` (vacio).

---

## 5) `data/songs.json`
Entidad/DTO: `Song` / `SongDTO`

Estructura (por item):
- `id` (number, opcional)
- `title` (string, obligatorio, 1-200)
- `artist` (string, obligatorio, 1-200)
- `imageUrl` (string, URL http/https)
- `genreIds` (array number, opcional)
- `instrumentIds` (array number, opcional)
- `lessons` (array number, opcional)

Validaciones sensibles:
- `Song` hereda `imageUrl` desde `Item` y lo marca como obligatorio en entidad, aunque `SongDTO` no lo requiere.
- Campos como `popular`, `decade`, `genreClassic`, `genrePopular` y `genreNew` existen en entidad pero no en DTO.

Estado actual: `[]` (vacio).

---

## 6) `data/song_lessons.json`
Entidad/DTO: `SongLesson` / `SongLessonDTO`

Estructura (por item):
- `id` (number, opcional)
- `difficulty` (string: `EASY` | `MEDIUM` | `ORIGINAL`)
- `tabSectionsIds` (array number, opcional)
- `chordIds` (array number, opcional)
- `cordedLyrics` (string, max 5000)  
- `videoUrl` (string, URL http/https)
- `songId` (number, obligatorio)
- `instrumentId` (number, obligatorio)

Notas:
- El DTO usa el campo `cordedLyrics` (sin "h"), aunque la entidad es `chordedLyrics`.

Estado actual: `[]` (vacio).

---

## 7) `data/tab_sections.json`
Entidad/DTO: `TabSection` / `TabSectionDTO`

Estructura (por item):
- `id` (number, opcional)
- `identifier` (string, obligatorio, 1-50)
- `content` (string, obligatorio, 1-10000)
- `songLessonId` (number, obligatorio)

Estado actual: `[]` (vacio).

---

## 8) `data/item_lists.json`
Entidad/DTO: `ItemList` / `ItemListDTO`

Estructura (por item):
- `id` (number, opcional)
- `name` (string, obligatorio, 1-100)
- `imageUrl` (string, URL http/https)
- `userId` (number, obligatorio)
- `itemIds` (array number, opcional)

Estado actual: `[]` (vacio).

---

## Coherencia y relaciones
- `instrument_genres.instrumentId` debe existir en `instruments.json`.
- `instrument_genres.genreId` debe existir en `genres.json`.
- `song_lessons.songId` debe existir en `songs.json`.
- `song_lessons.instrumentId` debe existir en `instruments.json`.
- `tab_sections.songLessonId` debe existir en `song_lessons.json`.
- `item_lists.itemIds` deben referir IDs de Items (Song, Lesson, Genre, InstrumentGenre) ya creados.

---

## Datos actuales en `data/`
- `chords.json`: contiene datos reales.
- `genres.json`: contiene datos reales (con IDs de video, no URLs completas).
- Resto: arrays vacios.


