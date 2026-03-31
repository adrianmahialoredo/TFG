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

