import { GenreDTO } from "../types";
import { storeData, getData } from "./storage.local";
import AsyncStorage from "@react-native-async-storage/async-storage";

const GENRE_CACHE_PREFIX = '@genre:';
const INDEX_CACHE_PREFIX = '@genre_index:';

export const saveSingle = async (genre: GenreDTO): Promise<void> => {
  const key = `${GENRE_CACHE_PREFIX}${genre.id}`;
  return storeData(key, genre);
};

export const getById = async (id: string): Promise<GenreDTO | null> => {
  const key = `${GENRE_CACHE_PREFIX}${id}`;
  return getData<GenreDTO>(key);
};

export const saveBatch = async (genres: GenreDTO[]): Promise<void> => {
  if (genres.length === 0) return;

  const pairs: [string, string][] = genres.map(genre => [
    `${GENRE_CACHE_PREFIX}${genre.id}`,
    JSON.stringify(genre)
  ]);

  try {
    await AsyncStorage.multiSet(pairs);
  } catch (e) {
    console.error("[GenreLocal] Error en saveBatch:", e);
  }
};


export const getAllById = async (ids: string[]): Promise<{ cached: GenreDTO[]; missing: string[] }> => {
  if (ids.length === 0) return { cached: [], missing: [] };

  const keys = ids.map(id => `${GENRE_CACHE_PREFIX}${id}`);
  const pairs = await AsyncStorage.multiGet(keys);

  const cached: GenreDTO[] = [];
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
    console.error("[GenreLocal] Error guardando índice:", e);
  }
};

export const getQueryIndex = async (key: string): Promise<string[] | null> => {
  try {
    const data = await AsyncStorage.getItem(`${INDEX_CACHE_PREFIX}${key}`);
    return data ? JSON.parse(data) : null;
  } catch (e) {
    console.error("[GenreLocal] Error leyendo índice:", e);
    return null;
  }
};

