import * as genreService from '../services/genre.service';
import * as genreLocal from '../local/genre.local';
import { useGenreEntitiesStore, useItemEntitiesStore } from "../stores/entities";
import { GenreDTO, ItemType } from '../types';
import { toItemSummary } from '../utils/mappers.utils';
import { createEntityCache } from '../utils/entityCache.utils';

/** Núcleo de caché de la entidad: RAM -> Disco -> API + Stale-While-Revalidate. */
const cache = createEntityCache<GenreDTO>({
    name: 'GenreRepo',
    getRam: () => useGenreEntitiesStore.getState().genres,
    // El resumen de la ItemEntitiesStore (tarjetas de carrusel) se mantiene al día con el DTO completo.
    setRam: (entities) => {
        useGenreEntitiesStore.getState().setGenres(entities);
        useItemEntitiesStore.getState().setItems(entities.map((e) => toItemSummary(e, ItemType.GENRE)));
    },
    local: genreLocal,
    fetchByIds: genreService.getAllById,
});

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
    let genre: GenreDTO|null = useGenreEntitiesStore.getState().genres[id] ?? await genreLocal.getById(id);
    if (genre) cache.revalidate([id]);

    if (!genre) {
        try {
            genre = await genreService.getById(id);
            cache.markFresh([id]);
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
export const getAllById = cache.getAllById;

/** Guarda en RAM + Disco DTOs completos recibidos por otra vía (p.ej. un feed mixto de items). */
export const hydrate = (entities: GenreDTO[]): string[] => cache.hydrate(entities);

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
