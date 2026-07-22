import * as chordService from '../services/chord.service';
import * as chordLocal from '../local/chord.local';
import { useChordEntitiesStore } from "../stores/entities";
import { ChordDTO } from '../types';

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
export const getAllById = async (ids: string[]): Promise<ChordDTO[]> => {
    const { cached, missing } = await chordLocal.getAllById(ids);

    let remoteChords: ChordDTO[] = [];

    if (missing.length > 0) {
        try {
            remoteChords = await chordService.getAllById(missing);
            if (remoteChords.length > 0) {
                chordLocal.saveBatch(remoteChords).catch(console.error);
            }
        } catch (error) {
            console.error("[ChordRepo] API Error", error);
        }
    }

    const all = [...cached, ...remoteChords];
    useChordEntitiesStore.getState().setChords(all);

    const map = new Map(all.map(c => [c.id.toString(), c]));
    return ids.map(id => map.get(id)).filter((c): c is ChordDTO => !!c);
}

/**
 * Obtiene un acorde por su ID
 * Cache first, backend fallback
 * @param chordId El ID del acorde
 * @returns ChordDTO o null
 */
export const getById = async (id: string): Promise<ChordDTO | null> => {
    let chord: ChordDTO|null = await chordLocal.getById(id);

    if (!chord) {
        try {
            chord = await chordService.getById(id);
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
