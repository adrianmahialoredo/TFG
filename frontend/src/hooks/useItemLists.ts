// hooks/useUserLists.ts
import { useEffect, useMemo, useState } from "react";
import {useItemListEntitiesStore} from "../stores/entities";
import * as itemListRepository from "../repositories/itemList.repository";

export const useItemLists = (userId: string | number |undefined) => {
    const [loading, setLoading] = useState(false);

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
            await itemListRepository.getByUserId(userId);
            setLoading(false);
        };
        load();
    }, [userId]);

    return { lists, loading };
};