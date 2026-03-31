import * as itemListService from '../services/itemList.service';
import * as itemListStorage from '../local/itemList.local';
import { ItemListDTO, ItemDTO } from '../types';
import axios from 'axios';

const BASE_URL = 'http://192.168.0.22:8080';

/**
 * Repositorio de ItemList
 * Maneja la lógica de cache + backend fallback para listas de items
 */

/**
 * Obtiene una lista de items completa con sus items
 * Cache first, backend fallback
 * @param itemListId El ID de la lista de items
 * @returns Objeto con la lista y sus items
 */
export const getItemListWithItems = async (itemListId: string): Promise<{
  list: ItemListDTO | null;
  items: ItemDTO[];
}> => {
  try {
    // 1. Intentar obtener de cache
    let list = await itemListStorage.getStoredItemListById(itemListId);
    
    if (!list) {
      // 2. Si no está en cache, obtener del backend
      list = await itemListService.getItemListById(itemListId);
      
      if (list) {
        // 3. Guardar en cache para futuras consultas
        await itemListStorage.storeItemLists([list]);
        console.log(`[ItemList Backend] Obtenida lista ${itemListId} del backend`);
      } else {
        console.warn(`No se encontró lista con ID ${itemListId}`);
        return { list: null, items: [] };
      }
    } else {
      console.log(`[ItemList Cache] Obtenida lista ${itemListId} del cache`);
    }

    // 4. Obtener los items de la lista
    if (list.itemIds && list.itemIds.length > 0) {
      const items = await getItemsByIds(list.itemIds);
      return { list, items };
    }

    return { list, items: [] };
  } catch (error) {
    console.error('Error obteniendo lista con items:', error);
    return { list: null, items: [] };
  }
};

/**
 * Obtiene items por sus IDs
 * @param itemIds Array de IDs de items
 * @returns Array de ItemDTO
 */
export const getItemsByIds = async (itemIds: string[]): Promise<ItemDTO[]> => {
  if (!itemIds || itemIds.length === 0) {
    return [];
  }

  try {
    const items: ItemDTO[] = [];
    
    for (const itemId of itemIds) {
      try {
        const response = await axios.get(`${BASE_URL}/items/${itemId}`);
        if (response.data) {
          items.push(response.data);
        }
      } catch (error) {
        console.warn(`Error obteniendo item ${itemId}:`, error);
      }
    }

    return items;
  } catch (error) {
    console.error('Error obteniendo items:', error);
    return [];
  }
};

/**
 * Obtiene los IDs de listas de un usuario
 * Cache first, backend fallback
 * @param userId El ID del usuario
 * @returns Array de IDs de listas
 */
export const getItemListIdsByUser = async (userId: string): Promise<string[]> => {
  try {
    // Intentar obtener de cache
    let listIds = await itemListStorage.getStoredItemListIdsByUser(userId);
    
    if (listIds) {
      console.log(`[ItemList IDs Cache] Obtenidos ${listIds.length} IDs del cache para usuario ${userId}`);
      return listIds;
    }

    // Si no están en cache, obtener del backend
    listIds = await itemListService.getItemListIdsByUser(userId);
    
    if (listIds && listIds.length > 0) {
      // Guardar en cache
      await itemListStorage.storeItemListIdsByUser(userId, listIds);
      console.log(`[ItemList IDs Backend] Obtenidos ${listIds.length} IDs del backend para usuario ${userId}`);
    }

    return listIds || [];
  } catch (error) {
    console.error('Error obteniendo IDs de listas:', error);
    return [];
  }
};

/**
 * Obtiene múltiples listas por IDs
 * Cache first, backend fallback
 * @param listIds Array de IDs de listas
 * @returns Array de ItemListDTO
 */
export const getItemListsByIds = async (listIds: string[]): Promise<ItemListDTO[]> => {
  if (!listIds || listIds.length === 0) {
    return [];
  }

  try {
    // Intentar obtener del cache
    const cachedLists = await itemListStorage.getStoredItemListsByIdList(listIds);
    
    // Identificar cuáles faltan en el cache
    const cachedIds = cachedLists.map(l => l.id);
    const missingIds = listIds.filter(id => !cachedIds.includes(id));

    // Si faltan listas, obtener del backend
    if (missingIds.length > 0) {
      const backendLists = await itemListService.getItemListsByIds(missingIds);
      
      // Guardar en cache
      if (backendLists && backendLists.length > 0) {
        await itemListStorage.storeItemLists([...cachedLists, ...backendLists]);
        console.log(`[ItemList] Obtenidas ${missingIds.length} listas del backend`);
        return [...cachedLists, ...backendLists];
      }
    }

    console.log(`[ItemList Cache] Obtenidas ${cachedLists.length} listas del cache`);
    return cachedLists;
  } catch (error) {
    console.error('Error obteniendo listas:', error);
    return [];
  }
};

