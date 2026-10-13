import { useState, useEffect, useMemo, useCallback } from 'react';
import { useAuthStore, useItemEntitiesStore, useRecentItemsStore } from '../stores/entities';
import * as itemFeedRepository from '../repositories/itemFeed.repository';
import { itemsToCarouselItems } from '../utils/mappers.utils';
import { ItemSummaryDTO } from '../types';

/**
 * Nivel 6: Hook de hidratación para el Home (HomeScreen).
 *
 * Orquesta el repositorio de items (que inyecta los DTOs en la Entity Store)
 * y mapea los IDs a objetos reactivos listos para los carruseles:
 *  - "Recientes": items que ha abierto el usuario actual, del más reciente al más antiguo.
 *    Salen de la RecentItemsStore (solo del front) y se pintan con el catálogo de items.
 *  - "Recomendaciones": items recomendados al usuario (`/recommendations/items/by-user`).
 *
 * También expone `getItemById` para que la UI pueda resolver el item completo
 * (con su `type`) y decidir la navegación al pulsarlo.
 */
export const useHomeItems = () => {
  // Suscripción reactiva a la Store de Entidades de Items
  const allItems = useItemEntitiesStore((state) => state.items);
  const currentUserId = useAuthStore((state) => state.currentUserId);
  const recentIds = useRecentItemsStore((state) => (currentUserId ? state.byUser[currentUserId] : undefined));

  // Estados locales para los IDs (la "lista de llaves")
  const [ids, setIds] = useState({
    recommendations: [] as string[],
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    // Cada sección se aplica por separado en cuanto llega: primero la caché
    // (al instante) y, si la hubo, después los IDs frescos de la red (`onFresh`).
    const setSection = (section: keyof typeof ids) => (sectionIds: string[]) => {
      if (cancelled) return;
      setIds((prev) => ({ ...prev, [section]: sectionIds }));
      // En cuanto hay algo que pintar se quita el spinner, sin esperar al resto.
      if (sectionIds.length > 0) setLoading(false);
    };

    const fetchIds = async () => {
      setLoading(true);
      try {
        // Los resúmenes ya están en la Store (catálogo); aquí solo guardamos los IDs
        await Promise.all([
          // Las recomendaciones son por usuario: sin sesión no se piden
          currentUserId
            ? itemFeedRepository.getRecommendedForUser(currentUserId, setSection('recommendations')).then(setSection('recommendations'))
            : Promise.resolve(),
        ]);

        setError(null);
      } catch (err) {
        console.error('[useHomeItems] Error:', err);
        setError('Error al sincronizar el inicio');
      } finally {
        setLoading(false);
      }
    };

    fetchIds();
    return () => {
      cancelled = true;
    };
  }, [currentUserId]);

  // HIDRATACIÓN REACTIVA CON MAPEO:
  // Si un item cambia en la Store, estos useMemo se disparan automáticamente.

  const recentItems = useMemo(() => {
    // Un item que ya no está en el catálogo (p. ej. borrado) simplemente no se pinta
    const items = (recentIds ?? []).map((id) => allItems[id]).filter(Boolean) as ItemSummaryDTO[];
    return itemsToCarouselItems(items);
  }, [recentIds, allItems]);

  const recommendations = useMemo(() => {
    const items = ids.recommendations.map((id) => allItems[id]).filter(Boolean) as ItemSummaryDTO[];
    return itemsToCarouselItems(items);
  }, [ids.recommendations, allItems]);

  // Permite a la UI resolver el item completo (con su `type`) para navegar.
  const getItemById = useCallback(
    (id: string): ItemSummaryDTO | undefined => allItems[id],
    [allItems]
  );

  return {
    recentItems,
    recommendations,
    getItemById,
    loading,
    error,
  } as const;
};


