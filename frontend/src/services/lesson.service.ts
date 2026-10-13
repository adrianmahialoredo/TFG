// src/services/LessonService.ts
import { apiClient } from './apiClient';
import { LessonDTO } from '../types';
import { API_ENDPOINTS } from '../config/apiConfig';

export const getAll = async (): Promise<LessonDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.LESSONS.BASE);
  return response.data;
};

export const getById = async (id: string): Promise<LessonDTO> => {
  const response = await apiClient.get(API_ENDPOINTS.LESSONS.BY_ID(id));
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<LessonDTO[]> => {
  const response = await apiClient.post(API_ENDPOINTS.LESSONS.BY_IDS, ids);
  return response.data;
};

export const getByGenreId = async (genreId: string): Promise<LessonDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.LESSONS.BY_GENRE(genreId));
  return response.data;
};

/** Lecciones recomendadas de un género (DTO completo, en orden de prioridad). */
export const getRecommendedByGenreId = async (genreId: string): Promise<LessonDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.LESSONS.RECOMMENDED_BY_GENRE(genreId));
  return response.data;
};

export const createLesson = async (lesson: LessonDTO): Promise<LessonDTO> => {
  const response = await apiClient.post(API_ENDPOINTS.LESSONS.BASE, lesson);
  return response.data;
};

export const updateLesson = async (id: string, lesson: LessonDTO): Promise<LessonDTO> => {
  const response = await apiClient.put(API_ENDPOINTS.LESSONS.BY_ID(id), lesson);
  return response.data;
};

export const deleteLesson = async (id: string): Promise<void> => {
  await apiClient.delete(API_ENDPOINTS.LESSONS.BY_ID(id));
};

// --- Feeds de carrusel: solo IDs (se resuelven contra el catálogo de items) ---

export const getPopularIds = async (): Promise<string[]> => {
  const response = await apiClient.get(API_ENDPOINTS.LESSONS.POPULAR_IDS);
  return response.data;
};

export const getNewIds = async (): Promise<string[]> => {
  const response = await apiClient.get(API_ENDPOINTS.LESSONS.NEW_IDS);
  return response.data;
};

export const getPopularByInstrumentIds = async (instrumentId: string): Promise<string[]> => {
  const response = await apiClient.get(API_ENDPOINTS.LESSONS.POPULAR_BY_INSTRUMENT_IDS(instrumentId));
  return response.data;
};

// --- Recomendaciones por usuario: DTO completo ---

export const getRecommendedByUser = async (userId: string): Promise<LessonDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.LESSONS.RECOMMENDED_BY_USER(userId));
  return response.data;
};
