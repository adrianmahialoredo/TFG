import axios from 'axios';
import { API_ENDPOINTS } from '../config/apiConfig';
import { SongLessonDTO } from '../types';

export const getAllSongLessons = async (): Promise<SongLessonDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONG_LESSONS.BASE);
  return response.data;
};

export const getSongLessonById = async (id: string): Promise<SongLessonDTO> => {
  const response = await axios.get(API_ENDPOINTS.SONG_LESSONS.BY_ID(id));
  return response.data;
};

export const getSongLessonsBySong = async (songId: string): Promise<SongLessonDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONG_LESSONS.BY_SONG(songId));
  return response.data;
};

export const createSongLesson = async (songLesson: SongLessonDTO): Promise<SongLessonDTO> => {
  const response = await axios.post(API_ENDPOINTS.SONG_LESSONS.BASE, songLesson);
  return response.data;
};

export const updateSongLesson = async (id: string, songLesson: SongLessonDTO): Promise<SongLessonDTO> => {
  const response = await axios.put(API_ENDPOINTS.SONG_LESSONS.BY_ID(id), songLesson);
  return response.data;
};

export const deleteSongLesson = async (id: string): Promise<void> => {
  await axios.delete(API_ENDPOINTS.SONG_LESSONS.BY_ID(id));
};

