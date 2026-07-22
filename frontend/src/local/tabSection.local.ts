import { TabSectionDTO } from "../types";
import { storeData, getData } from "./storage.local";
import AsyncStorage from "@react-native-async-storage/async-storage";

const TAB_SECTION_CACHE_PREFIX = '@tabSection:';
const INDEX_CACHE_PREFIX = '@tabSection_index:';

export const saveSingle = async (tabSection: TabSectionDTO): Promise<void> => {
  const key = `${TAB_SECTION_CACHE_PREFIX}${tabSection.id}`;
  return storeData(key, tabSection);
};

export const getById = async (id: string): Promise<TabSectionDTO | null> => {
  const key = `${TAB_SECTION_CACHE_PREFIX}${id}`;
  return getData<TabSectionDTO>(key);
};

export const saveBatch = async (tabSections: TabSectionDTO[]): Promise<void> => {
  if (tabSections.length === 0) return;

  const pairs: [string, string][] = tabSections.map(section => [
    `${TAB_SECTION_CACHE_PREFIX}${section.id}`,
    JSON.stringify(section)
  ]);

  try {
    await AsyncStorage.multiSet(pairs);
  } catch (e) {
    console.error("[TabSectionLocal] Error en saveBatch:", e);
  }
};

export const getAllById = async (ids: string[]): Promise<{ cached: TabSectionDTO[]; missing: string[] }> => {
  if (ids.length === 0) return { cached: [], missing: [] };

  const keys = ids.map(id => `${TAB_SECTION_CACHE_PREFIX}${id}`);
  const pairs = await AsyncStorage.multiGet(keys);

  const cached: TabSectionDTO[] = [];
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
    console.error("[TabSectionLocal] Error guardando índice:", e);
  }
};

export const getQueryIndex = async (key: string): Promise<string[] | null> => {
  try {
    const data = await AsyncStorage.getItem(`${INDEX_CACHE_PREFIX}${key}`);
    return data ? JSON.parse(data) : null;
  } catch (e) {
    console.error("[TabSectionLocal] Error leyendo índice:", e);
    return null;
  }
};

