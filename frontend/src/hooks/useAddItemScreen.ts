import { useState, useEffect, useMemo, useCallback } from 'react';
import { useItemEntitiesStore } from '../stores/entities/item.entities.store';
import { useItemListEntitiesStore } from '../stores/entities';
import * as itemRepository from '../repositories/item.repository';
import * as itemListRepository from '../repositories/itemList.repository';

/**
 * Nivel 6: Hook orquestador de la pantalla AddItemScreen.
 *
 * - Carga las recomendaciones ("Para ti") al montar.
 * - Busca items en el backend (con debounce) cuando cambia la query.
 * - Añade un item a la lista destino sincronizando la Entity Store (Hidratación Atómica).
 *
 * La UI (Nivel 7) solo consume este hook.
 */
export const useAddItemScreen = (itemListId: string) => {
  // Suscripción reactiva a las Entity Stores
  const allItems = useItemEntitiesStore((state) => state.items);
  const listItemIds = useItemListEntitiesStore(
    (state) => state.itemLists[itemListId]?.itemIds
  );

  const [recommendedIds, setRecommendedIds] = useState<string[]>([]);
  const [resultIds, setResultIds] = useState<string[]>([]);
  const [query, setQuery] = useState('');

  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // IDs añadidos en esta sesión (para feedback inmediato aunque la store tarde)
  const [addedIds, setAddedIds] = useState<string[]>([]);

  // 1. Cargar recomendaciones al montar
  useEffect(() => {
    const fetchRecommended = async () => {
      setLoading(true);
      try {
        const ids = await itemRepository.getRecommended();
        setRecommendedIds(ids);
        setError(null);
      } catch (err) {
        console.error('[useAddItemScreen] Error cargando recomendaciones:', err);
        setError('Error al cargar recomendaciones');
      } finally {
        setLoading(false);
      }
    };

    fetchRecommended();
  }, []);

  // 2. Búsqueda con debounce cuando cambia la query
  useEffect(() => {
    if (!query.trim()) {
      setResultIds([]);
      setSearching(false);
      return;
    }

    setSearching(true);
    const handler = setTimeout(async () => {
      try {
        const ids = await itemRepository.search(query);
        setResultIds(ids);
      } catch (err) {
        console.error('[useAddItemScreen] Error en búsqueda:', err);
      } finally {
        setSearching(false);
      }
    }, 350);

    return () => clearTimeout(handler);
  }, [query]);

  // 3. Hidratación reactiva: IDs -> objetos desde la Entity Store
  const recommended = useMemo(
    () => recommendedIds.map((id) => allItems[id]).filter(Boolean),
    [recommendedIds, allItems]
  );

  const results = useMemo(
    () => resultIds.map((id) => allItems[id]).filter(Boolean),
    [resultIds, allItems]
  );

  // Items mostrados: resultados si hay búsqueda, si no las recomendaciones
  const items = query.trim() ? results : recommended;

  // 4. Conjunto de IDs ya presentes en la lista (existentes + añadidos en sesión)
  const addedSet = useMemo(
    () => new Set<string>([...(listItemIds || []), ...addedIds]),
    [listItemIds, addedIds]
  );

  const isAdded = useCallback((itemId: string) => addedSet.has(itemId), [addedSet]);

  // 5. Añadir item a la lista
  const addItem = useCallback(
    async (itemId: string) => {
      if (addedSet.has(itemId)) return;
      // Feedback optimista
      setAddedIds((prev) => (prev.includes(itemId) ? prev : [...prev, itemId]));

      const updated = await itemListRepository.addItemToList(itemListId, itemId);
      if (!updated) {
        // Revertir si falló
        setAddedIds((prev) => prev.filter((id) => id !== itemId));
        setError('No se pudo añadir el item a la lista');
      }
    },
    [itemListId, addedSet]
  );

  return {
    items,
    query,
    setQuery,
    loading,
    searching,
    error,
    addItem,
    isAdded,
  } as const;
};


