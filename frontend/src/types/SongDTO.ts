import {ItemDTO, ItemType} from "./ItemDTO";

export interface SongDTO extends ItemDTO {
  type: ItemType.SONG;
  artist: string;
  genreIds: string[];
  instrumentIds: string[];
  lessonIds: string[];
}
