import { UserDTO } from "./UserDTO";

export interface AuthContextType {
  user: UserDTO | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  loading: boolean; 
}