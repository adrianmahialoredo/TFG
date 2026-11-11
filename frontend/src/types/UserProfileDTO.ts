/**
 * DTO para Usuario con información de favoritos
 * Extensión de UserDTO con datos adicionales
 */
export enum UserRole {
  USER = 'USER',
  ADMIN = 'ADMIN',
}

export interface UserProfileDTO {
  id: string;
  email: string;
  username: string;
  name: string;
  password?: string; // No incluir en respuestas de GET
  profileImageUrl?: string;
  role: UserRole;
  difficultyLevel: number; // nivel del usuario, de 0 a 5 (User.difficultyLevel)
  favoriteGenreIds: string[];
  favoriteInstrumentIds: string[];
  itemListIds?: string[];
  createdAt?: string;
  updatedAt?: string;
}

