import { apiClient } from './apiClient';
import { API_ENDPOINTS } from '../config/apiConfig';
import { TuneDTO } from '../types';

/** Afinación tal y como la devuelve el backend: los IDs son números. */
type BackendTuneDTO = Omit<TuneDTO, 'id' | 'instrumentId'> & {
  id: string | number;
  instrumentId: string | number;
};

/** En la app los IDs son string: así `instrumentId` se compara directamente con las claves de las Stores. */
export const toAppTuneDTO = (tune: BackendTuneDTO): TuneDTO => ({
  ...tune,
  id: String(tune.id),
  instrumentId: String(tune.instrumentId),
});

export const getAll = async (): Promise<TuneDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.TUNES.BASE);
  return response.data.map(toAppTuneDTO);
};

export const getById = async (id: string): Promise<TuneDTO> => {
  const response = await apiClient.get(API_ENDPOINTS.TUNES.BY_ID(id));
  return toAppTuneDTO(response.data);
};

export const getAllById = async (ids: string[]): Promise<TuneDTO[]> => {
  const response = await apiClient.post(API_ENDPOINTS.TUNES.BY_IDS, ids);
  return response.data.map(toAppTuneDTO);
};

export const getByInstrumentId = async (instrumentId: string): Promise<TuneDTO[]> => {
  const response = await apiClient.get(API_ENDPOINTS.TUNES.BY_INSTRUMENT(instrumentId));
  return response.data.map(toAppTuneDTO);
};
