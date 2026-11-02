import * as instrumentGenreService from '../services/instrumentGenre.service';
import * as instrumentGenreLocal from '../local/instrumentGenre.local';
import * as lessonRepository from './lesson.repository';
import * as itemRepository from './item.repository';
import { useInstrumentGenreEntitiesStore, useItemEntitiesStore } from "../stores/entities";
import { InstrumentGenreDTO, ItemType } from '../types';
import { toItemSummary } from '../utils/mappers.utils';
import { createEntityCache, OnFresh } from '../utils/entityCache.utils';

/** Núcleo de caché de la entidad: RAM -> Disco -> API + Stale-While-Revalidate. */
const cache = createEntityCache<InstrumentGenreDTO>({
    name: 'InstrumentGenreRepo',
    getRam: () => useInstrumentGenreEntitiesStore.getState().instrumentGenres,
    // El resumen de la ItemEntitiesStore (tarjetas de carrusel) se mantiene al día con el DTO completo.
    setRam: (entities) => {
        useInstrumentGenreEntitiesStore.getState().setInstrumentGenres(entities);
        useItemEntitiesStore.getState().setItems(entities.map((e) => toItemSummary(e, ItemType.INSTRUMENT_GENRE)));
    },
    local: instrumentGenreLocal,
    fetchByIds: instrumentGenreService.getAllById,
});

/**
 * Repositorio de InstrumentGenreScreen
 * Maneja la lógica de cache + backend fallback para relaciones instrumento-género
 */

/**
 * Obtiene un InstrumentGenreScreen por su ID
 * Cache first, backend fallback
 * @param id El ID del InstrumentGenreScreen
 * @returns InstrumentGenreDTO o null si no se encuentra
 */
export const getById = async (id: string): Promise<InstrumentGenreDTO | null> => {
    let instrumentGenre: InstrumentGenreDTO | null = useInstrumentGenreEntitiesStore.getState().instrumentGenres[id] ?? await instrumentGenreLocal.getById(id);
    if (instrumentGenre) cache.revalidate([id]);

    if (!instrumentGenre) {
        try {
            instrumentGenre = await instrumentGenreService.getInstrumentGenreById(id);
            cache.markFresh([id]);
            if (instrumentGenre) {
                await instrumentGenreLocal.saveBatch([instrumentGenre]).catch(e => console.error("Local save failed", e));
            }
        } catch (error) {
            console.error(`Error fetching InstrumentGenre with ID ${id}:`, error);
            return null;
        }
    }

    if (instrumentGenre) {
        useInstrumentGenreEntitiesStore.getState().setInstrumentGenres([instrumentGenre]);
    }

    return instrumentGenre;
}

/**
 * Obtiene múltiples InstrumentGenres por sus IDs
 * @param ids Array de IDs
 * @returns Array de InstrumentGenreDTO
 */
export const getAllById = cache.getAllById;

/** Guarda en RAM + Disco DTOs completos recibidos por otra vía (p.ej. un feed mixto de items). */
export const hydrate = (entities: InstrumentGenreDTO[]): string[] => cache.hydrate(entities);

// --- LISTAS (Cache-First: índice guardado al instante + refresco de red vía `onFresh`) ---

/**
 * Obtiene los InstrumentGenres de un género específico.
 * Precarga en segundo plano las lecciones asociadas.
 */
export const getByGenreId = (genreId: string, onFresh?: OnFresh): Promise<string[]> =>
    cache.list(`genre_${genreId}`, async () => {
        const instrumentGenres = await instrumentGenreService.getByGenreId(genreId);
        const allLessonIds = instrumentGenres.flatMap(ig => ig.lessonIds || []);
        if (allLessonIds.length > 0) {
            lessonRepository.getAllById(allLessonIds).catch(console.error);
        }
        return instrumentGenres;
    }, onFresh);

/** Obtiene los InstrumentGenres de un instrumento específico. */
export const getByInstrumentId = (instrumentId: string, onFresh?: OnFresh): Promise<string[]> =>
    cache.list(`instrument_${instrumentId}`, () => instrumentGenreService.getByInstrumentId(instrumentId), onFresh);

// --- RECOMENDACIONES (el backend devuelve IDs; las tarjetas se pintan con el catálogo de items) ---

/**
 * Combinaciones de los géneros e instrumentos favoritos del usuario que no están en 3 o más de sus listas
 * (tarjeta "Prueba esta combinación" del Home). Personalizado: el índice se borra en el logout
 * con el resto de índices de items.
 */
export const getFavouriteCombinations = (userId: string, onFresh?: OnFresh): Promise<string[]> =>
    itemRepository.getFeed('instrument_genres_favourite_combinations', () => instrumentGenreService.getFavouriteCombinationIdsByUser(userId), onFresh);
