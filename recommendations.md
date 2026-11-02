# Recomendaciones

Este documento recoge todos los algoritmos de recomendación del backend. Empieza por los
controladores (qué endpoint usa qué servicio y desde qué pantalla) y después describe cada
algoritmo nivel a nivel.

Las convenciones generales (resultado `SequencedSet`, niveles, guardas, `idsToExclude`,
`resolvePageable`) están en `architecture.md` §5 y §8. Aquí solo se describe **qué** hace
cada algoritmo.

---

## 1. Mapa de endpoints

### `RecommendationController` — `/api/recommendations`

| Endpoint | Servicio | Límite | Respuesta | Pantalla (frontend) |
|---|---|---|---|---|
| `GET /items/by-item-list/{id}` | `ItemRecommendationService.findForItemList` | por tipo (§2) | `List<ItemDTO>` completos, polimórficos | Añadir a lista → "Para ti" |
| `GET /items/by-user/{id}` | `ItemRecommendationService.findForUser` | por tipo (§2) | `List<ItemDTO>` completos, polimórficos | Home → "Recomendaciones" |
| `GET /songs/by-user/{id}?limit=n` | `SongRecommendationService.findForUser` | `limit` obligatorio (el front pide 10) | `List<SongDTO>` | Canciones → "Según tus gustos" |
| `GET /lessons/by-user/{id}` | `LessonRecommendationService.findForUser` | `RECOMMENDED_LESSONS_COUNT` (6) | `List<LessonDTO>` | Aprender → "Para ti" |
| `GET /lessons/by-genre/{id}` | `LessonRecommendationService.findForGenre` | `BY_GENRE_RECOMMENDED_LESSONS_COUNT` (8) | `List<LessonDTO>` | Género → pestaña Instrumentos (`GenreLessonsCarousel`) |

### `SpotifyRecommendationController` — `/api/spotify`

| Endpoint | Servicio | Límite | Respuesta | Pantalla (frontend) |
|---|---|---|---|---|
| `GET /recommendations/songs/{userId}?limit=n` | `SpotifyRecommendationService.findSongsForUser` | `limit` obligatorio (el front pide 10) | `List<SongDTO>` | Canciones → carrusel de Spotify (solo con cuenta vinculada) |
| `GET /recommendations/items/{userId}` | `SpotifyRecommendationService.findItemsForUser` | — | `List<ItemDTO>` | Ninguna: en desarrollo, devuelve `null` |

### Otros endpoints con "recommended" en la ruta (no son algoritmos)

| Endpoint | Qué hace en realidad |
|---|---|
| `GET /api/lessons/recommended/ids` (`LessonController`) | Todas las lecciones ordenadas por nivel ascendente (`findAllByOrderByLevelAscIdAsc`). El frontend ya no lo llama. |
| `GET /api/songs/recommended/ids` (documentado en `SongController`) | Sin implementar. El frontend ya no lo llama. |

---

## 2. `ItemRecommendationService` (combinador)

No tiene algoritmo propio: llama al servicio de cada tipo con su límite y junta los resultados en un
`LinkedHashSet<Item>`, en este orden. El resultado se mapea a DTOs completos con
`DtoMapper.itemsToItemDTOs`.

| Orden | Tipo | Constante | Valor |
|---|---|---|---|
| 1 | Canciones | `RECOMMENDED_SONGS_COUNT` | 10 |
| 2 | Géneros | `RECOMMENDED_GENRES_COUNT` | 1 |
| 3 | InstrumentGenres | `RECOMMENDED_INSTRUMENT_GENRES_COUNT` | 3 |
| 4 | Lecciones | `RECOMMENDED_LESSONS_COUNT` | 6 |

Lo usan tanto `findForItemList` como `findForUser`, cada uno con el método equivalente de cada servicio.
Como mucho devuelve 20 items.

---

## 3. Conceptos comunes

- **Top N de una lista** (`ItemList.getTopGenresFromX` / `getTopInstrumentsFromX`): cuenta cuántas veces
  aparece cada género o instrumento en los items de la lista y los ordena de más a menos usado
  (`Utils.getTopNMostRepeated`). Lo que aporta cada tipo de item:
  - Canción: sus géneros y sus instrumentos.
  - InstrumentGenre: su género y su instrumento.
  - Lección: su género (si `belongsToGenre`) y el género y el instrumento de su InstrumentGenre (si `belongsToInstrumentGenre`).
