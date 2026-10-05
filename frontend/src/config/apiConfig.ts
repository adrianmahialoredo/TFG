/**
 * Configuración centralizada de URLs de API
 * Todos los endpoints del backend se definen aquí para mayor mantenibilidad
 *
 * Verificado con el backend SpringBoot en:
 * - src/main/java/com/demo/demo/controller/
 *
 * Convenciones (iguales en todos los recursos):
 * - Todas las rutas cuelgan de `/api`.
 * - BASE: GET lista todos / POST crea.
 * - BY_ID: GET obtiene / PUT actualiza / DELETE borra.
 * - BY_IDS: POST con el array de IDs en el body.
 * - Filtros por relación: `/by-<relación>/{id}`.
 */

const BASE_URL = 'http://192.168.0.22:8080';
const API_URL = `${BASE_URL}/api`;

export const API_ENDPOINTS = {
  // Authentication & Users
  AUTH: {
    LOGIN: `${API_URL}/auth/login`,
    REGISTER: `${API_URL}/auth/register`,
  },

  USERS: {
    BASE: `${API_URL}/users`,
    BY_ID: (id: string | number) => `${API_URL}/users/${id}`,
    BY_IDS: `${API_URL}/users/by-ids`,
    FAVORITE_GENRES: (id: string | number) => `${API_URL}/users/${id}/favorite-genres`,
  },

  // Genres
  GENRES: {
    BASE: `${API_URL}/genres`,
    BY_ID: (id: string) => `${API_URL}/genres/${id}`,
    BY_IDS: `${API_URL}/genres/by-ids`,
  },

  // Songs
  SONGS: {
    BASE: `${API_URL}/songs`,
    BY_ID: (id: string) => `${API_URL}/songs/${id}`,
    BY_IDS: `${API_URL}/songs/by-ids`,
    BY_DECADE: (decade: string) => `${API_URL}/songs/by-decade/${decade}`,
    // Carruseles con DTO completo (decisión de negocio: el usuario suele abrir lo que ve)
    POPULAR_BY_GENRE: (genreId: string) => `${API_URL}/songs/by-genre/${genreId}/popular`,
    CLASSIC_BY_GENRE: (genreId: string) => `${API_URL}/songs/by-genre/${genreId}/classic`,
    NEW_BY_GENRE: (genreId: string) => `${API_URL}/songs/by-genre/${genreId}/new`,
    POPULAR: `${API_URL}/songs/popular`,
    NEW: `${API_URL}/songs/new`,
    SPOTIFY: `${API_URL}/songs/spotify`,
    // Carruseles por convención: solo IDs (se resuelven contra el catálogo de items)
    RECOMMENDED_IDS: `${API_URL}/songs/recommended/ids`,
  },

  // Song Lessons
  SONG_LESSONS: {
    BASE: `${API_URL}/song-lessons`,
    BY_ID: (id: string) => `${API_URL}/song-lessons/${id}`,
    BY_IDS: `${API_URL}/song-lessons/by-ids`,
    BY_SONG: (songId: string) => `${API_URL}/song-lessons/by-song/${songId}`,
  },

  // Lessons
  LESSONS: {
    BASE: `${API_URL}/lessons`,
    BY_ID: (id: string) => `${API_URL}/lessons/${id}`,
    BY_IDS: `${API_URL}/lessons/by-ids`,
    BY_GENRE: (genreId: string) => `${API_URL}/lessons/by-genre/${genreId}`,
    // Feeds de carrusel: devuelven solo IDs (se resuelven contra el catálogo de items)
    POPULAR_IDS: `${API_URL}/lessons/popular/ids`,
    NEW_IDS: `${API_URL}/lessons/new/ids`,
    RECOMMENDED_IDS: `${API_URL}/lessons/recommended/ids`,
  },

  // Lesson Sections
  LESSON_SECTIONS: {
    BASE: `${API_URL}/lesson-sections`,
    BY_ID: (id: string) => `${API_URL}/lesson-sections/${id}`,
    BY_IDS: `${API_URL}/lesson-sections/by-ids`,
    BY_LESSON: (lessonId: string) => `${API_URL}/lesson-sections/by-lesson/${lessonId}`,
  },

  // Chords
  CHORDS: {
    BASE: `${API_URL}/chords`,
    BY_ID: (id: string) => `${API_URL}/chords/${id}`,
    BY_IDS: `${API_URL}/chords/by-ids`,
    IDS_BY_SONG_LESSON: (songLessonId: string) => `${API_URL}/chords/by-song-lesson/${songLessonId}/ids`,
  },

  // Instruments
  INSTRUMENTS: {
    BASE: `${API_URL}/instruments`,
    BY_ID: (id: string) => `${API_URL}/instruments/${id}`,
    BY_IDS: `${API_URL}/instruments/by-ids`,
    BY_GENRE: (genreId: string) => `${API_URL}/instruments/by-genre/${genreId}`, // @TODO: Requiere implementación en el backend
  },

  // Instrument Genres
  INSTRUMENT_GENRES: {
    BASE: `${API_URL}/instrument-genres`,
    BY_ID: (id: string) => `${API_URL}/instrument-genres/${id}`,
    BY_IDS: `${API_URL}/instrument-genres/by-ids`,
    BY_GENRE: (genreId: string) => `${API_URL}/instrument-genres/by-genre/${genreId}`,
    BY_INSTRUMENT: (instrumentId: string) => `${API_URL}/instrument-genres/by-instrument/${instrumentId}`,
  },

  // Item Lists
  ITEM_LISTS: {
    BASE: `${API_URL}/item-lists`,
    BY_ID: (id: string) => `${API_URL}/item-lists/${id}`,
    BY_IDS: `${API_URL}/item-lists/by-ids`,
    IDS_BY_USER: (userId: string) => `${API_URL}/item-lists/by-user/${userId}/ids`,
    // POST añade el item a la lista, DELETE lo quita
    ITEM: (listId: string, itemId: string) => `${API_URL}/item-lists/${listId}/items/${itemId}`,
  },

  // Tab Sections
  TAB_SECTIONS: {
    BASE: `${API_URL}/tab-sections`,
    BY_ID: (id: string) => `${API_URL}/tab-sections/${id}`,
    BY_IDS: `${API_URL}/tab-sections/by-ids`,
    BY_SONG_LESSON: (songLessonId: string) => `${API_URL}/tab-sections/by-song-lesson/${songLessonId}`,
  },

  // Spotify Integration
  SPOTIFY: {
    LOGIN: `${API_URL}/spotify/login`,
    CALLBACK: `${API_URL}/spotify/callback`,
    RECOMMENDATIONS: (spotifyUserId: string) => `${API_URL}/spotify/recommendations/${spotifyUserId}`,
  },

  // Items (vista común de canciones, lecciones, géneros e instrument-genres)
  ITEMS: {
    BASE: `${API_URL}/items`,
    BY_ID: (id: string) => `${API_URL}/items/${id}`,
    BY_IDS: `${API_URL}/items/by-ids`,
    // Feeds mixtos con DTO completo de cada subtipo (decisión de negocio)
    RECENT: `${API_URL}/items/recent`,
    RECOMMENDED_BY_ITEM_LIST: (id: string) => `${API_URL}/recommendations/items/by-item-list/${id}`,
    // Feeds por convención: solo IDs (se resuelven contra el catálogo de items)
    RECOMMENDED_IDS: `${API_URL}/items/recommended/ids`,
    ACCESS: (id: string) => `${API_URL}/items/${id}/access`,
  },
};

export default API_ENDPOINTS;
