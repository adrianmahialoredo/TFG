import { InstrumentDTO } from "../types";
import { storeData, getData } from "./storage.local";
import AsyncStorage from "@react-native-async-storage/async-storage";

const INSTRUMENT_CACHE_PREFIX = '@instrument:';
const INDEX_CACHE_PREFIX = '@instrument_index:';

export const saveSingle = async (instrument: InstrumentDTO): Promise<void> => {
  const key = `${INSTRUMENT_CACHE_PREFIX}${instrument.id}`;
  return storeData(key, instrument);
};

export const getById = async (id: string): Promise<InstrumentDTO | null> => {
  const key = `${INSTRUMENT_CACHE_PREFIX}${id}`;
  return getData<InstrumentDTO>(key);
};

export const saveBatch = async (instruments: InstrumentDTO[]): Promise<void> => {
  if (instruments.length === 0) return;

  const pairs: [string, string][] = instruments.map(instrument => [
    `${INSTRUMENT_CACHE_PREFIX}${instrument.id}`,
    JSON.stringify(instrument)
  ]);

  try {
    await AsyncStorage.multiSet(pairs);
  } catch (e) {
    console.error("[InstrumentLocal] Error en saveBatch:", e);
  }
};


export const getAllById = async (ids: string[]): Promise<{ cached: InstrumentDTO[]; missing: string[] }> => {
  if (ids.length === 0) return { cached: [], missing: [] };

  const keys = ids.map(id => `${INSTRUMENT_CACHE_PREFIX}${id}`);
  const pairs = await AsyncStorage.multiGet(keys);

  const cached: InstrumentDTO[] = [];
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
    console.error("[InstrumentLocal] Error guardando índice:", e);
  }
};

export const getQueryIndex = async (key: string): Promise<string[] | null> => {
  try {
    const data = await AsyncStorage.getItem(`${INDEX_CACHE_PREFIX}${key}`);
    return data ? JSON.parse(data) : null;
  } catch (e) {
    console.error("[InstrumentLocal] Error leyendo índice:", e);
    return null;
  }
};