- **Género/instrumento principal** (`getMainXFromY`): el primero del top N anterior, si lo hay.
- **Top N del usuario** (`User.getTopGenresFromItemLists` / `getTopInstrumentsFromItemLists`): lo mismo
  sumando todas sus listas. Un item que está en varias listas cuenta una vez por lista.
- **Exclusiones**: se acumulan como `Set<Long>` de IDs. En las recomendaciones para una lista se parte de lo
  que ya tiene la lista; en las de usuario, de `User.getIdsExcludedFromRecommendationsOfType(Tipo.class)`, es
  decir, lo que está en `N_ITEM_LISTS_TO_EXCLUDE_FROM_RECOMMENDATIONS` (3) o más de sus listas (lo que solo está
  en una o dos se puede seguir recomendando). Cada nivel añade lo que recomienda, así que nunca se repite nada.
  La exclusión se hace en la consulta (`...IdNotIn`) para que lo excluido no ocupe sitio en el límite.
- **Nivel del usuario**: `User.difficultyLevel` (0–5), comparado con `Item.level` (1–5) mediante `LevelLessThanEqual`.

---

## 4. Recomendaciones para una lista (`findForItemList`)

Se excluye siempre lo que ya está en la lista y lo ya recomendado.

### 4.1 Canciones — `SongRecommendationServiceImpl.findForItemList`

Datos de partida:
- **Género principal**: el único género de la lista, si tiene exactamente uno como item; si no, el principal de sus canciones.
- **Top instrumentos**: los `NUMBER_OF_MATCHING_INSTRUMENTS_IN_SONG_RECOMMENDATION` (3) instrumentos más usados en las canciones de la lista.

| Nivel | Qué saca | Cuántas |
|---|---|---|
| 1 | Canciones del género principal **y** con alguno de los top instrumentos (solo si hay top instrumentos) | hasta `limit` |
| 2 | Lo que falta, repartido: mitad del género principal (la mitad grande si es impar) y mitad con alguno de los top instrumentos | lo que falte |
| 3a | Si la lista tiene un único InstrumentGenre (con género e instrumento): canciones de su género con su instrumento | lo que falte |
| 3b | Si no: canciones de hasta `MIN_NUMBER_OF_FAVOURITE_GENRES` géneros favoritos y `MIN_NUMBER_OF_FAVOURITE_INSTRUMENTS` instrumentos favoritos del usuario, elegidos al azar (`Collections.shuffle`) | lo que falte |

### 4.2 Géneros — `GenreRecommendationServiceImpl.findForItemList`

| Nivel | Qué saca |
|---|---|
| 1 | El género principal de las canciones, el de los InstrumentGenres y el de las lecciones, en ese orden |
| 2 | Los top `NUMBER_OF_GENRES_USED_IN_GENRE_RECOMMENDATION + 1` (2) géneros de las canciones, luego los de los InstrumentGenres y luego los de las lecciones |
| 3 | Los géneros favoritos del usuario |
| 4 | Cualquier otro género (`findByIdNotIn`) |

En los niveles 1–3 se descarta cualquier candidato que ya esté en la lista. Se para en cuanto se llega al límite.

### 4.3 InstrumentGenres — `InstrumentGenreRecommendationServiceImpl.findForItemList`

| Nivel | Qué saca |
|---|---|
| 1 | El InstrumentGenre que combina el género principal de la lista (el único género, o si no el principal de todos sus items) con su instrumento principal |
| 2 | Las combinaciones de los top `NUMBER_OF_MAX_TOP_INSTRUMENTS_AND_GENRES_USED_IN_INSTRUMENT_GENRE_RECOMMENDATION` (3) géneros × top 3 instrumentos de los items de la lista, en orden género a género |

Cada combinación es una consulta `findByGenreIdAndInstrumentId` (hasta 1 + 9). Si no existe un InstrumentGenre
para esa combinación no se añade nada en su lugar: el resultado puede quedarse por debajo del límite.

### 4.4 Lecciones — `LessonRecommendationServiceImpl.findForItemList`

