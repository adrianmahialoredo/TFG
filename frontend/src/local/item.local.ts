import AsyncStorage from "@react-native-async-storage/async-storage";
import { ItemSummaryDTO } from "../types";

const ITEM_CACHE_PREFIX = '@item:';
const INDEX_CACHE_PREFIX = '@item_index:';

export const saveBatch = async (items: ItemSummaryDTO[]): Promise<void> => {
    const pairs: [string, string][] = items.map(item => [
        `${ITEM_CACHE_PREFIX}${item.id}`,
        JSON.stringify(item)
    ]);
    await AsyncStorage.multiSet(pairs);
};

export const getAllById = async (ids: string[]): Promise<{ cached: ItemSummaryDTO[]; missing: string[] }> => {
    const keys = ids.map(id => `${ITEM_CACHE_PREFIX}${id}`);
    const pairs = await AsyncStorage.multiGet(keys);

    const cached: ItemSummaryDTO[] = [];
    const missing: string[] = [];

    pairs.forEach(([key, value], index) => {
        if (value) {
            try {
                cached.push(JSON.parse(value));
            } catch {
                missing.push(ids[index]);
            }
        } else {
            missing.push(ids[index]);
        }
    });

    return { cached, missing };
};

export const saveQueryIndex = async (key: string, ids: string[]): Promise<void> => {
    try {
        await AsyncStorage.setItem(`${INDEX_CACHE_PREFIX}${key}`, JSON.stringify(ids));
    } catch (e) {
        console.error("[ItemLocal] Error guardando índice:", e);
    }
};

export const getQueryIndex = async (key: string): Promise<string[] | null> => {
    try {
        const data = await AsyncStorage.getItem(`${INDEX_CACHE_PREFIX}${key}`);
        return data ? JSON.parse(data) : null;
    } catch (e) {
        console.error("[ItemLocal] Error leyendo índice:", e);
        return null;
    }
};

/**
 * Borra los índices de items (recientes, recomendaciones…) del Disco, salvo los de `keep`.
 * Las entidades se conservan: son contenido público, solo los índices dependen del usuario.
 */
export const clearQueryIndexes = async (keep: string[] = []): Promise<void> => {
    try {
        const kept = new Set(keep.map(key => `${INDEX_CACHE_PREFIX}${key}`));
        const allKeys = await AsyncStorage.getAllKeys();
        const keys = allKeys.filter(key => key.startsWith(INDEX_CACHE_PREFIX) && !kept.has(key));
        if (keys.length > 0) await AsyncStorage.multiRemove(keys);
    } catch (e) {
        console.error("[ItemLocal] Error en clearQueryIndexes:", e);
    }
};
