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
    VERIFICATION_CODE: `${API_URL}/auth/verification-code`,
  },

  USERS: {
    BASE: `${API_URL}/users`,
    BY_ID: (id: string | number) => `${API_URL}/users/${id}`,
    BY_IDS: `${API_URL}/users/by-ids`,
    FAVORITE_GENRES: (id: string | number) => `${API_URL}/users/${id}/favorite-genres`,
    FAVORITE_GENRE: (id: string | number, genreId: string | number) => `${API_URL}/users/${id}/favorite-genres/${genreId}`,
    FAVORITE_INSTRUMENTS: (id: string | number) => `${API_URL}/users/${id}/favorite-instruments`,
    FAVORITE_INSTRUMENT: (id: string | number, instrumentId: string | number) => `${API_URL}/users/${id}/favorite-instruments/${instrumentId}`,
    // Artistas más repetidos en las canciones de las listas del usuario (?total_artists=n, de 1 a 10)
    TOP_ARTISTS: (id: string | number) => `${API_URL}/users/${id}/top-artists`,
  },

  // Genres
  GENRES: {
    BASE: `${API_URL}/genres`,
    BY_ID: (id: string) => `${API_URL}/genres/${id}`,
    BY_IDS: `${API_URL}/genres/by-ids`,
    // Géneros más usados en las listas del usuario (que no estén ya en ellas): devuelve solo IDs
    MOST_USED_IN_ITEM_LISTS_IDS_BY_USER: (userId: string) =>
      `${API_URL}/recommendations/genres/most-used-in-item-lists/by-user/${userId}/ids`,
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
    SPOTIFY: (userId: string) => `${API_URL}/spotify/recommendations/songs/${userId}`,
    RECOMMENDED_BY_USER: (userId: string) => `${API_URL}/recommendations/songs/by-user/${userId}`,
    // Canciones de varios artistas (?limit=n&artist1=a&artist2=b..., hasta 10 artistas y 50 canciones)
    NEW_BY_ARTISTS: `${API_URL}/songs/by-artists/new`,
    POPULAR_BY_ARTISTS: `${API_URL}/songs/by-artists/popular`,
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
    RECOMMENDED_BY_GENRE: (genreId: string) => `${API_URL}/recommendations/lessons/by-genre/${genreId}`,
    RECOMMENDED_BY_USER: (userId: string) => `${API_URL}/recommendations/lessons/by-user/${userId}`,
    // Feeds de carrusel: devuelven solo IDs (se resuelven contra el catálogo de items)
    POPULAR_IDS: `${API_URL}/lessons/popular/ids`,
    NEW_IDS: `${API_URL}/lessons/new/ids`,
    POPULAR_BY_INSTRUMENT_IDS: (instrumentId: string) => `${API_URL}/lessons/by-instrument/${instrumentId}/popular/ids`,
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
    // Combinaciones de los géneros e instrumentos favoritos del usuario: devuelve solo IDs
    FAVOURITE_COMBINATION_IDS_BY_USER: (userId: string) =>
      `${API_URL}/recommendations/instrument-genres/favourite-combinations/by-user/${userId}/ids`,
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

  // Tunes (afinaciones del afinador)
  TUNES: {
    BASE: `${API_URL}/tunes`,
    BY_ID: (id: string) => `${API_URL}/tunes/${id}`,
    BY_IDS: `${API_URL}/tunes/by-ids`,
    BY_INSTRUMENT: (instrumentId: string) => `${API_URL}/tunes/by-instrument/${instrumentId}`,
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
    RECOMMENDED_BY_ITEM_LIST: (id: string) => `${API_URL}/recommendations/items/by-item-list/${id}`,
    RECOMMENDED_BY_USER: (userId: string) => `${API_URL}/recommendations/items/by-user/${userId}`,
  },
};

export default API_ENDPOINTS;