| Nivel | Qué saca | Cuántas |
|---|---|---|
| 1 | Lecciones del género más usado en los items de la lista | 2/3 del límite (redondeado hacia abajo) |
| 2 | Lecciones de cada género favorito del usuario, saltándose el del nivel 1 | lo que falte, favorito a favorito |
| 3 | Cualquier otra lección (`findByIdNotIn`) | lo que falte |

---

## 5. Recomendaciones para un usuario (`findForUser`)

Se excluye siempre lo que el usuario tiene en **3 o más** de sus listas (`N_ITEM_LISTS_TO_EXCLUDE_FROM_RECOMMENDATIONS`) y lo ya recomendado.

### 5.1 Canciones — `SongRecommendationServiceImpl.findForUser`

Datos de partida:
- **Géneros**: los `NUMBER_OF_GENRES_USED_IN_BY_USER_SONG_RECOMMENDATION - 1` (3) más usados en sus listas, completados con sus favoritos hasta 4. Siempre entra al menos un favorito.
- **Instrumentos**: igual, con `NUMBER_OF_INSTRUMENTS_USED_IN_BY_USER_SONG_RECOMMENDATION` (4).

| Nivel | Qué saca | Cuántas |
|---|---|---|
| 1 | Canciones de cada género con nivel ≤ el del usuario | `limit / 2` repartido entre los géneros; el resto de la división da una canción más a los primeros |
| 2 | Canciones de cada instrumento con nivel ≤ el del usuario | la otra mitad (`limit / 2`), repartida igual |
| — | Si `limit` es impar: una canción más del primer género (sin comprobar nivel) | 1 |
| 3 | Por la lista de géneros, en orden y sin comprobar nivel: todas las que falten de un género antes de pasar al siguiente | lo que falte |
| 4 | Igual que el 3 con la lista de instrumentos | lo que falte |

Si el usuario no tiene géneros (o instrumentos) ni en listas ni en favoritos, esa mitad se salta y la
completan los niveles 3 y 4 con lo que sí tenga.

### 5.2 Géneros — `GenreRecommendationServiceImpl.findForUser`

Candidatos (sin los géneros que ya tiene en 3 o más de sus listas):
- **Favoritos** del usuario.
- **De sus listas**: todos los géneros de sus listas, de más a menos usado.

| Nivel | Qué saca | Cuántos |
|---|---|---|
| 1 | Favoritos | la mitad grande del límite (`limit - limit / 2`); con límite 1 sale un favorito |
| 2 | De sus listas | hasta completar el límite |
| 3 | Si una fuente se ha quedado corta: lo que quede de favoritos y después de sus listas | hasta completar el límite |
| 4 | Cualquier otro género (`findByIdNotIn`) | lo que falte |

### 5.3 InstrumentGenres — `InstrumentGenreRecommendationServiceImpl.findForUser`

Candidatos (IDs sin repetir, en este orden):
- **Géneros**: los `NUMBER_OF_ITEM_LISTS_GENRES_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION` (2) más usados en sus listas y los `NUMBER_OF_FAVOURITE_GENRES_USED_IN_BY_USER_INSTRUMENT_GENRE_RECOMMENDATION` (2) primeros favoritos.
- **Instrumentos**: igual, con las constantes de instrumentos (2 + 2).

| Nivel | Qué saca |
|---|---|
| 1 | InstrumentGenres de cualquier combinación de esos géneros × instrumentos con nivel ≤ el del usuario (una sola consulta `GenreIdIn` + `InstrumentIdIn`) |
| 2 | Lo que falte, de las mismas combinaciones sin comprobar nivel |

Si no hay ningún género o ningún instrumento candidato, devuelve vacío sin consultar.

### 5.4 Lecciones — `LessonRecommendationServiceImpl.findForUser`

Candidatos (IDs sin repetir, en este orden):
- **Géneros**: el primer favorito y los `NUMBER_OF_ITEM_LISTS_GENRES_USED_IN_BY_USER_LESSON_RECOMMENDATION` (2) más usados en sus listas.
- **Instrumentos**: el primer favorito y los `NUMBER_OF_ITEM_LISTS_INSTRUMENTS_USED_IN_BY_USER_LESSON_RECOMMENDATION` (2) más usados en sus listas.

El límite se reparte en tres partes:

