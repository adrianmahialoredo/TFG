import { ItemListDTO } from "../types";
import { getData, storeData } from "./storage.local";

const ITEM_LISTS_KEY = 'itemLists';
const ITEM_LIST_IDS_BY_USER_PREFIX = 'itemListIds_by_user_';

/* Guarda todas las listas de items */
export const storeItemLists = async (itemLists: ItemListDTO[]): Promise<void> => {
  return storeData(ITEM_LISTS_KEY, itemLists);
};

/* Obtiene todas las listas de items */
export const getStoredItemLists = async (): Promise<ItemListDTO[] | null> => {
  return getData<ItemListDTO[]>(ITEM_LISTS_KEY);
};

/* Obtiene una lista de items por su ID */
export const getStoredItemListById = async (id: string): Promise<ItemListDTO | null> => {
  const itemLists = await getStoredItemLists();
  return itemLists?.find((list) => list.id === id) || null;
};

/* Obtiene todas las listas de un usuario */
export const getStoredItemListsByUser = async (userId: string): Promise<ItemListDTO[]> => {
  const itemLists = await getStoredItemLists();
  return itemLists?.filter((list) => list.userId === userId) || [];
};

/* Obtiene varias listas a partir de una lista de IDs */
export const getStoredItemListsByIdList = async (ids: string[]): Promise<ItemListDTO[]> => {
  const itemLists = await getStoredItemLists();
  return itemLists?.filter((list) => ids.includes(list.id)) || [];
};

/* Almacena los IDs de listas de un usuario en la cache */
export const storeItemListIdsByUser = async (userId: string, listIds: string[]): Promise<void> => {
  const key = `${ITEM_LIST_IDS_BY_USER_PREFIX}${userId}`;
  return storeData(key, listIds);
};

/* Obtiene los IDs de listas de un usuario de la cache */
export const getStoredItemListIdsByUser = async (userId: string): Promise<string[] | null> => {
  const key = `${ITEM_LIST_IDS_BY_USER_PREFIX}${userId}`;
  return getData<string[]>(key);
};

