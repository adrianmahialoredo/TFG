# Backend - TFG (Trabajo de Fin de Grado)

## 📋 Descripción General

Este es el backend de la aplicación TFG, desarrollado con **Spring Boot 3.4.3** y **Java 23**. Proporciona una API RESTful completa para gestionar géneros musicales, canciones, lecciones, acordes, instrumentos y más.

La aplicación utiliza:
- **Base de datos**: JPA/Hibernate para ORM
- **Seguridad**: JWT (JSON Web Tokens) para autenticación
- **API REST**: Spring Web
- **Integración Spotify**: Para recomendaciones musicales
- **Build**: Maven

---

## 🚀 Inicio Rápido

### Requisitos Previos
- Java 23 o superior
- Maven 3.9+
- Base de datos (configurada en `application.properties`)

### Instalación

```bash
# Clonar el repositorio
git clone <repository-url>
cd TFG

# Compilar el proyecto
./mvnw clean install

# Ejecutar el servidor
./mvnw spring-boot:run
```

El servidor estará disponible en: `http://192.168.0.22:8080`

---

## 📡 Endpoints de API

### 🔐 Autenticación (`/api/auth`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Registrar nuevo usuario |
| `POST` | `/api/auth/login` | Iniciar sesión (retorna JWT token) |

**Ejemplo Login:**
```json
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": { ... }
}
```

---

### 📐 Convenciones

Todos los controladores siguen la misma estructura:

- Todas las rutas cuelgan de `/api/<recurso>` (en plural y kebab-case).
- Operaciones comunes: `GET /` (todos), `POST /by-ids` (varios por ID, con el array de IDs en el body), `GET /{id}`, `POST /`, `PUT /{id}` y `DELETE /{id}`.
- Filtros por relación: `/by-<relación>/{id}` (p. ej. `/api/lessons/by-genre/{genreId}`). Si el filtro devuelve IDs en lugar de objetos, la ruta termina en `/ids`.
- Respuestas: `200 OK`, `201 Created` al crear, `204 No Content` al borrar y `404 Not Found` si el recurso no existe. El resto de errores los traduce `GlobalExceptionHandler` (`400` argumentos inválidos, `401` credenciales, `404` entidad no encontrada).

---

### 👥 Usuarios (`/api/users`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/users` | Obtener todos los usuarios |
| `POST` | `/api/users/by-ids` | Obtener varios usuarios por ID (array de IDs en el body) |
| `GET` | `/api/users/{id}` | Obtener usuario por ID |
| `GET` | `/api/users/{id}/favorite-genres` | IDs de los géneros favoritos del usuario |
| `POST` | `/api/users` | Crear usuario |
| `PUT` | `/api/users/{id}` | Actualizar usuario |
| `DELETE` | `/api/users/{id}` | Eliminar usuario |

---

### 🎵 Géneros (`/api/genres`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/genres` | Obtener todos los géneros |
| `POST` | `/api/genres/by-ids` | Obtener varios géneros por ID (array de IDs en el body) |
| `GET` | `/api/genres/{id}` | Obtener género por ID |
| `POST` | `/api/genres` | Crear género |
| `PUT` | `/api/genres/{id}` | Actualizar género |
| `DELETE` | `/api/genres/{id}` | Eliminar género |

**Modelo GenreDTO:**
```json
{
  "id": "1",
  "name": "Rock",
  "description": "Descripción del género",
  "bannerUrl": "https://...",
  "imageUrl": "https://...",
  "welcomeIntroVideoId": "video123",
  "welcomeSongVideoId": "video456",
  "songIds": ["1", "2", "3"],
  "lessonIds": ["1", "2"]
}
```

---

