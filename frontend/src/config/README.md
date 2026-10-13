// src/config/README.md
# Configuración de API

## Descripción

Toda la configuración de endpoints de API está centralizada en `apiConfig.ts` para facilitar el mantenimiento y evitar duplicación de código.

## Localización

**Archivo principal:** `src/config/apiConfig.ts`

## Estructura

### BASE_URL
- **Valor actual:** `http://192.168.0.22:8080`
- **Cambiar aquí si la URL del servidor cambia**

### API_ENDPOINTS
Estructura jerárquica de todos los endpoints disponibles:

```typescript
API_ENDPOINTS.AUTH.LOGIN                  // POST /api/auth/login
API_ENDPOINTS.AUTH.REGISTER               // POST /api/auth/register (con verificationCode)
API_ENDPOINTS.AUTH.VERIFICATION_CODE      // POST /api/auth/verification-code (body: { email })

API_ENDPOINTS.GENRES.BASE                 // GET (todos) / POST (crear) /api/genres
API_ENDPOINTS.GENRES.BY_ID(id)            // GET / PUT / DELETE /api/genres/:id
API_ENDPOINTS.GENRES.BY_IDS               // POST /api/genres/by-ids  (body: array de IDs)

API_ENDPOINTS.SONGS.POPULAR_BY_GENRE(id)  // GET /api/songs/by-genre/:id/popular
API_ENDPOINTS.ITEM_LISTS.ITEM(l, i)       // POST (añadir) / DELETE (quitar) /api/item-lists/:l/items/:i
```

Todos los recursos siguen las mismas convenciones (ver cabecera de `apiConfig.ts`):
`BASE`, `BY_ID`, `BY_IDS` (siempre POST con los IDs en el body) y filtros `BY_<RELACIÓN>`.
Los filtros que devuelven IDs en lugar de objetos se llaman `IDS_BY_<RELACIÓN>`.

## Cómo usar

### En servicios
```typescript
import { API_ENDPOINTS } from '../config/apiConfig';

export const login = async (email: string, password: string) => {
  const response = await axios.post(API_ENDPOINTS.AUTH.LOGIN, { email, password });
  return response.data;
};
```

### Para funciones con parámetros
```typescript
// Endpoints con parámetros usan funciones
API_ENDPOINTS.USERS.BY_ID(userId)    // Retorna: http://192.168.0.22:8080/api/users/123
API_ENDPOINTS.GENRES.BY_ID(genreId)  // Retorna: http://192.168.0.22:8080/api/genres/456
```

## Archivos actualizados

Los siguientes servicios ya están importando desde `apiConfig.ts`:

- ✅ `src/services/authService.ts`
- ✅ `src/services/genreService.ts`
- ✅ `src/services/songService.ts`
- ✅ `src/services/userService.ts`

## Cambios futuros

Si necesitas agregar un nuevo endpoint:

1. Abre `src/config/apiConfig.ts`
2. Agrega la nueva ruta en `API_ENDPOINTS`
3. Usa esa ruta en tu servicio importándola

**Ejemplo:** Agregar un nuevo endpoint de instrumentos

```typescript
// En apiConfig.ts
INSTRUMENTS: {
  BASE: `${API_URL}/instruments`,
  BY_ID: (id: string) => `${API_URL}/instruments/${id}`,
  BY_IDS: `${API_URL}/instruments/by-ids`,
}

// En instrumentService.ts
import { API_ENDPOINTS } from '../config/apiConfig';

export const getAllInstruments = async () => {
  const response = await axios.get(API_ENDPOINTS.INSTRUMENTS.BASE);
  return response.data;
};
```

