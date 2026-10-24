// hooks/useUserLists.ts
import { useEffect, useMemo, useState } from "react";
import {useItemListEntitiesStore} from "../stores/entities";
import * as itemListRepository from "../repositories/itemList.repository";
import { CreateItemListDTO } from "../types";

export const useItemLists = (userId: string | number |undefined) => {
    const [loading, setLoading] = useState(false);
    const [creating, setCreating] = useState(false);

    // Selector estable: evitamos crear arrays nuevos dentro del snapshot de Zustand.
    const itemListsMap = useItemListEntitiesStore((state) => state.itemLists);

    // Derivamos en memo para mantener referencias estables entre renders.
    const lists = useMemo(() => {
        if (!userId) return [];
        const normalizedUserId = userId.toString();
        return Object.values(itemListsMap).filter((list) => list.userId === normalizedUserId);
    }, [itemListsMap, userId]);

    useEffect(() => {
        if (!userId) return;
        const load = async () => {
            setLoading(true);
            try {
                await itemListRepository.getByUserId(userId.toString());
            } catch (error) {
                console.error('[useItemLists] Error cargando listas:', error);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [userId]);

    const createList = async (
        payload: Omit<CreateItemListDTO, 'userId'>
    ) => {
        if (!userId) return null;

        setCreating(true);
        try {
            return await itemListRepository.createItemList({
                ...payload,
                userId: userId.toString(),
            });
        } finally {
            setCreating(false);
        }
    };

    return { lists, loading, creating, createList };
};