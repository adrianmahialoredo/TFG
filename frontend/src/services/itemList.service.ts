import axios from 'axios';
import { API_ENDPOINTS } from '../config/apiConfig';
import { CreateItemListDTO, ItemListDTO } from '../types';

type ItemListBackendDTO = {
  id: string | number;
  userId: string | number;
  name: string;
  imageUrl?: string;
  description?: string;
  itemIds?: Array<string | number>;
};

const toAppItemListDTO = (backend: ItemListBackendDTO): ItemListDTO => ({
  id: String(backend.id),
  userId: String(backend.userId),
  name: backend.name,
  imageUrl: backend.imageUrl,
  description: backend.description,
  itemIds: (backend.itemIds ?? []).map((id) => String(id)),
});

const toBackendCreatePayload = (payload: CreateItemListDTO) => ({
  userId: Number(payload.userId),
  name: payload.name,
  imageUrl: payload.imageUrl,
  itemIds: (payload.itemIds ?? []).map((id) => Number(id)),
});

export const getAll = async (): Promise<ItemListDTO[]> => {
  const response = await axios.get(API_ENDPOINTS.ITEM_LISTS.BASE);
  return (response.data as ItemListBackendDTO[]).map(toAppItemListDTO);
};

export const getById = async (id: string): Promise<ItemListDTO> => {
  const response = await axios.get(API_ENDPOINTS.ITEM_LISTS.BY_ID(id));
  return toAppItemListDTO(response.data as ItemListBackendDTO);
};

export const getUserIds = async (userId: string): Promise<string[]> => {
  const response = await axios.get(API_ENDPOINTS.ITEM_LISTS.IDS_BY_USER(userId));
  // Convertir los IDs numéricos a strings si es necesario
  return response.data.map((id: number | string) => String(id));
};

export const getAllById = async (ids: string[]): Promise<ItemListDTO[]> => {
  const response = await axios.post(API_ENDPOINTS.ITEM_LISTS.BY_IDS, ids);
  return (response.data as ItemListBackendDTO[]).map(toAppItemListDTO);
};

export const createItemList = async (itemList: CreateItemListDTO): Promise<ItemListDTO> => {
  const response = await axios.post(API_ENDPOINTS.ITEM_LISTS.BASE, toBackendCreatePayload(itemList));
  return toAppItemListDTO(response.data as ItemListBackendDTO);
};

export const updateItemList = async (id: string, itemList: ItemListDTO): Promise<ItemListDTO> => {
  const response = await axios.put(API_ENDPOINTS.ITEM_LISTS.BY_ID(id), itemList);
  return toAppItemListDTO(response.data as ItemListBackendDTO);
};

export const deleteItemList = async (id: string): Promise<void> => {
  await axios.delete(API_ENDPOINTS.ITEM_LISTS.BY_ID(id));
};

export const addItemToList = async (listId: string, itemId: string): Promise<ItemListDTO> => {
  const response = await axios.post(API_ENDPOINTS.ITEM_LISTS.ITEM(listId, itemId));
  return toAppItemListDTO(response.data as ItemListBackendDTO);
};

export const removeItemFromList = async (listId: string, itemId: string): Promise<ItemListDTO> => {
  const response = await axios.delete(API_ENDPOINTS.ITEM_LISTS.ITEM(listId, itemId));
  return toAppItemListDTO(response.data as ItemListBackendDTO);
};

