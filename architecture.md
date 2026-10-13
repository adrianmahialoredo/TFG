# Arquitectura del proyecto

Este documento recoge las decisiones de arquitectura, patrones y nomenclatura del proyecto (backend Spring Boot + app React Native). Es la referencia para escribir código nuevo: si algo no encaja con lo que aquí se describe, o se adapta el código o se actualiza este documento.

La arquitectura interna del frontend está detallada en [`frontend/bible.md`](frontend/bible.md); aquí se resume y se documenta el contrato entre ambos.

---

## 1. Visión general

| Pieza | Tecnología |
|---|---|
| Backend | Spring Boot 3.4, Java 23, Spring Data JPA (Hibernate 6), Spring Security + JWT, Lombok |
| Base de datos | PostgreSQL. Esquema gestionado por Hibernate (`spring.jpa.hibernate.ddl-auto=update`) |
| Frontend | React Native (Expo), Axios, Zustand, AsyncStorage |
| Carga de datos | Scripts Python en `scripts/` que llaman a la API (`load_all_data.py`) |
| Emails | Spring Mail (SMTP, Gmail) con plantillas hechas en React Email (`emails/`) y exportadas a `templates/email` |

### Capas del backend

```
controller  ->  service (interfaz + Impl)  ->  repository  ->  model (entidades JPA)
     |               |
     |               +-- utils/EntityResolver   (id -> entidad)
     |               +-- utils/mapper           (entidad <-> DTO)
     +-- dto (contrato de la API)
exception/GlobalExceptionHandler  (excepción -> respuesta HTTP)
```

| Paquete | Contenido y regla |
|---|---|
| `controller` | Solo HTTP: rutas, parámetros, códigos de respuesta. Sin lógica de negocio ni acceso a repositorios. |
| `service` | Una interfaz `XService` y su implementación `XServiceImpl` por recurso. Trabaja con DTOs hacia fuera y con entidades hacia dentro. |
| `repository` | Interfaces Spring Data (`JpaRepository<Entidad, Long>`). |
| `model` | Entidades JPA y la lógica de dominio que depende solo de la propia entidad (p. ej. los cálculos de `ItemList`). |
| `dto` | Objetos de la API. Las relaciones viajan como IDs (`genreId`, `lessonIds`…), nunca como entidades anidadas. |
| `utils` | `EntityResolver`, `Utils` (`resolvePageable`, `getTopNMostRepeated`, `subtract`), `RecommendationConstants`, `Identifiable`. |
| `utils/mapper` | `DtoMapper` (entidad → DTO) y `EntityMapper` (DTO → entidad). Los mappers no acceden a repositorios: reciben las entidades relacionadas ya resueltas. |
| `exception` | `GlobalExceptionHandler` (`@RestControllerAdvice`). |
| `security` / `config` | Filtro JWT, `SecurityConfig`, propiedades de Spotify. |

---

## 2. Modelo de dominio

### Jerarquía de `Item`

- `Item` es la base abstracta de `Song`, `Lesson`, `Genre` e `InstrumentGenre` (`@Inheritance(JOINED)`). Comparten espacio de IDs.
- Los IDs de `Item`, `Instrument`, `Chord` y `SongLesson` se **asignan manualmente** (vienen en los JSON de `data/`); no hay `@GeneratedValue`. Sí lo tienen `User`, `ItemList`, `LessonSection` y `TabSection`.

### Igualdad de entidades

- `Item`, `Instrument`, `Chord` y `SongLesson` implementan `equals`/`hashCode` **por id** (con `instanceof` para que funcione con proxies de Hibernate).
- Motivo: las colecciones del dominio son `Set`; sin esto un `Set` podría contener dos instancias de la misma canción.
- Requisito: el id debe estar asignado **antes** de meter la entidad en un `Set` (se cumple porque los IDs son manuales).
- Las entidades que se usan en rankings implementan `Identifiable<Long>`.

### Colecciones: `Set` vs `List`

Regla: **en el dominio musical nunca hay entidades repetidas**, así que las colecciones de entidades son `Set`. Se usa `List` solo cuando el orden es parte del dato o cuando se cuentan repeticiones.

| Colección | Tipo | Motivo |
|---|---|---|
| `Song.genres`, `Song.instruments`, `Genre.lessons`, `Preferences.favorite*` | `Set` | Relación sin duplicados |
| `Song.lessons`, `SongLesson.chords` | `Set` + `@OrderBy("id ASC")` | Sin duplicados y con orden estable (el frontend usa el primero como lección por defecto) |
| `InstrumentGenre.lessons` | `Set` + `@OrderBy("indexInInstrumentGenre ASC")` | Lecciones ordenadas dentro del instrument-genre |
| `ItemList.items` | `List` **sin duplicados** | Conserva el orden en que el usuario añadió los items. `addItem` ignora repetidos y `setItems` deduplica |
| `Lesson.sections`, `SongLesson.tabSections`, `Chord.notes` | `List` | Contenido ordenado |
| `User.itemLists` | `List` | Fuera del dominio musical |

