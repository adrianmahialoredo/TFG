import type { SongDTO } from './SongDTO';
import type { LessonDTO } from './LessonDTO';
import type { GenreDTO } from './GenreDTO';
import type { InstrumentGenreDTO } from './InstrumentGenreDTO';

export type { AuthContextType } from './AuthContextType';
export type { UserDTO } from './UserDTO';
export type { SongDTO } from './SongDTO';
export type { GenreDTO } from './GenreDTO';
export type { ChordDTO } from './ChordDTO';
export type { InstrumentDTO } from './InstrumentDTO';
export type { InstrumentGenreDTO } from './InstrumentGenreDTO';
export type { SongLessonDTO } from './SongLessonDTO';
export type { LessonDTO } from './LessonDTO';
export type { TabSectionDTO } from './TabSectionDTO';
export type { LessonSectionDTO } from './LessonSectionDTO';
export type { TuneDTO } from './TuneDTO';
export type { ItemListDTO, CreateItemListDTO } from './ItemListDTO';
export type { ItemDTO } from './ItemDTO';
export type { ItemSummaryDTO } from './ItemSummaryDTO';
export type { UserProfileDTO, UserRole } from './UserProfileDTO';
export type { AuthResponseDTO, LoginCredentialsDTO, RegisterDTO } from './AuthDTO';
export type { SpotifyRecommendationDTO, SpotifyUserDTO } from './SpotifyDTO';
export type { CarouselItemDTO } from './CarouselItemDTO';
export type { RootStackParamList } from './RootStackParamList';

export type AnyItemDTO =
    | SongDTO
    | LessonDTO
    | GenreDTO
    | InstrumentGenreDTO;

export { DifficultyLevel } from './SongLessonDTO';
export { ItemType } from './ItemDTO';