import * as itemListService from '../services/itemList.service';
import * as itemListLocal from '../local/itemList.local';
import * as itemRepository from '../repositories/item.repository';
import { useItemListEntitiesStore } from "../stores/entities";
import {AnyItemDTO, ItemListDTO} from '../types';

export const getById = async (id: string): Promise<ItemListDTO | null> => {
    let itemList: ItemListDTO|null = await itemListLocal.getById(id);

    if (!itemList) {
        try {
            itemList = await itemListService.getById(id);
            if (itemList) {
                await itemListLocal.saveBatch([itemList]).catch(e => console.error("Local save failed", e));
            }
        } catch (error) {
            console.error(`Error fetching itemList with ID ${id}:`, error);
            return null;
        }
    }

    if (itemList) {
        useItemListEntitiesStore.getState().setItemLists([itemList]);
    }

    return itemList;
}

export const getAllById = async (ids: string[]): Promise<ItemListDTO[]> => {
    const { cached, missing } = await itemListLocal.getAllById(ids);

    let remoteItemLists: ItemListDTO[] = [];

    if (missing.length > 0) {
        try {
            remoteItemLists = await itemListService.getAllById(missing);
            if (remoteItemLists.length > 0) {
                itemListLocal.saveBatch(remoteItemLists).catch(console.error);
            }
        } catch (error) {
            console.error("[ItemListRepo] API Error", error);
        }
    }

    const all = [...cached, ...remoteItemLists];
    useItemListEntitiesStore.getState().setItemLists(all);

    const map = new Map(all.map(i => [i.id.toString(), i]));
    return ids.map(id => map.get(id)).filter((i): i is ItemListDTO => !!i);
}

export const getItemListWithItems = async (
    itemListId: string
): Promise<{ list: ItemListDTO | null, items: AnyItemDTO[] }> => {
    try {
        // 1. Obtener la cabecera de la lista
        let list = await itemListLocal.getById(itemListId);
        if (!list) {
            list = await itemListService.getById(itemListId);
            if (list) await itemListLocal.saveSingle(list);
        }

        if (!list) return { list: null, items: [] };

        // 2. Obtener los objetos reales usando el nuevo repo polimórfico
        // Esto buscará en Local -> Service (Batch) -> RAM
        const items = await itemRepository.getAllById(list.itemIds || []);

        return { list, items };
    } catch (error) {
        console.error("[ItemListRepo] Error en getItemListWithItems:", error);
        return { list: null, items: [] };
    }
};

export const getByUserId = async (userId: string): Promise<ItemListDTO[]> => {
    try {
        // 1. Intentar siempre primero el DISCO para velocidad
        const localLists = await itemListLocal.getByUserId(userId);

        if (localLists && localLists.length > 0) {
            // Actualizamos la RAM para que la UI reaccione, pero devolvemos local
            useItemListEntitiesStore.getState().setItemLists(localLists);

            // Opcional: Actualizar de fondo sin bloquear
            itemListService.getByUserId(userId).then(remote => {
                if (remote.length > 0) {
                    itemListLocal.saveBatchWithUserIndex(remote);
                    useItemListEntitiesStore.getState().setItemLists(remote);
                }
            }).catch(() => {});

            return localLists;
        }

        // 2. Si no hay nada en disco, vamos a la API
        const remoteLists = await itemListService.getByUserId(userId);
        if (remoteLists.length > 0) {
            useItemListEntitiesStore.getState().setItemLists(remoteLists);
            await itemListLocal.saveBatchWithUserIndex(remoteLists);
        }
        return remoteLists;

    } catch (error) {
        console.error("[ItemListRepo] Error en getByUserId:", error);
        return [];
    }
};

/**
 * Añade un item a una lista y sincroniza la lista actualizada (Hidratación Atómica:
 * RAM + Local) con la respuesta del backend antes de retornar.
 */
export const addItemToList = async (listId: string, itemId: string): Promise<ItemListDTO | null> => {
    try {
        const updated = await itemListService.addItemToList(listId, itemId);
        if (updated) {
            useItemListEntitiesStore.getState().setItemLists([updated]);
            await itemListLocal.saveSingle(updated).catch(console.error);
        }
        return updated;
    } catch (error) {
        console.error("[ItemListRepo] Error en addItemToList:", error);
        return null;
    }
};

/**
 * Quita un item de una lista y sincroniza la lista actualizada (RAM + Local).
 */
export const removeItemFromList = async (listId: string, itemId: string): Promise<ItemListDTO | null> => {
    try {
        const updated = await itemListService.removeItemFromList(listId, itemId);
        if (updated) {
            useItemListEntitiesStore.getState().setItemLists([updated]);
            await itemListLocal.saveSingle(updated).catch(console.error);
        }
        return updated;
    } catch (error) {
        console.error("[ItemListRepo] Error en removeItemFromList:", error);
        return null;
    }
};

