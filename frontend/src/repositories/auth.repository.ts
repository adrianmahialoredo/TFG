import * as authService from '../services/auth.service';
import * as userLocal from '../local/user.local';
import * as itemListLocal from '../local/itemList.local';
import * as itemLocal from '../local/item.local';
import * as songLocal from '../local/song.local';
import { SPOTIFY_INDEX_KEY } from './song.repository';
import { useAuthStore, useUserEntitiesStore, useItemListEntitiesStore } from '../stores/entities';
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
    console.warn("[AuthRepo] Login rejected by backend");
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
    // 1. Limpiar RAM: sesión y datos privados del usuario
    useAuthStore.getState().clearAuth();
    useUserEntitiesStore.getState().clearUsers();
    useItemListEntitiesStore.getState().clearItemLists();

    // 2. LIMPIAR DISCO (Vital para la privacidad)
    // Borramos los datos del usuario, sus listas y los índices de feeds personalizados
    // (recientes, recomendaciones, Spotify…), que otro usuario no debe ver.
    await Promise.all([
      userLocal.clearAll(),
      itemListLocal.clearAll(),
      // El catálogo de items es público: se conserva.
      itemLocal.clearQueryIndexes(['items_catalog']),
      // Las canciones de Spotify van con DTO completo y su índice vive en el de canciones.
      songLocal.removeQueryIndexes([SPOTIFY_INDEX_KEY]),
    ]);

    // Las entidades de contenido público (canciones, lecciones, géneros…)
    // se conservan como caché compartida.
  } catch (error) {
    console.error("[AuthRepo] Logout failed:", error);
  }
};
