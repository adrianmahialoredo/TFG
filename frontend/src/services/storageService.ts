// src/services/storageService.ts
import AsyncStorage from '@react-native-async-storage/async-storage';
import { UserDTO } from '../types/UserDTO';
import { GenreDTO } from '../types/GenreDTO';

const TOKEN_KEY = 'token';
const USER_KEY = 'user';
const GENRES_KEY = 'genres';

/* User Storage Functions */  

export const storeUserData = async (token: string, user: UserDTO) => {
  await AsyncStorage.setItem(TOKEN_KEY, token);
  await AsyncStorage.setItem(USER_KEY, JSON.stringify(user));
};

export const getStoredUser = async (): Promise<{ token: string; user: UserDTO } | null> => {
  const token = await AsyncStorage.getItem(TOKEN_KEY);
  const userString = await AsyncStorage.getItem(USER_KEY);
  if (token && userString) {
    return { token, user: JSON.parse(userString) };
  }
  return null;
};

export const clearUserData = async () => {
  await AsyncStorage.removeItem(TOKEN_KEY);
  await AsyncStorage.removeItem(USER_KEY);
};

/* Genre Storage Functions */

export const storeGenres = async (genres: GenreDTO[]) => {
  await AsyncStorage.setItem(GENRES_KEY, JSON.stringify(genres));
};

export const getStoredGenres = async (): Promise<GenreDTO[] | null> => {
  const genresString = await AsyncStorage.getItem(GENRES_KEY);
  return genresString ? JSON.parse(genresString) : null;
};

export const getStoredGenresById = async (id: string): Promise<GenreDTO | null> => {
  const genres = await getStoredGenres();
  if (genres) {
    return genres.find(genre => genre.id === id) || null;
  }
  return null;
}

export const getStoredGenresByIdList = async (ids: string[]): Promise<GenreDTO[]> => {
  const genres = await getStoredGenres();
  if (genres) {
    return genres.filter(genre => ids.includes(genre.id));
  }
  return [];
}
