import * as itemListService from '../services/itemList.service';
import * as itemListLocal from '../local/itemList.local';
import { uploadProfileImageToCloudinary } from '../services/cloudinary.service';
import * as itemRepository from '../repositories/item.repository';
import { useItemListEntitiesStore } from "../stores/entities";
import { CreateItemListDTO, ItemListDTO } from '../types';
import { createEntityCache } from '../utils/entityCache.utils';

/** Núcleo de caché de la entidad: RAM -> Disco -> API + Stale-While-Revalidate. */
const cache = createEntityCache<ItemListDTO>({
    name: 'ItemListRepo',
    getRam: () => useItemListEntitiesStore.getState().itemLists,
    setRam: (entities) => useItemListEntitiesStore.getState().setItemLists(entities),
    local: { ...itemListLocal, saveBatch: itemListLocal.saveBatchWithUserIndex },
    fetchByIds: itemListService.getAllById,
});

/** Toda escritura pasa por aquí: RAM + Disco + markFresh (invalida revalidaciones en vuelo). */
const hydrateItemLists = async (itemLists: ItemListDTO[]) => {
    if (itemLists.length === 0) return;
    useItemListEntitiesStore.getState().setItemLists(itemLists);
    cache.markFresh(itemLists.map(l => l.id.toString()));
    await itemListLocal.saveBatchWithUserIndex(itemLists).catch(console.error);
};

export const getById = async (id: string): Promise<ItemListDTO | null> => {
    // 1. RAM
    const inRam = useItemListEntitiesStore.getState().itemLists[id];
    if (inRam) {
        cache.revalidate([id]);
        return inRam;
    }

    // 2. DISCO
    let itemList: ItemListDTO | null = await itemListLocal.getById(id);
    if (itemList) cache.revalidate([id]);

    // 3. API
    if (!itemList) {
        try {
            itemList = await itemListService.getById(id);
            cache.markFresh([id]);
            if (itemList) await hydrateItemLists([itemList]);
        } catch (error) {
            console.error(`Error fetching itemList with ID ${id}:`, error);
            return null;
        }
    }

    if (itemList) useItemListEntitiesStore.getState().setItemLists([itemList]);

    return itemList;
}

export const getAllById = cache.getAllById;

/**
 * Carga una lista y sus items (cabecera + entidades) en la RAM.
 * Retorna los IDs de los items en el orden de la lista; la UI los resuelve
 * contra las Entity Stores.
 */
export const getItemListWithItems = async (itemListId: string): Promise<string[]> => {
    try {
        const list = await getById(itemListId);
        const itemIds = list?.itemIds || [];
        await itemRepository.getAllById(itemIds);
        return itemIds;
    } catch (error) {
        console.error("[ItemListRepo] Error en getItemListWithItems:", error);
        return [];
    }
};

/**
 * Obtiene las listas de un usuario. Retorna IDs (hidratando la RAM y el Local).
 * Cache-first con sincronización de fondo.
 */
export const getByUserId = async (userId: string): Promise<string[]> => {
    try {
        // 1. Cache-first: DISCO
        const localLists = await itemListLocal.getByUserId(userId);

        if (localLists && localLists.length > 0) {
            useItemListEntitiesStore.getState().setItemLists(localLists);

            // 2. Sync de fondo para mantener RAM/Disco frescos
            itemListService.getUserIds(userId).then(async (remoteIds) => {
                if (remoteIds.length > 0) {
                    const remoteLists = await itemListService.getAllById(remoteIds);
                    await hydrateItemLists(remoteLists);
                }
            }).catch(() => {});

            return localLists.map(l => l.id.toString());
        }

        // 3. Si no hay nada en local: API (ids -> entidades completas)
        const remoteIds = await itemListService.getUserIds(userId);
        if (remoteIds.length === 0) return [];

        const remoteLists = await itemListService.getAllById(remoteIds);
        await hydrateItemLists(remoteLists);
        return remoteLists.map(l => l.id.toString());

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
        if (updated) await hydrateItemLists([updated]);
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
        if (updated) await hydrateItemLists([updated]);
        return updated;
    } catch (error) {
        console.error("[ItemListRepo] Error en removeItemFromList:", error);
        return null;
    }
};

/**
 * Crea una lista nueva y garantiza hidratación atómica (RAM + Disco) antes de devolver.
 */
export const createItemList = async (payload: CreateItemListDTO): Promise<ItemListDTO | null> => {
    try {
        const created = await itemListService.createItemList(payload);
        await hydrateItemLists([created]);
        return created;
    } catch (error) {
        console.error("[ItemListRepo] Error en createItemList:", error);
        return null;
    }
};

/**
 * Actualiza una lista existente y sincroniza RAM + Disco antes de retornar.
 */
export const updateItemList = async (
    listId: string,
    updates: Partial<ItemListDTO>
): Promise<ItemListDTO | null> => {
    try {
        const current = await getById(listId);
        if (!current) return null;

        const payload: ItemListDTO = {
            ...current,
            ...updates,
            id: current.id,
            userId: current.userId,
            itemIds: updates.itemIds ?? current.itemIds ?? [],
        };

        const updated = await itemListService.updateItemList(listId, payload);
        if (updated) await hydrateItemLists([updated]);
        return updated;
    } catch (error) {
        console.error('[ItemListRepo] Error en updateItemList:', error);
        return null;
    }
};

/**
 * Sube una imagen de lista a Cloudinary y actualiza la lista con la URL resultante.
 */
export const updateItemListImage = async (listId: string, localUri: string): Promise<ItemListDTO | null> => {
    const secureUrl = await uploadProfileImageToCloudinary(localUri);
    return updateItemList(listId, { imageUrl: secureUrl });
};
