import React, { createContext, useContext, useState, useEffect } from 'react';
import { UserDTO } from '../types';
import { getStoredUser, storeUserData } from '../local/user.local';
import { storeGenres, getStoredGenres } from '../local';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { AuthContextType } from '../types';
import { login as loginRequest } from '../services/auth.service';
import { getAllGenres } from '../services';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }

  return context;
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserDTO | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true); 

  useEffect(() => {
    const initializeAuth = async () => {
      try {
        // Cargar usuario del storage
        const storedUserData = await getStoredUser();
        if (storedUserData) {
          setUser(storedUserData.user);
          setToken(storedUserData.token);
        }

        // Cargar géneros del storage primero
        const storedGenres = await getStoredGenres();
        
        // Si no hay géneros en storage, cargarlos de la API
        if (!storedGenres || storedGenres.length === 0) {
          try {
            const genres = await getAllGenres();
            await storeGenres(genres);
          } catch (err) {
            console.error('Error cargando géneros de la API:', err);
          }
        }
      } catch (err) {
        console.error('Error durante la inicialización:', err);
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();
  }, []);

  const login = async (email: string, password: string) => {
    const { token, user } = await loginRequest(email, password);
    await storeUserData(token, user);
    setUser(user);
    setToken(token);
  };

  const logout = async () => {
    await AsyncStorage.clear();
    setUser(null);
    setToken(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};
