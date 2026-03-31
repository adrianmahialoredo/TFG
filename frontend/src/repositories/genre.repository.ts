import * as genreService from '../services/genre.service';
import * as genreStorage from '../local/genre.local';
import { GenreDTO } from '../types';

/**
 * Repositorio de Genre
 * Maneja la lógica de cache + backend fallback para géneros
 */

/**
 * Obtiene un género por su ID
 * Cache first, backend fallback
 * @param genreId El ID del género
 * @returns GenreDTO o undefined si no se encuentra
 */
export const getGenreById = async (genreId: string): Promise<GenreDTO | undefined> => {
  try {
    // 1. Intentar obtener del storage
    let genre = await genreStorage.getStoredGenresById(genreId);

    if (genre) {
      console.log(`[Genre Cache] Obtenido género ${genreId} del storage`);
      return genre;
    }

    // 2. Si no está en storage, obtener del backend
    genre = await genreService.getGenreById(genreId);

    if (genre) {
      // 3. Guardar en storage para futuras consultas
      await genreStorage.storeGenres([genre]);
      console.log(`[Genre Backend] Obtenido género ${genreId} del backend`);
    }

    return genre;
  } catch (error) {
    console.error('Error obteniendo género:', error);
    return undefined;
  }
};

/**
 * Obtiene todos los géneros
 * Cache first, backend fallback
 * @returns Array de GenreDTO
 */
export const getAllGenres = async (): Promise<GenreDTO[]> => {
  try {
    // 1. Intentar obtener del storage
    let genres = await genreStorage.getStoredGenres();

    if (genres && genres.length > 0) {
      console.log(`[Genre Cache] Obtenidos ${genres.length} géneros del storage`);
      return genres;
    }

    // 2. Si no están en storage, obtener del backend
    genres = await genreService.getAllGenres();

    if (genres && genres.length > 0) {
      // 3. Guardar en storage para futuras consultas
      await genreStorage.storeGenres(genres);
      console.log(`[Genre Backend] Obtenidos ${genres.length} géneros del backend`);
    }

    return genres || [];
  } catch (error) {
    console.error('Error obteniendo géneros:', error);
    return [];
  }
};


