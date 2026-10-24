import * as genreService from '../services/genre.service';
import * as genreLocal from '../local/genre.local';
import * as itemRepository from './item.repository';
import { useGenreEntitiesStore, useItemEntitiesStore } from "../stores/entities";
import { GenreDTO, ItemType } from '../types';
import { toItemSummary } from '../utils/mappers.utils';
import { createEntityCache, OnFresh } from '../utils/entityCache.utils';

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

/** Clave del índice con el catálogo completo (distinto de los géneros sueltos ya cacheados). */
const ALL_KEY = 'all';

/**
 * Obtiene todos los géneros
 * Cache-First sobre el índice del catálogo completo: devuelve lo guardado al instante y lo
 * refresca en segundo plano (la Store se actualiza sola). Sin índice, espera a la API.
 * @returns Array de GenreDTO
 */
export const getAll = async (): Promise<GenreDTO[]> => {
    const ids = await cache.list(ALL_KEY, genreService.getAll);
    const ram = useGenreEntitiesStore.getState().genres;
    return ids.map((id) => ram[id]).filter((e): e is GenreDTO => !!e);
};

// --- RECOMENDACIONES (el backend devuelve IDs; las tarjetas se pintan con el catálogo de items) ---

/**
 * Géneros más usados en las listas del usuario, completados con sus favoritos, que no están ya en
 * ellas (tarjeta "Explora este género" del Home). Personalizado: el índice se borra en el logout con el resto de índices de items.
 */
export const getMostUsedInItemLists = (userId: string, onFresh?: OnFresh): Promise<string[]> =>
    itemRepository.getFeed('genres_most_used_in_item_lists', () => genreService.getMostUsedInItemListsIdsByUser(userId), onFresh);
