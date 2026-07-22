/**
 * Barrel export para servicios sin conflictos
 * Para servicios con métodos similares (getAll, getById, getAllById), use imports específicos:
 * 
 * Uso: import * as genreService from './genre.service';
 *      import { getAll, getById } from './genre.service';
 */

// Auth
export * from './auth.service';

// Users
export * from './user.service';

// Instruments
export * from './instrument.service';

// Instrument Genres
export * from './instrumentGenre.service';


// Spotify
export * from './spotify.service';