En el código de servicios se aplica lo mismo: resultados como `Set`/`LinkedHashSet` (o `SequencedSet` si el orden importa). Excepciones deliberadas: los **rankings** (`getTopXFrom…`) son `List` porque están ordenados y se accede por posición, la lista interna de `getTopN` es `List` porque cuenta repeticiones, y las copias para `Collections.shuffle` necesitan `List`.

### Relaciones bidireccionales

- Un solo dueño por relación. Ejemplo: `Lesson.instrumentGenre` (`@ManyToOne`, columna `lessons.instrument_genre_id`) es el dueño; `InstrumentGenre.lessons` es el lado inverso (`mappedBy`).
- La sincronización de ambos lados vive en la entidad: `InstrumentGenre.setLessons(...)` une las lecciones en orden (su índice es su posición) y desvincula las que salen; `Lesson.joinInstrumentGenre(ig, index)` / `leaveInstrumentGenre()` mantienen coherentes FK, `belongsToInstrumentGenre` e `indexInInstrumentGenre`.
- Como el dueño es la lección, el servicio enlaza las lecciones con la instancia **ya guardada** del instrument-genre y dentro de `@Transactional`. Al borrar un instrument-genre primero se desvinculan sus lecciones.

### Lógica de dominio en las entidades (`ItemList`)

- `ItemList` calcula a partir de sus propios items: `getItemsOfType(Class)`, `getOnlyItemOfType(Class)`, `getSongs()`, `getGenres()`, rankings `getTopGenresFrom{Songs,InstrumentGenres,Lessons,Items}`, `getTopInstrumentsFrom{Songs,Lessons,Items}` y los "principales" `getMainGenreFrom…` / `getMainInstrumentFromItems`.
- Todos los rankings pasan por un único `getTopN(topN, extractor)` y unos extractores por tipo de item, de modo que la regla de cada tipo está en un solo sitio. Una lección aporta su género (si `belongsToGenre`) y el género e instrumento de su instrument-genre (si `belongsToInstrumentGenre`).
- Criterio para sacar lógica de la entidad a otra clase: cuando necesite datos externos (repositorios, preferencias del usuario) o pesos/puntuaciones.
- Los parámetros de los algoritmos **no** viven en la entidad: están en `utils/RecommendationConstants`.

### Esquema y migraciones

- Hibernate añade columnas y tablas, pero nunca borra ni relaja columnas existentes.
- Campos primitivos nuevos en tablas con datos: añadir `@ColumnDefault` (PostgreSQL rechaza añadir una columna `NOT NULL` sin valor por defecto si la tabla ya tiene filas).
- Cambios que requieren mover datos se hacen con un script idempotente en `scripts/migrate_*.py` (ejemplos: `migrate_preferences_table.py`, `migrate_instrument_genre_lessons.py`, `migrate_song_chords_join_column.py`).
- Hibernate tampoco renombra columnas: si cambia el nombre de una columna o de una columna de unión, hace falta un script de migración (si no, intenta añadir la nueva y falla si es `NOT NULL`).

---

## 3. Resolución de entidades: `EntityResolver`

Nadie llama a `repository.findById` fuera de `utils/EntityResolver`. Es un `@Component` que recibe los repositorios por constructor y los guarda en campos estáticos, para que los servicios hagan `import static ...EntityResolver.resolveX`.

| Método | Devuelve | Si el id es `null` | Si no existe | Uso |
|---|---|---|---|---|
| `findX(id)` | `Optional<X>` | `Optional.empty()` | `Optional.empty()` | El recurso pedido en la URL: el controlador convierte el vacío en **404** |
| `resolveX(id)` | `X` | `IllegalArgumentException` (**400**) | `EntityNotFoundException` (**404**) | Relaciones que tienen que existir (IDs en el body) |
| `resolveXs(ids)` | `Set<X>` (`LinkedHashSet`) | colección vacía si la lista es `null`/vacía | `EntityNotFoundException` | Colecciones de relaciones, en el orden pedido |

Hay `findX`/`resolveX` para todas las entidades con repositorio. `resolveX` está construido sobre `findX`, así que la consulta vive en un único sitio.

---

## 4. Patrón `Optional`

- Todo método que puede no tener resultado devuelve `Optional`, nunca `null`: `findX`, `service.findById`, `update`, `ItemList.getOnly…`, `ItemList.getMain…`, `UserService.findFavoriteGenreIdsForUser`…
- Se consume de forma funcional: `map`, `filter`, `or`, `ifPresent`, `ifPresentOrElse`, `orElseThrow`. **Prohibido** `isPresent()` + `get()`.
- Relaciones opcionales (0..1) y campos opcionales de los DTOs se tratan con `Optional.ofNullable(...)` (p. ej. `genreId` de una lección, cabecera `Authorization` en el filtro JWT).
- No se usa `Optional` para colecciones (se devuelve la colección vacía) ni para validaciones de argumentos.
- En los controladores: `ResponseEntity.of(optional)` (200 o 404).

