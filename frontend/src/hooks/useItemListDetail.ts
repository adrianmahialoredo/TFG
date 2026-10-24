import { useState, useEffect, useMemo, useCallback } from 'react';
import { useItemEntitiesStore, useItemListEntitiesStore } from '../stores/entities';
import * as itemListRepository from '../repositories/itemList.repository';
import * as itemRepository from '../repositories/item.repository';
import { ItemListDTO, ItemSummaryDTO } from '../types';

/**
 * Nivel 6: Hook de hidratación del detalle de una lista (ListScreen).
 *
 * La lista y sus items se leen SIEMPRE de las Entity Stores (SSOT), así que
 * cualquier cambio (añadir/quitar items, editar la lista) se refleja sin recargar.
 */
export const useItemListDetail = (itemListId: string) => {
    const list = useItemListEntitiesStore((state) => state.itemLists[itemListId]);
    const allItems = useItemEntitiesStore((state) => state.items);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const load = useCallback(async () => {
        try {
            setLoading(true);
            await itemListRepository.getItemListWithItems(itemListId);
            setError(null);
        } catch (e) {
            setError("Error al cargar el detalle");
        } finally {
            setLoading(false);
        }
    }, [itemListId]);

    useEffect(() => {
        if (itemListId) load();
    }, [itemListId, load]);

    // Si cambian los itemIds de la lista (p.ej. se añade un item), traemos
    // los que falten en RAM (cache-first: RAM/Disco antes que API).
    const itemIds = list?.itemIds;
    useEffect(() => {
        if (!itemIds?.length) return;
        const missing = itemIds.filter((id) => !allItems[id]);
        if (missing.length > 0) itemRepository.getAllById(missing).catch(console.error);
    }, [itemIds, allItems]);

    const items = useMemo(
        () => (itemIds || []).map((id) => allItems[id]).filter(Boolean) as ItemSummaryDTO[],
        [itemIds, allItems]
    );

    const updateList = async (updates: Partial<ItemListDTO>) => {
        const updated = await itemListRepository.updateItemList(itemListId, updates);
        if (!updated) {
            throw new Error('No se pudo actualizar la lista');
        }
        return updated;
    };

    const updateImage = async (localUri: string) => {
        const updated = await itemListRepository.updateItemListImage(itemListId, localUri);
        if (!updated) {
            throw new Error('No se pudo actualizar la foto de la lista');
        }
        return updated;
    };

    /** Quita un item de la lista. La Store se actualiza con la respuesta del backend. */
    const removeItem = async (itemId: string) => {
        const updated = await itemListRepository.removeItemFromList(itemListId, itemId);
        if (!updated) {
            throw new Error('No se pudo quitar el item de la lista');
        }
        return updated;
    };

    /** Elimina la lista. Devuelve true si se eliminó. */
    const deleteList = useCallback(() => itemListRepository.deleteItemList(itemListId), [itemListId]);

    return { list: list || null, items, loading, error, refresh: load, updateList, updateImage, removeItem, deleteList };
};
