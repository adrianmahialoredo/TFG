// src/services/SongService.ts
import axios from 'axios';
import { SongDTO } from '../types/SongDTO';
import { API_ENDPOINTS } from '../config/apiConfig';

export const getAll = async (): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.BASE);
  return response.data;
};

export const getById = async (id: string): Promise<SongDTO> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.BY_ID(id));
  return response.data;
};

export const getPopular = async (): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.POPULAR);
  return response.data;
};

export const getPopularIds = async (limit?: number): Promise<number[]> => {
  const url = limit ? `${API_ENDPOINTS.SONGS.POPULAR_IDS}?limit=${limit}` : API_ENDPOINTS.SONGS.POPULAR_IDS;
  const response = await axios.get(url);
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<SongDTO[]> => {
  const response = await axios.post(API_ENDPOINTS.SONGS.BY_IDS, ids);
  return response.data;
};

export const getPopularByGenre = async (genreId: string): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.POPULAR_BY_GENRE(genreId));
  return response.data;
};

export const getPopularByGenreId = async (genreId: string): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.POPULAR_BY_GENRE_ID(genreId));
  return response.data;
};

export const getClassicByGenreId = async (genreId: string): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.CLASSIC_BY_GENRE_ID(genreId));
  return response.data;
};

export const getNewByGenreId = async (genreId: string): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.NEW_BY_GENRE_ID(genreId));
  return response.data;
};

export const getNewByGenre = async (genreId: string): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.NEW_BY_GENRE(genreId));
  return response.data;
};

export const getClassicByGenre = async (genreId: string): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.CLASSIC_BY_GENRE(genreId));
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

export const getNew = async (): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.HOME_NEW);
  return response.data;
};

export const getRecommended = async (): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.HOME_RECOMMENDED);
  return response.data;
};

export const getFromSpotify = async (): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.HOME_SPOTIFY);
  return response.data;
};
