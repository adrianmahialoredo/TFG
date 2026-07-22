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

### 👥 Usuarios (`/api/users`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/users` | Obtener todos los usuarios |
| `GET` | `/api/users/{id}` | Obtener usuario por ID |
| `PUT` | `/api/users/{id}` | Actualizar usuario |
| `DELETE` | `/api/users/{id}` | Eliminar usuario |
| `DELETE` | `/api/users/all` | Eliminar todos los usuarios |

---

### 🎵 Géneros (`/genres`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/genres` | Obtener todos los géneros |
| `GET` | `/genres/{id}` | Obtener género por ID |
| `POST` | `/genres` | Crear nuevo género |
| `PUT` | `/genres/{id}` | Actualizar género |
| `DELETE` | `/genres/{id}` | Eliminar género |

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

### 🎸 Canciones (`/songs`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/songs` | Obtener todas las canciones |
| `GET` | `/songs/{id}` | Obtener canción por ID |
| `GET` | `/songs/decades/{decade}` | Obtener canciones por década |
| `GET` | `/songs/popular` | Obtener canciones populares |
| `GET` | `/songs/popular/{genreId}` | Canciones populares por género |
| `GET` | `/songs/new/{genreId}` | Canciones nuevas por género |
| `GET` | `/songs/classic/{genreId}` | Canciones clásicas por género |
| `POST` | `/songs` | Crear nueva canción |
| `PUT` | `/songs/{id}` | Actualizar canción |
| `DELETE` | `/songs/{id}` | Eliminar canción |

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

### 📖 Lecciones de Canciones (`/song-lessons`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/song-lessons/{id}` | Obtener lección por ID |
| `GET` | `/song-lessons/by-song/{songId}` | Obtener lecciones de una canción |
| `POST` | `/song-lessons` | Crear nueva lección |
| `PUT` | `/song-lessons/{id}` | Actualizar lección |
| `DELETE` | `/song-lessons/{id}` | Eliminar lección |

---

### 🎼 Acordes (`/api/chords`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/chords` | Obtener todos los acordes |
| `GET` | `/api/chords/{id}` | Obtener acorde por ID |
| `POST` | `/api/chords` | Crear nuevo acorde |
| `PUT` | `/api/chords/{id}` | Actualizar acorde |
| `DELETE` | `/api/chords/{id}` | Eliminar acorde |

---

### 🎹 Instrumentos (`/instruments`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/instruments` | Obtener todos los instrumentos |
| `GET` | `/instruments/{id}` | Obtener instrumento por ID |
| `POST` | `/instruments` | Crear nuevo instrumento |
| `PUT` | `/instruments/{id}` | Actualizar instrumento |
| `DELETE` | `/instruments/{id}` | Eliminar instrumento |

---

### 🎵🎹 Géneros de Instrumentos (`/instrument-genres`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/instrument-genres` | Obtener todas las relaciones |
| `GET` | `/instrument-genres/{id}` | Obtener relación por ID |
| `POST` | `/instrument-genres` | Crear nueva relación |
| `PUT` | `/instrument-genres/{id}` | Actualizar relación |
| `DELETE` | `/instrument-genres/{id}` | Eliminar relación |

---

### 📝 Listas de Elementos (`/item-lists`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/item-lists` | Obtener todas las listas |
| `GET` | `/item-lists/{id}` | Obtener lista por ID |
| `GET` | `/item-lists/by-user/{userId}` | Obtener listas de un usuario |
| `POST` | `/item-lists` | Crear nueva lista |
| `PUT` | `/item-lists/{id}` | Actualizar lista |
| `DELETE` | `/item-lists/{id}` | Eliminar lista |

---

### 🎼 Secciones de Tab (`/tab-sections`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/tab-sections/{id}` | Obtener sección por ID |
| `GET` | `/tab-sections/by-lesson/{lessonId}` | Obtener secciones de una lección |
| `POST` | `/tab-sections` | Crear nueva sección |
| `PUT` | `/tab-sections/{id}` | Actualizar sección |
| `DELETE` | `/tab-sections/{id}` | Eliminar sección |

---

### 🎵 Spotify (`/api/spotify`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/spotify/login` | Iniciar sesión con Spotify |
| `GET` | `/api/spotify/callback` | Callback de autenticación Spotify |
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
