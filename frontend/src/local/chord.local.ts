import { ChordDTO } from "../types";
import { storeData, getData } from "./storage.local";
import AsyncStorage from "@react-native-async-storage/async-storage";

const CHORD_CACHE_PREFIX = '@chord:';
const INDEX_CACHE_PREFIX = '@chord_index:';

export const saveSingle = async (chord: ChordDTO): Promise<void> => {
  const key = `${CHORD_CACHE_PREFIX}${chord.id}`;
  return storeData(key, chord);
};

export const getById = async (id: string): Promise<ChordDTO | null> => {
  const key = `${CHORD_CACHE_PREFIX}${id}`;
  return getData<ChordDTO>(key);
};

export const saveBatch = async (chords: ChordDTO[]): Promise<void> => {
  if (chords.length === 0) return;

  const pairs: [string, string][] = chords.map(chord => [
    `${CHORD_CACHE_PREFIX}${chord.id}`,
    JSON.stringify(chord)
  ]);

  try {
    await AsyncStorage.multiSet(pairs);
  } catch (e) {
    console.error("[ChordLocal] Error en saveBatch:", e);
  }
};

export const getAllById = async (ids: string[]): Promise<{ cached: ChordDTO[]; missing: string[] }> => {
  if (ids.length === 0) return { cached: [], missing: [] };

  const keys = ids.map(id => `${CHORD_CACHE_PREFIX}${id}`);
  const pairs = await AsyncStorage.multiGet(keys);

  const cached: ChordDTO[] = [];
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
    console.error("[ChordLocal] Error guardando índice:", e);
  }
};

export const getQueryIndex = async (key: string): Promise<string[] | null> => {
  try {
    const data = await AsyncStorage.getItem(`${INDEX_CACHE_PREFIX}${key}`);
    return data ? JSON.parse(data) : null;
  } catch (e) {
    console.error("[ChordLocal] Error leyendo índice:", e);
    return null;
  }
};

