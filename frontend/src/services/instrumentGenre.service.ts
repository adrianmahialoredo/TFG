import { apiClient } from './apiClient';
import { API_ENDPOINTS } from '../config/apiConfig';
import { InstrumentGenreDTO } from '../types/InstrumentGenreDTO';

export const getAllInstrumentGenres = async (): Promise<InstrumentGenreDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.INSTRUMENT_GENRES.BASE);
  return response.data;
};

export const getInstrumentGenreById = async (id: string): Promise<InstrumentGenreDTO> => {
  const response = await apiClient.get(API_ENDPOINTS.INSTRUMENT_GENRES.BY_ID(id));
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<InstrumentGenreDTO[]> => {
  const response = await apiClient.post(API_ENDPOINTS.INSTRUMENT_GENRES.BY_IDS, ids);
  return response.data;
};

export const getByGenreId = async (genreId: string): Promise<InstrumentGenreDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.INSTRUMENT_GENRES.BY_GENRE(genreId));
  return response.data;
};

export const getByInstrumentId = async (instrumentId: string): Promise<InstrumentGenreDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.INSTRUMENT_GENRES.BY_INSTRUMENT(instrumentId));
  return response.data;
};

/**
 * IDs de los géneros-instrumento que combinan un género y un instrumento favoritos del usuario
 * y no están en 3 o más de sus listas (los de su nivel primero).
 */
export const getFavouriteCombinationIdsByUser = async (userId: string): Promise<string[]> => {
  const response = await apiClient.get(API_ENDPOINTS.INSTRUMENT_GENRES.FAVOURITE_COMBINATION_IDS_BY_USER(userId));
  return response.data;
};

export const createInstrumentGenre = async (instrumentGenre: InstrumentGenreDTO): Promise<InstrumentGenreDTO> => {
  const response = await apiClient.post(API_ENDPOINTS.INSTRUMENT_GENRES.BASE, instrumentGenre);
  return response.data;
};

export const updateInstrumentGenre = async (id: string, instrumentGenre: InstrumentGenreDTO): Promise<InstrumentGenreDTO> => {
  const response = await apiClient.put(API_ENDPOINTS.INSTRUMENT_GENRES.BY_ID(id), instrumentGenre);
  return response.data;
};

export const deleteInstrumentGenre = async (id: string): Promise<void> => {
  await apiClient.delete(API_ENDPOINTS.INSTRUMENT_GENRES.BY_ID(id));
};

