import * as genreService from '../services/genre.service';
import * as genreLocal from '../local/genre.local';
import { useGenreEntitiesStore } from "../stores/entities";
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
export const getById = async (id: string): Promise<GenreDTO | null> => {
    let genre: GenreDTO|null = await genreLocal.getById(id);

    if (!genre) {
        try {
            genre = await genreService.getById(id);
            if (genre) {
                await genreLocal.saveBatch([genre]).catch(e => console.error("Local save failed", e));
            }
        } catch (error) {
            console.error(`Error fetching genre with ID ${id}:`, error);
            return null;
        }
    }

    if (genre) {
        useGenreEntitiesStore.getState().setGenres([genre]);
    }

    return genre;
}

/**
 * Obtiene todos los géneros
 * Cache first, backend fallback
 * @returns Array de GenreDTO
 */
export const getAllById = async (ids: string[]): Promise<GenreDTO[]> => {
    const { cached, missing } = await genreLocal.getAllById(ids);

    let remoteGenres: GenreDTO[] = [];

    if (missing.length > 0) {
        try {
            remoteGenres = await genreService.getAllById(missing);
            if (remoteGenres.length > 0) {
                genreLocal.saveBatch(remoteGenres).catch(console.error);
            }
        } catch (error) {
            console.error("[GenreRepo] API Error", error);
        }
    }

    const all = [...cached, ...remoteGenres];
    useGenreEntitiesStore.getState().setGenres(all);

    const map = new Map(all.map(g => [g.id.toString(), g]));
    return ids.map(id => map.get(id)).filter((g): g is GenreDTO => !!g);
}

export const getAll = async (): Promise<GenreDTO[]> => {
    try {
        // 1. INTENTAR LOCAL
        // Usamos || [] para convertir el null en un array vacío y poder preguntar .length
        const localGenres: GenreDTO[] = (await genreLocal.getAll()) || [];

        // 2. SI HAY DATOS EN LOCAL, LOS POSICIONAMOS EN RAM Y RETORNAMOS
        if (localGenres.length > 0) {
            useGenreEntitiesStore.getState().setGenres(localGenres);

            // OPCIONAL: Lanzar actualización en segundo plano (Background Sync)
            // Esto actualiza la RAM silenciosamente si hay cambios en el servidor
            genreService.getAll().then(fresh => {
                if (fresh.length > 0) {
                    genreLocal.saveBatch(fresh);
                    useGenreEntitiesStore.getState().setGenres(fresh);
                }
            }).catch(() => {});

            return localGenres;
        }

        // 3. SI LOCAL ESTÁ VACÍO (NULL O []), IR A LA API
        const remoteGenres = await genreService.getAll();

        if (remoteGenres.length > 0) {
            // Guardar en RAM
            useGenreEntitiesStore.getState().setGenres(remoteGenres);
            // Guardar en Disco (sin await para no bloquear)
            genreLocal.saveBatch(remoteGenres).catch(console.error);
        }

        return remoteGenres;

    } catch (error) {
        console.error("[GenreRepo] Error crítico en getAll:", error);
        // Fallback: devolver lo que haya en RAM si existe, o array vacío
        return Object.values(useGenreEntitiesStore.getState().genres) || [];
    }
}