### 🎸 Canciones (`/api/songs`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/songs` | Obtener todos los canciones |
| `POST` | `/api/songs/by-ids` | Obtener varios canciones por ID (array de IDs en el body) |
| `GET` | `/api/songs/{id}` | Obtener canción por ID |
| `GET` | `/api/songs/by-decade/{decade}` | Canciones de una década (p. ej. `1990s`) |
| `GET` | `/api/songs/by-genre/{genreId}/popular` | Canciones populares de un género (DTO completo) |
| `GET` | `/api/songs/by-genre/{genreId}/classic` | Canciones clásicas de un género (DTO completo) |
| `GET` | `/api/songs/by-genre/{genreId}/new` | Canciones nuevas de un género (DTO completo) |
| `GET` | `/api/songs/popular?limit={n}` | Canciones populares (DTO completo) |
| `GET` | `/api/songs/new?limit={n}` | Canciones nuevas (DTO completo) |
| `GET` | `/api/songs/recommended/ids?limit={n}` | IDs de las canciones recomendadas para el usuario (**pendiente**: aún sin implementar) |
| `GET` | `/api/songs/spotify?limit={n}` | Canciones recomendadas por Spotify (DTO completo) |
| `POST` | `/api/songs` | Crear canción |
| `PUT` | `/api/songs/{id}` | Actualizar canción |
| `DELETE` | `/api/songs/{id}` | Eliminar canción |

**Modelo SongDTO:**
```json
{
  "id": "1",
  "title": "Stairway to Heaven",
  "artist": "Led Zeppelin",
  "imageUrl": "https://...",
  "genreIds": ["1"],
  "instrumentIds": ["1", "2"],
  "lessonIds": ["1"]
}
```

---

### 📖 Lecciones de Canciones (`/api/song-lessons`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/song-lessons` | Obtener todos los lecciones de canción |
| `POST` | `/api/song-lessons/by-ids` | Obtener varios lecciones de canción por ID (array de IDs en el body) |
| `GET` | `/api/song-lessons/{id}` | Obtener lección de canción por ID |
| `GET` | `/api/song-lessons/by-song/{songId}` | Lecciones de una canción |
| `POST` | `/api/song-lessons` | Crear lección de canción |
| `PUT` | `/api/song-lessons/{id}` | Actualizar lección de canción |
| `DELETE` | `/api/song-lessons/{id}` | Eliminar lección de canción |

---

### 🎼 Secciones de Tab (`/api/tab-sections`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/tab-sections` | Obtener todos los secciones de tab |
| `POST` | `/api/tab-sections/by-ids` | Obtener varios secciones de tab por ID (array de IDs en el body) |
| `GET` | `/api/tab-sections/{id}` | Obtener sección de tab por ID |
| `GET` | `/api/tab-sections/by-song-lesson/{songLessonId}` | Secciones de tab de una lección de canción |
| `POST` | `/api/tab-sections` | Crear sección de tab |
| `PUT` | `/api/tab-sections/{id}` | Actualizar sección de tab |
| `DELETE` | `/api/tab-sections/{id}` | Eliminar sección de tab |

---

### 🎼 Acordes (`/api/chords`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/chords` | Obtener todos los acordes |
| `POST` | `/api/chords/by-ids` | Obtener varios acordes por ID (array de IDs en el body) |
| `GET` | `/api/chords/{id}` | Obtener acorde por ID |
| `GET` | `/api/chords/by-song-lesson/{songLessonId}/ids` | IDs de los acordes de una lección de canción |
| `POST` | `/api/chords` | Crear acorde |
| `PUT` | `/api/chords/{id}` | Actualizar acorde |
| `DELETE` | `/api/chords/{id}` | Eliminar acorde |

---

### 📚 Lecciones (`/api/lessons`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/lessons` | Obtener todos los lecciones |
| `POST` | `/api/lessons/by-ids` | Obtener varios lecciones por ID (array de IDs en el body) |
| `GET` | `/api/lessons/{id}` | Obtener lección por ID |
| `GET` | `/api/lessons/by-genre/{genreId}` | Lecciones que pertenecen a un género |
| `GET` | `/api/lessons/popular/ids` | IDs de las lecciones populares |
| `GET` | `/api/lessons/new/ids` | IDs de las lecciones más nuevas |
| `GET` | `/api/lessons/recommended/ids` | IDs de las lecciones recomendadas |
| `POST` | `/api/lessons` | Crear lección |
| `PUT` | `/api/lessons/{id}` | Actualizar lección |
| `DELETE` | `/api/lessons/{id}` | Eliminar lección |

---

