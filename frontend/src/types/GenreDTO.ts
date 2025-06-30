export interface GenreDTO {
    id: string;
    name: string
    description: string;
    bannerUrl: string;
    imageUrl: string;
    welcomeIntroVideoId: string;
    welcomeSongVideoId: string;
    songIds: string[];
    lessonIds: string[];
}