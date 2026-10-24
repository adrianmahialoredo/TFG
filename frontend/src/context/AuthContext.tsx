import React, { createContext, useContext, useEffect, useRef, useState } from 'react';
import { AuthContextType } from '../types';
import {useAuthStore, useUserEntitiesStore} from '../stores/entities';
import * as authRepository from '../repositories/auth.repository';
import * as userRepository from '../repositories/user.repository';
import * as genreRepository from '../repositories/genre.repository';
import * as itemRepository from '../repositories/item.repository';

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
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const isHydrated = useAuthStore((state) => state.isHydrated);

  // Petición de login en curso (para deshabilitar el formulario)
  const [isLoading, setIsLoading] = useState(false);

  // 2. Buscamos al usuario en la store de entidades usando el ID anterior
  const user = useUserEntitiesStore(state =>
      currentUserId ? state.users[currentUserId] : null
  );

  // Evita recargar los datos globales en cada cambio de `user`.
  const initialDataLoadedRef = useRef(false);

  useEffect(() => {
    // Esperamos a que la AuthStore termine de leer del disco para no parpadear.
    if (!isHydrated) return;

    // Sin sesión (p.ej. tras un logout) el próximo login vuelve a cargar los datos globales.
    if (!token) initialDataLoadedRef.current = false;

    const initializeAuth = async () => {
      try {
        // Sesión persistida (hay token + ID) pero la RAM está vacía tras reiniciar:
        // restauramos el usuario desde el repositorio (RAM -> Local -> API).
        if (token && currentUserId && !user) {
          await userRepository
              .getById(currentUserId)
              .catch((e) => console.error('[AuthContext] Error restaurando usuario:', e));
        }

        // Cargamos los datos globales UNA sola vez por sesión. También cubre el login:
        // al guardarse el token este efecto vuelve a ejecutarse y los carga.
        if (token && !initialDataLoadedRef.current) {
          initialDataLoadedRef.current = true;
          await loadInitialData();
        }
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();
  }, [isHydrated, token, currentUserId, user]);

  /**
   * Función de login
   */
  const login = async (identifier: string, password: string): Promise<void> => {
    setIsLoading(true);
    try {
      // El estado se actualiza vía useAuthStore y el efecto carga los datos globales
      await authRepository.login(identifier, password);
    } catch (error) {
      console.warn('[AuthContext] Login rejected by backend');
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  return (
      <AuthContext.Provider
          value={{
            user: user || null, // Ahora 'user' viene de las entidades, no del aire
            token: token || null,
            isAuthenticated,
            isReady: isHydrated,
            isLoading,
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
   * Carga los datos globales al inicializar la app (cache-first, Disco -> RAM, con sync de fondo):
   *  - Géneros: se esperan.
   *  - Catálogo de items (resúmenes): en segundo plano, para no retrasar el arranque.
   */
  const loadInitialData = async () => {
    itemRepository.getCatalog()
      .catch((err) => console.error('[AuthContext] Error cargando el catálogo de items:', err));
    try {
      await genreRepository.getAll();
    } catch (err) {
      console.error('[AuthContext] Error cargando géneros:', err);
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

