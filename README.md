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

El servidor escucha en el puerto `8080` de todas las interfaces (`http://localhost:8080` en el propio PC y `http://<IP local del PC>:8080` desde la red).

---

## 📱 App móvil (`frontend/`)

Hay dos formas de probar la app:

| | **A. Expo Go** (lo más rápido) | **B. Development build con EAS** (app nativa completa) |
|---|---|---|
| Dónde | Móvil físico (Android o iOS) o emulador | Emulador de Android o móvil Android |
| Qué hay que instalar | Node.js y la app **Expo Go** | Node.js y `eas-cli` (cuenta gratuita de expo.dev) |
| Compilar | No | En la nube, una sola vez (15–20 min); nada en local |
| Cambios en directo | Sí | Sí: Metro recarga el JavaScript al guardar, igual que en Expo Go |
| Afinador | Modo demostración: una cuerda simulada en vez del micrófono | Micrófono real |

El afinador usa `react-native-audio-api`, un módulo nativo que **no está en Expo Go**: ahí todo funciona igual salvo el micrófono, que se sustituye por una cuerda simulada que pasa por el mismo análisis. Detalle en [`architecture.md`](architecture.md) §12 (*Afinador*).

### Configuración (común)

Con el backend arrancado (ver arriba):

```bash
cd frontend
npm install
cp .env.example .env   # y rellenar
```

`EXPO_PUBLIC_API_URL` es la URL del backend (sin `/api`):

| Dónde se ejecuta la app | Valor |
|---|---|
| Emulador de Android | vacío (usa `http://10.0.2.2:8080`, el PC visto desde el emulador) |
| Simulador de iOS | vacío (usa `http://localhost:8080`) |
| Móvil físico | `http://<IP local del PC>:8080` (por ejemplo `http://192.168.1.20:8080`); el móvil y el PC en la misma wifi |

La IP local del PC se ve con `ipconfig` (Windows, "Dirección IPv4") o `ipconfig getifaddr en0` (macOS). En Windows, la primera vez que arranca el backend el firewall pregunta si se permite Java en la red: hay que permitirlo en **redes privadas** o el móvil no llegará al puerto `8080`.

Las variables `EXPO_PUBLIC_*` se incrustan al empaquetar: tras cambiarlas, `npx expo start --clear` (o volver a compilar).

### A. Expo Go

1. Instalar **Expo Go** en el móvil (Play Store / App Store). Tiene que ser la versión compatible con el **SDK 58** de Expo (la última de la tienda).
2. Arrancar Metro en modo Expo Go:

   ```bash
   npx expo start --go
   ```

3. Escanear el código QR: con la cámara en iOS o desde Expo Go en Android.

También sirve en el emulador: con el emulador abierto, pulsar `a` en la terminal de Metro instala Expo Go en él y abre la app.

Si el móvil no conecta con Metro (redes de universidad o con aislamiento de clientes), `npx expo start --go --tunnel` lo hace a través de internet. Ojo: el backend sigue teniendo que ser accesible desde el móvil por la IP local.

### B. Development build con EAS

La *development build* es la app nativa completa (con `expo-dev-client`, el módulo del micrófono y el resto de módulos nativos). Se compila **en la nube con EAS Build**, así que no hace falta Android Studio para compilar ni JDK, y se evitan los problemas de rutas largas de Windows. Solo se compila una vez: después se desarrolla **en directo** igual que con Expo Go, porque Metro sirve el JavaScript desde el PC y la app se recarga al guardar.

#### Compilar e instalar (una vez)

Desde `frontend/`:

```bash
npm install -g eas-cli
eas login                                       # cuenta gratuita de expo.dev
eas init                                        # solo la primera vez: añade el projectId a app.json
eas build -p android --profile development
```

- Si pregunta por la clave de firma (*keystore*), contestar **Yes** para que EAS la cree y la guarde.
- Al terminar da un enlace (y un QR) al `.apk`. En el emulador se instala **arrastrándolo a su ventana**; en un móvil, abriendo el enlace (hay que permitir instalar apps de origen desconocido).
- En esta build no hace falta configurar variables en EAS: la URL del backend y el resto de `EXPO_PUBLIC_*` las pone Metro desde el `.env` local.

