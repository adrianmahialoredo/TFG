import { InstrumentGenreDTO } from "../types";
import { storeData, getData } from "./storage.local";
import AsyncStorage from "@react-native-async-storage/async-storage";

const INSTRUMENT_GENRE_CACHE_PREFIX = '@instrumentGenre:';
const INDEX_CACHE_PREFIX = '@instrumentGenre_index:';

export const saveSingle = async (instrumentGenre: InstrumentGenreDTO): Promise<void> => {
  const key = `${INSTRUMENT_GENRE_CACHE_PREFIX}${instrumentGenre.id}`;
  return storeData(key, instrumentGenre);
};

export const getById = async (id: string): Promise<InstrumentGenreDTO | null> => {
  const key = `${INSTRUMENT_GENRE_CACHE_PREFIX}${id}`;
  return getData<InstrumentGenreDTO>(key);
};

export const saveBatch = async (instrumentGenres: InstrumentGenreDTO[]): Promise<void> => {
  if (instrumentGenres.length === 0) return;

  const pairs: [string, string][] = instrumentGenres.map(ig => [
    `${INSTRUMENT_GENRE_CACHE_PREFIX}${ig.id}`,
    JSON.stringify(ig)
  ]);

  try {
    await AsyncStorage.multiSet(pairs);
  } catch (e) {
    console.error("[InstrumentGenreLocal] Error en saveBatch:", e);
  }
};

export const getAllById = async (ids: string[]): Promise<{ cached: InstrumentGenreDTO[]; missing: string[] }> => {
  if (ids.length === 0) return { cached: [], missing: [] };

  const keys = ids.map(id => `${INSTRUMENT_GENRE_CACHE_PREFIX}${id}`);
  const pairs = await AsyncStorage.multiGet(keys);

  const cached: InstrumentGenreDTO[] = [];
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
    console.error("[InstrumentGenreLocal] Error guardando índice:", e);
  }
};

export const getQueryIndex = async (key: string): Promise<string[] | null> => {
  try {
    const data = await AsyncStorage.getItem(`${INDEX_CACHE_PREFIX}${key}`);
    return data ? JSON.parse(data) : null;
  } catch (e) {
    console.error("[InstrumentGenreLocal] Error leyendo índice:", e);
    return null;
  }
};

