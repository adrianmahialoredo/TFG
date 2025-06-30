// context/AuthContext.tsx
import { createContext, useContext, useState, useEffect } from 'react';
import { UserDTO } from '../types/UserDTO';
import { getStoredUser, storeGenres, storeUserData } from '../services/storageService';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { AuthContextType } from '../types/AuthContextType';
import { login as loginRequest } from '../services/authService';
import { getAllGenres } from '../services/genreService';

const AuthContext = createContext<AuthContextType>({} as AuthContextType);

export const useAuth = () => useContext(AuthContext);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserDTO | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true); 

  useEffect(() => {
    getStoredUser().then(async data => {
      if (data) {
        setUser(data.user);
        setToken(data.token);
      }

      try {
        const genres = await getAllGenres();     // llamada a /genres
        await storeGenres(genres);               // se guardan en storage
      } catch (err) {
        console.error('Error cargando géneros:', err);
      }

      setLoading(false);
    });
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
