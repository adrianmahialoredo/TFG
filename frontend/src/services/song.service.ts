// src/services/SongService.ts
import axios from 'axios';
import { SongDTO } from '../types/SongDTO';
import { API_ENDPOINTS } from '../config/apiConfig';

export const getAllSongs = async (): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.BASE);
  return response.data;
};

export const getSongById = async (id: string): Promise<SongDTO> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.BY_ID(id));
  return response.data;
};

export const getPopularSongs = async (): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.POPULAR);
  return response.data;
};

export const getPopularSongIds = async (limit?: number): Promise<number[]> => {
  const url = limit ? `${API_ENDPOINTS.SONGS.POPULAR_IDS}?limit=${limit}` : API_ENDPOINTS.SONGS.POPULAR_IDS;
  const response = await axios.get(url);
  return response.data;
};

export const getSongsByIds = async (ids: number[]): Promise<SongDTO[]> => {
  const response = await axios.post(API_ENDPOINTS.SONGS.BY_IDS, ids);
  return response.data;
};

export const getPopularSongsByGenre = async (genreId: string): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.POPULAR_BY_GENRE(genreId));
  return response.data;
};

export const getNewSongsByGenre = async (genreId: string): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.NEW_BY_GENRE(genreId));
  return response.data;
};

export const getClassicSongsByGenre = async (genreId: string): Promise<SongDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.SONGS.CLASSIC_BY_GENRE(genreId));
  return response.data;
};

export const getSongsByDecade = async (decade: string): Promise<SongDTO[]> => {
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

