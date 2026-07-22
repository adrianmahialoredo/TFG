export interface UserDTO {
  id: string | number;
  email: string;
  name: string;
  username: string;
  password?: string; // Opcional, no se devuelve desde el backend
  profileImageUrl?: string;
  favouriteGenresIds?: Array<string | number>;
  favouriteInstrumentsIds?: Array<string | number>;
}