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
- `name` (string, obligatorio, unico por instrumento)
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
- `instrumentId` (number, obligatorio)

Notas:
- `capoFret` esta validado entre 0 y 12 en el backend.
- El mismo nombre puede repetirse en instrumentos distintos (p. ej. `Cm` de guitarra y `Cm` de piano), con `id` distinto.
- Los `chordIds` de una lección de canción deben ser acordes del `instrumentId` de esa lección; si no, el backend responde 400.
- Todos los acordes actuales son de guitarra (`instrumentId: 1`).

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
  "capoFret": 0,
  "instrumentId": 1
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

Relacion con las lecciones:
- Cada leccion pertenece como mucho a un InstrumentGenre (`Lesson.instrumentGenre`, columna `lessons.instrument_genre_id`).
- Al crear/actualizar el InstrumentGenre, las lecciones de `lessonIds` quedan con `belongsToInstrumentGenre = true` e `indexInInstrumentGenre` = su posicion en el array (empezando en 0). Por eso las lecciones deben cargarse antes.
- Tambien se puede asignar desde la propia leccion (`LessonDTO`): `instrumentGenreId` + `indexInInstrumentGenre`.

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
- `decade` (string, formato `1990s`)
- `popular`, `newSong` (boolean): carrusel "Populares" del catalogo; `newSong` no lo usa ningun carrusel
- `genreClassic`, `genrePopular`, `genreNew` (boolean): carruseles del genero. `genreNew` tambien es el carrusel "Nuevas" de la pantalla de canciones
- `artistClassic`, `artistPopular`, `artistNew` (boolean): carruseles de artistas favoritos del Home
- `lessons` (array number, opcional)

Validaciones sensibles:
- `Song` hereda `imageUrl` desde `Item` y lo marca como obligatorio en entidad, aunque `SongDTO` no lo requiere.
- Los flags de carrusel solo se guardan si vienen en el JSON: una cancion sin ellos no sale en ningun carrusel (solo en busqueda y recomendaciones).

Estado actual: `[]` (vacio).

---

## 6) `data/song_lessons.json`
Entidad/DTO: `SongLesson` / `SongLessonDTO`

Estructura (por item):
- `id` (number, opcional)
- `difficulty` (string: `EASY` | `MEDIUM` | `ORIGINAL`)
- `tabSectionsIds` (array number, opcional)
- `chordIds` (array number, opcional; todos del mismo `instrumentId` que la leccion)
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

## 9) `data/lessons.json`
Entidad/DTO: `Lesson` / `LessonDTO` (las secciones viajan dentro, como `LessonSectionDTO`)

Estructura (por item):
- `id` (number, obligatorio: los IDs de `Item` se asignan a mano)
- `title` (string, obligatorio)
- `level` (number, 1-5)
- `imageUrl` (string, opcional; si falta y pertenece a un genero, se usa la imagen del genero)
- `belongsToGenre` + `genreId`, `belongsToInstrument` + `instrumentId` (opcionales)
- `popular`, `genrePopular`, `instrumentPopular` (boolean)
- `sections` (array, obligatorio, no vacio), cada una con:
  - `title` (string, obligatorio)
  - `htmlContent` (string, obligatorio; HTML con su propio titulo)
  - `orderIndex` (number, desde 0)
  - `videoUrl` (string, opcional): enlace de YouTube de apoyo (`https://www.youtube.com/watch?v=...`, `https://youtu.be/...`). La app lo muestra encima del contenido de la seccion.

Notas:
- Se cargan antes que `instrument_genres.json`: los instrument-genres referencian las lecciones por `lessonIds`.
- Igual que `song_lessons.videoUrl`, el video debe ser de YouTube: la app extrae el ID del video de la URL.

---

## 10) `data/tunes.json`
Entidad/DTO: `Tune` / `TuneDTO`. Afinaciones que ofrece el afinador.

Estructura (por item):
- `id` (number, obligatorio): asignado a mano, como el resto del catálogo. Dentro de un instrumento, la app las muestra por id: la estándar va primero.
- `title` (string, obligatorio, max 50): nombre de la afinación (`Estándar`, `Drop D`...). La app muestra `Instrumento (title)`.
- `instrumentId` (number, obligatorio): debe existir en `instruments.json`.
- `notes` (array string, obligatorio, no vacío): una nota por cuerda, **de la más grave a la más aguda**, en notación científica: nombre en mayúscula, `#` opcional y octava 0-8 (`E2`, `F#3`). No se admiten bemoles (`Bb2` se escribe `A#2`) ni notas sin octava.

Ejemplo real:
```json
{ "id": 2, "title": "Drop D", "instrumentId": 1, "notes": ["D2", "A2", "D3", "G3", "B3", "E4"] }
```

Notas:
- Se cargan las últimas (`scripts/insert_tunes.py`), después de los instrumentos.
- La frecuencia de cada nota no se guarda: la calcula la app (La4 = 440 Hz).

---

## Coherencia y relaciones
- `instrument_genres.instrumentId` debe existir en `instruments.json`.
- `instrument_genres.genreId` debe existir en `genres.json`.
- `song_lessons.songId` debe existir en `songs.json`.
- `song_lessons.instrumentId` debe existir en `instruments.json`.
- `tab_sections.songLessonId` debe existir en `song_lessons.json`.
- `item_lists.itemIds` deben referir IDs de Items (Song, Lesson, Genre, InstrumentGenre) ya creados.
- `tunes.instrumentId` debe existir en `instruments.json`.

---

## Datos actuales en `data/`
- `chords.json`: contiene datos reales.
- `genres.json`: contiene datos reales (con IDs de video, no URLs completas).
- `tunes.json`: afinaciones de guitarra (estándar, Drop D, medio tono abajo, Open G, DADGAD), bajo (estándar, Drop D, 5 cuerdas) y ukelele (estándar).
- Resto: arrays vacios.