### 🧩 Secciones de Lección (`/api/lesson-sections`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/lesson-sections` | Obtener todos los secciones de lección |
| `POST` | `/api/lesson-sections/by-ids` | Obtener varios secciones de lección por ID (array de IDs en el body) |
| `GET` | `/api/lesson-sections/{id}` | Obtener sección de lección por ID |
| `GET` | `/api/lesson-sections/by-lesson/{lessonId}` | Secciones de una lección |
| `POST` | `/api/lesson-sections` | Crear sección de lección |
| `PUT` | `/api/lesson-sections/{id}` | Actualizar sección de lección |
| `DELETE` | `/api/lesson-sections/{id}` | Eliminar sección de lección |

---

### 🎹 Instrumentos (`/api/instruments`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/instruments` | Obtener todos los instrumentos |
| `POST` | `/api/instruments/by-ids` | Obtener varios instrumentos por ID (array de IDs en el body) |
| `GET` | `/api/instruments/{id}` | Obtener instrumento por ID |
| `POST` | `/api/instruments` | Crear instrumento |
| `PUT` | `/api/instruments/{id}` | Actualizar instrumento |
| `DELETE` | `/api/instruments/{id}` | Eliminar instrumento |

---

### 🎵🎹 Géneros de Instrumentos (`/api/instrument-genres`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/instrument-genres` | Obtener todos los instrument-genres |
| `POST` | `/api/instrument-genres/by-ids` | Obtener varios instrument-genres por ID (array de IDs en el body) |
| `GET` | `/api/instrument-genres/{id}` | Obtener instrument-genre por ID |
| `GET` | `/api/instrument-genres/by-genre/{genreId}` | Instrument-genres de un género |
| `GET` | `/api/instrument-genres/by-instrument/{instrumentId}` | Instrument-genres de un instrumento |
| `POST` | `/api/instrument-genres` | Crear instrument-genre |
| `PUT` | `/api/instrument-genres/{id}` | Actualizar instrument-genre |
| `DELETE` | `/api/instrument-genres/{id}` | Eliminar instrument-genre |

---

### 📝 Listas de Elementos (`/api/item-lists`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/item-lists` | Obtener todos los listas |
| `POST` | `/api/item-lists/by-ids` | Obtener varios listas por ID (array de IDs en el body) |
| `GET` | `/api/item-lists/{id}` | Obtener lista por ID |
| `GET` | `/api/item-lists/by-user/{userId}/ids` | IDs de las listas de un usuario |
| `POST` | `/api/item-lists` | Crear lista |
| `PUT` | `/api/item-lists/{id}` | Actualizar lista |
| `DELETE` | `/api/item-lists/{id}` | Eliminar lista |
| `POST` | `/api/item-lists/{listId}/items/{itemId}` | Añadir un elemento a una lista (sin duplicados) |
| `DELETE` | `/api/item-lists/{listId}/items/{itemId}` | Quitar un elemento de una lista |

---

> **IDs u objetos:** por defecto los carruseles devuelven solo IDs (rutas `/ids`) y la app pinta las tarjetas con su catálogo (`GET /api/items`). Algunos carruseles devuelven el DTO completo por decisión de negocio (se espera que el usuario los abra). Detalle en [`architecture.md`](architecture.md) §7.

### 🗂️ Elementos (`/api/items`)

Devuelven `ItemSummaryDTO` (`id`, `title`, `imageUrl`, `type`, `level`, `artist`), salvo las rutas `/ids` (solo IDs) y `/recent` (DTO completo de cada subtipo, con `type`).

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/items` | Resumen de todos los elementos (catálogo que la app carga al arrancar) |
| `POST` | `/api/items/by-ids` | Obtener varios elementos por ID (array de IDs en el body) |
| `GET` | `/api/items/{id}` | Obtener elemento por ID |
| `GET` | `/api/items/search?q={texto}` | Buscar elementos por título |
| `GET` | `/api/items/recent` | Elementos accedidos más recientemente (DTO completo de cada subtipo) |
| `GET` | `/api/items/recommended/ids` | IDs de los elementos recomendados para el usuario (**pendiente**: aún sin implementar) |
| `GET` | `/api/items/recommended/by-item-list/{itemListId}` | Elementos recomendados para una lista (marcador: los 20 más nuevos) |
| `PUT` | `/api/items/{id}/access` | Marcar un elemento como accedido |

---

### 💡 Recomendaciones (`/api/recommendations`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/recommendations/items/by-item-list/{id}` | Elementos recomendados para añadir a una lista, DTO completo de cada subtipo (algoritmo por niveles; excluye los que ya están) |

