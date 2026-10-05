import * as chordService from '../services/chord.service';
import * as chordLocal from '../local/chord.local';
import { useChordEntitiesStore } from "../stores/entities";
import { ChordDTO } from '../types';
import { createEntityCache } from '../utils/entityCache.utils';

/** Núcleo de caché de la entidad: RAM -> Disco -> API + Stale-While-Revalidate. */
const cache = createEntityCache<ChordDTO>({
    name: 'ChordRepo',
    getRam: () => useChordEntitiesStore.getState().chords,
    setRam: (entities) => useChordEntitiesStore.getState().setChords(entities),
    local: chordLocal,
    fetchByIds: chordService.getAllById,
});

/**
 * Repositorio de Chord
 * Maneja la lógica de cache + backend fallback para acordes
 */

/**
 * Obtiene los acordes por sus IDs
 * Cache first, backend fallback
 * @param chordIds Array de IDs de acordes (pueden ser strings o números)
 * @returns Array de ChordDTO
 */
export const getAllById = cache.getAllById;

/**
 * Obtiene un acorde por su ID
 * Cache first, backend fallback
 * @param chordId El ID del acorde
 * @returns ChordDTO o null
 */
export const getById = async (id: string): Promise<ChordDTO | null> => {
    let chord: ChordDTO|null = useChordEntitiesStore.getState().chords[id] ?? await chordLocal.getById(id);
    if (chord) cache.revalidate([id]);

    if (!chord) {
        try {
            chord = await chordService.getById(id);
            cache.markFresh([id]);
            if (chord) {
                await chordLocal.saveBatch([chord]).catch(e => console.error("Local save failed", e));
            }
        } catch (error) {
            console.error(`Error fetching chord with ID ${id}:`, error);
            return null;
        }
    }

    if (chord) {
        useChordEntitiesStore.getState().setChords([chord]);
    }

    return chord;
}
