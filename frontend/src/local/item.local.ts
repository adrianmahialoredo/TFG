import AsyncStorage from "@react-native-async-storage/async-storage";
import { AnyItemDTO } from "../types";

const ITEM_CACHE_PREFIX = '@item:';
const INDEX_CACHE_PREFIX = '@item_index:';

export const saveBatch = async (items: AnyItemDTO[]): Promise<void> => {
    const pairs: [string, string][] = items.map(item => [
        `${ITEM_CACHE_PREFIX}${item.id}`,
        JSON.stringify(item)
    ]);
    await AsyncStorage.multiSet(pairs);
};

export const getAllById = async (ids: string[]): Promise<{ cached: AnyItemDTO[]; missing: string[] }> => {
    const keys = ids.map(id => `${ITEM_CACHE_PREFIX}${id}`);
    const pairs = await AsyncStorage.multiGet(keys);

    const cached: AnyItemDTO[] = [];
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
