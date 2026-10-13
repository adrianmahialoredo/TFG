# Backend - TFG (Trabajo de Fin de Grado)

## 📋 Descripción General

Este es el backend de la aplicación TFG, desarrollado con **Spring Boot 3.4.3** y **Java 23**. Proporciona una API RESTful completa para gestionar géneros musicales, canciones, lecciones, acordes, instrumentos y más.

La aplicación utiliza:
- **Base de datos**: PostgreSQL con Spring Data JPA (Hibernate 6)
- **Seguridad**: JWT (JSON Web Tokens) para autenticación
- **API REST**: Spring Web
- **Integración Spotify**: Para recomendaciones musicales
- **Build**: Maven

---

## 🚀 Inicio Rápido

### Requisitos Previos
- Java 23 o superior
- Maven 3.9+
- PostgreSQL 14+ con una base de datos `demo` creada (`CREATE DATABASE demo;`)

### Instalación

```bash
# Clonar el repositorio
git clone <repository-url>
cd TFG

# Crear el fichero de variables locales y rellenar las credenciales
cp .env.example .env

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
| `POST` | `/api/auth/verification-code` | Enviar un código de verificación al email de un registro (`204`; `429` si se pide otro antes de 60 s) |
| `POST` | `/api/auth/register` | Registrar nuevo usuario con ese código (`verificationCode` en el body) y devolver el JWT |
| `POST` | `/api/auth/login` | Iniciar sesión (retorna JWT token) |

**Registro con verificación de email:**
```json
POST /api/auth/verification-code
{ "email": "user@example.com" }

POST /api/auth/register
{
  "email": "user@example.com",
  "name": "...", "username": "...", "password": "...",
  "favouriteGenresIds": [1, 2], "favouriteInstrumentsIds": [3, 4],
  "verificationCode": "482913"
}
```
El código tiene 6 dígitos, caduca a los 10 minutos y se descarta tras 5 intentos fallidos. Detalle en [`architecture.md`](architecture.md) §9.

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
| `POST` | `/api/users/{id}/favorite-genres/{genreId}` | Añadir un género a los favoritos del usuario |
| `DELETE` | `/api/users/{id}/favorite-genres/{genreId}` | Quitar un género de los favoritos del usuario |
| `GET` | `/api/users/{id}/favorite-instruments` | IDs de los instrumentos favoritos del usuario |
| `POST` | `/api/users/{id}/favorite-instruments/{instrumentId}` | Añadir un instrumento a los favoritos del usuario |
| `DELETE` | `/api/users/{id}/favorite-instruments/{instrumentId}` | Quitar un instrumento de los favoritos del usuario |
| `POST` | `/api/users` | Crear usuario |
| `PUT` | `/api/users/{id}` | Actualizar usuario (el email no se cambia: se ignora el del body) |
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

Devuelven `ItemSummaryDTO` (`id`, `title`, `imageUrl`, `type`, `level`, `artist`), salvo las rutas `/ids` (solo IDs).

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/items` | Resumen de todos los elementos (catálogo que la app carga al arrancar) |
| `POST` | `/api/items/by-ids` | Obtener varios elementos por ID (array de IDs en el body) |
| `GET` | `/api/items/{id}` | Obtener elemento por ID |
| `GET` | `/api/items/search?q={texto}` | Buscar elementos por título |
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

1. Usuario pide un código de verificación para su email y se registra con él
2. Usuario hace login (el registro ya devuelve el token)
3. Backend devuelve JWT token
4. Cliente incluye token en todos los requests
5. Backend valida token con `JwtAuthenticationFilter`

### Control de acceso

- **Públicas:** `/api/auth/**`, `/api/spotify/callback`, `/actuator/health` y las listas `GET /api/genres` y `GET /api/instruments` (el formulario de registro las necesita antes de tener token; el detalle por id sigue protegido). Todo lo demás exige JWT (sin token: `401`).
- **Solo `ADMIN`:** crear, editar y borrar contenido del catálogo (géneros, canciones, lecciones, acordes, instrumentos…) `GET`/`POST /api/users` y `GET /api/item-lists` (todas las listas).
- **Solo el propio usuario (o un `ADMIN`):** leer y modificar su perfil y sus favoritos, sus listas y sus items, y pedir sus recomendaciones (las personales y las de Spotify). Si no es suyo: `403`.
- **Administrador inicial:** se crea al arrancar con `ADMIN_EMAIL` y `ADMIN_PASSWORD` del `.env`. Los scripts de carga de datos (`scripts/insert_*.py`) inician sesión con él.

Detalle en [`architecture.md`](architecture.md) §9.

---

## 📁 Estructura del Proyecto

