/**
 * Barrel export para servicios sin conflictos
 * Para servicios con métodos similares (getAll, getById, getAllById), use imports específicos:
 * 
 * Uso: import * as genreService from './genre.service';
 *      import { getAll, getById } from './genre.service';
 */

// Spotify
export { spotifyLogin, getSpotifyRecommendations } from './spotify.service';

// Media
export { uploadProfileImageToCloudinary } from './cloudinary.service';

