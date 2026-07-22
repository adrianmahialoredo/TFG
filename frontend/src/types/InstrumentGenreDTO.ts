import {ItemDTO, ItemType} from "./ItemDTO";

/**
 * DTO para Género de Instrumento (InstrumentGenreScreen)
 * Representa la relación entre un instrumento y un género
 */
export interface InstrumentGenreDTO extends ItemDTO {
  type: ItemType.INSTRUMENT_GENRE;
  instrumentId: string;
  genreId: string;
  lessonIds?: string[];
}

