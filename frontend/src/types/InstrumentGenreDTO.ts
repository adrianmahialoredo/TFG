/**
 * DTO para Género de Instrumento (InstrumentGenre)
 * Representa la relación entre un instrumento y un género
 */
export interface InstrumentGenreDTO {
  id: string;
  name?: string;
  imageUrl?: string;
  level?: number;
  instrumentId: string;
  genreId: string;
  lessonIds?: string[];
}

