// src/services/GenreService.ts
import { apiClient } from './apiClient';
import { GenreDTO } from '../types';
import { API_ENDPOINTS } from '../config/apiConfig';

export const getAll = async (): Promise<GenreDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.GENRES.BASE);
  return response.data;
};

export const getById = async (id: string): Promise<GenreDTO> => {
  const response = await apiClient.get(API_ENDPOINTS.GENRES.BY_ID(id));
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<GenreDTO[]> => {
  const response = await apiClient.post(API_ENDPOINTS.GENRES.BY_IDS, ids);
  return response.data;
};

export const createGenre = async (genre: GenreDTO): Promise<GenreDTO> => {
  const response = await apiClient.post(API_ENDPOINTS.GENRES.BASE, genre);
  return response.data;
};

export const updateGenre = async (id: string, genre: GenreDTO): Promise<GenreDTO> => {
  const response = await apiClient.put(API_ENDPOINTS.GENRES.BY_ID(id), genre);
  return response.data;
};

export const deleteGenre = async (id: string): Promise<void> => {
  await apiClient.delete(API_ENDPOINTS.GENRES.BY_ID(id));
};

/**
 * IDs de los géneros más usados en las listas del usuario (por los géneros de sus canciones,
 * géneros-instrumento y lecciones) y después sus favoritos, sin los que ya están guardados en ellas.
 */
export const getMostUsedInItemListsIdsByUser = async (userId: string): Promise<string[]> => {
  const response = await apiClient.get(API_ENDPOINTS.GENRES.MOST_USED_IN_ITEM_LISTS_IDS_BY_USER(userId));
  return response.data;
};
