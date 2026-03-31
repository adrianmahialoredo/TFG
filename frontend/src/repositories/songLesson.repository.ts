import * as songLessonService from '../services/songLesson.service';
import * as tabSectionService from '../services/tabSection.service';
import * as songLessonStorage from '../local/songLesson.local';
import * as tabSectionStorage from '../local/tabSection.local';
import * as chordRepository from './chord.repository';
import { SongLessonDTO, ChordDTO, TabSectionDTO } from '../types';

/**
 * Repositorio de SongLesson
 * Maneja la lógica de cache + backend fallback para lecciones de canción
 */

/**
 * Obtiene las lecciones de canción para una canción específica
 * Cache first, backend fallback
 * @param songId El ID de la canción
 * @returns Array de SongLessonDTO
 */
export const getSongLessonsBySong = async (songId: string): Promise<SongLessonDTO[]> => {
  try {
    // Intentar obtener del storage
    let songLessons = await songLessonStorage.getStoredSongLessonsBySong(songId);
    
    if (songLessons && songLessons.length > 0) {
      console.log(`[SongLesson Cache] Obtenidas ${songLessons.length} lecciones del storage para song ${songId}`);
      return songLessons;
    }

    // Si no están en el storage, obtener del backend
    songLessons = await songLessonService.getSongLessonsBySong(songId);
    
    if (songLessons && songLessons.length > 0) {
      // Guardar en storage para futuras consultas
      await songLessonStorage.storeSongLessons(songLessons);
      console.log(`[SongLesson Backend] Obtenidas ${songLessons.length} lecciones del backend para song ${songId}`);
    }
    
    return songLessons || [];
  } catch (error) {
    console.error('Error obteniendo song lessons:', error);
    return [];
  }
};

/**
 * Obtiene las secciones de tab para una lección de canción
 * Cache first, backend fallback
 * @param songLessonId El ID de la lección de canción
 * @returns Array de TabSectionDTO
 */
export const getTabSectionsByLesson = async (songLessonId: string): Promise<TabSectionDTO[]> => {
  try {
    // Intentar obtener del storage
    let tabSections = await tabSectionStorage.getStoredTabSectionsByLesson(songLessonId);
    
    if (tabSections && tabSections.length > 0) {
      console.log(`[TabSection Cache] Obtenidas ${tabSections.length} tab sections del storage para lesson ${songLessonId}`);
      return tabSections;
    }

    // Si no están en el storage, obtener del backend
    tabSections = await tabSectionService.getTabSectionsByLesson(songLessonId);
    
    if (tabSections && tabSections.length > 0) {
      // Guardar en storage para futuras consultas
      await tabSectionStorage.storeTabSections(tabSections);
      console.log(`[TabSection Backend] Obtenidas ${tabSections.length} tab sections del backend para lesson ${songLessonId}`);
    }
    
    return tabSections || [];
  } catch (error) {
    console.error('Error obteniendo tab sections:', error);
    return [];
  }
};

/**
 * Selecciona la lección de canción predeterminada
 * Preferencia: EASY > MEDIUM > ORIGINAL
 * @param songLessons Array de SongLessonDTO
 * @returns La SongLessonDTO seleccionada o undefined si no hay lecciones
 */
export const selectDefaultSongLesson = (songLessons: SongLessonDTO[]): SongLessonDTO | undefined => {
  if (!songLessons || songLessons.length === 0) {
    return undefined;
  }

  // Preferencia: EASY > MEDIUM > ORIGINAL
  const easyLesson = songLessons.find(l => l.difficulty === 'EASY');
  if (easyLesson) return easyLesson;

  const mediumLesson = songLessons.find(l => l.difficulty === 'MEDIUM');
  if (mediumLesson) return mediumLesson;

  const originalLesson = songLessons.find(l => l.difficulty === 'ORIGINAL');
  if (originalLesson) return originalLesson;

  return songLessons[0];
};

/**
 * Obtiene todos los datos necesarios para la pantalla de canción
 * Combina song lessons, acordes y tab sections con fallback a storage/backend
 * @param songId El ID de la canción
 * @returns Objeto con songLessons, defaultSongLesson, chords y tabSections
 */
export const getSongScreenData = async (songId: string) => {
  try {
    // 1. Obtener song lessons
    const songLessons = await getSongLessonsBySong(songId);
    
    if (!songLessons || songLessons.length === 0) {
      console.warn(`No hay lecciones para la canción ${songId}`);
      return {
        songLessons: [],
        defaultSongLesson: undefined,
        chords: {},
        tabSections: {},
      };
    }

    // 2. Seleccionar la lección por defecto
    const defaultSongLesson = selectDefaultSongLesson(songLessons);

    if (!defaultSongLesson) {
      return {
        songLessons,
        defaultSongLesson: undefined,
        chords: {},
        tabSections: {},
      };
    }

    // 3. Obtener acordes para la lección por defecto
    const defaultChords = await chordRepository.getChordsByIds(defaultSongLesson.chordIds);

    // 4. Obtener tab sections para la lección por defecto
    const defaultTabSections = await getTabSectionsByLesson(defaultSongLesson.id);

    // 5. Crear un mapa para acceso rápido por ID de lección
    const chordsMap: Record<string, ChordDTO[]> = {
      [defaultSongLesson.id]: defaultChords,
    };

    const tabSectionsMap: Record<string, TabSectionDTO[]> = {
      [defaultSongLesson.id]: defaultTabSections,
    };

    return {
      songLessons,
      defaultSongLesson,
      chords: chordsMap,
      tabSections: tabSectionsMap,
    };
  } catch (error) {
    console.error('Error obteniendo datos de la pantalla de canción:', error);
    return {
      songLessons: [],
      defaultSongLesson: undefined,
      chords: {},
      tabSections: {},
    };
  }
};



