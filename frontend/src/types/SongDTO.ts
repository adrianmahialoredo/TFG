import {ItemDTO, ItemType} from "./ItemDTO";

export interface SongDTO extends ItemDTO {
  type: ItemType.SONG;
  artist: string;
  /** Década de la canción (p.ej. "1990s"); puede faltar en cachés antiguas. */
  decade?: string | null;
  genreIds: string[];
  instrumentIds: string[];
  lessonIds: string[];
}