```
src/main/java/com/uo289390/toco/
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
├── repository/         # Acceso a datos (Spring Data JPA)
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

No contiene secretos: las contraseñas y credenciales se leen de variables con `${...}`.

```properties
# Server
server.address=0.0.0.0
server.port=8080

# Variables locales desde .env (si existe)
spring.config.import=optional:file:.env[.properties]

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/demo
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update

# Spotify
spotify.client-id=${SPOTIFY_CLIENT_ID}
spotify.client-secret=${SPOTIFY_CLIENT_SECRET}
spotify.redirect-uri=${SPOTIFY_REDIRECT_URI:http://127.0.0.1:8080/api/spotify/callback}

# Email (código de verificación del registro)
spring.mail.host=${MAIL_HOST:}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
app.mail.from=${MAIL_FROM:TOCO <${MAIL_USERNAME:no-reply@toco.app}>}
```

### `.env`

Fichero local en la raíz del proyecto, **ignorado por git**. Se crea copiando la plantilla `.env.example`:

```properties
DB_PASSWORD=<contraseña de PostgreSQL>
SPOTIFY_CLIENT_ID=<client id de la app de Spotify>
SPOTIFY_CLIENT_SECRET=<client secret de la app de Spotify>
ADMIN_EMAIL=<email del administrador inicial>
ADMIN_PASSWORD=<contraseña del administrador inicial>
# Opcional: túnel HTTPS para probar el login de Spotify desde el móvil
# SPOTIFY_REDIRECT_URI=https://<dominio>.ngrok-free.dev/api/spotify/callback
# SMTP para el código de verificación (Gmail: smtp.gmail.com y una contraseña de aplicación)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<cuenta de Gmail>
MAIL_PASSWORD=<contraseña de aplicación de 16 letras>
# MAIL_FROM=TOCO <...>   (por defecto, TOCO <MAIL_USERNAME>)
```

Spring lo importa al arrancar y los scripts de `scripts/` (`clean_db.py`, `migrate_*.py`) también lo leen (`scripts/env_utils.py`). Las variables de entorno reales tienen prioridad sobre el `.env`.

---

## 🛠️ Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|-----------|---------|----------|
| Spring Boot | 3.4.3 | Framework principal |
| Java | 23 | Lenguaje de programación |
| Spring Data JPA (Hibernate 6) | 3.4.3 (Boot) | ORM |
| Spring Security | 3.4.3 (Boot) | Autenticación/Autorización |
| JWT (jjwt) | 0.11.5 | Token-based auth |
| PostgreSQL | 14+ | Base de datos |
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
- Asegurarse de que PostgreSQL está corriendo en `localhost:5432` y que existe la base de datos `demo`
- Comprobar que `.env` existe y que `DB_PASSWORD` es correcta
- `Could not resolve placeholder 'DB_PASSWORD'` (o `SPOTIFY_*`): falta la variable en `.env`

### Error: JWT token inválido
- El token ha expirado
- Token incorrecto o corrupto
- Verificar `jwt.secret-key` (si no se define, se usa una clave por defecto)

### Error: CORS
- Configurar CORS en `WebConfig`
- Agregar anotación `@CrossOrigin` en controladores

### Error: no llega el código de verificación
- `Couldn't connect to host, port: localhost, 587`: `MAIL_HOST` está vacío o comentado en `.env`. Con la propiedad vacía Spring crea igualmente el cliente de correo y apunta a `localhost`.
- `535 Authentication failed` (Gmail): `MAIL_PASSWORD` tiene que ser una contraseña de aplicación (https://myaccount.google.com/apppasswords, requiere verificación en dos pasos), no la contraseña de la cuenta.
- Tras cambiar `.env` hay que reiniciar el backend: solo se lee al arrancar.

---

## ✉️ Plantillas de email (React Email)

Las plantillas se escriben en React con [React Email](https://react.email) en la carpeta `emails/` (proyecto Node aparte: el backend Java no puede renderizar React).

```bash
cd emails
npm install
npm run dev      # vista previa en el navegador
npm run export   # genera src/main/resources/templates/email/<plantilla>.html y .txt
```

`export` deja marcadores `{{code}}`, `{{minutes}}` que `EmailServiceImpl` sustituye al enviar. Hay que volver a ejecutarlo tras cambiar una plantilla y subir los ficheros generados.

---

## 🚀 Deployment

### Build para producción

```bash
./mvnw clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Variables de entorno necesarias

Las mismas que en `.env` (en producción, como variables de entorno reales):

```bash
DB_PASSWORD=
SPOTIFY_CLIENT_ID=
SPOTIFY_CLIENT_SECRET=
SPOTIFY_REDIRECT_URI=
ADMIN_EMAIL=
ADMIN_PASSWORD=
MAIL_HOST=
MAIL_PORT=
MAIL_USERNAME=
MAIL_PASSWORD=
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