#### Desarrollar en directo

Con el backend arrancado:

```bash
npx expo start
```

Abrir la app **Toco** instalada (no Expo Go) y tocar el servidor que aparece en el lanzador ("Development servers"), o pulsar `a` en la terminal de Metro.

- Al guardar un fichero, la app se recarga sola. `r` en Metro la recarga entera.
- Si se cambia el `.env`, parar Metro y arrancar con `npx expo start --clear`.
- **Solo hay que volver a ejecutar `eas build`** si cambia algo nativo: una dependencia con código nativo, los `plugins` o permisos de `app.json`, o la versión del SDK de Expo. El síntoma típico de que falta recompilar es un error de *native module not found*. El APK nuevo se instala encima del anterior.

#### Probar el micrófono en el emulador

Por defecto el emulador de Android no oye el micrófono del PC:

1. En el emulador, **⋯** (*Extended controls*) → **Microphone** → activar **"Virtual microphone uses host audio input"**. Se desactiva cada vez que se reinicia el emulador.
2. La primera vez que se abre el afinador, aceptar el permiso de micrófono.

El micrófono del emulador puede tener algo de retardo o ruido: la prueba buena del afinador es en un móvil físico.

---

## 🌐 Despliegue (Render + Neon + APK)

El backend se publica en **Render** (con Docker: Render no tiene runtime de Java) y la base de datos en **Neon** (PostgreSQL). La app se distribuye como **APK** generado con **EAS Build**, que compila en la nube, sin herramientas nativas en local.

### 1. Base de datos (Neon)

