import { SongLessonDTO } from "../types";
import { getData, storeData } from "./storage.local";

const SONG_LESSONS_KEY = 'songLessons';

/* Guarda todas las lecciones de canciones */
export const storeSongLessons = async (songLessons: SongLessonDTO[]): Promise<void> => {
  return storeData(SONG_LESSONS_KEY, songLessons);
};

/* Obtiene todas las lecciones de canciones */
export const getStoredSongLessons = async (): Promise<SongLessonDTO[] | null> => {
  return getData<SongLessonDTO[]>(SONG_LESSONS_KEY);
};

/* Obtiene una lección de canción por su ID */
export const getStoredSongLessonById = async (id: string): Promise<SongLessonDTO | null> => {
  const songLessons = await getStoredSongLessons();
  return songLessons?.find((lesson) => lesson.id === id) || null;
};

/* Obtiene todas las lecciones de una canción */
export const getStoredSongLessonsBySong = async (songId: string): Promise<SongLessonDTO[]> => {
  const songLessons = await getStoredSongLessons();
  return songLessons?.filter((lesson) => lesson.songId === songId) || [];
};

/* Obtiene varias lecciones a partir de una lista de IDs */
export const getStoredSongLessonsByIdList = async (ids: string[]): Promise<SongLessonDTO[]> => {
  const songLessons = await getStoredSongLessons();
  return songLessons?.filter((lesson) => ids.includes(lesson.id)) || [];
};

