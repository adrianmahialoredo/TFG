# 📜 Biblia de Arquitectura: Mobile Music App (v2.5 - 2026)

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
- **Flujo de Lectura (Cache-First):** 1. Mirar RAM (Zustand Entity Store).
    2. Si no está, mirar Local (AsyncStorage).
    3. Si no está, llamar a API.
- **Flujo de Escritura:** Al recibir datos de la API, DEBE actualizar la RAM y el Disco antes de retornar.

### Nivel 4: Entity Store (`zustand/entities`)
- **Estructura:** Diccionario normalizado `{ entities: Record<string, EntityDTO> }`.
- **Regla de Oro:** Solo vive la entidad aquí. No se guarda el "usuario actual" con sus datos, solo su ID en la AuthStore.

### Nivel 5: Screen Store (`zustand/ui`)
- **Responsabilidad:** Estado temporal (loading, errores, filtros de búsqueda, pestaña activa).
- **Persistencia:** Normalmente no se persiste.

### Nivel 6: Custom Hooks (`useEntity.ts`)
- **Responsabilidad:** Unir IDs con Objetos.
- **Lógica:** Aquí van los `.map`, `.filter` y `.sort` envueltos en `useMemo`.
- **Ejemplo:** `const user = useUserEntitiesStore(state => state.users[currentUserId])`.

### Nivel 7: UI Components (`*.tsx`)
- **Responsabilidad:** Renderizar.
- **Regla:** Solo consumen Hooks de Nivel 6. No conocen el Repository ni la API.

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

4. **LEY DE PREVENCIÓN DE PARPADEO (FLICKER):**
   - El `AppNavigator` debe consultar `isHydrated` (Zustand Persist). Si es `false`, mostrar Splash Screen. Nunca mostrar el Login si no se ha terminado de leer el disco.

---

## 🛠️ FEATURE BLUEPRINT: Implementar una nueva lista (Ej: "Canciones Favoritas")

1. **Local:** Crear `saveFavoriteIndex(ids)` y `getFavoriteIndex()`.
2. **Repository:** - `getFavorites()`: 
     - Intenta API -> Si éxito, guarda canciones en `SongEntitiesStore`, guarda IDs en Local Index y devuelve IDs.
     - Si falla API -> Devuelve IDs del Local Index.
3. **Hook:** - Llama al Repositorio para obtener los IDs.
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