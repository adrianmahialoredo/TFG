import {ItemDTO, ItemType} from "./ItemDTO";

export interface LessonDTO extends ItemDTO {
  type: ItemType.LESSON;
  belongsToGenre: boolean;
  genreId?: string;
  belongsToInstrumentGenre: boolean;
  instrumentGenreId?: string;
  indexInInstrumentGenre: number;
  sectionIds: string[];
}

