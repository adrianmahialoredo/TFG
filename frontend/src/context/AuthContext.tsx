// context/AuthContext.tsx
import { createContext, useContext, useState, useEffect } from 'react';
import { UserDTO } from '../types/UserDTO';
import { getStoredUser, storeUserData } from '../services/storageService';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { AuthContextType } from '../types/AuthContextType';
import { login as loginRequest } from '../services/authService';

const AuthContext = createContext<AuthContextType>({} as AuthContextType);

export const useAuth = () => useContext(AuthContext);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserDTO | null>(null);
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    getStoredUser().then(data => {
      if (data) {
        setUser(data.user);
        setToken(data.token);
      }
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
    <AuthContext.Provider value={{ user, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
