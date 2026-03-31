// src/services/GenreService.ts
import axios from 'axios';
import { GenreDTO } from '../types/GenreDTO';
import { API_ENDPOINTS } from '../config/apiConfig';

export const getAllGenres = async (): Promise<GenreDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.GENRES.BASE);
  return response.data;
};

export const getGenreById = async (id: string): Promise<GenreDTO> => {
  const response = await axios.get(API_ENDPOINTS.GENRES.BY_ID(id));
  return response.data;
};

export const createGenre = async (genre: GenreDTO): Promise<GenreDTO> => {
  const response = await axios.post(API_ENDPOINTS.GENRES.BASE, genre);
  return response.data;
};

export const updateGenre = async (id: string, genre: GenreDTO): Promise<GenreDTO> => {
  const response = await axios.put(API_ENDPOINTS.GENRES.BY_ID(id), genre);
  return response.data;
};

export const deleteGenre = async (id: string): Promise<void> => {
  await axios.delete(API_ENDPOINTS.GENRES.BY_ID(id));
};