| Parte | Filtro | Cupo (con límite 6) |
|---|---|---|
| Por género | `genre ∈ géneros` y `belongsToGenre = true` (no se cuelan las de un InstrumentGenre) | mitad grande de `limit / 2` (2) |
| Por instrumento | `instrument ∈ instrumentos` y `belongsToInstrument = true` | mitad pequeña de `limit / 2` (1) |
| Genéricas | `belongsToGenre`, `belongsToInstrument` y `belongsToInstrumentGenre` a `false` | `limit - limit / 2` (3) |

| Nivel | Qué saca |
|---|---|
| 1 | Las tres partes con nivel ≤ el del usuario |
| 2 | Lo que le falte a cada parte, sin comprobar nivel |

Si no hay géneros (o instrumentos) candidatos, esa parte tiene cupo 0. Ninguna parte rellena el hueco de
otra, así que el resultado puede quedarse por debajo del límite.

### 5.5 Canciones de Spotify — `SpotifyRecommendationServiceImpl.findSongsForUser`

Requiere la cuenta de Spotify vinculada. El token se renueva solo si ha caducado (`SpotifyAuthService.getValidAccessToken`).

| Paso | Qué hace |
|---|---|
| 1 | Pide a Spotify el doble del límite (máximo 50) de **canciones top** del usuario, repartidas así: mitad `medium_term`, y la otra mitad a partes iguales entre `long_term` y `short_term`. De cada una se queda con título y artista principal. |
| 2 | Las busca todas en la base de datos con una sola consulta (`SongRepository.findByTitleAndArtistIn`, pareja título/artista sin distinguir mayúsculas). |
| 3 | Se queda con las encontradas en el orden de Spotify, sin repetidas y sin las que el usuario ya tiene en 3 o más de sus listas, hasta el límite. |
| 4 | Si faltan: pide sus `NUMBER_OF_TOP_ARTISTS_TO_FETCH_FROM_SPOTIFY` (5) **artistas top** (`medium_term`) y saca canciones de cada uno: primero sus populares, luego sus clásicas y luego sus nuevas (`artistPopular` / `artistClassic` / `artistNew`). Cada artista aporta `faltan / nº artistas` y el primero se queda además con el resto. |

Las exclusiones del paso 4 parten de las canciones que están en 3 o más listas del usuario más las del paso 3.

`findItemsForUser` (`/recommendations/items/{userId}`) está a medias: pide el artista top y sus top tracks
a Spotify, pero devuelve `null`.

---

## 6. Lecciones de la pantalla de género — `LessonRecommendationServiceImpl.findForGenre`

No es un algoritmo por niveles. Es una selección editorial que se sirve desde el controlador de
recomendaciones:

- Lecciones del género con `genrePopular = true` y `belongsToInstrumentGenre = false`, es decir, las marcadas a
  mano como destacadas del género y que no forman parte de un InstrumentGenre.
- Hasta `BY_GENRE_RECOMMENDED_LESSONS_COUNT` (8).
- No depende del usuario, así que no excluye nada.

Se pinta en el carrusel de lecciones (`GenreLessonsCarousel`) de la pestaña Instrumentos de la pantalla de género.

---

## 7. Observaciones

- **Empates en los top N**: `getTopNMostRepeated` agrupa con un `HashMap`, así que entre géneros o
  instrumentos igual de usados el orden no está definido.
- **"Aleatorios" que no lo son**: el último nivel de géneros y lecciones para una lista (`findByIdNotIn`)
  devuelve las primeras filas en el orden de la base de datos, no al azar. La única aleatoriedad real es el
  `Collections.shuffle` del nivel 3b de canciones para una lista.
- **Favorito "principal"**: las preferencias son un `Set`, así que "el primer favorito" (lecciones e
  InstrumentGenres por usuario) es el primero en el orden de iteración del set, no uno elegido por el usuario.
- **Constantes sin usar**: `HOME_RECOMMENDED_*_COUNT`, `BY_USER_RECOMMENDED_SONGS_COUNT` y
  `NUMBER_OF_FAVOURITE_GENRES_USED_IN_GENRE_RECOMMENDATION` no se usan en ningún sitio.
- **Consultas derivadas**: los nombres de los métodos de recomendación de los repositorios solo se validan
  al arrancar Spring, no al compilar.
