import { SongDTO } from '../types';
import { storeData, getData } from './storage.local';
import AsyncStorage from "@react-native-async-storage/async-storage";

const SONG_CACHE_PREFIX = '@song:';
const INDEX_CACHE_PREFIX = '@song_index:'; // Prefijo para las "fotos"

export const saveSingle = async (song: SongDTO): Promise<void> => {
  const key = `${SONG_CACHE_PREFIX}${song.id}`;
  return storeData(key, song);
};

export const getById = async (songId: string): Promise<SongDTO | null> => {
  const key = `${SONG_CACHE_PREFIX}${songId}`;
  return getData<SongDTO>(key);
};

/**
 * MEJORADO: Uso de multiSet para guardar todo de un golpe.
 * Mucho más eficiente que un bucle for.
 */
export const saveBatch = async (songs: SongDTO[]): Promise<void> => {
  if (songs.length === 0) return;

  const pairs: [string, string][] = songs.map(song => [
    `${SONG_CACHE_PREFIX}${song.id}`,
    JSON.stringify(song)
  ]);

  try {
    await AsyncStorage.multiSet(pairs);
  } catch (e) {
    console.error("[SongLocal] Error en saveBatch:", e);
  }
};

export const getAllById = async (ids: string[]): Promise<{ cached: SongDTO[]; missing: string[] }> => {
  if (ids.length === 0) return { cached: [], missing: [] };

  const keys = ids.map(id => `${SONG_CACHE_PREFIX}${id}`);
  const pairs = await AsyncStorage.multiGet(keys);

  const cached: SongDTO[] = [];
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

/**
 * MEJORADO: Métodos de índice con prefijo consistente
 */
export const saveQueryIndex = async (key: string, ids: string[]): Promise<void> => {
  try {
    await AsyncStorage.setItem(`${INDEX_CACHE_PREFIX}${key}`, JSON.stringify(ids));
  } catch (e) {
    console.error("[SongLocal] Error guardando índice:", e);
  }
};

export const getQueryIndex = async (key: string): Promise<string[] | null> => {
  try {
    const data = await AsyncStorage.getItem(`${INDEX_CACHE_PREFIX}${key}`);
    return data ? JSON.parse(data) : null;
  } catch (e) {
    console.error("[SongLocal] Error leyendo índice:", e);
    return null;
  }
};

