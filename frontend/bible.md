# 📜 Biblia de Arquitectura: Mobile Music App (v2.6 - 2026)

Esta arquitectura es la ley suprema del proyecto. Su objetivo es el desacoplamiento total, la persistencia offline-first y la **Sincronización Atómica**. 

---

## 🏗️ Los 7 Niveles de Implementación (Detalle Técnico)

### Nivel 1: API Service (`*.service.ts`)
- **Responsabilidad:** Peticiones HTTP puras (Axios).
- **Regla:** Mapear la respuesta del backend (`BackendDTO`) al tipo de datos de la app (`AppDTO`).
- **Prohibido:** No debe tocar ninguna Store ni el LocalStorage.

### Nivel 2: Local Storage (`*.local.ts`)
- **Entidades:** Métodos `saveSingle`, `getById`, `saveBatch` (usar siempre `AsyncStorage.multiSet`).
- **Índices:** Métodos `saveQueryIndex(key, ids)` y `getQueryIndex(key)`. 
- **Prefijos:** Usar `@entity:id` para datos y `@index:key` para listas.

### Nivel 3: Repository (`*.repository.ts`)
- **Responsabilidad:** Orquestar la verdad. Es el único que habla con el Service, el Local y las Stores.
- **Contrato de retorno:**
    - `getById(id)` → `DTO | null`.
    - `getAllById(ids)` / `getAll()` → `DTO[]` (en el orden de los `ids` pedidos).
    - **Cualquier otra lectura** (populares, por género, búsqueda, listas de un usuario…) → `string[]` de IDs.
    - Escrituras (`create`, `update`, `add…`) → el `DTO` actualizado que devuelve el backend.
- **Flujo de lectura por ID (Cache-First):**
    1. Mirar RAM (Zustand Entity Store).
    2. Si no está, mirar Local (AsyncStorage).
    3. Si no está, llamar a API.
    - **Stale-While-Revalidate:** lo servido desde RAM/Disco se devuelve al instante y se re-pide al backend en segundo plano (`revalidator.revalidate(ids)`, una vez por entidad y sesión). Lo que llega de la API, incluidos los endpoints de lista, se marca como fresco (`revalidator.markFresh(ids)`). Así la caché nunca se queda congelada.
- **Flujo de lectura de listas (Cache-First + refresco):** `repo.getX(onFresh?)`
    1. Si hay índice guardado: subir sus entidades a RAM (RAM → Disco, sin red) y devolver los IDs **al instante**. En paralelo se pide a la API y, cuando responde, se hidrata RAM + Disco + índice y se llama a `onFresh(idsFrescos)`.
    2. Si no hay índice: esperar a la API, hidratar y devolver los IDs.
    3. Sin red y sin índice: `[]`.
    - El índice se guarda aunque venga vacío, para no resucitar resultados viejos.
- **Implementación común:** todo lo anterior vive en `utils/entityCache.utils.ts` (`createEntityCache`). Cada Repository crea su `cache` y expone `getAllById = cache.getAllById` y sus listas como `cache.list(key, fetch, onFresh)` (endpoint que devuelve objetos) o `cache.idList(key, fetchIds, onFresh)` (endpoint que devuelve IDs: se resuelven contra RAM → Disco y solo se piden por `by-ids` los que falten). Prohibido reimplementar estos flujos a mano en un Repository.
- **Carruseles:** todas las tarjetas se pintan con el resumen de la `ItemEntitiesStore` (`itemsToCarouselItems`). Según el contrato (ver *Contrato Back↔Front*) hay dos tipos:
    - **Por defecto (IDs):** `itemRepository.getFeed(key, fetchIds, onFresh)`. El DTO completo solo se pide al abrir el detalle.
    - **Excepciones de negocio (DTO completo):** el repositorio de la entidad usa `cache.list(...)` (p.ej. `songRepository.getPopular`), o `itemFeed.repository` si el feed mezcla subtipos (recomendados para una lista o para el usuario): reparte cada DTO en la Store de su subtipo según su `type`.
    - Las cachés de Song, Lesson, Genre e InstrumentGenre actualizan el resumen de la `ItemEntitiesStore` cada vez que un DTO completo entra en RAM (`toItemSummary`), así la tarjeta y el detalle nunca discrepan.
