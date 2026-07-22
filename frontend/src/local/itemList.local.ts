import { ItemListDTO } from "../types";
import { storeData, getData } from "./storage.local";
import AsyncStorage from "@react-native-async-storage/async-storage";

const ITEM_LIST_CACHE_PREFIX = '@itemList:';
const INDEX_CACHE_PREFIX = '@itemList_index:';
const USER_INDEX_PREFIX = '@itemList_user:';

export const saveSingle = async (itemList: ItemListDTO): Promise<void> => {
  const key = `${ITEM_LIST_CACHE_PREFIX}${itemList.id}`;
  return storeData(key, itemList);
};

export const getById = async (id: string): Promise<ItemListDTO | null> => {
  const key = `${ITEM_LIST_CACHE_PREFIX}${id}`;
  return getData<ItemListDTO>(key);
};

export const saveBatch = async (itemLists: ItemListDTO[]): Promise<void> => {
  if (itemLists.length === 0) return;

  const pairs: [string, string][] = itemLists.map(list => [
    `${ITEM_LIST_CACHE_PREFIX}${list.id}`,
    JSON.stringify(list)
  ]);

  try {
    await AsyncStorage.multiSet(pairs);
  } catch (e) {
    console.error("[ItemListLocal] Error en saveBatch:", e);
  }
};

export const getAllById = async (ids: string[]): Promise<{ cached: ItemListDTO[]; missing: string[] }> => {
  if (ids.length === 0) return { cached: [], missing: [] };

  const keys = ids.map(id => `${ITEM_LIST_CACHE_PREFIX}${id}`);
  const pairs = await AsyncStorage.multiGet(keys);

  const cached: ItemListDTO[] = [];
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

export const saveQueryIndex = async (key: string, ids: string[]): Promise<void> => {
  try {
    await AsyncStorage.setItem(`${INDEX_CACHE_PREFIX}${key}`, JSON.stringify(ids));
  } catch (e) {
    console.error("[ItemListLocal] Error guardando índice:", e);
  }
};

export const getQueryIndex = async (key: string): Promise<string[] | null> => {
  try {
    const data = await AsyncStorage.getItem(`${INDEX_CACHE_PREFIX}${key}`);
    return data ? JSON.parse(data) : null;
  } catch (e) {
    console.error("[ItemListLocal] Error leyendo índice:", e);
    return null;
  }
};

/**
 * Obtiene todas las listas de un usuario específico
 */
export const getByUserId = async (userId: string): Promise<ItemListDTO[]> => {
  try {
    // 1. Buscamos el índice: un array de IDs que pertenecen a este usuario
    const indexKey = `${USER_INDEX_PREFIX}${userId}`;
    const listIds = await getData<string[]>(indexKey);

    if (!listIds || listIds.length === 0) return [];

    // 2. Usamos tu método getAllById para recuperar los objetos completos
    const { cached } = await getAllById(listIds);
    return cached;
  } catch (e) {
    console.error("[ItemListLocal] Error en getByUserId:", e);
    return [];
  }
};

/**
 * Sobrescribimos saveBatch para que también actualice el índice por usuario
 */
export const saveBatchWithUserIndex = async (itemLists: ItemListDTO[]): Promise<void> => {
  if (itemLists.length === 0) return;

  // 1. Guardamos las listas normalmente
  await saveBatch(itemLists);

  // 2. Agrupamos IDs por usuario para actualizar índices
  const userGroups: Record<string, string[]> = {};
  itemLists.forEach(list => {
    if (!userGroups[list.userId]) userGroups[list.userId] = [];
    userGroups[list.userId].push(list.id.toString());
  });

  // 3. Actualizamos cada índice de usuario
  try {
    const updatePromises = Object.entries(userGroups).map(async ([userId, newIds]) => {
      const indexKey = `${USER_INDEX_PREFIX}${userId}`;
      const existingIds = await getData<string[]>(indexKey) || [];

      // Combinamos sin duplicados
      const updatedIds = Array.from(new Set([...existingIds, ...newIds]));
      await storeData(indexKey, updatedIds);
    });

    await Promise.all(updatePromises);
  } catch (e) {
    console.error("[ItemListLocal] Error actualizando índices de usuario:", e);
  }
};

