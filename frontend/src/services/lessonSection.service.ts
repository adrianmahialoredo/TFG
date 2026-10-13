import { apiClient } from './apiClient';
import { API_ENDPOINTS } from '../config/apiConfig';
import { LessonSectionDTO } from '../types';

export const getAll = async (): Promise<LessonSectionDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.LESSON_SECTIONS.BASE);
  return response.data;
};

export const getById = async (id: string): Promise<LessonSectionDTO> => {
  const response = await apiClient.get(API_ENDPOINTS.LESSON_SECTIONS.BY_ID(id));
  return response.data;
};

export const getByLesson = async (lessonId: string): Promise<LessonSectionDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.LESSON_SECTIONS.BY_LESSON(lessonId));
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<LessonSectionDTO[]> => {
  const response = await apiClient.post(API_ENDPOINTS.LESSON_SECTIONS.BY_IDS, ids);
  return response.data;
};

export const createLessonSection = async (lessonSection: LessonSectionDTO): Promise<LessonSectionDTO> => {
  const response = await apiClient.post(API_ENDPOINTS.LESSON_SECTIONS.BASE, lessonSection);
  return response.data;
};

export const updateLessonSection = async (id: string, lessonSection: LessonSectionDTO): Promise<LessonSectionDTO> => {
  const response = await apiClient.put(API_ENDPOINTS.LESSON_SECTIONS.BY_ID(id), lessonSection);
  return response.data;
};

export const deleteLessonSection = async (id: string): Promise<void> => {
  await apiClient.delete(API_ENDPOINTS.LESSON_SECTIONS.BY_ID(id));
};
