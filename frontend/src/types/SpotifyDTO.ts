/**
 * DTO para recomendación de Spotify
 * Representa una canción recomendada desde Spotify
 */
export interface SpotifyRecommendationDTO {
  id: string;
  name: string;
  artist?: string;
  imageUrl?: string;
  spotifyUrl?: string;
  previewUrl?: string;
}

/**
 * DTO para usuario de Spotify
 * Información del usuario conectado a Spotify
 */
export interface SpotifyUserDTO {
  id: string;
  spotifyId: string;
  email: string;
  displayName: string;
  profileImageUrl?: string;
  accessToken: string;
  refreshToken?: string;
}

