import { useState, useEffect } from 'react';
import * as itemListRepository from '../repositories/itemList.repository';
import { ItemListDTO, AnyItemDTO } from '../types';

export const useItemListDetail = (itemListId: string) => {
    const [list, setList] = useState<ItemListDTO | null>(null);
    const [items, setItems] = useState<AnyItemDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const load = async () => {
        try {
            setLoading(true);
            const res = await itemListRepository.getItemListWithItems(itemListId);
            setList(res.list);
            setItems(res.items);
        } catch (e) {
            setError("Error al cargar el detalle");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (itemListId) load();
    }, [itemListId]);

    return { list, items, loading, error, refresh: load };
};