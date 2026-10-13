import { apiClient } from './apiClient';
import { SongDTO } from '../types';
import { API_ENDPOINTS } from '../config/apiConfig';

export const getAll = async (): Promise<SongDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.SONGS.BASE);
  return response.data;
};

export const getById = async (id: string): Promise<SongDTO> => {
  const response = await apiClient.get(API_ENDPOINTS.SONGS.BY_ID(id));
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<SongDTO[]> => {
  const response = await apiClient.post(API_ENDPOINTS.SONGS.BY_IDS, ids);
  return response.data;
};

export const getByDecade = async (decade: string): Promise<SongDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.SONGS.BY_DECADE(decade));
  return response.data;
};

export const createSong = async (song: SongDTO): Promise<SongDTO> => {
  const response = await apiClient.post(API_ENDPOINTS.SONGS.BASE, song);
  return response.data;
};

export const updateSong = async (id: string, song: SongDTO): Promise<SongDTO> => {
  const response = await apiClient.put(API_ENDPOINTS.SONGS.BY_ID(id), song);
  return response.data;
};

export const deleteSong = async (id: string): Promise<void> => {
  await apiClient.delete(API_ENDPOINTS.SONGS.BY_ID(id));
};

// --- Carruseles con DTO completo (decisión de negocio: el usuario suele abrir lo que ve) ---

const withLimit = (url: string, limit?: number) => (limit ? `${url}?limit=${limit}` : url);

export const getPopular = async (limit?: number): Promise<SongDTO[]> => {
  const response = await apiClient.get(withLimit(API_ENDPOINTS.SONGS.POPULAR, limit));
  return response.data;
};

export const getNew = async (limit?: number): Promise<SongDTO[]> => {
  const response = await apiClient.get(withLimit(API_ENDPOINTS.SONGS.NEW, limit));
  return response.data;
};

export const getRecommendedFromSpotify = async (userId: string, limit: number): Promise<SongDTO[]> => {
  const response = await apiClient.get(withLimit(API_ENDPOINTS.SONGS.SPOTIFY(userId), limit));
  return response.data;
};

export const getPopularByGenreId = async (genreId: string): Promise<SongDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.SONGS.POPULAR_BY_GENRE(genreId));
  return response.data;
};

export const getClassicByGenreId = async (genreId: string): Promise<SongDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.SONGS.CLASSIC_BY_GENRE(genreId));
  return response.data;
};

export const getNewByGenreId = async (genreId: string): Promise<SongDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.SONGS.NEW_BY_GENRE(genreId));
  return response.data;
};

export const getRecommendedByUser = async (userId: string, limit: number): Promise<SongDTO[]> => {
  const response = await apiClient.get(withLimit(API_ENDPOINTS.SONGS.RECOMMENDED_BY_USER(userId), limit));
  return response.data;
};