---

## 5. Repositorios

1. **Consultas derivadas del nombre** siempre que sea posible. `@Query` solo cuando no se puede expresar con el nombre.
2. `Distinct` solo cuando se filtra una colección con `In`, porque una canción que coincide con varios de los valores saldría repetida: `findDistinctByInstrumentsInAndIdNotIn`. Filtrar por un único id (`findByGenresIdAndGenreNewTrue`) no repite resultados: la tabla intermedia tiene clave primaria, así que cada canción solo puede casar con ese género una vez.
3. Exclusiones con IDs: `...AndIdNotIn(Collection<Long> excludedIds, ...)` (el servicio convierte las entidades a IDs). Los IDs se pasan **siempre** por `Utils.idsToExclude(ids)`: con una colección vacía Hibernate enlaza un único `NULL` (`id not in (null)`), que en SQL nunca es cierto, y la consulta no devolvería ninguna fila. `idsToExclude` sustituye la colección vacía por un ID imposible (`-1`).
4. `In` sobre colecciones de entidades: `findDistinctByInstrumentsInAndIdNotIn(Collection<Instrument>, ...)`.
5. Límites con `Pageable` construido por `Utils.resolvePageable(limit)` (`null` = sin límite; `<= 0` lanza `IllegalArgumentException`). Los servicios no llaman a `resolvePageable(0)`: comprueban antes si falta algo.
6. No se redefinen métodos que ya da `JpaRepository` (`findAllById` ya devuelve `List`).
7. Si el dato se obtiene navegando una relación de la entidad, no se crea una consulta (p. ej. IDs de los acordes de una lección = `songLesson.getChords()`; IDs de las listas de un usuario = `user.getItemLists()`).
8. Los repositorios no contienen métodos `default` de conveniencia ni marcadores tipo `WHERE 1 = 0`: eso va en el servicio. **Excepción:** una consulta que no se puede derivar del nombre ni escribir con una `@Query` fija porque depende de un número variable de condiciones se construye con una `Specification` en un método `default` del propio repositorio (que extiende `JpaSpecificationExecutor`), para que los servicios no monten criterios JPA. Ejemplo: `SongRepository.findByTitleAndArtistIn(Collection<TitleArtist>)`, un OR de parejas título/artista sin distinguir mayúsculas que usan las recomendaciones de Spotify; el record `TitleArtist` vive en el repositorio.
9. Las consultas **sin** `Pageable` pueden devolver `Set` (Spring Data convierte el resultado). Las que reciben `Pageable` **tienen que** devolver `List`, `Page`, `Slice` o `Window`; si no, Spring falla al arrancar ("Method has to have one of the following return types"). Las consultas de recomendación devuelven `List` y el servicio añade el resultado a su `LinkedHashSet`, que es quien garantiza que no haya duplicados y que se mantenga el orden. Este error no lo detecta la compilación: aparece al arrancar.

---

## 6. Nomenclatura

### Servicios y controladores

El nombre de la entidad **no** se repite en el método (ya lo indica la clase). Los servicios usan `find…` para leer; los controladores, `get…`.

| Operación | Servicio | Controlador |
|---|---|---|
| Todos | `findAll()` | `getAll()` |
| Por id | `findById(id)` → `Optional` | `getById(id)` |
| Por varios ids | `findByIds(ids)` | `getByIds(ids)` |
| Filtro por relación | `find[Calificador]For<Relación>(id)` | `get[Calificador]For<Relación>(id)` |
| Filtro global | `find<Calificador>(...)` | `get<Calificador>(...)` |
| Solo IDs | `findIds[Calificador][For<Relación>](...)` | `getIds[Calificador][For<Relación>](...)` |
| Crear / actualizar / borrar | `create(dto)` / `update(id, dto)` → `Optional` / `delete(id)` → `boolean` | `create` / `update` / `delete` |
| Acciones | verbo de la acción: `addItem`, `removeItem`, `markAsAccessed`, `search`, `register`, `login` | igual |

Ejemplos de la cadena completa:

