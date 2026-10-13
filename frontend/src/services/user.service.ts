import { apiClient } from './apiClient';
import { UserDTO } from '../types';
import { API_ENDPOINTS } from '../config/apiConfig';

export const getById = async (id: string): Promise<UserDTO> => {
  const response = await apiClient.get(API_ENDPOINTS.USERS.BY_ID(id));
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<UserDTO[]> => {
  if (ids.length === 0) return [];
  
  const response = await apiClient.post(API_ENDPOINTS.USERS.BY_IDS, ids);
  return response.data;
};

export const updateUserProfile = async (user: UserDTO): Promise<UserDTO> => {
  const response = await apiClient.put(API_ENDPOINTS.USERS.BY_ID(user.id), user);
  return response.data;
};

export const addFavoriteGenre = async (userId: string, genreId: string): Promise<UserDTO> => {
  const response = await apiClient.post(API_ENDPOINTS.USERS.FAVORITE_GENRE(userId, genreId));
  return response.data;
};

export const removeFavoriteGenre = async (userId: string, genreId: string): Promise<UserDTO> => {
  const response = await apiClient.delete(API_ENDPOINTS.USERS.FAVORITE_GENRE(userId, genreId));
  return response.data;
};

export const addFavoriteInstrument = async (userId: string, instrumentId: string): Promise<UserDTO> => {
  const response = await apiClient.post(API_ENDPOINTS.USERS.FAVORITE_INSTRUMENT(userId, instrumentId));
  return response.data;
};

export const removeFavoriteInstrument = async (userId: string, instrumentId: string): Promise<UserDTO> => {
  const response = await apiClient.delete(API_ENDPOINTS.USERS.FAVORITE_INSTRUMENT(userId, instrumentId));
  return response.data;
};
