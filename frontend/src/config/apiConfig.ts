// src/config/apiConfig.ts
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
    BY_ID: (id: string) => `${BASE_URL}/api/users/${id}`,
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
    NEW_BY_GENRE: (genreId: string) => `${BASE_URL}/songs/new/${genreId}`,
    CLASSIC_BY_GENRE: (genreId: string) => `${BASE_URL}/songs/classic/${genreId}`,
  },

  // Song Lessons
  SONG_LESSONS: {
    BASE: `${BASE_URL}/song-lessons`,
    BY_ID: (id: string) => `${BASE_URL}/song-lessons/${id}`,
    BY_SONG: (songId: string) => `${BASE_URL}/song-lessons/by-song/${songId}`,
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
  },

  // Instrument Genres
  INSTRUMENT_GENRES: {
    BASE: `${BASE_URL}/instrument-genres`,
    BY_ID: (id: string) => `${BASE_URL}/instrument-genres/${id}`,
  },

  // Item Lists
  ITEM_LISTS: {
    BASE: `${BASE_URL}/item-lists`,
    BY_ID: (id: string) => `${BASE_URL}/item-lists/${id}`,
    BY_USER: (userId: string) => `${BASE_URL}/item-lists/by-user/${userId}`,
    BY_IDS: `${BASE_URL}/item-lists/by-ids`,
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
};

export default API_ENDPOINTS;

