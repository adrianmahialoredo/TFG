// src/services/LessonService.ts
import axios from 'axios';
import { LessonDTO } from '../types';
import { API_ENDPOINTS } from '../config/apiConfig';

export const getAll = async (): Promise<LessonDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.LESSONS.BASE);
  return response.data;
};

export const getById = async (id: string): Promise<LessonDTO> => {
  const response = await axios.get(API_ENDPOINTS.LESSONS.BY_ID(id));
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<LessonDTO[]> => {
  const response = await axios.post(API_ENDPOINTS.LESSONS.BY_IDS, ids);
  return response.data;
};

export const getByGenreId = async (genreId: string): Promise<LessonDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.LESSONS.BY_GENRE(genreId));
  return response.data;
};

export const createLesson = async (lesson: LessonDTO): Promise<LessonDTO> => {
  const response = await axios.post(API_ENDPOINTS.LESSONS.BASE, lesson);
  return response.data;
};

export const updateLesson = async (id: string, lesson: LessonDTO): Promise<LessonDTO> => {
  const response = await axios.put(API_ENDPOINTS.LESSONS.BY_ID(id), lesson);
  return response.data;
};

export const deleteLesson = async (id: string): Promise<void> => {
  await axios.delete(API_ENDPOINTS.LESSONS.BY_ID(id));
};

export const getPopular = async (): Promise<LessonDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.LESSONS.HOME_POPULAR);
  return response.data;
};

export const getNew = async (): Promise<LessonDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.LESSONS.HOME_NEW);
  return response.data;
};

export const getRecommended = async (): Promise<LessonDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.LESSONS.HOME_RECOMMENDED);
  return response.data;
};

