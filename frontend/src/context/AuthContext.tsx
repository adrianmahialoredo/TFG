import React, { createContext, useContext, useEffect, useRef, useState } from 'react';
import { AuthContextType } from '../types';
import {useAuthStore, useUserEntitiesStore} from '../stores/entities';
import * as authRepository from '../repositories/auth.repository';
import * as userRepository from '../repositories/user.repository';
import * as genreService from '../services/genre.service';
import * as genreLocal from '../local/genre.local';
import { useGenreEntitiesStore } from '../stores/entities';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }
  return context;
};

// AuthContext.tsx
export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [loading, setLoading] = useState(true);

  // 1. Selectores ATÓMICOS del AuthStore (evita re-renders y warnings de getSnapshot)
  const currentUserId = useAuthStore((state) => state.currentUserId);
  const token = useAuthStore((state) => state.token);
  const isHydrated = useAuthStore((state) => state.isHydrated);

  // 2. Buscamos al usuario en la store de entidades usando el ID anterior
  const user = useUserEntitiesStore(state =>
      currentUserId ? state.users[currentUserId] : null
  );

  // Evita recargar los géneros en cada cambio de `user`.
  const genresLoadedRef = useRef(false);

  useEffect(() => {
    // Esperamos a que la AuthStore termine de leer del disco para no parpadear.
    if (!isHydrated) return;

    const initializeAuth = async () => {
      try {
        // Sesión persistida (hay token + ID) pero la RAM está vacía tras reiniciar:
        // restauramos el usuario desde el repositorio (RAM -> Local -> API).
        if (token && currentUserId && !user) {
          await userRepository
              .getById(currentUserId)
              .catch((e) => console.error('[AuthContext] Error restaurando usuario:', e));
        }

        // Cargamos los géneros UNA sola vez por sesión.
        if (token && !genresLoadedRef.current) {
          genresLoadedRef.current = true;
          await loadInitialGenres();
        }
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();
  }, [isHydrated, token, currentUserId, user]);

  return (
      <AuthContext.Provider
          value={{
            user: user || null, // Ahora 'user' viene de las entidades, no del aire
            token: token || null,
            login,
            logout,
            loading,
          }}
      >
        {children}
      </AuthContext.Provider>
  );
};

  /**
   * Cargar géneros al inicializar la app
   */
  const loadInitialGenres = async () => {
    try {
      // Intentar cargar desde local
      const localGenres = await genreLocal.getAll();

      if (localGenres && localGenres.length > 0) {
        // Si hay en local, usar esos
        useGenreEntitiesStore.getState().setGenres(localGenres);

        // Actualizar en background
        genreService.getAll()
          .then(fresh => {
            if (fresh && fresh.length > 0) {
              genreLocal.saveBatch(fresh);
              useGenreEntitiesStore.getState().setGenres(fresh);
            }
          })
          .catch(() => {});
      } else {
        // Si local está vacío, traer de la API
        const genres = await genreService.getAll();
        if (genres && genres.length > 0) {
          genreLocal.saveBatch(genres).catch(console.error);
          useGenreEntitiesStore.getState().setGenres(genres);
        }
      }
    } catch (err) {
      console.error('[AuthContext] Error cargando géneros:', err);
    }
  };

  /**
   * Función de login
   */
  const login = async (email: string, password: string): Promise<void> => {
    try {
      await authRepository.login(email, password);
      // El estado se actualiza automáticamente vía useAuthStore
      await loadInitialGenres();
    } catch (error) {
      console.error('[AuthContext] Login failed:', error);
      throw error;
    }
  };

  /**
   * Función de logout
   */
  const logout = async (): Promise<void> => {
    try {
      await authRepository.logout();
      // El estado se actualiza automáticamente vía useAuthStore
    } catch (error) {
      console.error('[AuthContext] Logout failed:', error);
      throw error;
    }
  };

