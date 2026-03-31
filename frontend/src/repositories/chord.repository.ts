import * as chordService from '../services/chord.service';
import * as chordStorage from '../local/chord.local';
import { ChordDTO } from '../types';

/**
 * Repositorio de Chord
 * Maneja la lógica de cache + backend fallback para acordes
 */

/**
 * Obtiene los acordes por sus IDs
 * Cache first, backend fallback
 * @param chordIds Array de IDs de acordes (pueden ser strings o números)
 * @returns Array de ChordDTO
 */
export const getChordsByIds = async (chordIds: (string | number)[]): Promise<ChordDTO[]> => {
  if (!chordIds || chordIds.length === 0) {
    return [];
  }

  // Convertir todos los IDs a strings
  const normalizedIds = chordIds.map(id => String(id));

  try {
    // Intentar obtener del storage
    const cachedChords = await chordStorage.getStoredChordsByIdList(normalizedIds);
    
    // Identificar cuáles faltan en el storage
    const cachedIds = cachedChords.map(c => c.id);
    const missingIds = normalizedIds.filter(id => !cachedIds.includes(id));

    // Si faltan acordes, obtener del backend
    if (missingIds.length > 0) {
      const backendChords = await chordService.getChordsByIds(missingIds);
      
      // Guardar en storage para futuras consultas
      if (backendChords && backendChords.length > 0) {
        await chordStorage.storeChords([...cachedChords, ...backendChords]);
        console.log(`[Chord] Obtenidos ${missingIds.length} acordes del backend`);
        return [...cachedChords, ...backendChords];
      }
    }

    console.log(`[Chord Cache] Obtenidos ${cachedChords.length} acordes del storage`);
    return cachedChords;
  } catch (error) {
    console.error('Error obteniendo chords:', error);
    return [];
  }
};

/**
 * Obtiene un acorde por su ID
 * Cache first, backend fallback
 * @param chordId El ID del acorde
 * @returns ChordDTO o null
 */
export const getChordById = async (chordId: string): Promise<ChordDTO | null> => {
  try {
    // Intentar obtener del storage
    const cachedChord = await chordStorage.getStoredChordById(chordId);
    
    if (cachedChord) {
      console.log(`[Chord Cache] Obtenido acorde ${chordId} del storage`);
      return cachedChord;
    }

    // Si no está en storage, obtener del backend
    const chord = await chordService.getChordById(chordId);
    
    if (chord) {
      // Guardar en storage
      await chordStorage.storeChords([chord]);
      console.log(`[Chord Backend] Obtenido acorde ${chordId} del backend`);
    }

    return chord || null;
  } catch (error) {
    console.error('Error obteniendo chord:', error);
    return null;
  }
};

