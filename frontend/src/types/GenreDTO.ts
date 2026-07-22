import {ItemDTO, ItemType} from "./ItemDTO";

export interface GenreDTO extends ItemDTO {
    type: ItemType.GENRE;
    description: string;
    bannerUrl: string;
    welcomeIntroVideoId: string;
    welcomeSongVideoId: string;
    lessonIds: string[];
}