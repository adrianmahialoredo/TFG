// src/services/GenreService.ts
import axios from 'axios';
import { GenreDTO } from '../types/GenreDTO';

const API_URL = 'http://192.168.0.22:8080/genres'; // misma base que en tu servicio de auth

export const getAllGenres = async (): Promise<GenreDTO[]> => {
  const response = await axios.get(`${API_URL}`);
  return response.data;
};

export const getGenreById = async (id: string): Promise<GenreDTO> => {
  const response = await axios.get(`${API_URL}/${id}`);
  return response.data;
};