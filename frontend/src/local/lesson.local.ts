import { LessonDTO } from '../types';
import { storeData, getData } from './storage.local';
import AsyncStorage from "@react-native-async-storage/async-storage";

const LESSON_CACHE_PREFIX = '@lesson:';
const INDEX_CACHE_PREFIX = '@lesson_index:';

export const saveSingle = async (lesson: LessonDTO): Promise<void> => {
  const key = `${LESSON_CACHE_PREFIX}${lesson.id}`;
  return storeData(key, lesson);
};

export const getById = async (lessonId: string): Promise<LessonDTO | null> => {
  const key = `${LESSON_CACHE_PREFIX}${lessonId}`;
  return getData<LessonDTO>(key);
};

export const saveBatch = async (lessons: LessonDTO[]): Promise<void> => {
  if (lessons.length === 0) return;

  const pairs: [string, string][] = lessons.map(lesson => [
    `${LESSON_CACHE_PREFIX}${lesson.id}`,
    JSON.stringify(lesson)
  ]);

  try {
    await AsyncStorage.multiSet(pairs);
  } catch (e) {
    console.error("[LessonLocal] Error en saveBatch:", e);
  }
};

export const getAllById = async (ids: string[]): Promise<{ cached: LessonDTO[]; missing: string[] }> => {
  if (ids.length === 0) return { cached: [], missing: [] };

  const keys = ids.map(id => `${LESSON_CACHE_PREFIX}${id}`);
  const pairs = await AsyncStorage.multiGet(keys);

  const cached: LessonDTO[] = [];
  const missing: string[] = [];

  pairs.forEach(([key, value], index) => {
    if (value !== null) {
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
    console.error("[LessonLocal] Error guardando índice:", e);
  }
};

export const getQueryIndex = async (key: string): Promise<string[] | null> => {
  try {
    const data = await AsyncStorage.getItem(`${INDEX_CACHE_PREFIX}${key}`);
    return data ? JSON.parse(data) : null;
  } catch (e) {
    console.error("[LessonLocal] Error leyendo índice:", e);
    return null;
  }
};
