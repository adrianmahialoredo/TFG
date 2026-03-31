/**
 * Barrel export para todos los DTOs
 * Facilita las importaciones desde otros archivos
 * 
 * Uso: import { UserDTO, SongDTO, ChordDTO } from '@/types';
 */

export type { AuthContextType } from './AuthContextType';
export type { UserDTO } from './UserDTO';
export type { SongDTO } from './SongDTO';
export type { GenreDTO } from './GenreDTO';
export type { ChordDTO } from './ChordDTO';
export type { InstrumentDTO } from './InstrumentDTO';
export type { InstrumentGenreDTO } from './InstrumentGenreDTO';
export type { SongLessonDTO } from './SongLessonDTO';
export { DifficultyLevel } from './SongLessonDTO';
export type { TabSectionDTO } from './TabSectionDTO';
export type { ItemListDTO } from './ItemListDTO';
export type { ItemDTO, ItemType } from './ItemDTO';
export type { UserProfileDTO, UserRole } from './UserProfileDTO';
export type { AuthResponseDTO, LoginCredentialsDTO, RegisterDTO } from './AuthDTO';
export type { SpotifyRecommendationDTO, SpotifyUserDTO } from './SpotifyDTO';
export type { CarouselItemDTO } from './CarouselItemDTO';
export type { RootStackParamList } from './RootStackParamList';


