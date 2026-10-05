import axios, { AxiosError } from 'axios';
import { UserDTO } from '../types/UserDTO';
import { API_ENDPOINTS } from '../config/apiConfig';
import { useAuthStore } from '../stores/entities/auth.entities.store';

type AuthErrorPayload = {
  message?: string;
  errors?: Record<string, string>;
};

export class AuthServiceError extends Error {
  status?: number;
  fieldErrors?: Record<string, string>;

  constructor(message: string, status?: number, fieldErrors?: Record<string, string>) {
    super(message);
    this.name = 'AuthServiceError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

const pickFieldError = (errors?: Record<string, string>) => {
  if (!errors) return null;
  const firstKey = Object.keys(errors)[0];
  return firstKey ? errors[firstKey] : null;
};

/** Mensajes en español para los errores de validación del registro, por campo del backend. */
const REGISTER_FIELD_MESSAGES: Record<string, string> = {
  email: 'El email no es válido',
  username: 'El usuario debe tener entre 3 y 20 caracteres',
  name: 'El nombre debe tener entre 2 y 100 caracteres',
  password: 'La contraseña necesita al menos 8 caracteres, una mayúscula, un número y un símbolo, y no puede tener espacios',
};

/** Errores de negocio del registro que devuelve el backend (IllegalArgumentException → 400). */
const REGISTER_BACKEND_MESSAGES: Record<string, string> = {
  'Email already registered': 'Ya existe una cuenta con ese email',
  'Username already taken': 'Ese nombre de usuario ya está en uso',
};

/**
 * En el registro no se oculta nada: "Email o contraseña incorrectos" solo tiene sentido en el
 * login, y en el registro escondía el motivo real (p.ej. una regla de validación del backend).
 */
const toRegisterServiceError = (error: unknown): AuthServiceError => {
  if (axios.isAxiosError(error) && error.response?.status === 400) {
    const payload = (error.response.data || {}) as AuthErrorPayload;
    const field = payload.errors ? Object.keys(payload.errors)[0] : undefined;
    const message =
      (field && (REGISTER_FIELD_MESSAGES[field] ?? payload.errors?.[field])) ||
      (payload.message && REGISTER_BACKEND_MESSAGES[payload.message]) ||
      payload.message ||
      'No se pudo completar el registro';
    return new AuthServiceError(message, 400, payload.errors);
  }
  return toAuthServiceError(error, 'No se pudo completar el registro');
};

const toAuthServiceError = (error: unknown, fallbackMessage: string): AuthServiceError => {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status;
    const payload = (error.response?.data || {}) as AuthErrorPayload;
    const fieldError = pickFieldError(payload.errors);

    if (status === 400 || status === 401) {
      const backendMessage = payload.message?.toLowerCase() || '';
      const shouldHideValidationDetail =
        backendMessage.includes('validation') || Boolean(payload.errors?.password);

      return new AuthServiceError(
        shouldHideValidationDetail
          ? 'Email o contrasena incorrectos'
          : fieldError || payload.message || 'Email o contrasena incorrectos',
        status,
        payload.errors
      );
    }

    if (!error.response) {
      return new AuthServiceError('No se pudo conectar con el servidor. Revisa tu red.', status);
    }

    return new AuthServiceError(payload.message || fallbackMessage, status, payload.errors);
  }

  if (error instanceof Error) {
    return new AuthServiceError(error.message || fallbackMessage);
  }

  return new AuthServiceError(fallbackMessage);
};

// Crear instancia de axios con configuración base
const apiClient = axios.create({
  baseURL: API_ENDPOINTS.AUTH.LOGIN.split('/api/auth/login')[0],
});

/**
 * Interceptor de solicitud: Agregar token JWT a cada petición
 */
apiClient.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Interceptor de respuesta: Manejar errores 401 (token expirado)
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Token expirado o inválido: limpiar auth y redirigir al login
      useAuthStore.getState().clearAuth();
      console.warn('[AuthService] Token expirado, sesión cerrada');
    }
    return Promise.reject(error);
  }
);

/**
 * Login: Autentica un usuario con email y contraseña
 */
export const login = async (
  email: string,
  password: string
): Promise<{ token: string; user: UserDTO }> => {
  try {
    const response = await apiClient.post(API_ENDPOINTS.AUTH.LOGIN, {
      email,
      password,
    });

    // Validar respuesta
    if (!response.data?.token || !response.data?.user) {
      throw new Error('Respuesta inválida del servidor: falta token o usuario');
    }

    return {
      token: response.data.token,
      user: response.data.user,
    };
  } catch (error: unknown) {
    throw toAuthServiceError(error, 'No se pudo iniciar sesion');
  }
};

/**
 * Register: Registra un nuevo usuario
 * El servidor retorna token automáticamente (auto-login)
 */
export const register = async (user: UserDTO): Promise<{ token: string; user: UserDTO }> => {
  try {
    const response = await apiClient.post(API_ENDPOINTS.AUTH.REGISTER, {
      email: user.email,
      username: user.username,
      name: user.name,
      password: user.password,
      favouriteGenresIds: user.favouriteGenresIds ?? [],
      favouriteInstrumentsIds: user.favouriteInstrumentsIds ?? [],
    });

    // El backend debe retornar token y usuario
    // Si no lo hace, se retorna sin token (el usuario debe hacer login manual)
    if (response.data?.token && response.data?.user) {
      return {
        token: response.data.token,
        user: response.data.user,
      };
    } else if (response.data?.user) {
      // Registro exitoso pero sin token automático
      return {
        token: '',
        user: response.data.user,
      };
    }

    throw new Error('Respuesta inválida del servidor');
  } catch (error: unknown) {
    throw toRegisterServiceError(error);
  }
};

/**
 * Logout: Limpia el token del cliente
 * (El servidor puede mantener logs, pero el cliente pierde acceso)
 */
export const logout = async (): Promise<void> => {
  try {
    // Si el servidor requiere una petición de logout, descomentar:
    // await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT);
    
    // Limpiar token del cliente
    useAuthStore.getState().clearAuth();
  } catch (error: any) {
    console.error('[AuthService] Logout error:', error.message);
    throw error;
  }
};