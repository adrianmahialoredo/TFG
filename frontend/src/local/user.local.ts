import { UserDTO } from '../types';
import { storeData, getData, removeData } from './storage.local';
import AsyncStorage from "@react-native-async-storage/async-storage";

const USER_CACHE_PREFIX = '@user:';
const INDEX_CACHE_PREFIX = '@user_index:';

/**
 * Guarda un usuario individual
 */
export const saveSingle = async (user: UserDTO): Promise<void> => {
  const key = `${USER_CACHE_PREFIX}${user.id}`;
  return storeData(key, user);
};

/**
 * Obtiene un usuario por ID
 */
export const getById = async (userId: string): Promise<UserDTO | null> => {
  const key = `${USER_CACHE_PREFIX}${userId}`;
  return getData<UserDTO>(key);
};

/**
 * Guarda múltiples usuarios de un golpe
 */
export const saveBatch = async (users: UserDTO[]): Promise<void> => {
  if (users.length === 0) return;

  const pairs: [string, string][] = users.map(user => [
    `${USER_CACHE_PREFIX}${user.id}`,
    JSON.stringify(user)
  ]);

  try {
    await AsyncStorage.multiSet(pairs);
  } catch (e) {
    console.error("[UserLocal] Error en saveBatch:", e);
  }
};

/**
 * Obtiene múltiples usuarios por IDs
 */
export const getAllById = async (ids: string[]): Promise<{ cached: UserDTO[]; missing: string[] }> => {
  if (ids.length === 0) return { cached: [], missing: [] };

  const keys = ids.map(id => `${USER_CACHE_PREFIX}${id}`);
  const pairs = await AsyncStorage.multiGet(keys);

  const cached: UserDTO[] = [];
  const missing: string[] = [];

  pairs.forEach(([key, value], index) => {
    if (value) {
      try {
        cached.push(JSON.parse(value));
      } catch (e) {
        missing.push(ids[index]);
      }
    } else {
      missing.push(ids[index]);
    }
  });

  return { cached, missing };
};

/**
 * Métodos de índice con prefijo consistente
 */
export const saveQueryIndex = async (key: string, ids: string[]): Promise<void> => {
  try {
    await AsyncStorage.setItem(`${INDEX_CACHE_PREFIX}${key}`, JSON.stringify(ids));
  } catch (e) {
    console.error("[UserLocal] Error guardando índice:", e);
  }
};

export const getQueryIndex = async (key: string): Promise<string[] | null> => {
  try {
    const data = await AsyncStorage.getItem(`${INDEX_CACHE_PREFIX}${key}`);
    return data ? JSON.parse(data) : null;
  } catch (e) {
    console.error("[UserLocal] Error leyendo índice:", e);
    return null;
  }
};

/**
 * Limpia todos los datos de usuarios e índices del disco
 * Se usa principalmente en el Logout para no dejar rastro.
 */
export const clearAll = async (): Promise<void> => {
  try {
    // 1. Obtenemos todas las llaves que existen en el dispositivo
    const allKeys = await AsyncStorage.getAllKeys();

    // 2. Filtramos solo las que pertenecen a este módulo (User y UserIndex)
    const userKeys = allKeys.filter(key =>
        key.startsWith(USER_CACHE_PREFIX) ||
        key.startsWith(INDEX_CACHE_PREFIX)
    );

    // 3. Borramos ese lote de llaves de un solo golpe
    if (userKeys.length > 0) {
      await AsyncStorage.multiRemove(userKeys);
    }

    console.log(`[UserLocal] Limpieza completada. ${userKeys.length} llaves eliminadas.`);
  } catch (e) {
    console.error("[UserLocal] Error en clearAll:", e);
  }
};

