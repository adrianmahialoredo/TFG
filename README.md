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

> ⚠️ **Windows: compilar la app nativa de Android (`frontend/`) necesita rutas largas.** CMake repite la ruta del proyecto dentro de sus carpetas y supera el límite de 260 caracteres de Windows, incluso desde una ruta corta como `E:\frontend`. Hay que activar las rutas largas de Windows y usar Ninja 1.12 o posterior, o compilar en la nube con EAS Build. No afecta al backend ni a Expo Go. Ver [Problemas en Windows](#problemas-en-windows).

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

| | **A. Expo Go** (lo más rápido) | **B. Development build** (app nativa completa) |
|---|---|---|
| Dónde | Móvil físico (Android o iOS) o emulador | Emulador de Android / simulador de iOS (o móvil por USB) |
| Qué hay que instalar | Node.js y la app **Expo Go** | Node.js, Android Studio (o Xcode en macOS), JDK 17–23 |
| Compilar | No | Sí (la primera vez, 5–15 min) |
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

1. Instalar **Expo Go** en el móvil (Play Store / App Store). Tiene que ser la versión compatible con el **SDK 57** de Expo (la última de la tienda).
2. Arrancar Metro en modo Expo Go:

   ```bash
   npx expo start --go
   ```

3. Escanear el código QR: con la cámara en iOS o desde Expo Go en Android.

También sirve en el emulador: con el emulador abierto, pulsar `a` en la terminal de Metro instala Expo Go en él y abre la app.

Si el móvil no conecta con Metro (redes de universidad o con aislamiento de clientes), `npx expo start --go --tunnel` lo hace a través de internet. Ojo: el backend sigue teniendo que ser accesible desde el móvil por la IP local.

### B. Development build

> ⚠️ **Windows:** sin rutas largas activadas y Ninja 1.12 o posterior, la compilación falla en un `buildCMake…` con `ninja: error: mkdir(...)`. Ver [Problemas en Windows](#problemas-en-windows). Alternativa sin compilar en local: `eas build -p android --profile development` (ver [Despliegue](#-despliegue-render--neon--apk)).

#### Requisitos

| | Android | iOS |
|---|---|---|
| Sistema | Windows, macOS o Linux | **Solo macOS** |
| Node.js | 20 LTS o superior | 20 LTS o superior |
| Herramientas | [Android Studio](https://developer.android.com/studio) con un emulador (AVD) creado (*Device Manager*) | Xcode (App Store) con un simulador, y CocoaPods (`brew install cocoapods`) |
| JDK | **17 a 23** (por ejemplo [Temurin 17](https://adoptium.net/)). El que trae Android Studio puede ser más nuevo y no sirve (ver *Problemas en Windows*) | — |
| Variables de entorno | `ANDROID_HOME` = carpeta del SDK y `%ANDROID_HOME%\platform-tools` en el `PATH`; `JAVA_HOME` = el JDK 17–23 | — |

La carpeta del SDK aparece en Android Studio en *Settings → Languages & Frameworks → Android SDK* (en Windows suele ser `C:\Users\<usuario>\AppData\Local\Android\Sdk`). En Windows, las variables se crean en *Editar las variables de entorno de esta cuenta*; después hay que **cerrar y volver a abrir** las terminales y VS Code para que las vean.

#### Ejecutar

Con el backend arrancado y el emulador abierto:

```bash
npm run android          # compila, instala la app en el emulador y arranca Metro
npm run ios              # ídem en el simulador de iOS (macOS)
```

La app instalada es una *development build*: al abrirla sin Metro muestra el lanzador de `expo-dev-client` ("Development servers"). Con la app ya instalada no hace falta volver a compilar: basta con `npx expo start` y pulsar `a` (o tocar el servidor en el lanzador). Solo hay que recompilar si cambian las dependencias nativas o `app.json`.

Para probar la app **sin Metro** (el JavaScript va dentro de la app):

```bash
npm run android:release  # instala una release; el APK queda en android/app/build/outputs/apk/release/
npm run ios:release      # release en el simulador de iOS
```

Las carpetas `android/` e `ios/` se generan desde `app.json` y no se versionan. Si se cambia `app.json` (permisos, plugins…) o se mueve el proyecto de carpeta, hay que regenerarlas con `npm run prebuild` (o `npx expo prebuild --clean --platform android`).

#### Problemas en Windows

| Error | Causa | Solución |
|---|---|---|
| `SDK location not found` | La terminal no ve `ANDROID_HOME` | Crear la variable y abrir una terminal nueva (cerrar también VS Code). |
| `WARNING: A restricted method in java.lang.System has been called` en `configureCMake…` | Gradle usa un JDK 24 o superior (el que trae Android Studio, por ejemplo) | Instalar un JDK 17–23 y añadir a `%USERPROFILE%\.gradle\gradle.properties` la línea `org.gradle.java.home=C:/Program Files/Java/jdk-17` (con la ruta de ese JDK). Después, `cd android && gradlew --stop`. |
| `mkdir: command not found` / `rm: command not found` en `react-native-audio-api:downloadPrebuiltBinaries` | El script que descarga los binarios del afinador se ejecuta con el Bash de Git y necesita sus comandos | Compilar desde **Git Bash**, o añadir `C:\Program Files\Git\usr\bin` **al final** del `PATH` (en cmd, solo para esa ventana: `set "PATH=%PATH%;C:\Program Files\Git\usr\bin"`). |
| `ninja: error: mkdir(...): No such file or directory` en un `buildCMake…` (`react-native-audio-api`, `react-native-gesture-handler`…) | Las rutas de los objetos de CMake superan los 260 caracteres de Windows, incluso con el proyecto en una ruta corta (`E:\frontend`), y el Ninja 1.10.2 del SDK (`cmake\3.22.1`) no admite rutas largas | 1) Activar las rutas largas, en un PowerShell **como administrador**: `New-ItemProperty -Path HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem -Name LongPathsEnabled -Value 1 -PropertyType DWORD -Force`. 2) Sustituir `ninja.exe` de `%ANDROID_HOME%\cmake\3.22.1\bin` por el de [Ninja 1.12.1](https://github.com/ninja-build/ninja/releases/tag/v1.12.1) (`ninja-win.zip`), guardando el original. 3) Reiniciar el PC, `cd android && gradlew --stop && gradlew clean` y volver a compilar. Una ruta corta sigue ayudando; una unidad `subst` no sirve, porque Gradle resuelve la ruta real. |

---

## 🌐 Despliegue (Render + Neon + APK)

El backend se publica en **Render** (con Docker: Render no tiene runtime de Java) y la base de datos en **Neon** (PostgreSQL). La app se distribuye como **APK** generado con **EAS Build**, que compila en la nube y evita los problemas de compilar en Windows.

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
2. Rellenar las variables que pide (las de `sync: false`). `JWT_SECRET_KEY` la genera Render y `SHOW_SQL` va a `false`.
3. Al terminar, el backend queda en `https://<servicio>.onrender.com` (comprobarlo en `/actuator/health`).

| Variable | Valor |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | los de Neon |
| `SPOTIFY_CLIENT_ID`, `SPOTIFY_CLIENT_SECRET` | los de la app de Spotify |
| `SPOTIFY_REDIRECT_URI` | `https://<servicio>.onrender.com/api/spotify/callback` |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | el administrador inicial (lo usan los scripts de carga) |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | el SMTP del código de verificación |

- **Correo:** los servicios gratuitos de Render pueden bloquear los puertos SMTP habituales (25, 465 y 587). Si no llegan los códigos, usar un proveedor con otro puerto, por ejemplo Brevo (`smtp-relay.brevo.com`, puerto `2525`).
- **Arranque en frío:** el plan gratuito se duerme tras 15 minutos sin peticiones y la primera tarda en torno a un minuto. Antes de una demo, abrir `/actuator/health` para despertarlo.

### 3. Datos iniciales

Con el backend ya publicado, cargar el catálogo desde local apuntando los scripts a Render (en el `.env` de la raíz):

```bash
# .env: API_BASE_URL=https://<servicio>.onrender.com y ADMIN_EMAIL/ADMIN_PASSWORD los de Render
python scripts/load_all_data.py
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

Al terminar, EAS da un enlace (y un QR) para descargar el `.apk` e instalarlo en cualquier Android (hay que permitir instalar apps de origen desconocido). EAS crea y guarda la clave de firma la primera vez: todos los APK se firman con la misma, así que las versiones nuevas se instalan encima sin desinstalar. En cada versión nueva, subir `android.versionCode` (y `version`) en `app.json`.

Los perfiles de `eas.json`:

| Perfil | Qué genera |
|---|---|
| `preview` | APK para distribuir: el JavaScript va dentro, sin Metro |
| `development` | APK de la *development build* (con `expo-dev-client`), para desarrollar sin compilar en local |

---

## 📡 Endpoints de API

### 🔐 Autenticación (`/api/auth`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/auth/verification-code` | Enviar un código de verificación al email de un registro (`204`; `429` si se pide otro antes de 60 s) |
| `POST` | `/api/auth/register` | Registrar nuevo usuario con ese código (`verificationCode` en el body) y devolver el JWT |
| `POST` | `/api/auth/login` | Iniciar sesión con email o nombre de usuario (retorna JWT token) |

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
| `GET` | `/api/spotify/recommendations/items/{userId}` | Elementos recomendados a partir de su Spotify, DTO completo de cada subtipo (carrusel "Desde tu Spotify" del Inicio): 6 canciones como las anteriores, 3 lecciones de sus géneros, 2 instrument-genres de sus géneros e instrumentos más repetidos y el género más repetido. Barajados y sin nada de lo que ya está en 3 o más de sus listas; vacío si ninguna canción de Spotify está en la app |

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
2. Usuario hace login (el registro ya devuelve el token)
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
java -jar target/TOCO-0.0.1-SNAPSHOT.jar
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
- [Swagger/OpenAPI Docs](http://localhost:8080/swagger-ui.html) (si está configurado)
- [API Config Frontend](../frontend/src/config/apiConfig.ts)

---

**Última actualización**: Marzo 2026