1. Crear un proyecto en [neon.tech](https://neon.tech) (región de Europa, la misma zona que Render).
2. En *Connection Details* copiar el host, la base, el usuario y la contraseña. Neon da una URL `postgresql://usuario:contraseña@host/base?sslmode=require`; Spring necesita el formato JDBC con el usuario y la contraseña aparte:

| Variable | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://<host>.neon.tech/<base>?sslmode=require` |
| `DB_USERNAME` | el usuario de Neon |
| `DB_PASSWORD` | la contraseña de Neon |

Las tablas las crea Hibernate al arrancar (`ddl-auto=update`).

### 2. Backend (Render)

1. En Render: *New → Blueprint* y elegir el repositorio. `render.yaml` crea el servicio `toco-backend` (Docker, plan gratuito, *health check* en `/actuator/health`).
2. Rellenar las variables que pide (las de `sync: false`). `JWT_SECRET_KEY` la genera Render, `SHOW_SQL` va a `false` y `LAZY_INIT` a `true` (arranque perezoso, ver `application.properties`).
3. Al terminar, el backend queda en `https://<servicio>.onrender.com` (comprobarlo en `/actuator/health`).

| Variable | Valor |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | los de Neon |
| `SPOTIFY_CLIENT_ID`, `SPOTIFY_CLIENT_SECRET` | los de la app de Spotify |
| `SPOTIFY_REDIRECT_URI` | `https://<servicio>.onrender.com/api/spotify/callback` |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | el administrador inicial (lo usan los scripts de carga) |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | el SMTP del código de verificación |

- **Correo:** el plan gratuito de Render bloquea los puertos SMTP habituales (25, 465 y 587), así que Gmail no funciona (`Couldn't connect to host, port: smtp.gmail.com, 587`). Hay que usar un proveedor con otro puerto, por ejemplo Brevo (`smtp-relay.brevo.com`, puerto `2525`). El correo no forma parte de `/actuator/health` (`management.health.mail.enabled=false`): si el SMTP falla, el backend sigue en `UP` y el error sale al enviar el código.
- **Arranque en frío:** el plan gratuito se duerme tras 15 minutos sin peticiones y la primera tarda en torno a un minuto. Antes de una demo, abrir `/actuator/health` para despertarlo.

### 3. Datos iniciales

Con el backend ya publicado, cargar el catálogo desde local apuntando los scripts a Render. El `.env` de la raíz tiene al final un bloque **PRODUCCIÓN** comentado: descomentar `API_BASE_URL` y usar el `ADMIN_EMAIL`/`ADMIN_PASSWORD` configurados en Render.

```bash
python scripts/load_all_data.py
```

Para vaciar la base de datos de Neon antes de recargar, descomentar también `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` de ese bloque (`clean_db.py` lee el `?sslmode=require` de la URL) y reiniciar el servicio en Render después, para que `AdminBootstrap` vuelva a crear el administrador (`clean_db.py` vacía también la tabla de usuarios).

```bash
python scripts/clean_db.py
```

**Al terminar, volver a comentar el bloque**: con él activo, los scripts y el backend local trabajan contra producción.

**Base de datos ya existente:** `Preferences` ya no tiene `difficultyLevel` (el nivel es `users.difficulty_level`). Hibernate (`ddl-auto=update`) no borra columnas y la vieja `preferences.difficulty_level` es `NOT NULL`, así que los registros nuevos fallarían. Antes de desplegar esta versión, ejecutar una vez (con el bloque de producción descomentado para Neon):

```bash
python scripts/migrate_preferences_table.py
```

### 4. Spotify

En el [dashboard de Spotify](https://developer.spotify.com/dashboard), en *Redirect URIs*, añadir `https://<servicio>.onrender.com/api/spotify/callback` (exactamente igual que `SPOTIFY_REDIRECT_URI`). La vuelta del login pasa por el backend, no por la app, así que no hace falta nada más en la app.

Mientras la app de Spotify esté en **modo desarrollo**, solo pueden conectar las cuentas añadidas en *User Management* (con un número máximo de usuarios). Al resto, "Conectar con Spotify" les da error.

### 5. APK (EAS Build)

Una sola vez, desde `frontend/`:

```bash
npm install -g eas-cli
eas login                 # cuenta gratuita de expo.dev
eas init                  # crea el proyecto en Expo y añade su projectId a app.json
```

Las variables `EXPO_PUBLIC_*` se incrustan al compilar. El `.env` no se sube a EAS (está en `.gitignore`), así que se configuran en EAS para el entorno `preview`:

```bash
eas env:create --environment preview --name EXPO_PUBLIC_API_URL --value https://<servicio>.onrender.com --visibility plaintext
eas env:create --environment preview --name EXPO_PUBLIC_CLOUDINARY_CLOUD_NAME --value <cloud name> --visibility plaintext
eas env:create --environment preview --name EXPO_PUBLIC_CLOUDINARY_UPLOAD_PRESET --value <preset> --visibility plaintext
```

Generar el APK:

```bash
eas build -p android --profile preview
```

Al terminar, EAS da un enlace (y un QR) para descargar el `.apk` e instalarlo en cualquier Android (hay que permitir instalar apps de origen desconocido). EAS crea y guarda la clave de firma la primera vez: todos los APK se firman con la misma, así que las versiones nuevas se instalan encima sin desinstalar. La development build usa la misma clave y el mismo paquete (`com.uo289390.toco`), así que el APK también se instala encima de ella (y la sustituye: para volver a desarrollar con Metro hay que reinstalar la development build). En cada versión nueva, subir `android.versionCode` (y `version`) en `app.json`.

Los perfiles de `eas.json`:

| Perfil | Qué genera |
|---|---|
| `preview` | APK para distribuir: el JavaScript va dentro, sin Metro |
| `development` | APK de la *development build* (con `expo-dev-client`), para desarrollar en directo con Metro (ver [B. Development build con EAS](#b-development-build-con-eas)) |

---

## 📡 Endpoints de API

### 🔐 Autenticación (`/api/auth`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/auth/verification-code` | Enviar un código de verificación al email de un registro (`204`; `429` si se pide otro antes de 60 s) |
| `POST` | `/api/auth/register` | Registrar nuevo usuario con ese código (`verificationCode` en el body) y devolver el JWT |
| `POST` | `/api/auth/login` | Iniciar sesión con email o nombre de usuario (retorna JWT token) |
| `POST` | `/api/auth/password-reset/code` | "Olvidé mi contraseña": enviar un código al email de la cuenta (`204` exista o no la cuenta; `429` si se pide otro antes de 60 s) |
| `POST` | `/api/auth/password-reset/verify` | Comprobar ese código sin gastarlo (`204`; `400` si no es válido) |
| `POST` | `/api/auth/password-reset` | Cambiar la contraseña con el código (`204`; `400` si el código o la contraseña no valen) |

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

**Recuperación de contraseña** (desde "Olvidé mi contraseña" en el login):
```json
POST /api/auth/password-reset/code    { "email": "user@example.com" }
POST /api/auth/password-reset/verify  { "email": "user@example.com", "code": "730215" }
POST /api/auth/password-reset         { "email": "user@example.com", "code": "730215", "newPassword": "Nueva1!x" }
```
Los códigos de recuperación siguen las mismas reglas que los del registro, pero se guardan aparte: uno no sirve para el otro. Si el email no tiene cuenta no se envía nada, pero la respuesta es la misma (`204`). La contraseña nueva cumple la misma política que en el registro. Después, el usuario inicia sesión con ella.

**Ejemplo Login** (`identifier` es el email o el nombre de usuario; se sigue aceptando el campo antiguo `email`):
```json
POST /api/auth/login
{
  "identifier": "user@example.com",
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
| `GET` | `/api/users/{id}/top-artists?total_artists={n}` | Artistas favoritos del usuario: los más repetidos en las canciones de sus listas (`n` de 1 a 10, 5 por defecto) |
| `POST` | `/api/users` | Crear usuario |
| `PUT` | `/api/users/{id}` | Actualizar usuario (el email no se cambia: se ignora el del body). `difficultyLevel` (0 a 5) es opcional: si no se envía, se conserva; fuera de rango, `400` |
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
| `GET` | `/api/songs/by-artists/new?limit={n}&artist1={a}&artist2={b}…` | Canciones nuevas de esos artistas, mezclados por turnos (DTO completo; hasta 10 artistas y 50 canciones, 10 por defecto) |
| `GET` | `/api/songs/by-artists/popular?limit={n}&artist1={a}&artist2={b}…` | Canciones populares de esos artistas, mezclados por turnos (DTO completo; mismos límites) |
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

**Modelo ChordDto:**
```json
{
  "id": 1,
  "name": "Gmaj7",
  "notes": ["G", "B", "D", "F#"],
  "imageUrl": "https://...",
  "open": true, "barre": false, "powerChord": false,
  "minor": false, "major": true, "seventh": true,
  "capo": false,
  "capoFret": 0,
  "instrumentId": 1
}
```
`capoFret` es el traste del capo (0 a 12; 0 = sin capo). La app muestra estos datos en un modal al pulsar un acorde de una canción.

`instrumentId` es obligatorio: cada acorde pertenece a un instrumento, así que un mismo nombre (p. ej. `Cm`) puede existir una vez para guitarra, otra para piano, etc. (el nombre es único **por instrumento**). Una lección de canción solo puede usar acordes de su propio instrumento; si `chordIds` incluye uno de otro instrumento, crear o actualizar la lección devuelve `400`.

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

**Modelo LessonSectionDTO:**
```json
{
  "id": 7,
  "lessonId": 101,
  "title": "El acorde de quinta",
  "htmlContent": "<h3>El acorde de quinta</h3><p>...</p>",
  "videoUrl": "https://www.youtube.com/watch?v=...",
  "orderIndex": 0
}
```
`videoUrl` es opcional: un vídeo de apoyo de YouTube que la app muestra encima del contenido de la sección.

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

### 🎚️ Afinaciones (`/api/tunes`)

Afinaciones de cada instrumento que usa el afinador de la app. Las notas van de la cuerda más grave a la más aguda, en notación científica (`E2`, `F#3`); la app calcula su frecuencia (La4 = 440 Hz).

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/tunes` | Obtener todas las afinaciones |
| `POST` | `/api/tunes/by-ids` | Obtener varias afinaciones por ID (array de IDs en el body) |
| `GET` | `/api/tunes/{id}` | Obtener afinación por ID |
| `GET` | `/api/tunes/by-instrument/{instrumentId}` | Afinaciones de un instrumento (la estándar primero) |
| `POST` | `/api/tunes` | Crear afinación |
| `PUT` | `/api/tunes/{id}` | Actualizar afinación |
| `DELETE` | `/api/tunes/{id}` | Eliminar afinación |

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
| `GET` | `/api/spotify/recommendations/songs/{userId}?limit={n}` | Canciones recomendadas a partir de su Spotify (DTO completo): sus canciones top que están en la app, completadas con las de sus 5 artistas top |
| `GET` | `/api/spotify/recommendations/items/{userId}` | Elementos recomendados a partir de su Spotify, DTO completo de cada subtipo (carrusel "Basado en tu Spotify" del Inicio): 6 canciones como las anteriores, 3 lecciones de sus géneros, 2 instrument-genres de sus géneros e instrumentos más repetidos y el género más repetido. Barajados y sin nada de lo que ya está en 3 o más de sus listas; vacío si ninguna canción de Spotify está en la app |

`{userId}` es el ID del usuario de la app, que debe tener Spotify vinculado. Si no lo tiene, el Inicio muestra el botón "Conectar con Spotify" en lugar del carrusel.

---

## 🔒 Seguridad

### JWT Authentication

La API utiliza **JSON Web Tokens (JWT)** para autenticación. Después de login, incluye el token en el header:

```bash
Authorization: Bearer <jwt-token>
```

### Flujo de Autenticación

1. Usuario pide un código de verificación para su email y se registra con él
2. Usuario hace login (el registro ya devuelve el token). Si olvida la contraseña, la cambia con un código enviado a su email ("Olvidé mi contraseña")
3. Backend devuelve JWT token
4. Cliente incluye token en todos los requests
5. Backend valida token con `JwtAuthenticationFilter`

### Control de acceso

- **Públicas:** `/api/auth/**`, `/api/spotify/callback`, `/actuator/health` y las listas `GET /api/genres` y `GET /api/instruments` (el formulario de registro las necesita antes de tener token; el detalle por id sigue protegido). Todo lo demás exige JWT (sin token: `401`).
- **Solo `ADMIN`:** crear, editar y borrar contenido del catálogo (géneros, canciones, lecciones, acordes, instrumentos, afinaciones…) `GET`/`POST /api/users` y `GET /api/item-lists` (todas las listas).
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
server.port=${PORT:8080}
# Beans al primer uso (true en Render, para abrir el puerto a tiempo con su poca CPU)
spring.main.lazy-initialization=${LAZY_INIT:false}

# Variables locales desde .env (si existe)
spring.config.import=optional:file:.env[.properties]

# PostgreSQL
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/demo}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=${SHOW_SQL:true}

# JWT (vacía: clave por defecto insegura, solo para local)
jwt.secret-key=${JWT_SECRET_KEY:}

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

Fichero local en la raíz del proyecto, **ignorado por git**. Se crea copiando la plantilla `.env.example`, organizada por bloques:

```properties
# --- Base de datos ---  (local: localhost:5432/demo, usuario postgres)
DB_PASSWORD=<contraseña de PostgreSQL>

# --- Administrador inicial ---
ADMIN_EMAIL=<email del administrador inicial>
ADMIN_PASSWORD=<contraseña del administrador inicial>
# ADMIN_USERNAME=admin

# --- Spotify ---
SPOTIFY_CLIENT_ID=<client id de la app de Spotify>
SPOTIFY_CLIENT_SECRET=<client secret de la app de Spotify>
# SPOTIFY_REDIRECT_URI=https://<dominio>.ngrok-free.dev/api/spotify/callback   (túnel para el móvil)

# --- Email --- (sin MAIL_HOST el código sale en el log)
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=2525
MAIL_USERNAME=<usuario SMTP de Brevo>
MAIL_PASSWORD=<clave SMTP de Brevo>
# MAIL_FROM=TOCO <...>   (por defecto, TOCO <MAIL_USERNAME>)

# --- Opcionales ---
# JWT_SECRET_KEY=
# SHOW_SQL=false

# --- PRODUCCIÓN (comentado: descomentar solo para trabajar contra Neon/Render desde el PC) ---
# DB_URL=jdbc:postgresql://<host>.neon.tech/<base>?sslmode=require
# DB_USERNAME=
# DB_PASSWORD=
# API_BASE_URL=https://<servicio>.onrender.com
```

En local también sirve Gmail (`smtp.gmail.com`, `587` y una contraseña de aplicación); en Render no, porque bloquea ese puerto (ver *Despliegue*).

Spring lo importa al arrancar y los scripts de `scripts/` (`clean_db.py`, `migrate_*.py` y los de carga, que usan `API_BASE_URL` y `ADMIN_*`) también lo leen (`scripts/env_utils.py`). Si una variable aparece dos veces, vale la última. Las variables de entorno reales tienen prioridad sobre el `.env`.

---

## 🧪 Pruebas

- **Unitarias:** `./mvnw test` (backend, JUnit 5 + JaCoCo en `target/site/jacoco/`) y `cd frontend && npm run test:coverage` (Jest + Testing Library).
- **De sistema, de carga (k6) y de usabilidad (SUS):** herramientas y guía en [`tests/README.md`](tests/README.md). Los resultados reales quedan en las carpetas `results/` de cada una.

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
| k6 | 2.3 | Pruebas de carga (`tests/load`) |

---

## 📝 Modelos de Datos

### Relaciones Principales

- **User** → **ItemList** (1 a N)
- **Genre** → **Song** (N a N)
- **Song** → **SongLesson** (1 a N)
- **SongLesson** → **TabSection** (1 a N)
- **Instrument** → **Genre** (N a N vía InstrumentGenre)
- **Instrument** → **Tune** (1 a N): afinaciones del afinador

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
- **404 en todas las pantallas tras `clean_db.py`:** la app guarda un JWT de un usuario que ya no existe. Cerrar sesión y volver a registrarse.

### Error: CORS
- Configurar CORS en `WebConfig`
- Agregar anotación `@CrossOrigin` en controladores

### Error: no llega el código de verificación
- `Couldn't connect to host, port: localhost, 587`: `MAIL_HOST` está vacío o comentado en `.env`. Con la propiedad vacía Spring crea igualmente el cliente de correo y apunta a `localhost`.
- `Couldn't connect to host, port: smtp.gmail.com, 587` en Render: el plan gratuito bloquea ese puerto; usar Brevo en el `2525`.
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

Plantillas: `verification-code` (registro) y `password-reset` (recuperación de contraseña). `export` deja marcadores `{{code}}`, `{{minutes}}` que `EmailServiceImpl` sustituye al enviar. Hay que volver a ejecutarlo tras cambiar una plantilla y subir los ficheros generados.

---

## 🚀 Deployment

### Build para producción

```bash
./mvnw clean package -DskipTests
java -jar target/TOCO-0.0.1-SNAPSHOT.jar
```

### Variables de entorno necesarias

Las mismas que en `.env`, como variables de entorno reales (en Render, las de `render.yaml`):

```bash
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET_KEY=
SHOW_SQL=false
LAZY_INIT=true
SPOTIFY_CLIENT_ID=
SPOTIFY_CLIENT_SECRET=
SPOTIFY_REDIRECT_URI=
ADMIN_EMAIL=
ADMIN_PASSWORD=
MAIL_HOST=
MAIL_PORT=
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=
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
- [Swagger/OpenAPI Docs](http://localhost:8080/swagger-ui.html) (si está configurado)
- [API Config Frontend](../frontend/src/config/apiConfig.ts)

---

**Última actualización**: Marzo 2026