| Repositorio | Servicio | Controlador | Ruta |
|---|---|---|---|
| `findDistinctByGenresIdAndGenreNewTrue` | `findNewForGenre` | `getNewForGenre` | `GET /api/songs/by-genre/{genreId}/new` |
| `findByDecade` | `findForDecade` | `getForDecade` | `GET /api/songs/by-decade/{decade}` |
| `findByPopularTrue` | `findPopular` | `getPopular` | `GET /api/songs/popular` |
| `findByPopularTrue` | `findIdsPopular` | `getIdsPopular` | `GET /api/lessons/popular/ids` |
| `findByGenreIdAndBelongsToGenreTrue` | `findForGenre` | `getForGenre` | `GET /api/lessons/by-genre/{genreId}` |
| `findBySongId` | `findForSong` | `getForSong` | `GET /api/song-lessons/by-song/{songId}` |
| (navegación `user.getItemLists()`) | `findIdsForUser` | `getIdsForUser` | `GET /api/item-lists/by-user/{userId}/ids` |

Servicios de recomendación: `findForItemList(id, limit)`; `ItemService.findRecommendedForItemList` / `findRecommendedForUser`.

Métodos privados de ayuda en los `Impl`: `updateFromDto`, `updateEntity`, `addCandidate(s)`… (no deben coincidir con los nombres públicos).

### Otros nombres

- Entidades en singular; DTOs `XDTO` (algunos históricos `XDto`: `ChordDto`, `UserDto`).
- Mappers: `xToXDto` (`DtoMapper`) y `xDtoToX` (`EntityMapper`).
- Rankings de `ItemList`: `getTop<Qué>From<Origen>` / `getMain<Qué>From<Origen>` (`From`, no `In`).
- Constantes de algoritmos: `RECOMMENDED_<X>_COUNT`, `NUMBER_OF_…_IN_<X>_RECOMMENDATION` en `RecommendationConstants`.
- Rutas en kebab-case y plural; constantes del frontend en `SCREAMING_SNAKE_CASE` (`BY_IDS`, `IDS_BY_USER`).

---

## 7. API REST

### Convenciones de rutas