- **Catálogos completos (`getAll`):** son una lista más, `cache.list('all', service.getAll)`, con su propio índice. Prohibido deducir el catálogo de las entidades sueltas que haya en disco (p.ej. una clave `@x_all` que reescribe cada `saveBatch`): ahí solo están las que cargó otra pantalla.
- **Flujo de Escritura:** Al recibir datos de la API, DEBE actualizar la RAM y el Disco antes de retornar.

### Nivel 4: Entity Store (`zustand/entities`)
- **Estructura:** Diccionario normalizado `{ entities: Record<string, EntityDTO> }`.
- **Regla de Oro:** Solo vive la entidad aquí. No se guarda el "usuario actual" con sus datos, solo su ID en la AuthStore.

### Nivel 5: Screen Store (`zustand/ui`)
- **Responsabilidad:** Estado temporal (loading, errores, filtros de búsqueda, pestaña activa).
- **Persistencia:** Normalmente no se persiste.

### Nivel 6: Custom Hooks (`useEntity.ts`)
- **Responsabilidad:** Unir IDs con Objetos.
- **Listas:** el hook pasa un `onFresh` por sección y aplica cada sección por separado en cuanto llega (primero la caché, luego la red). El `loading` se quita en cuanto hay algo que pintar; el spinner solo se ve si no hay nada guardado.
- **Regla:** Los objetos se leen SIEMPRE de la Entity Store mediante selector. Prohibido copiar DTOs en `useState` (la copia no se entera de los cambios en la Store).
- **IDs sueltos:** Si un componente recibe IDs (p.ej. `user.favouriteGenresIds`), usa un hook `use<Entity>sByIds(ids)` que resuelve contra la Store y pide al Repository los que falten.
- **Lógica:** Aquí van los `.map`, `.filter` y `.sort` envueltos en `useMemo`.
- **Ejemplo:** `const user = useUserEntitiesStore(state => state.users[currentUserId])`.

### Nivel 7: UI Components (`*.tsx`)
- **Responsabilidad:** Renderizar.
- **Regla:** Solo consumen Hooks de Nivel 6. No conocen el Repository, la API ni las Stores.
- Los Contexts (`AuthContext`, …) cuentan como Nivel 6: pueden llamar a Repositories, nunca a Services ni Locals.

---

## 🔌 CONTRATO BACK↔FRONT (Cuándo devolver IDs y cuándo objetos)

"El front trabaja con IDs" es una regla **interna del front** (Repository → Hook). **No** obliga al backend a devolver solo IDs: el Repository normaliza lo que reciba. Lo que se optimiza en el back es el número de peticiones, porque en móvil manda la latencia.

| Caso | El backend devuelve | Motivo |
|---|---|---|
| Carruseles de entidades del catálogo (**por defecto**: lecciones, por década, por nivel, otros géneros, "Para ti" de Canciones y del Home… y cualquiera nuevo) | **`List<Long>`** (ruta `/ids`) | El cliente ya tiene el resumen de todas en el catálogo: pinta con él y solo pide por `by-ids` las que falten |
| Carruseles de alta probabilidad de apertura (**excepciones de negocio**): canciones populares y nuevas, Spotify (canciones e items), recomendados para añadir a una lista, populares/clásicas/nuevas de un género y "Populares en <género favorito>" | **DTOs completos** | Se espera que el usuario abra lo que ve: el detalle abre al instante. La lista completa y sus endpoints está en `architecture.md` §7 |
| Filtros cuyo consumidor necesita el objeto completo (`lessons/by-genre`, `instrument-genres/by-genre`) | **Lista de DTOs** | Esos objetos no están en caché: 1 petición mejor que 2 (IDs + `by-ids`) |
| Relaciones de una entidad (`song.lessonIds`, `itemList.itemIds`, `user.favouriteGenresIds`) | **IDs dentro del DTO padre** | Mantiene la normalización; los hijos se piden solo si se necesitan |
| Pertenencia que cambia mucho sobre entidades estables (listas de un usuario) | `List<Long>` + `POST /by-ids` | La caché acierta y no se re-descarga lo ya conocido |
| `getById` / `by-ids` / `getAll` | DTO / lista de DTOs | — |
| Crear / actualizar | **El DTO resultante** | Se inyecta directo en la Store sin re-pedirlo |
| Borrar / marcar acceso | `void` (204) | — |

