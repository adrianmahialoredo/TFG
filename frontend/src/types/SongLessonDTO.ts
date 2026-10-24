/**
 * Enum para los niveles de dificultad de una lección
 */
export enum DifficultyLevel {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  ORIGINAL = 'ORIGINAL',
}

/**
 * DTO para Lección de Canción (SongLesson)
 * Representa una lección de una canción con acordes, letras y tabs
 */
export interface SongLessonDTO {
  id: string;
  songId: string;
  instrumentId?: string;
  difficulty: DifficultyLevel;
  chordedLyrics: string; // formato "[chord] lyric line 1\n[chord] lyric line 2\n..."
  videoUrl?: string;
  chordIds: string[];
  tabSectionsIds?: string[]; // Mismo nombre que en el backend (SongLessonDTO.tabSectionsIds)
}

