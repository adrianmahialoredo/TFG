import { useCallback, useEffect, useMemo, useState } from 'react';
import { useAuthStore } from '../stores/entities';
import * as itemListRepository from '../repositories/itemList.repository';
import { useItemLists } from './useItemLists';

/**
 * Nivel 6: añadir un item a una de las listas del usuario actual (modal "Añadir a una lista"
 * de los resultados de búsqueda).
 *
 * Las listas se leen de la ItemListEntitiesStore, así que tras añadir la lista ya contiene el
 * item (y el resto de la app se entera: ver `useUserTasteKey`).
 */
export const useAddToList = (itemId: string | null) => {
  const currentUserId = useAuthStore((state) => state.currentUserId);
  const { lists, loading } = useItemLists(currentUserId ?? undefined);

  // Lista a la que se está añadiendo ahora mismo (para su spinner)
  const [addingListId, setAddingListId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  // El error de un intento anterior no se arrastra a otro item
  useEffect(() => {
    setError(null);
  }, [itemId]);

  const sortedLists = useMemo(
    () => [...lists].sort((a, b) => a.name.localeCompare(b.name)),
    [lists]
  );

  const contains = useCallback(
    (listId: string) => !!itemId && sortedLists.some(
      (list) => list.id.toString() === listId && (list.itemIds ?? []).map(String).includes(itemId)
    ),
    [sortedLists, itemId]
  );

  /** Añade el item a la lista. Devuelve true si se añadió (o ya estaba). */
  const addToList = useCallback(
    async (listId: string): Promise<boolean> => {
      if (!itemId) return false;
      if (contains(listId)) return true;
      setAddingListId(listId);
      setError(null);
      try {
        const updated = await itemListRepository.addItemToList(listId, itemId);
        if (!updated) setError('No se pudo añadir a la lista');
        return !!updated;
      } finally {
        setAddingListId(null);
      }
    },
    [itemId, contains]
  );

  return { lists: sortedLists, loading, addingListId, error, contains, addToList } as const;
};
