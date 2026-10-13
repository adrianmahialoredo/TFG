import { LessonSectionDTO } from "../types";
import { storeData, getData } from "./storage.local";
import AsyncStorage from "@react-native-async-storage/async-storage";

const LESSON_SECTION_CACHE_PREFIX = '@lessonSection:';
const INDEX_CACHE_PREFIX = '@lessonSection_index:';

export const saveSingle = async (lessonSection: LessonSectionDTO): Promise<void> => {
  const key = `${LESSON_SECTION_CACHE_PREFIX}${lessonSection.id}`;
  return storeData(key, lessonSection);
};

export const getById = async (id: string): Promise<LessonSectionDTO | null> => {
  const key = `${LESSON_SECTION_CACHE_PREFIX}${id}`;
  return getData<LessonSectionDTO>(key);
};

export const saveBatch = async (lessonSections: LessonSectionDTO[]): Promise<void> => {
  if (lessonSections.length === 0) return;

  const pairs: [string, string][] = lessonSections.map(section => [
    `${LESSON_SECTION_CACHE_PREFIX}${section.id}`,
    JSON.stringify(section)
  ]);

  try {
    await AsyncStorage.multiSet(pairs);
  } catch (e) {
    console.error("[LessonSectionLocal] Error en saveBatch:", e);
  }
};

export const getAllById = async (ids: string[]): Promise<{ cached: LessonSectionDTO[]; missing: string[] }> => {
  if (ids.length === 0) return { cached: [], missing: [] };

  const keys = ids.map(id => `${LESSON_SECTION_CACHE_PREFIX}${id}`);
  const pairs = await AsyncStorage.multiGet(keys);

  const cached: LessonSectionDTO[] = [];
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
    console.error("[LessonSectionLocal] Error guardando índice:", e);
  }
};

export const getQueryIndex = async (key: string): Promise<string[] | null> => {
  try {
    const data = await AsyncStorage.getItem(`${INDEX_CACHE_PREFIX}${key}`);
    return data ? JSON.parse(data) : null;
  } catch (e) {
    console.error("[LessonSectionLocal] Error leyendo índice:", e);
    return null;
  }
};

