export interface UserDTO {
  id: string | number;
  email: string;
  name: string;
  username: string;
  password?: string; // Opcional, no se devuelve desde el backend
  profileImageUrl?: string;
  favouriteGenresIds?: Array<string | number>;
  favouriteInstrumentsIds?: Array<string | number>;
  spotifyConnected?: boolean; // Lo calcula el backend; se ignora al enviarlo
  difficultyLevel?: number; // Nivel musical autopercibido (0 a 5, como las estrellas); se edita en el perfil
}