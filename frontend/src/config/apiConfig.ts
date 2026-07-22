/**
 * Configuración centralizada de URLs de API
 * Todos los endpoints del backend se definen aquí para mayor mantenibilidad
 * 
 * Verificado con el backend SpringBoot en:
 * - src/main/java/com/demo/demo/controller/
 */

const BASE_URL = 'http://192.168.0.22:8080';

export const API_ENDPOINTS = {
  // Authentication & Users
  AUTH: {
    LOGIN: `${BASE_URL}/api/auth/login`,
    REGISTER: `${BASE_URL}/api/auth/register`,
  },

  USERS: {
    BASE: `${BASE_URL}/api/users`,
    BY_ID: (id: string | number) => `${BASE_URL}/api/users/${id}`,
    BY_IDS: `${BASE_URL}/api/users/by-ids`,
    DELETE_ALL: `${BASE_URL}/api/users/all`,
  },

  // Genres
  GENRES: {
    BASE: `${BASE_URL}/genres`,
    BY_ID: (id: string) => `${BASE_URL}/genres/${id}`,
  },

  // Songs
  SONGS: {
    BASE: `${BASE_URL}/songs`,
    BY_ID: (id: string) => `${BASE_URL}/songs/${id}`,
    BY_DECADE: (decade: string) => `${BASE_URL}/songs/decades/${decade}`,
    POPULAR: `${BASE_URL}/songs/popular`,
    POPULAR_IDS: `${BASE_URL}/songs/popular`,
    BY_IDS: `${BASE_URL}/songs/ids`,
    POPULAR_BY_GENRE: (genreId: string) => `${BASE_URL}/songs/popular/${genreId}`,
    POPULAR_BY_GENRE_ID: (genreId: string) => `${BASE_URL}/songs/popular-by-genre/${genreId}`,
    CLASSIC_BY_GENRE: (genreId: string) => `${BASE_URL}/songs/classic/${genreId}`,
    CLASSIC_BY_GENRE_ID: (genreId: string) => `${BASE_URL}/songs/classic-by-genre/${genreId}`,
    NEW_BY_GENRE: (genreId: string) => `${BASE_URL}/songs/new/${genreId}`,
    NEW_BY_GENRE_ID: (genreId: string) => `${BASE_URL}/songs/new-by-genre/${genreId}`,
    HOME_POPULAR: `${BASE_URL}/songs/home/popular`,
    HOME_NEW: `${BASE_URL}/songs/home/new`,
    HOME_RECOMMENDED: `${BASE_URL}/songs/home/recommended`,
    HOME_SPOTIFY: `${BASE_URL}/songs/home/spotify`,
  },

  // Song Lessons
  SONG_LESSONS: {
    BASE: `${BASE_URL}/song-lessons`,
    BY_ID: (id: string) => `${BASE_URL}/song-lessons/${id}`,
    BY_SONG: (songId: string) => `${BASE_URL}/song-lessons/by-song/${songId}`,
  },

  // Lessons
  LESSONS: {
    BASE: `${BASE_URL}/lessons`,
    BY_ID: (id: string) => `${BASE_URL}/lessons/${id}`,
    BY_GENRE: (genreId: string) => `${BASE_URL}/lessons/by-genre/${genreId}`,
    BY_IDS: `${BASE_URL}/lessons/ids`,
    HOME_POPULAR: `${BASE_URL}/lessons/home/popular`,
    HOME_NEW: `${BASE_URL}/lessons/home/new`,
    HOME_RECOMMENDED: `${BASE_URL}/lessons/home/recommended`,
  },

  // Chords
  CHORDS: {
    BASE: `${BASE_URL}/api/chords`,
    BY_ID: (id: string) => `${BASE_URL}/api/chords/${id}`,
    BY_IDS: `${BASE_URL}/api/chords/by-ids`,
    BY_SONG_LESSON: (songLessonId: string) => `${BASE_URL}/api/chords/by-song-lesson/${songLessonId}`,
  },

  // Instruments
  INSTRUMENTS: {
    BASE: `${BASE_URL}/instruments`,
    BY_ID: (id: string) => `${BASE_URL}/instruments/${id}`,
    BY_GENRE: (genreId: string) => `${BASE_URL}/instruments/by-genre/${genreId}`, // @TODO: Requiere implementación
    BY_IDS: `${BASE_URL}/instruments/by-ids`, // @TODO: Requiere implementación
  },

  // Instrument Genres
  INSTRUMENT_GENRES: {
    BASE: `${BASE_URL}/instrument-genres`,
    BY_ID: (id: string) => `${BASE_URL}/instrument-genres/${id}`,
    BY_GENRE: (genreId: string) => `${BASE_URL}/instrument-genres/by-genre/${genreId}`, // @TODO: Requiere implementación
    BY_INSTRUMENT: (instrumentId: string) => `${BASE_URL}/instrument-genres/by-instrument/${instrumentId}`, // @TODO: Requiere implementación
    BY_IDS: `${BASE_URL}/instrument-genres/by-ids`, // @TODO: Requiere implementación
  },

  // Item Lists
  ITEM_LISTS: {
    BASE: `${BASE_URL}/item-lists`,
    BY_ID: (id: string) => `${BASE_URL}/item-lists/${id}`,
    BY_USER: (userId: string) => `${BASE_URL}/item-lists/by-user/${userId}`,
    BY_IDS: `${BASE_URL}/item-lists/by-ids`,
    ADD_ITEM: (listId: string, itemId: string) => `${BASE_URL}/item-lists/${listId}/add-item/${itemId}`,
    REMOVE_ITEM: (listId: string, itemId: string) => `${BASE_URL}/item-lists/${listId}/remove-item/${itemId}`,
  },

  // Tab Sections
  TAB_SECTIONS: {
    BASE: `${BASE_URL}/tab-sections`,
    BY_ID: (id: string) => `${BASE_URL}/tab-sections/${id}`,
    BY_LESSON: (lessonId: string) => `${BASE_URL}/tab-sections/by-lesson/${lessonId}`,
  },

  // Spotify Integration
  SPOTIFY: {
    LOGIN: `${BASE_URL}/api/spotify/login`,
    CALLBACK: `${BASE_URL}/api/spotify/callback`,
    RECOMMENDATIONS: (spotifyUserId: string) => `${BASE_URL}/api/spotify/recommendations/${spotifyUserId}`,
  },

  ITEMS: {
    BASE: `${BASE_URL}/items`,
    BY_ID: (id: string) => `${BASE_URL}/items/${id}`,
    BY_IDS: `${BASE_URL}/items/batch`, // O como se llame en tu backend
    SEARCH: (q: string) => `${BASE_URL}/items/search?q=${q}`,
    RECOMMENDED: `${BASE_URL}/items/recommended`,
    RECENT: `${BASE_URL}/items/recent`,
    RECOMMENDATIONS: `${BASE_URL}/items/recommendations`,
    ACCESS: (id: string) => `${BASE_URL}/items/${id}/access`,
  },
};

export default API_ENDPOINTS;

