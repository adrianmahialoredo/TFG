import { SongLessonDTO } from "../types";
import { storeData, getData } from "./storage.local";
import AsyncStorage from "@react-native-async-storage/async-storage";

const SONG_LESSON_CACHE_PREFIX = '@songLesson:';
const INDEX_CACHE_PREFIX = '@songLesson_index:';

export const saveSingle = async (songLesson: SongLessonDTO): Promise<void> => {
  const key = `${SONG_LESSON_CACHE_PREFIX}${songLesson.id}`;
  return storeData(key, songLesson);
};

export const getById = async (id: string): Promise<SongLessonDTO | null> => {
  const key = `${SONG_LESSON_CACHE_PREFIX}${id}`;
  return getData<SongLessonDTO>(key);
};

export const saveBatch = async (songLessons: SongLessonDTO[]): Promise<void> => {
  if (songLessons.length === 0) return;

  const pairs: [string, string][] = songLessons.map(lesson => [
    `${SONG_LESSON_CACHE_PREFIX}${lesson.id}`,
    JSON.stringify(lesson)
  ]);

  try {
    await AsyncStorage.multiSet(pairs);
  } catch (e) {
    console.error("[SongLessonLocal] Error en saveBatch:", e);
  }
};

export const getAllById = async (ids: string[]): Promise<{ cached: SongLessonDTO[]; missing: string[] }> => {
  if (ids.length === 0) return { cached: [], missing: [] };

  const keys = ids.map(id => `${SONG_LESSON_CACHE_PREFIX}${id}`);
  const pairs = await AsyncStorage.multiGet(keys);

  const cached: SongLessonDTO[] = [];
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
    console.error("[SongLessonLocal] Error guardando índice:", e);
  }
};

export const getQueryIndex = async (key: string): Promise<string[] | null> => {
  try {
    const data = await AsyncStorage.getItem(`${INDEX_CACHE_PREFIX}${key}`);
    return data ? JSON.parse(data) : null;
  } catch (e) {
    console.error("[SongLessonLocal] Error leyendo índice:", e);
    return null;
  }
};

