import { useState, useEffect, useMemo, useCallback, useRef } from 'react';
import { useItemEntitiesStore } from '../stores/entities';
import { useItemListEntitiesStore } from '../stores/entities';
import * as itemRepository from '../repositories/item.repository';
import * as itemFeedRepository from '../repositories/itemFeed.repository';
import * as itemListRepository from '../repositories/itemList.repository';
import { ItemSummaryDTO } from '../types';

/** Máximo de resultados de búsqueda pintados a la vez. */
const MAX_RESULTS = 50;

/**
 * Recomendaciones visibles a la vez. El resto espera su turno: cuando una se añade
 * (y termina su tic), entra la siguiente.
 */
export const VISIBLE_RECOMMENDATIONS = 5;

/** Tiempo que se ve el tic antes de que el item desaparezca de la pantalla. */
const DISMISS_DELAY_MS = 600;

/** Minúsculas y sin tildes, para que "cancion" encuentre "Canción". */
const normalize = (text?: string | null) =>
  (text ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();

/**
 * Nivel 6: Hook orquestador de la pantalla AddItemScreen.
 *
 * - Sin búsqueda: muestra las recomendaciones ("Para ti") del backend para esta lista,
 *   de VISIBLE_RECOMMENDATIONS en VISIBLE_RECOMMENDATIONS (al añadir una entra la siguiente).
 * - Con búsqueda: filtra en local el catálogo completo de items (cargado al arrancar
 *   la app), sin llamar al backend.
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
  const [catalogIds, setCatalogIds] = useState<string[]>([]);
  const [query, setQuery] = useState('');

  const [loading, setLoading] = useState(true);
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // IDs añadidos en esta sesión (para feedback inmediato aunque la store tarde)
  const [addedIds, setAddedIds] = useState<string[]>([]);
  // Añadidos que ya han mostrado el tic y se quitan de la pantalla
  const [dismissedIds, setDismissedIds] = useState<string[]>([]);
  const dismissTimers = useRef<Record<string, ReturnType<typeof setTimeout>>>({});
  // true en cuanto se añade algo en esta pantalla (ver onFresh)
  const hasAddedRef = useRef(false);

  useEffect(() => {
    const timers = dismissTimers.current;
    return () => Object.values(timers).forEach(clearTimeout);
  }, []);

  // 1. Cargar recomendaciones al montar
  useEffect(() => {
    let cancelled = false;
    // Si se sirvió la caché, la red avisa después con los IDs frescos. Si el usuario ya ha empezado
    // a añadir, se ignoran: cambiar la cola de golpe hacía salir y entrar todas las filas a la vez
    // en mitad de la animación del añadido. La próxima vez que abra la pantalla ya verá las frescas.
    const onFresh = (fresh: string[]) => {
      if (!cancelled && !hasAddedRef.current) setRecommendedIds(fresh);
    };

    const fetchRecommended = async () => {
      setLoading(true);
      try {
        const ids = await itemFeedRepository.getRecommendedByItemList(itemListId, onFresh);
        if (cancelled) return;
        setRecommendedIds(ids);
        setError(null);
      } catch (err) {
        console.error('[useAddItemScreen] Error cargando recomendaciones:', err);
        setError('Error al cargar recomendaciones');
      } finally {
        setLoading(false);
      }
    };

    if (itemListId) fetchRecommended();
    else setLoading(false);
    return () => {
      cancelled = true;
    };
  }, [itemListId]);

  // 2. Catálogo completo para la búsqueda local (normalmente ya está en RAM desde el arranque)
  useEffect(() => {
    let cancelled = false;
    const apply = (ids: string[]) => {
      if (!cancelled) setCatalogIds(ids);
    };

    itemRepository.getCatalog(apply)
      .then(apply)
      .catch((err) => console.error('[useAddItemScreen] Error cargando el catálogo:', err))
      .finally(() => {
        if (!cancelled) setCatalogLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  // 3. Hidratación reactiva: IDs -> objetos desde la Entity Store
  const recommended = useMemo(
    () => recommendedIds.map((id) => allItems[id]).filter(Boolean),
    [recommendedIds, allItems]
  );

  // Búsqueda local por título (y artista en canciones). Primero los que empiezan por la query.
  const results = useMemo(() => {
    const q = normalize(query.trim());
    if (!q) return [];

    const matches: { item: ItemSummaryDTO; title: string }[] = [];
    for (const id of catalogIds) {
      const item = allItems[id];
      if (!item) continue;
      const title = normalize(item.title);
      if (title.includes(q) || normalize(item.artist).includes(q)) {
        matches.push({ item, title });
      }
    }

    return matches
      .sort((a, b) =>
        Number(b.title.startsWith(q)) - Number(a.title.startsWith(q)) ||
        a.title.localeCompare(b.title)
      )
      .slice(0, MAX_RESULTS)
      .map((m) => m.item);
  }, [query, catalogIds, allItems]);

  // Solo se "busca" mientras el catálogo aún no ha llegado (primer arranque sin caché)
  const searching = !!query.trim() && catalogLoading && catalogIds.length === 0;

  // 4. Conjunto de IDs ya presentes en la lista (existentes + añadidos en sesión)
  const addedSet = useMemo(
    () => new Set<string>([...(listItemIds || []), ...addedIds]),
    [listItemIds, addedIds]
  );

  // Items mostrados: resultados si hay búsqueda, si no las recomendaciones. Los que ya están
  // en la lista no se muestran, salvo los recién añadidos mientras enseñan el tic.
  // De las recomendaciones solo se ven las primeras VISIBLE_RECOMMENDATIONS: el recién añadido
  // ocupa su hueco mientras enseña el tic y, al quitarse, entra la siguiente en espera.
  const items = useMemo(() => {
    const showingCheck = new Set(addedIds.filter((id) => !dismissedIds.includes(id)));
    const searchingText = !!query.trim();
    const visible = (searchingText ? results : recommended)
      .filter((item) => !addedSet.has(item.id) || showingCheck.has(item.id));
    return searchingText ? visible : visible.slice(0, VISIBLE_RECOMMENDATIONS);
  }, [query, results, recommended, addedSet, addedIds, dismissedIds]);

  const isAdded = useCallback((itemId: string) => addedSet.has(itemId), [addedSet]);

  // 5. Añadir item a la lista
  const addItem = useCallback(
    async (itemId: string) => {
      if (addedSet.has(itemId)) return;
      hasAddedRef.current = true;
      // Feedback optimista
      setAddedIds((prev) => (prev.includes(itemId) ? prev : [...prev, itemId]));
      dismissTimers.current[itemId] = setTimeout(() => {
        delete dismissTimers.current[itemId];
        setDismissedIds((prev) => [...prev, itemId]);
      }, DISMISS_DELAY_MS);

      const updated = await itemListRepository.addItemToList(itemListId, itemId);
      if (!updated) {
        // Revertir si falló: el item vuelve a aparecer con el más
        clearTimeout(dismissTimers.current[itemId]);
        delete dismissTimers.current[itemId];
        setAddedIds((prev) => prev.filter((id) => id !== itemId));
        setDismissedIds((prev) => prev.filter((id) => id !== itemId));
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


