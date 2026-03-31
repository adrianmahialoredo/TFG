import { ChordDTO } from "../types";
import { getData, storeData } from "./storage.local";

const CHORDS_KEY = 'chords';

/* Guarda todos los acordes */
export const storeChords = async (chords: ChordDTO[]): Promise<void> => {
  return storeData(CHORDS_KEY, chords);
};

/* Obtiene todos los acordes */
export const getStoredChords = async (): Promise<ChordDTO[] | null> => {
  return getData<ChordDTO[]>(CHORDS_KEY);
};

/* Obtiene un acorde por su ID */
export const getStoredChordById = async (id: string): Promise<ChordDTO | null> => {
  const chords = await getStoredChords();
  return chords?.find((chord) => chord.id === id) || null;
};

/* Obtiene varios acordes a partir de una lista de IDs */
export const getStoredChordsByIdList = async (ids: string[]): Promise<ChordDTO[]> => {
  const chords = await getStoredChords();
  return chords?.filter((chord) => ids.includes(chord.id)) || [];
};

