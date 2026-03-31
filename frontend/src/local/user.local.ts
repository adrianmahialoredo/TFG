import AsyncStorage from "@react-native-async-storage/async-storage";
import { UserDTO } from "../types";

const TOKEN_KEY = 'token';
const USER_KEY = 'user';

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

