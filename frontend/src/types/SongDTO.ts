export interface SongDTO {
  id: string;
  title: string;
  artist: string;
  imageUrl: string;
  genreIds: string[];
  instrumentIds: string[];
  lessonIds: string[];
}
