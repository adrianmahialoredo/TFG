import axios, { AxiosError } from 'axios';
import { useAuthStore } from '../stores/entities/auth.entities.store';

/**
 * Instancia de axios compartida por todos los services que llaman al backend.
 *
 * - Petición: añade `Authorization: Bearer <token>` si hay sesión. Todas las rutas del
 *   backend exigen JWT salvo `/api/auth/**` (ahí el token simplemente no existe todavía).
 * - Respuesta: un 401 con sesión abierta significa token caducado o inválido → se cierra
 *   la sesión y el AppNavigator vuelve al login.
 *
 * Los endpoints de `API_ENDPOINTS` son URLs absolutas, así que no hace falta `baseURL`.
 * Las llamadas a servicios externos (Cloudinary) NO deben usar esta instancia: enviarían el JWT.
 */
export const apiClient = axios.create();

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 && useAuthStore.getState().token) {
      useAuthStore.getState().clearAuth();
      console.log('[apiClient] Token expirado o inválido, sesión cerrada');
    }
    return Promise.reject(error);
  }
);
