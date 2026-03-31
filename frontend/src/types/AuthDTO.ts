/**
 * DTO para respuesta de autenticación
 * Se retorna después de login o registro exitoso
 */
export interface AuthResponseDTO {
  token: string;
  user: {
    id: string;
    email: string;
    username: string;
    name: string;
    profileImageUrl?: string;
  };
}

/**
 * DTO para credenciales de login
 */
export interface LoginCredentialsDTO {
  email: string;
  password: string;
}

/**
 * DTO para registro de nuevo usuario
 */
export interface RegisterDTO {
  email: string;
  username: string;
  password: string;
  name: string;
}