- Todo cuelga de `/api/<recurso>` (plural, kebab-case).
- Conjunto estándar en todos los recursos CRUD:

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/<recurso>` | Todos |
| `POST` | `/api/<recurso>/by-ids` | Varios por ID; el array de IDs va en el **body** |
| `GET` | `/api/<recurso>/{id}` | Uno |
| `POST` | `/api/<recurso>` | Crear |
| `PUT` | `/api/<recurso>/{id}` | Actualizar |
| `DELETE` | `/api/<recurso>/{id}` | Borrar |

- `by-ids` es `POST` a propósito: axios serializa los arrays en query como `ids[]=1`, que Spring no enlaza, y además evita URLs largas.
- Filtros por relación: `/by-<relación>/{id}` (`/api/lessons/by-genre/{genreId}`). Con calificador: `/by-genre/{genreId}/popular`.
- Si el filtro devuelve IDs en lugar de objetos, la ruta termina en `/ids` (`/api/chords/by-song-lesson/{id}/ids`).
- Subrecursos: `POST`/`DELETE /api/item-lists/{listId}/items/{itemId}` para añadir/quitar.
- Acciones: `PUT /api/items/{id}/access`.
- Filtros globales como subruta: `/popular`, `/new`, `/recommended`, terminados en `/ids` cuando devuelven IDs (con `?limit=` opcional en canciones).

La tabla completa de endpoints está en el [`README.md`](README.md).

### IDs u objetos en las respuestas

El frontend trabaja internamente con IDs (sus repositorios normalizan lo que reciben en stores por id), pero eso **no** obliga a la API a devolver solo IDs. Lo que se optimiza en la API es el número de peticiones, porque en móvil manda la latencia: devolver IDs en un feed obliga a una segunda petición `by-ids`.

| Caso | La API devuelve | Motivo |
|---|---|---|
| Feeds de carrusel de entidades del catálogo (**regla por defecto**: todos los de lecciones, por década, por nivel, otros géneros… y cualquier carrusel nuevo) | **`List<Long>`** en una ruta `/ids` | El cliente ya tiene el resumen de todas en su catálogo (`GET /api/items`): pinta las tarjetas con él y solo pide por `by-ids` las que falten |
| Carruseles de "alta probabilidad de apertura" (excepciones de negocio, ver abajo) | **Lista de DTOs completos** (sin `/ids`) | Se espera que el usuario abra lo que ve: así el detalle abre al instante, sin otra petición |
| Resto de filtros `by-<relación>` cuyo consumidor necesita el objeto completo (p. ej. `lessons/by-genre`, `instrument-genres/by-genre`) | **Lista de DTOs** | Esos objetos no están en caché: 1 petición mejor que 2 |
| Relaciones de una entidad (`song.lessonIds`, `itemList.itemIds`, `user.favouriteGenresIds`) | **IDs dentro del DTO padre** | Mantiene la normalización; los hijos se piden solo si se necesitan |
| Pertenencia que cambia mucho sobre entidades estables (listas de un usuario) | `List<Long>` en una ruta `/ids` + `POST /by-ids` | El cliente ya tiene en caché casi todas las entidades y solo pide las que le faltan |
| `getById` / `by-ids` / `getAll` | DTO / lista de DTOs | — |
| Crear / actualizar | **El DTO resultante** | El cliente lo guarda directamente sin volver a pedirlo |
| Borrar / acciones (`/access`) | `204 No Content` | — |

#### Excepciones de negocio: carruseles que devuelven DTOs completos

Son carruseles a los que se espera que el usuario entre en cuanto los ve, así que conviene tener ya cargada la entidad completa:

| Carrusel | Endpoint | Motivo |
|---|---|---|
| Canciones populares y nuevas | `GET /api/songs/popular`, `GET /api/songs/new` | Carruseles principales de la pantalla de canciones |
| Recomendaciones de Spotify (canciones y, cuando exista, items) | `GET /api/songs/spotify` (y el futuro de items) | Personalizadas: alta probabilidad de apertura |
| Recomendados para añadir a una lista | `GET /api/recommendations/items/by-item-list/{id}` | Si el usuario añade un item, se espera que después vaya a él |
| Canciones de un género: populares, clásicas y nuevas | `GET /api/songs/by-genre/{genreId}/popular\|classic\|new` | Al entrar en un género y en "Canciones", lo normal es abrir alguna |
| Pantalla de canciones: "Populares en <género favorito>" | `GET /api/songs/by-genre/{genreId}/popular` | Mismo caso que el anterior, con uno de los géneros favoritos del usuario |

Cualquier carrusel nuevo sigue la regla por defecto (IDs) salvo que se añada explícitamente a esta tabla.

Los feeds **mixtos** de items (recomendados para una lista o para el usuario) devuelven `List<ItemDTO>` polimórfica: cada elemento es el DTO completo de su subtipo (`SongDTO`, `LessonDTO`, `GenreDTO`, `InstrumentGenreDTO`) y lleva `type` (`ItemDTO.getType()`, de solo lectura) para que el cliente lo guarde en su sitio. El mapeo lo hace `DtoMapper.itemToItemDto`.

Reglas asociadas:

- Nunca se anidan DTOs completos de otras entidades dentro de un DTO (ver `dto` en §1).
- Un DTO resumen (p. ej. `ItemSummaryDTO`) es un tipo **independiente** del DTO completo: no hereda de `ItemDTO` (base de los DTOs completos y escribibles, con validaciones) porque es una proyección de solo lectura. El frontend los guarda en stores separadas.
- Catálogo: `GET /api/items` devuelve el resumen de todos los items. Es lo que permite que los feeds devuelvan IDs: el criterio no es "IDs siempre" sino "IDs cuando el cliente ya tiene lo que necesita para pintar, y DTO completo cuando además se espera que lo abra". La app lo carga al arrancar y busca sobre él en local (pantalla de añadir a una lista), sin llamar al backend en cada tecla.
- Los nombres de los campos JSON son el contrato: si el frontend necesita otro nombre, lo mapea en su capa Service.

Detalle del lado cliente en [`frontend/bible.md`](frontend/bible.md), sección *Contrato Back↔Front*.

### Respuestas y errores

- Los controladores siempre devuelven `ResponseEntity`.
- `200 OK`; `201 Created` al crear (`ResponseEntity.status(HttpStatus.CREATED).body(...)`); `204 No Content` al borrar; `404 Not Found` con `ResponseEntity.of(optional)` o con `delete(id) == false`.
- `GlobalExceptionHandler`: `IllegalArgumentException` → 400, `BadCredentialsException` → 401, `EntityNotFoundException` → 404, errores de validación → 400 con el detalle por campo.
- `@Valid` solo está activo donde ya se usaba (instrument-genres y auth). Activarlo en el resto requiere revisar antes las restricciones de los DTOs frente a los datos de `data/`.

### Estructura de un controlador

```java
/**
 * REST controller for <recurso>.
 *
 * Base path: {@code /api/<recurso>}
 *
 * Endpoints:
 * - GET    /api/<recurso>        ...
 * - ...
 *
 * Responses: 200 OK, 201 Created (create), 204 No Content (delete) and 404 Not Found ...
 *
 * @author / @version / @since
 */
