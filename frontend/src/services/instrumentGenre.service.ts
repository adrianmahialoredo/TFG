import axios from 'axios';
import { API_ENDPOINTS } from '../config/apiConfig';
import { InstrumentGenreDTO } from '../types/InstrumentGenreDTO';

export const getAllInstrumentGenres = async (): Promise<InstrumentGenreDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.INSTRUMENT_GENRES.BASE);
  return response.data;
};

export const getInstrumentGenreById = async (id: string): Promise<InstrumentGenreDTO> => {
  const response = await axios.get(API_ENDPOINTS.INSTRUMENT_GENRES.BY_ID(id));
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<InstrumentGenreDTO[]> => {
  // @TODO: Requiere endpoint POST /instrument-genres/by-ids
  const response = await axios.post(`${API_ENDPOINTS.INSTRUMENT_GENRES.BASE}/by-ids`, ids);
  return response.data;
};

export const getByGenreId = async (genreId: string): Promise<InstrumentGenreDTO[]> => {
  // @TODO: Requiere endpoint GET /instrument-genres/by-genre/{genreId}
  const response = await axios.get(`${API_ENDPOINTS.INSTRUMENT_GENRES.BASE}/by-genre/${genreId}`);
  return response.data;
};

export const getByInstrumentId = async (instrumentId: string): Promise<InstrumentGenreDTO[]> => {
  // @TODO: Requiere endpoint GET /instrument-genres/by-instrument/{instrumentId}
  const response = await axios.get(`${API_ENDPOINTS.INSTRUMENT_GENRES.BASE}/by-instrument/${instrumentId}`);
  return response.data;
};

export const createInstrumentGenre = async (instrumentGenre: InstrumentGenreDTO): Promise<InstrumentGenreDTO> => {
  const response = await axios.post(API_ENDPOINTS.INSTRUMENT_GENRES.BASE, instrumentGenre);
  return response.data;
};

export const updateInstrumentGenre = async (id: string, instrumentGenre: InstrumentGenreDTO): Promise<InstrumentGenreDTO> => {
  const response = await axios.put(API_ENDPOINTS.INSTRUMENT_GENRES.BY_ID(id), instrumentGenre);
  return response.data;
};

export const deleteInstrumentGenre = async (id: string): Promise<void> => {
  await axios.delete(API_ENDPOINTS.INSTRUMENT_GENRES.BY_ID(id));
};

