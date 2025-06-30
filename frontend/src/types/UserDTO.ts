export interface UserDTO {
  id?: string;
  email: string;
  name: string;
  username: string;
  password: string;
  profileImageUrl?: string;
  favouriteGenresIds: string[];
}