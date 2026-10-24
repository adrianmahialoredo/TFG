import { TuneDTO } from "../types";
import { storeData, getData } from "./storage.local";
import AsyncStorage from "@react-native-async-storage/async-storage";

const TUNE_CACHE_PREFIX = '@tune:';
const INDEX_CACHE_PREFIX = '@tune_index:';

export const saveSingle = async (tune: TuneDTO): Promise<void> => {
  const key = `${TUNE_CACHE_PREFIX}${tune.id}`;
  return storeData(key, tune);
};

export const getById = async (id: string): Promise<TuneDTO | null> => {
  const key = `${TUNE_CACHE_PREFIX}${id}`;
  return getData<TuneDTO>(key);
};

export const saveBatch = async (tunes: TuneDTO[]): Promise<void> => {
  if (tunes.length === 0) return;

  const pairs: [string, string][] = tunes.map(tune => [
    `${TUNE_CACHE_PREFIX}${tune.id}`,
    JSON.stringify(tune)
  ]);

  try {
    await AsyncStorage.multiSet(pairs);
  } catch (e) {
    console.error("[TuneLocal] Error en saveBatch:", e);
  }
};

export const getAllById = async (ids: string[]): Promise<{ cached: TuneDTO[]; missing: string[] }> => {
  if (ids.length === 0) return { cached: [], missing: [] };

  const keys = ids.map(id => `${TUNE_CACHE_PREFIX}${id}`);
  const pairs = await AsyncStorage.multiGet(keys);

  const cached: TuneDTO[] = [];
  const missing: string[] = [];

  pairs.forEach(([key, value], index) => {
    if (value) {
      try {
        cached.push(JSON.parse(value));
      } catch (e) {
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
    console.error("[TuneLocal] Error guardando índice:", e);
  }
};

export const getQueryIndex = async (key: string): Promise<string[] | null> => {
  try {
    const data = await AsyncStorage.getItem(`${INDEX_CACHE_PREFIX}${key}`);
    return data ? JSON.parse(data) : null;
  } catch (e) {
    console.error("[TuneLocal] Error leyendo índice:", e);
    return null;
  }
};