**DTO resumen vs completo:** un DTO resumen (p.ej. `ItemSummaryDTO`, en `types/ItemSummaryDTO.ts`) es un tipo independiente: no extiende `ItemDTO`, que es la base de los DTOs completos. Vive en su propia Store (`ItemEntitiesStore`) y **nunca** se guarda en la Store de la entidad completa (`SongEntitiesStore`), o pisaría campos del objeto completo.

**Catálogo de items:** al arrancar, `AuthContext` carga en segundo plano el resumen de todos los items (`itemRepository.getCatalog()`, Cache-First, refresco de red una vez por sesión). Las búsquedas de items se hacen en local sobre ese catálogo; el logout lo conserva porque es contenido público.

**Nombres de campos:** el `AppDTO` usa los mismos nombres que el JSON del backend (p.ej. `imageUrl`). Si alguno difiere, el mapeo se hace en el Service (Nivel 1), nunca más arriba.

---

## 🚫 LEYES INQUEBRANTABLES (Antipatrones)

1. **LEY DE LA FUENTE ÚNICA (SSOT):** - Prohibido duplicar el objeto `User` en `AuthStore`. 
   - La `AuthStore` solo guarda `{ token: string, currentUserId: string }`.
   - Cualquier cambio en el usuario debe hacerse en `UserEntitiesStore`.

2. **LEY DE HIDRATACIÓN ATÓMICA:**
   - El Repositorio nunca devuelve un objeto que no haya sido guardado primero en la Store de Entidades.
   - Mal: `return apiResponse;`
   - Bien: `store.set(apiResponse); return apiResponse;`

3. **LEY DE RENDIMIENTO STORAGE:**
   - Prohibido hacer `for` loops de `AsyncStorage.setItem`.
   - Obligatorio usar `AsyncStorage.multiSet` para batches de datos.

4. **LEY DE PRIVACIDAD EN LOGOUT:**
   - El logout borra (RAM + Disco) todo lo que pertenece al usuario: su entidad, sus listas y los índices personalizados (recomendaciones, Spotify). Los recientes del Home se conservan: van por usuario en `RecentItemsStore` y nadie más los ve.
   - Las entidades de contenido público (canciones, lecciones, géneros…) se conservan como caché.

5. **LEY DE PREVENCIÓN DE PARPADEO (FLICKER):**
   - El `AppNavigator` debe consultar `isHydrated` (Zustand Persist). Si es `false`, mostrar Splash Screen. Nunca mostrar el Login si no se ha terminado de leer el disco.

---

## 🛠️ FEATURE BLUEPRINT: Implementar una nueva lista (Ej: "Canciones Favoritas")

1. **Local:** Crear `saveFavoriteIndex(ids)` y `getFavoriteIndex()`.
2. **Repository:** - `getFavorites(onFresh?)`:
     - `export const getFavorites = (onFresh?: OnFresh) => cache.list('favorites', () => songService.getFavorites(), onFresh);`
     - El `cache` ya se encarga del índice, la hidratación de RAM + Disco y el refresco.
3. **Hook:** - Llama al Repositorio para obtener los IDs, pasando un `onFresh` que sustituya los IDs cuando llegue la red.
   - Usa un selector de Zustand para obtener las canciones reales de `SongEntitiesStore` usando esos IDs.
   - Devuelve la lista de objetos completa a la UI.

---

## 📂 Estructura de Directorios
```text
src/
 ├── services/      # Nivel 1: API (axios)
 ├── local/         # Nivel 2: AsyncStorage & QueryIndexes
 ├── repositories/  # Nivel 3: Lógica de negocio (Orquestadores)
 ├── stores/
 │    ├── entities/ # Nivel 4: Diccionarios normalizados (Zustand)
 │    └── ui/       # Nivel 5: Estado de pantallas (Zustand)
 ├── hooks/         # Nivel 6: Hooks de hidratación (useAuth, useSongs)
 ├── screens/       # Nivel 7: Pantallas (Navegación)
 └── components/    # Nivel 7: Átomos y moléculas de UI