import { ItemSummaryDTO } from "../types";
import * as itemLocal from "../local/item.local";
import * as itemService from "../services/item.service";
import { useItemEntitiesStore } from "../stores/entities/item.entities.store";
import { createEntityCache, OnFresh } from '../utils/entityCache.utils';

/**
 * Repositorio de los resúmenes de items (`ItemSummaryDTO`).
 * Los DTOs completos de cada subtipo se piden a su propio repositorio.
 */

/** Núcleo de caché de la entidad: RAM -> Disco -> API + Stale-While-Revalidate. */
const cache = createEntityCache<ItemSummaryDTO>({
    name: 'ItemRepo',
    getRam: () => useItemEntitiesStore.getState().items,
    setRam: (entities) => useItemEntitiesStore.getState().setItems(entities),
    local: itemLocal,
    fetchByIds: itemService.getAllById,
});

/**
 * Trae una lista de items mezclados por sus IDs.
 * Sigue la regla: RAM -> DISCO -> API
 */
export const getAllById = cache.getAllById;

/** Guarda en RAM + Disco resúmenes recibidos por otra vía (p.ej. derivados de DTOs completos). */
export const hydrateSummaries = (summaries: ItemSummaryDTO[]): string[] => cache.hydrate(summaries);

/**
 * Feed a medida sobre el índice de items: `load` pide e hidrata lo que haga falta y devuelve
 * los IDs. Cache-First con refresco vía `onFresh`. Lo usa `itemFeed.repository` para los
 * feeds mixtos que traen DTOs completos.
 */
export const getCustomFeed = (cacheKey: string, load: () => Promise<string[]>, onFresh?: OnFresh): Promise<string[]> =>
    cache.feed(cacheKey, load, onFresh);

// --- CATÁLOGO ---

const CATALOG_KEY = 'items_catalog';

// El catálogo se refresca desde la red como mucho una vez por sesión.
let catalogSynced = false;

/**
 * Catálogo completo de items (resúmenes), para buscar sin llamar al backend.
 * Se carga al arrancar la app. Cache-First: si ya hay catálogo guardado se
 * devuelve al instante; la red solo se consulta una vez por sesión.
 */
export const getCatalog = async (onFresh?: OnFresh): Promise<string[]> => {
    if (catalogSynced) {
        const ids = await cache.readIndex(CATALOG_KEY);
        if (ids.length > 0) return ids;
    }
    return cache.list(CATALOG_KEY, async () => {
        const items = await itemService.getAll();
        catalogSynced = true;
        return items;
    }, onFresh);
};

// --- FEEDS POR CONVENCIÓN (el backend devuelve IDs; se resuelven contra el catálogo) ---

/**
 * Feed genérico de items: el endpoint devuelve solo IDs y aquí se resuelven contra
 * el catálogo (RAM -> Disco), pidiendo por `by-ids` solo los que falten.
 * Cache-First: índice guardado al instante + refresco de red vía `onFresh`.
 * Lo usan también los repositorios de canciones y lecciones para sus carruseles.
 */
export const getFeed = (cacheKey: string, fetchIds: () => Promise<(string | number)[]>, onFresh?: OnFresh): Promise<string[]> =>
    cache.idList(cacheKey, fetchIds, onFresh);

/** Items recomendados para el Home (Home "Recomendaciones"). */
export const getRecommendations = (onFresh?: OnFresh): Promise<string[]> =>
    getFeed('items_home_recommendations', () => itemService.getRecommendedIds(), onFresh);

/**
 * Marca un item como accedido (actualiza `lastTimeAccessed` en el backend).
 * Fire-and-forget: no propaga errores para no interrumpir la navegación del usuario.
 */
export const markAsAccessed = async (id: string): Promise<void> => {
    if (!id) return;
    try {
        await itemService.markAsAccessed(id);
    } catch (error) {
        console.error(`[ItemRepo] Error en markAsAccessed(${id}):`, error);
    }
};
