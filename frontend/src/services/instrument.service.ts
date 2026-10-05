import axios from 'axios';
import { API_ENDPOINTS } from '../config/apiConfig';
import { InstrumentDTO } from '../types';

export const getAllInstruments = async (): Promise<InstrumentDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.INSTRUMENTS.BASE);
  return response.data;
};

export const getInstrumentById = async (id: string): Promise<InstrumentDTO> => {
  const response = await axios.get(API_ENDPOINTS.INSTRUMENTS.BY_ID(id));
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<InstrumentDTO[]> => {
  const response = await axios.post(API_ENDPOINTS.INSTRUMENTS.BY_IDS, ids);
  return response.data;
};

export const getByGenreId = async (genreId: string): Promise<InstrumentDTO[]> => {
  // @TODO: Requiere endpoint GET /api/instruments/by-genre/{genreId} en el backend
  const response = await axios.get(API_ENDPOINTS.INSTRUMENTS.BY_GENRE(genreId));
  return response.data;
};

export const createInstrument = async (instrument: InstrumentDTO): Promise<InstrumentDTO> => {
  const response = await axios.post(API_ENDPOINTS.INSTRUMENTS.BASE, instrument);
  return response.data;
};

export const updateInstrument = async (id: string, instrument: InstrumentDTO): Promise<InstrumentDTO> => {
  const response = await axios.put(API_ENDPOINTS.INSTRUMENTS.BY_ID(id), instrument);
  return response.data;
};

export const deleteInstrument = async (id: string): Promise<void> => {
  await axios.delete(API_ENDPOINTS.INSTRUMENTS.BY_ID(id));
};

