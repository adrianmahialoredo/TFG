import axios from 'axios';
import { API_ENDPOINTS } from '../config/apiConfig';
import { ChordDTO } from '../types';

export const getAll = async (): Promise<ChordDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.CHORDS.BASE);
  return response.data;
};

export const getById = async (id: string): Promise<ChordDTO> => {
  const response = await axios.get(API_ENDPOINTS.CHORDS.BY_ID(id));
  return response.data;
};

export const getAllById = async (ids: string[]): Promise<ChordDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.CHORDS.BY_IDS, {
    params: { ids },
  });
  return response.data;
};

export const getIdsBySongLesson = async (songLessonId: string): Promise<string[]> => {
  const response = await axios.get(API_ENDPOINTS.CHORDS.BY_SONG_LESSON(songLessonId));
  // Convertir los IDs numéricos a strings si es necesario
  return response.data.map((id: number | string) => String(id));
};

export const createChord = async (chord: ChordDTO): Promise<ChordDTO> => {
  const response = await axios.post(API_ENDPOINTS.CHORDS.BASE, chord);
  return response.data;
};

export const updateChord = async (id: string, chord: ChordDTO): Promise<ChordDTO> => {
  const response = await axios.put(API_ENDPOINTS.CHORDS.BY_ID(id), chord);
  return response.data;
};

export const deleteChord = async (id: string): Promise<void> => {
  await axios.delete(API_ENDPOINTS.CHORDS.BY_ID(id));
};

