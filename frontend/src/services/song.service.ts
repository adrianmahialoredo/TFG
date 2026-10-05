import axios from 'axios';
import { SongDTO } from '../types';
import { API_ENDPOINTS } from '../config/apiConfig';

export const getAll = async (): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.BASE);
  return response.data;
};

export const getById = async (id: string): Promise<SongDTO> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.BY_ID(id));
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<SongDTO[]> => {
  const response = await axios.post(API_ENDPOINTS.SONGS.BY_IDS, ids);
  return response.data;
};

export const getByDecade = async (decade: string): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.BY_DECADE(decade));
  return response.data;
};

export const createSong = async (song: SongDTO): Promise<SongDTO> => {
  const response = await axios.post(API_ENDPOINTS.SONGS.BASE, song);
  return response.data;
};

export const updateSong = async (id: string, song: SongDTO): Promise<SongDTO> => {
  const response = await axios.put(API_ENDPOINTS.SONGS.BY_ID(id), song);
  return response.data;
};

export const deleteSong = async (id: string): Promise<void> => {
  await axios.delete(API_ENDPOINTS.SONGS.BY_ID(id));
};

// --- Carruseles con DTO completo (decisión de negocio: el usuario suele abrir lo que ve) ---

const withLimit = (url: string, limit?: number) => (limit ? `${url}?limit=${limit}` : url);

export const getPopular = async (limit?: number): Promise<SongDTO[]> => {
  const response = await axios.get(withLimit(API_ENDPOINTS.SONGS.POPULAR, limit));
  return response.data;
};

export const getNew = async (limit?: number): Promise<SongDTO[]> => {
  const response = await axios.get(withLimit(API_ENDPOINTS.SONGS.NEW, limit));
  return response.data;
};

export const getFromSpotify = async (limit?: number): Promise<SongDTO[]> => {
  const response = await axios.get(withLimit(API_ENDPOINTS.SONGS.SPOTIFY, limit));
  return response.data;
};

export const getPopularByGenreId = async (genreId: string): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.POPULAR_BY_GENRE(genreId));
  return response.data;
};

export const getClassicByGenreId = async (genreId: string): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.CLASSIC_BY_GENRE(genreId));
  return response.data;
};

export const getNewByGenreId = async (genreId: string): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.NEW_BY_GENRE(genreId));
  return response.data;
};

// --- Carruseles por convención: solo IDs (se resuelven contra el catálogo de items) ---

export const getRecommendedIds = async (limit?: number): Promise<string[]> => {
  const response = await axios.get(withLimit(API_ENDPOINTS.SONGS.RECOMMENDED_IDS, limit));
  return response.data;
};
