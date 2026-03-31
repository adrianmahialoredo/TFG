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
API_ENDPOINTS.AUTH.LOGIN        // POST /api/auth/login
API_ENDPOINTS.AUTH.REGISTER     // POST /api/auth/register

API_ENDPOINTS.USERS.BASE        // Base: /api/users
API_ENDPOINTS.USERS.PROFILE(id) // GET/PUT /api/users/:id

API_ENDPOINTS.GENRES.BASE       // GET /genres
API_ENDPOINTS.GENRES.BY_ID(id)  // GET /genres/:id

API_ENDPOINTS.SONGS.BASE        // GET /songs
API_ENDPOINTS.SONGS.BY_ID(id)   // GET /songs/:id
API_ENDPOINTS.SONGS.BY_GENRE(genreId)  // GET /songs/genre/:genreId
API_ENDPOINTS.SONGS.BATCH       // POST /songs/batch
```

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
API_ENDPOINTS.USERS.PROFILE(userId)  // Retorna: http://192.168.0.22:8080/api/users/123
API_ENDPOINTS.GENRES.BY_ID(genreId)  // Retorna: http://192.168.0.22:8080/genres/456
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
  BASE: `${BASE_URL}/instruments`,
  BY_ID: (id: string) => `${BASE_URL}/instruments/${id}`,
}

// En instrumentService.ts
import { API_ENDPOINTS } from '../config/apiConfig';

export const getAllInstruments = async () => {
  const response = await axios.get(API_ENDPOINTS.INSTRUMENTS.BASE);
  return response.data;
};
```

