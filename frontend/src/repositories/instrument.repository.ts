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

/**
 * Obtiene todos los instrumentos
 * Cache first, backend fallback
 * @returns Array de InstrumentDTO
 */
export const getAll = async (): Promise<InstrumentDTO[]> => {
    try {
        // 1. INTENTAR LOCAL
        const localInstruments: InstrumentDTO[] = (await instrumentLocal.getAll()) || [];

        // 2. SI HAY DATOS EN LOCAL, LOS POSICIONAMOS EN RAM Y RETORNAMOS
        if (localInstruments.length > 0) {
            useInstrumentEntitiesStore.getState().setInstruments(localInstruments);

            // OPCIONAL: Lanzar actualización en segundo plano (Background Sync)
            instrumentService.getAllInstruments().then(fresh => {
                if (fresh.length > 0) {
                    instrumentLocal.saveBatch(fresh);
                    useInstrumentEntitiesStore.getState().setInstruments(fresh);
                }
            }).catch(() => {});

            return localInstruments;
        }

        // 3. SI LOCAL ESTÁ VACÍO, IR A LA API
        const remoteInstruments = await instrumentService.getAllInstruments();

        if (remoteInstruments.length > 0) {
            // Guardar en RAM
            useInstrumentEntitiesStore.getState().setInstruments(remoteInstruments);
            // Guardar en Disco
            instrumentLocal.saveBatch(remoteInstruments).catch(console.error);
        }

        return remoteInstruments;

    } catch (error) {
        console.error("[InstrumentRepo] Error crítico en getAll:", error);
        // Fallback: devolver lo que haya en RAM
        return Object.values(useInstrumentEntitiesStore.getState().instruments) || [];
    }
}