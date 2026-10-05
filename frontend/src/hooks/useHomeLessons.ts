import { useState, useEffect, useMemo } from 'react';
import { useItemEntitiesStore } from '../stores/entities';
import * as lessonRepository from '../repositories/lesson.repository';
import { itemsToCarouselItems } from '../utils/mappers.utils';

/**
 * Nivel 6: Hook de hidratación para el Home de Lecciones (LearnScreen).
 * Orquesta los repositorios (que inyectan los DTOs en la Entity Store)
 * y mapea los IDs a objetos reactivos listos para los carruseles.
 */
export const useHomeLessons = () => {
  // Suscripción reactiva a la Store de Entidades de Lecciones
  const allItems = useItemEntitiesStore((state) => state.items);

  // Estados locales para los IDs (la "lista de llaves")
  const [ids, setIds] = useState({
    popular: [] as string[],
    news: [] as string[],
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
          lessonRepository.getPopular(setSection('popular')).then(setSection('popular')),
          lessonRepository.getNew(setSection('news')).then(setSection('news')),
          lessonRepository.getRecommended(setSection('recommendations')).then(setSection('recommendations')),
        ]);

        setError(null);
      } catch (err) {
        console.error("[useHomeLessons] Error:", err);
        setError("Error al sincronizar lecciones");
      } finally {
        setLoading(false);
      }
    };

    fetchIds();
    return () => {
      cancelled = true;
    };
  }, []);

  // HIDRATACIÓN REACTIVA CON MAPEO:
  // Si una lección cambia en la Store, estos useMemo se disparan automáticamente.

  const populars = useMemo(() => {
    const lessons = ids.popular.map(id => allItems[id]).filter(Boolean);
    return itemsToCarouselItems(lessons);
  }, [ids.popular, allItems]);

  const news = useMemo(() => {
    const lessons = ids.news.map(id => allItems[id]).filter(Boolean);
    return itemsToCarouselItems(lessons);
  }, [ids.news, allItems]);

  const recommendations = useMemo(() => {
    const lessons = ids.recommendations.map(id => allItems[id]).filter(Boolean);
    return itemsToCarouselItems(lessons);
  }, [ids.recommendations, allItems]);

  return {
    populars,
    news,
    recommendations,
    loading,
    error,
  } as const;
};

