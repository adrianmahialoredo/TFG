import axios from 'axios';
import { API_ENDPOINTS } from '../config/apiConfig';
import { ItemListDTO } from '../types';

export const getAllItemLists = async (): Promise<ItemListDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.ITEM_LISTS.BASE);
  return response.data;
};

export const getItemListById = async (id: string): Promise<ItemListDTO> => {
  const response = await axios.get(API_ENDPOINTS.ITEM_LISTS.BY_ID(id));
  return response.data;
};

export const getItemListsByUser = async (userId: string): Promise<ItemListDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.ITEM_LISTS.BY_USER(userId));
  return response.data;
};

export const getItemListIdsByUser = async (userId: string): Promise<string[]> => {
  const response = await axios.get(API_ENDPOINTS.ITEM_LISTS.BY_USER(userId));
  // Convertir los IDs numéricos a strings si es necesario
  return response.data.map((id: number | string) => String(id));
};

export const getItemListsByIds = async (ids: string[]): Promise<ItemListDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.ITEM_LISTS.BY_IDS, {
    params: { ids },
  });
  return response.data;
};

export const createItemList = async (itemList: ItemListDTO): Promise<ItemListDTO> => {
  const response = await axios.post(API_ENDPOINTS.ITEM_LISTS.BASE, itemList);
  return response.data;
};

export const updateItemList = async (id: string, itemList: ItemListDTO): Promise<ItemListDTO> => {
  const response = await axios.put(API_ENDPOINTS.ITEM_LISTS.BY_ID(id), itemList);
  return response.data;
};

export const deleteItemList = async (id: string): Promise<void> => {
  await axios.delete(API_ENDPOINTS.ITEM_LISTS.BY_ID(id));
};

