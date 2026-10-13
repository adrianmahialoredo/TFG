import * as instrumentService from '../services/instrument.service';
import * as instrumentLocal from '../local/instrument.local';
import { useInstrumentEntitiesStore } from "../stores/entities";
import { InstrumentDTO } from '../types';
import { createEntityCache } from '../utils/entityCache.utils';

/** Núcleo de caché de la entidad: RAM -> Disco -> API + Stale-While-Revalidate. */
const cache = createEntityCache<InstrumentDTO>({
    name: 'InstrumentRepo',
    getRam: () => useInstrumentEntitiesStore.getState().instruments,
    setRam: (entities) => useInstrumentEntitiesStore.getState().setInstruments(entities),
    local: instrumentLocal,
    fetchByIds: instrumentService.getAllById,
});

/**
 * Repositorio de Instruments
 * Maneja la lógica de cache + backend fallback para instrumentos
 */

/**
 * Obtiene un instrumento por su ID
 * Cache first, backend fallback
 * @returns InstrumentDTO o null si no se encuentra
 * @param id
 */
export const getById = async (id: string): Promise<InstrumentDTO | null> => {
    let instrument: InstrumentDTO | null = useInstrumentEntitiesStore.getState().instruments[id] ?? await instrumentLocal.getById(id);
    if (instrument) cache.revalidate([id]);

    if (!instrument) {
        try {
            instrument = await instrumentService.getInstrumentById(id);
            cache.markFresh([id]);
            if (instrument) {
                await instrumentLocal.saveBatch([instrument]).catch(e => console.error("Local save failed", e));
            }
        } catch (error) {
            console.error(`Error fetching instrument with ID ${id}:`, error);
            return null;
        }
    }

    if (instrument) {
        useInstrumentEntitiesStore.getState().setInstruments([instrument]);
    }

    return instrument;
}

/**
 * Obtiene múltiples instrumentos por sus IDs
 * Cache first, backend fallback
 * @param ids Array de IDs de instrumentos
 * @returns Array de InstrumentDTO
 */
export const getAllById = cache.getAllById;

/** Clave del índice con el catálogo completo (distinto de los instrumentos sueltos ya cacheados). */
const ALL_KEY = 'all';

/**
 * Obtiene todos los instrumentos
 * Cache-First sobre el índice del catálogo completo: devuelve lo guardado al instante y lo
 * refresca en segundo plano (la Store se actualiza sola). Sin índice, espera a la API.
 * @returns Array de InstrumentDTO
 */
export const getAll = async (): Promise<InstrumentDTO[]> => {
    const ids = await cache.list(ALL_KEY, instrumentService.getAllInstruments);
    const ram = useInstrumentEntitiesStore.getState().instruments;
    return ids.map((id) => ram[id]).filter((e): e is InstrumentDTO => !!e);
};