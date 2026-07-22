import * as authService from '../services/auth.service';
import * as userLocal from '../local/user.local';
import {useAuthStore, useUserEntitiesStore} from '../stores/entities';
import { UserDTO } from '../types';

export const login = async (email: string, password: string): Promise<{ token: string; user: UserDTO }> => {
  try {
    const { token, user } = await authService.login(email, password);
    const userId = user.id.toString();

    // 1. Hidratación de RAM: El objeto completo va AQUÍ
    useUserEntitiesStore.getState().setUsers([user]);

    // 2. Persistencia en Disco: El objeto completo va AQUÍ
    await userLocal.saveBatch([user]).catch(e => console.error("Local save failed", e));

    // 3. Autenticación: AQUÍ SOLO EL ID
    useAuthStore.getState().setAuth(token, userId);

    return { token, user };
  } catch (error) {
    console.error("[AuthRepo] Login failed:", error);
    throw error;
  }
};

export const register = async (formData: any): Promise<{ token: string; user: UserDTO }> => {
  try {
    // 1. Limpiamos el objeto: mandamos solo lo que el service espera
    // (quitamos confirmPassword antes de enviar al Nivel 1)
    const { confirmPassword, ...registerData } = formData;

    const { user: registeredUser, token } = await authService.register(registerData);
    const userId = registeredUser.id.toString();

    // 2. Hidratación RAM & Disco (Atómica)
    useUserEntitiesStore.getState().setUsers([registeredUser]);
    await userLocal.saveBatch([registeredUser]);

    // 3. Activar Sesión
    if (token) {
      useAuthStore.getState().setAuth(token, userId);
    }

    return { token: token || '', user: registeredUser };
  } catch (error) {
    throw error;
  }
};

export const logout = async (): Promise<void> => {
  try {
    // 1. Limpiar RAM
    useAuthStore.getState().clearAuth();
    useUserEntitiesStore.getState().clearUsers();

    // 2. LIMPIAR DISCO (Vital para la privacidad)
    // Debemos borrar el token y los datos del usuario del storage local
    await userLocal.clearAll();

    // No borramos TODAS las entidades (como canciones globales),
    // pero sí la sesión del usuario.
  } catch (error) {
    console.error("[AuthRepo] Logout failed:", error);
  }
};