import { UserDTO } from "./UserDTO";

export interface AuthContextType {
  user: UserDTO | null;
  token: string | null;
  isAuthenticated: boolean;
  /** La AuthStore ya ha leído la sesión guardada en disco. */
  isReady: boolean;
  /** Hay una petición de login en curso. */
  isLoading: boolean;
  /** `identifier`: email o nombre de usuario. */
  login: (identifier: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  /** Arranque de la app: restaurando la sesión y cargando los datos globales. */
  loading: boolean;
}
