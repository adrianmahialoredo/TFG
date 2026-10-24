import * as tuneService from '../services/tune.service';
import * as tuneLocal from '../local/tune.local';
import { useTuneEntitiesStore } from "../stores/entities";
import { TuneDTO } from '../types';
import { createEntityCache, OnFresh } from '../utils/entityCache.utils';

/** Núcleo de caché de la entidad: RAM -> Disco -> API + Stale-While-Revalidate. */
const cache = createEntityCache<TuneDTO>({
    name: 'TuneRepo',
    getRam: () => useTuneEntitiesStore.getState().tunes,
    setRam: (entities) => useTuneEntitiesStore.getState().setTunes(entities),
    local: tuneLocal,
    fetchByIds: tuneService.getAllById,
});

/**
 * Repositorio de Tune (afinaciones del afinador)
 * Las afinaciones son contenido público: se conservan como caché tras el logout.
 */

/**
 * Obtiene las afinaciones por sus IDs
 * Cache first, backend fallback
 * @returns Array de TuneDTO en el orden pedido
 */
export const getAllById = cache.getAllById;

/**
 * Obtiene una afinación por su ID
 * Cache first, backend fallback
 * @returns TuneDTO o null
 */
export const getById = async (id: string): Promise<TuneDTO | null> => {
    const [tune] = await cache.getAllById([id]);
    return tune ?? null;
};

/** Clave del índice con el catálogo completo de afinaciones. */
const ALL_KEY = 'all';

/**
 * Obtiene los IDs de todas las afinaciones.
 * Cache-First sobre el índice del catálogo completo: devuelve lo guardado al instante y lo
 * refresca en segundo plano, avisando con `onFresh`. Sin índice, espera a la API.
 */
export const getAll = (onFresh?: OnFresh): Promise<string[]> =>
    cache.list(ALL_KEY, tuneService.getAll, onFresh);

/**
 * Obtiene los IDs de las afinaciones de un instrumento (Cache-First, índice por instrumento).
 */
export const getByInstrumentId = (instrumentId: string, onFresh?: OnFresh): Promise<string[]> =>
    cache.list(`by_instrument:${instrumentId}`, () => tuneService.getByInstrumentId(instrumentId), onFresh);
