/**
 * Enum para los tipos de items en el sistema
 */
export enum ItemType {
  SONG = 'Song',
  LESSON = 'Lesson',
  GENRE = 'Genre',
  INSTRUMENT_GENRE = 'InstrumentGenre',
}

/**
 * DTO para Item (Item)
 * Clase base que representa un item genérico en el sistema
 * Puede ser una canción, lección, género, o género-instrumento
 */
export interface ItemDTO {
  id: string;
  title: string;
  imageUrl: string;
  level?: number; // entre 1 y 5 estrellas
  createdAt?: string;
  /** Fecha ISO del último acceso al item. Alimenta la sección "Recientes". */
  lasttimeaccess?: string;
  type: ItemType;
}

