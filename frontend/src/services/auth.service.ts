import axios from 'axios';
import { apiClient } from './apiClient';
import { UserDTO } from '../types/UserDTO';
import { API_ENDPOINTS } from '../config/apiConfig';
import { MIN_FAVOURITE_GENRES, MIN_FAVOURITE_INSTRUMENTS } from '../constants/Preferences';
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
  favouriteGenresIds: `Elige al menos ${MIN_FAVOURITE_GENRES} géneros favoritos`,
  favouriteInstrumentsIds: `Elige al menos ${MIN_FAVOURITE_INSTRUMENTS} instrumentos`,
};

/** Errores de negocio del registro que devuelve el backend (IllegalArgumentException → 400). */
const REGISTER_BACKEND_MESSAGES: Record<string, string> = {
  'Email already registered': 'Ya existe una cuenta con ese email',
  'Username already taken': 'Ese nombre de usuario ya está en uso',
  'Verification code is required': 'Introduce el código que te hemos enviado',
  'Verification code not requested': 'Pide un código de verificación primero',
  'Invalid verification code': 'El código no es correcto',
  'Verification code expired': 'El código ha caducado, pide uno nuevo',
  'Too many wrong verification codes': 'Demasiados intentos fallidos, pide un código nuevo',
};

/**
 * En el registro no se oculta nada: "Usuario, email o contraseña incorrectos" solo tiene sentido en el
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
  // Un 401/403 en el registro no es un fallo de credenciales (no hay credenciales que comprobar):
  // no debe acabar en el mensaje del login.
  if (axios.isAxiosError(error) && (error.response?.status === 401 || error.response?.status === 403)) {
    return new AuthServiceError('No se pudo completar el registro', error.response.status);
  }
  return toAuthServiceError(error, 'No se pudo completar el registro');
};

const toAuthServiceError = (error: unknown, fallbackMessage: string): AuthServiceError => {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status;
    const payload = (error.response?.data || {}) as AuthErrorPayload;
    const fieldError = pickFieldError(payload.errors);

    // 401 = credenciales incorrectas: siempre el mensaje genérico, sin el texto del backend
    // (no debe revelar si el usuario existe ni salir en inglés).
    if (status === 401) {
      return new AuthServiceError('Usuario, email o contraseña incorrectos', status, payload.errors);
    }

    if (status === 400) {
      const backendMessage = payload.message?.toLowerCase() || '';
      const shouldHideValidationDetail =
        backendMessage.includes('validation') || Boolean(payload.errors?.password);

      return new AuthServiceError(
        shouldHideValidationDetail
          ? 'Usuario, email o contraseña incorrectos'
          : fieldError || payload.message || 'Usuario, email o contraseña incorrectos',
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

/**
 * Login: Autentica un usuario con su email o su nombre de usuario y la contraseña
 * (el backend busca primero por email y después por usuario).
 */
export const login = async (
  identifier: string,
  password: string
): Promise<{ token: string; user: UserDTO }> => {
  try {
    const response = await apiClient.post(API_ENDPOINTS.AUTH.LOGIN, {
      identifier: identifier.trim(),
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
    throw toAuthServiceError(error, 'No se pudo iniciar sesión');
  }
};

/**
 * Pide al backend que envíe un código de verificación al email de un registro que aún no existe.
 */
export const requestVerificationCode = async (email: string): Promise<void> => {
  try {
    await apiClient.post(API_ENDPOINTS.AUTH.VERIFICATION_CODE, { email });
  } catch (error: unknown) {
    if (axios.isAxiosError(error) && error.response?.status === 429) {
      throw new AuthServiceError('Espera un minuto antes de pedir otro código', 429);
    }
    if (axios.isAxiosError(error) && error.response && error.response.status >= 500) {
      throw new AuthServiceError('No se pudo enviar el email de verificación. Inténtalo más tarde', error.response.status);
    }
    throw toRegisterServiceError(error);
  }
};

/** Mensajes de la recuperación de contraseña: los del código son los mismos que en el registro. */
const toPasswordResetError = (error: unknown): AuthServiceError => {
  if (axios.isAxiosError(error) && error.response?.status === 429) {
    return new AuthServiceError('Espera un minuto antes de pedir otro código', 429);
  }
  if (axios.isAxiosError(error) && error.response?.status === 400) {
    const payload = (error.response.data || {}) as AuthErrorPayload;
    const field = payload.errors ? Object.keys(payload.errors)[0] : undefined;
    const message =
      (field === 'newPassword' && REGISTER_FIELD_MESSAGES.password) ||
      (field && REGISTER_FIELD_MESSAGES[field]) ||
      (payload.message && REGISTER_BACKEND_MESSAGES[payload.message]) ||
      'No se pudo cambiar la contraseña';
    return new AuthServiceError(message, 400, payload.errors);
  }
  if (axios.isAxiosError(error) && !error.response) {
    return new AuthServiceError('No se pudo conectar con el servidor. Revisa tu red.');
  }
  return new AuthServiceError(
    'No se pudo cambiar la contraseña. Inténtalo más tarde',
    axios.isAxiosError(error) ? error.response?.status : undefined
  );
};

/**
 * Olvidé mi contraseña, paso 1: pide un código para la cuenta de ese email.
 * El backend responde igual exista o no la cuenta (no revela qué emails están registrados).
 */
export const requestPasswordResetCode = async (email: string): Promise<void> => {
  try {
    await apiClient.post(API_ENDPOINTS.AUTH.PASSWORD_RESET_CODE, { email });
  } catch (error: unknown) {
    throw toPasswordResetError(error);
  }
};

/** Olvidé mi contraseña, paso 2: comprueba el código (sin gastarlo) antes de pedir la contraseña nueva. */
export const verifyPasswordResetCode = async (email: string, code: string): Promise<void> => {
  try {
    await apiClient.post(API_ENDPOINTS.AUTH.PASSWORD_RESET_VERIFY, { email, code });
  } catch (error: unknown) {
    throw toPasswordResetError(error);
  }
};

/** Olvidé mi contraseña, paso 3: guarda la contraseña nueva con el código ya comprobado. */
export const resetPassword = async (email: string, code: string, newPassword: string): Promise<void> => {
  try {
    await apiClient.post(API_ENDPOINTS.AUTH.PASSWORD_RESET, { email, code, newPassword });
  } catch (error: unknown) {
    throw toPasswordResetError(error);
  }
};

/**
 * Register: Registra un nuevo usuario
 * El servidor retorna token automáticamente (auto-login)
 */
export const register = async (
  user: UserDTO,
  verificationCode: string
): Promise<{ token: string; user: UserDTO }> => {
  try {
    const response = await apiClient.post(API_ENDPOINTS.AUTH.REGISTER, {
      email: user.email,
      username: user.username,
      name: user.name,
      password: user.password,
      favouriteGenresIds: user.favouriteGenresIds ?? [],
      favouriteInstrumentsIds: user.favouriteInstrumentsIds ?? [],
      verificationCode,
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