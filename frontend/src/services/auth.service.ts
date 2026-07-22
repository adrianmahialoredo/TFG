import axios, { AxiosError } from 'axios';
import { UserDTO } from '../types/UserDTO';
import { API_ENDPOINTS } from '../config/apiConfig';
import { useAuthStore } from '../stores/entities/auth.entities.store';

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
  } catch (error: any) {
    console.error('[AuthService] Login error:', error.response?.data || error.message);
    throw error;
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
  } catch (error: any) {
    console.error('[AuthService] Register error:', error.response?.data || error.message);
    throw error;
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