@RestController
@RequestMapping("/api/<recurso>")
@RequiredArgsConstructor
public class XController {
    private final XService xService;
    // orden: getAll, getByIds, getById, filtros (get…For…), acciones, create, update, delete
}
```

Cada método lleva Javadoc con `@param` y `@return` indicando los códigos de respuesta.

---

## 8. Recomendaciones

- Un servicio por tipo de item (`Song`, `Genre`, `InstrumentGenre`, `Lesson`) con `findForItemList(itemListId, limit)`, y `ItemRecommendationService` que los combina.
- Algoritmos por **niveles**: cada nivel completa lo que falta del anterior; se para en cuanto se alcanza el límite.
- Resultado `SequencedSet` (`LinkedHashSet`): sin duplicados y en orden de prioridad.
- Se excluye lo que ya está en la playlist y lo ya recomendado.
- Las exclusiones se acumulan como `Set<Long>` de IDs (lo que reciben las consultas `...IdNotIn`), no como entidades: se inicializan con los IDs de lo que ya tiene la playlist/usuario y cada nivel añade los IDs de lo que recomienda. Así no se vuelve a mapear el conjunto entero en cada consulta.
- Guardas: nunca `getFirst()` sobre listas posiblemente vacías, nunca índices fuera de rango (for-each sobre los rankings), nunca `resolvePageable(0)`, ni consultas con colecciones vacías cuando se pueden evitar.
- La aleatoriedad (`Collections.shuffle` sobre las preferencias del usuario) forma parte del algoritmo y se conserva.
- Parámetros en `RecommendationConstants`.

---

## 9. Seguridad y autenticación

- JWT en la cabecera `Authorization: Bearer <token>`; `JwtAuthenticationFilter` rellena el `SecurityContext`. Sin sesión (`STATELESS`), CSRF desactivado.
- El token lleva el id del usuario (subject) y su rol (claim `role`); el filtro concede `ROLE_<rol>`. Los tokens antiguos sin `role` cuentan como `USER`. Un cambio de rol se aplica en el siguiente login.
- Reglas (`SecurityConfig`):
  - Públicas: `/api/auth/**`, `/api/spotify/callback`, `/actuator/health`, `/error` y las listas `GET /api/genres` y `GET /api/instruments` (el formulario de registro elige favoritos antes de tener token; solo la lista exacta: el detalle por id, `by-ids` y las escrituras siguen protegidos).
  - Solo `ADMIN`: `POST`/`PUT`/`DELETE` sobre el catálogo (`genres`, `songs`, `song-lessons`, `tab-sections`, `chords`, `lessons`, `lesson-sections`, `instruments`, `instrument-genres`), `GET`/`POST /api/users`, `GET /api/item-lists` (todas las listas) y el resto del actuator.
  - `POST /api/<recurso>/by-ids` es una lectura: basta con estar autenticado.
  - Todo lo demás exige JWT. Sin token (o con uno inválido) se responde **401**, no 403, para que la app cierre la sesión.
- Propiedad de los datos, para leer y para modificar (method security, `@PreAuthorize` + `security/OwnershipChecker`): usuario y favoritos (`/api/users/{id}`, `/api/users/{id}/...`, `POST /api/users/by-ids` solo con el propio id), listas (`/api/item-lists/{id}`, `/by-ids`, `/by-user/{userId}/ids`, sus items; al crear o actualizar, el `userId` del body también tiene que ser el del usuario) y recomendaciones personales (`/api/recommendations/.../by-user/{id}`, `/api/recommendations/items/by-item-list/{id}`, `/api/items/recommended/by-item-list/{id}`, `/api/spotify/recommendations/.../{userId}`). `/api/recommendations/lessons/by-genre/{id}` es catálogo. Un `ADMIN` puede actuar sobre cualquiera. Si no es suyo: **403** (`GlobalExceptionHandler` traduce `AccessDeniedException`). Una lista que no existe pasa la comprobación para que el controlador responda 404.
- Administrador inicial: `config/AdminBootstrap` lo crea al arrancar (o asciende a `ADMIN` al usuario con ese email) a partir de `ADMIN_EMAIL`/`ADMIN_PASSWORD` del `.env`. El registro siempre crea usuarios `USER`. Los scripts de carga inician sesión con él (`scripts/api_auth.py`).
- `SpotifyAuthController.getLoginUrl` sigue leyendo el usuario del `SecurityContext`.
- `POST /api/auth/register` registra y devuelve directamente el `LoginResponse` (token + usuario), igual que `login`. Exige el código de verificación del email (ver abajo).

### Verificación del email en el registro

1. La app valida el formulario y llama a `POST /api/auth/verification-code` con el email. `EmailVerificationServiceImpl` rechaza un email ya registrado (400), genera un código de 6 dígitos (`SecureRandom`) y lo envía con `EmailService`.
2. El usuario escribe el código en la app y esta llama a `POST /api/auth/register` con los datos y `verificationCode` (campo `WRITE_ONLY` de `UserDto`, nunca se devuelve).
3. `AuthController` comprueba el código (`checkCode`) antes de registrar y lo descarta (`consume`) solo cuando el usuario se ha guardado: si el registro falla por otro motivo (p. ej. usuario ocupado), se puede corregir y reintentar con el mismo código.

Reglas: el código caduca a los 10 minutos, se descarta tras 5 intentos fallidos y no se puede pedir otro para el mismo email hasta pasados 60 s (`ResponseStatusException` 429). Los emails se comparan sin mayúsculas ni espacios. Los códigos pendientes viven en memoria (una sola instancia, igual que los `state` de Spotify): un reinicio obliga a pedir uno nuevo.

Envío (`EmailServiceImpl`): SMTP configurado con `MAIL_*` en `.env`. Lee la plantilla HTML y la de texto plano de `templates/email` (generadas desde `emails/` con React Email) y sustituye sus marcadores `{{...}}`. Si no hay `JavaMailSender`, escribe el código en el log. Un fallo de SMTP se traduce en `IllegalStateException` (500) y el código no se guarda, para poder pedirlo otra vez al momento.

El email no se puede cambiar después del registro: el perfil de la app lo muestra como solo lectura y `PUT /api/users/{id}` ignora el que llegue en el body (`UserServiceImpl.update` conserva el guardado y solo comprueba que el nombre de usuario esté libre).

### Flujo OAuth2 de Spotify

1. La app llama a `GET /api/spotify/login` **con su JWT** y recibe `{ authUrl }`.
2. El backend genera un `state` aleatorio, de un solo uso y con caducidad (10 min), asociado al usuario, y lo incluye en la URL.
3. La app abre `authUrl` en el navegador. Spotify redirige al navegador a `GET /api/spotify/callback?code=&state=`. Esa redirección no lleva el JWT: el usuario se identifica por el `state`.
4. El backend intercambia el código por tokens, los asocia al usuario y responde con una página HTML que pide volver a la app.

Vinculación: una cuenta de Spotify solo puede estar vinculada a un usuario de la app (`spotifyUserId` es único) y un usuario solo a una cuenta. Si un usuario vincula una cuenta que ya tenía otro, se desvincula del anterior y gana la vinculación más reciente. Si el usuario ya tenía otra cuenta, su registro se reutiliza con la nueva. Como `User.spotifyUser` tiene `orphanRemoval`, desvincular borra el registro; por eso se hace `flush` antes de vincular (Hibernate ejecuta los inserts antes que los deletes).

Renovación del token: el `accessToken` de Spotify caduca en una hora. Cualquier servicio que llame a la API de Spotify debe obtenerlo con `SpotifyAuthService.getValidAccessToken(userId)`, nunca leyéndolo directamente de `SpotifyUser`. Si el token caduca en menos de un minuto, se renueva con el `refreshToken` (`grant_type=refresh_token`) y se guarda; si Spotify devuelve un `refreshToken` nuevo, sustituye al anterior. El método usa `Propagation.REQUIRES_NEW` porque quien lo llama suele estar en una transacción de solo lectura, donde el token renovado no se guardaría. Si Spotify rechaza la renovación (acceso revocado), lanza `IllegalStateException` y el usuario tiene que volver a vincular su cuenta.

Limitaciones: los `state` pendientes viven en memoria (una sola instancia del backend); Spotify solo admite `http` para `127.0.0.1`, así que la `redirect-uri` actual funciona desde el ordenador/emulador pero no desde un móvil en la red local.

---

## 10. Transacciones y carga perezosa

- `@Transactional` en las operaciones que modifican varias entidades o el lado dueño de una relación (crear/actualizar/borrar instrument-genres, lecciones, canciones).
- Las lecturas se apoyan en *open-in-view* (activo por defecto en Spring Boot) para acceder a colecciones perezosas desde los mappers.

---

## 11. Datos iniciales

- `scripts/load_all_data.py` carga los JSON de `data/` llamando a la API en orden: géneros, instrumentos, lecciones, instrument-genres, acordes, canciones. Las lecciones van antes de los instrument-genres porque estos las referencian por `lessonIds` (el índice de cada lección es su posición en ese array).
- Formato de los JSON: [`DATA_JSON_GUIDE.md`](data.md).
- Los scripts usan las mismas rutas `/api/...` que la app.

---

## 12. Frontend

Resumen de [`frontend/bible.md`](frontend/bible.md):

| Nivel | Carpeta | Responsabilidad |
|---|---|---|
| 1. Service | `services/*.service.ts` | Peticiones HTTP puras (axios) y mapeo `BackendDTO` → `AppDTO`. No toca stores ni disco. |
| 2. Local | `local/*.local.ts` | AsyncStorage (`multiSet` para lotes) e índices de consultas. |
| 3. Repository | `repositories/*.repository.ts` | Orquesta las lecturas y escrituras y guarda en store y disco antes de devolver. Por id: RAM → disco → API. Listas: índice guardado al instante + refresco de red. Siempre con *stale-while-revalidate*. La lógica común vive en `utils/entityCache.utils.ts`. |
| 4. Entity Store | `stores/entities` | Diccionarios normalizados por id (Zustand). |
| 5. Screen Store | `stores/ui` | Estado temporal de pantalla. |
| 6. Hooks | `hooks/` | Convierten IDs en objetos (`useMemo`) leyendo siempre de las stores. Los Context cuentan como este nivel. |
| 7. UI | `screens/`, `components/` | Solo renderizan y consumen hooks. |

Contrato con el backend:

- Todas las URLs están en `src/config/apiConfig.ts`. Nadie escribe URLs a mano.
- Las constantes siguen las convenciones de la API: `BASE`, `BY_ID`, `BY_IDS` (siempre `POST` con el array en el body), `BY_<RELACIÓN>`, `IDS_BY_<RELACIÓN>` y rutas de acción (`ITEM_LISTS.ITEM` con `POST`/`DELETE`, `ITEMS.ACCESS`).
- Todos los services usan la instancia compartida `services/apiClient.ts`: añade `Authorization: Bearer <token>` si hay sesión y, ante un 401 con sesión abierta, la cierra. Las llamadas a servicios externos (Cloudinary) no la usan para no enviar el JWT.
- Qué endpoints devuelven IDs y cuáles objetos: ver *IDs u objetos en las respuestas* (§7).

Caché y sincronización en el cliente:

- Por id (`getById`, `getAllById`): RAM → disco → API. Lo servido desde caché se vuelve a pedir en segundo plano una vez por entidad y sesión.
- Listas (`repo.getX(onFresh?)`): si hay índice guardado se devuelven sus IDs al instante y la red refresca después avisando con `onFresh`. Sin índice, se espera a la red; sin red ni índice, `[]`.
- Catálogo y feeds: al arrancar se carga el resumen de todos los items. Las tarjetas de todos los carruseles se pintan con ese resumen. Los carruseles por defecto reciben solo IDs (`itemRepository.getFeed`) y el DTO completo se pide al abrir el detalle; las excepciones de negocio (§7) reciben el DTO completo, que se guarda en la store de su entidad y deja su resumen al día.
- Logout: borra de RAM y disco los datos del usuario (su entidad, sus listas e índices personalizados). El contenido público se conserva como caché.
- Catálogos completos (`genreRepository.getAll`, `instrumentRepository.getAll`): son una lista más (`cache.list('all', ...)`), con su propio índice. No se deducen de las entidades sueltas que haya en disco, que pueden ser solo las que cargó otra pantalla.

Sesión:

- Al arrancar, `auth.entities.store` descarta el token guardado si ha caducado (lee el `exp` del JWT con `utils/jwt.utils.ts`, con 30 s de margen) antes de marcar la store como hidratada: la app va directa al login sin lanzar peticiones que acabarían en 401.
- Si el token caduca con la app abierta, el 401 lo gestiona `apiClient` (cierra la sesión). `entityCache.utils` no avisa de esos 401 (petición con token); sí de los 401 sin token, que indican un endpoint que no es público.

---

### Recientes del Home

La sección "Recientes" del Home es solo del frontend: no hay endpoint ni campo en el backend
(el antiguo `Item.lastTimeAccessed` era global, de todos los usuarios, y se eliminó).
`itemRepository.markAsAccessed(id)` (al abrir una canción, un género o un InstrumentGenre) guarda el ID
en `RecentItemsStore`, por usuario, del más reciente al más antiguo y hasta `MAX_RECENT_ITEMS` (12),
persistida en AsyncStorage. Las tarjetas se pintan con el catálogo de items. Al ser local, no se
sincroniza entre dispositivos.

Sin recientes (primera vez) o sin recomendaciones, el carrusel no se oculta: muestra una card dorada
(`components/carousels/EmptyCarouselCard`, vía la prop `emptyMessage` de `GenericCarousel`) que invita
a explorar la app. Solo aparece cuando ha terminado la carga, para no parpadear.

## 13. Pendiente

- `GET /api/items/recommended/ids` (sección "Recomendaciones" del Home) y `GET /api/songs/recommended/ids` ("Según tus gustos") los usa el frontend pero ningún controlador los expone.
- Recomendaciones de Spotify: `GET /api/spotify/recommendations/items/{userId}` (`List<ItemDTO>`) y `.../songs/{userId}` (`List<SongDTO>`) están en desarrollo; el frontend aún no los usa. Devuelven DTOs completos (excepción de negocio, §7). `findSpotify` de canciones es un marcador que devuelve vacío.
- `ItemRecommendationService` (algoritmo real por niveles) solo está expuesto en `GET /api/recommendations/items/by-item-list/{id}`; `GET /api/items/recommended/by-item-list/{id}` usa un marcador (los 20 items más nuevos) y el frontend no lo llama.
- `LessonRecommendationService` devuelve vacío.
- `GET /api/instruments/by-genre/{genreId}` lo usa el frontend pero no existe en el backend.
- Comprobar en ejecución las consultas `IdNotIn` con colecciones vacías (Hibernate 6 debería resolverlas, no se ha probado).
