import { useState, useEffect, useMemo, useCallback } from 'react';
import { useItemEntitiesStore } from '../stores/entities';
import * as itemRepository from '../repositories/item.repository';
import { itemsToCarouselItems } from '../utils/mappers.utils';
import { AnyItemDTO } from '../types';

/**
 * Nivel 6: Hook de hidratación para el Home (HomeScreen).
 *
 * Orquesta el repositorio de items (que inyecta los DTOs en la Entity Store)
 * y mapea los IDs a objetos reactivos listos para los carruseles:
 *  - "Recientes": items accedidos más recientemente (lasttimeaccess).
 *  - "Recomendaciones": items recomendados (esqueleto en backend).
 *
 * También expone `getItemById` para que la UI pueda resolver el item completo
 * (con su `type`) y decidir la navegación al pulsarlo.
 */
export const useHomeItems = () => {
  // Suscripción reactiva a la Store de Entidades de Items
  const allItems = useItemEntitiesStore((state) => state.items);

  // Estados locales para los IDs (la "lista de llaves")
  const [ids, setIds] = useState({
    recent: [] as string[],
    recommendations: [] as string[],
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchIds = async () => {
      setLoading(true);
      try {
        // El repositorio ya inyecta los DTOs en la Store internamente
        const [recent, recommendations] = await Promise.all([
          itemRepository.getRecent(),
          itemRepository.getRecommendations(),
        ]);

        setIds({ recent, recommendations });
        setError(null);
      } catch (err) {
        console.error('[useHomeItems] Error:', err);
        setError('Error al sincronizar el inicio');
      } finally {
        setLoading(false);
      }
    };

    fetchIds();
  }, []);

  // HIDRATACIÓN REACTIVA CON MAPEO:
  // Si un item cambia en la Store, estos useMemo se disparan automáticamente.

  const recentItems = useMemo(() => {
    const items = ids.recent.map((id) => allItems[id]).filter(Boolean) as AnyItemDTO[];
    return itemsToCarouselItems(items);
  }, [ids.recent, allItems]);

  const recommendations = useMemo(() => {
    const items = ids.recommendations.map((id) => allItems[id]).filter(Boolean) as AnyItemDTO[];
    return itemsToCarouselItems(items);
  }, [ids.recommendations, allItems]);

  // Permite a la UI resolver el item completo (con su `type`) para navegar.
  const getItemById = useCallback(
    (id: string): AnyItemDTO | undefined => allItems[id],
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