---

### 🎵 Spotify (`/api/spotify`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/spotify/login` | Iniciar sesión con Spotify |
| `GET` | `/api/spotify/callback?code={code}` | Callback de autenticación Spotify |
| `GET` | `/api/spotify/recommendations/{spotifyUserId}` | Obtener recomendaciones |

---

## 🔒 Seguridad

### JWT Authentication

La API utiliza **JSON Web Tokens (JWT)** para autenticación. Después de login, incluye el token en el header:

```bash
Authorization: Bearer <jwt-token>
```

### Flujo de Autenticación

1. Usuario se registra con email y contraseña
2. Usuario hace login
3. Backend devuelve JWT token
4. Cliente incluye token en todos los requests
5. Backend valida token con `JwtAuthenticationFilter`

---

## 📁 Estructura del Proyecto

```
src/main/java/com/demo/demo/
├── controller/          # Controladores REST
│   ├── AuthController
│   ├── UserController
│   ├── GenreController
│   ├── SongController
│   └── ...
├── service/            # Lógica de negocio
│   ├── AuthService
│   ├── UserService
│   ├── GenreService
│   └── ...
├── repository/         # Acceso a datos (JPA)
│   ├── UserRepository
│   ├── GenreRepository
│   ├── SongRepository
│   └── ...
├── model/             # Entidades JPA
│   ├── User
│   ├── Genre
│   ├── Song
│   └── ...
├── dto/               # Data Transfer Objects
│   ├── UserDto
│   ├── GenreDTO
│   ├── SongDTO
│   └── ...
├── security/          # Seguridad JWT
│   ├── JwtUtil
│   └── JwtAuthenticationFilter
└── DemoApplication.java  # Clase principal
```

---

## ⚙️ Configuración

### `application.properties`

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/tfg
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update

# JWT
jwt.secret=your-secret-key-here
jwt.expiration=86400000  # 24 hours

# Spotify
spotify.client-id=your-client-id
spotify.client-secret=your-client-secret
```

---

## 🛠️ Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|-----------|---------|----------|
| Spring Boot | 3.4.3 | Framework principal |
| Java | 23 | Lenguaje de programación |
| Spring Data JPA | Latest | ORM |
| Spring Security | Latest | Autenticación/Autorización |
| JWT | Latest | Token-based auth |
| MySQL | 8+ | Base de datos |
| Maven | 3.9+ | Gestor de dependencias |
| Lombok | Latest | Reducción de boilerplate |
| Spotify API | Latest | Integración Spotify |

---

## 📝 Modelos de Datos

### Relaciones Principales

- **User** → **ItemList** (1 a N)
- **Genre** → **Song** (N a N)
- **Song** → **SongLesson** (1 a N)
- **SongLesson** → **TabSection** (1 a N)
- **Instrument** → **Genre** (N a N via InstrumentGenreScreen)

---

## 🐛 Troubleshooting

### Error: Base de datos no conecta
- Verificar `application.properties`
- Asegurarse de que MySQL está corriendo
- Verificar credenciales de base de datos

### Error: JWT token inválido
- El token ha expirado
- Token incorrecto o corrupto
- Verificar `jwt.secret` en properties

### Error: CORS
- Configurar CORS en `WebConfig`
- Agregar anotación `@CrossOrigin` en controladores

---

## 🚀 Deployment

### Build para producción

```bash
./mvnw clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Variables de entorno necesarias

```bash
DATABASE_URL=
DATABASE_USER=
DATABASE_PASSWORD=
JWT_SECRET=
SPOTIFY_CLIENT_ID=
SPOTIFY_CLIENT_SECRET=
```

---

## 📞 Soporte

Para reportar bugs o solicitar features, crea un issue en el repositorio.

---

## 👤 Autor

**Adrián Mahía**

---

## 📄 Licencia

Este proyecto está bajo licencia MIT.

---

## 🔗 Enlaces Útiles

- [Documentación Spring Boot](https://spring.io/projects/spring-boot)
- [Swagger/OpenAPI Docs](http://192.168.0.22:8080/swagger-ui.html) (si está configurado)
- [API Config Frontend](../frontend/src/config/apiConfig.ts)

---

**Última actualización**: Marzo 2026
