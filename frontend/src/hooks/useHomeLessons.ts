import { useState, useEffect, useMemo } from 'react';
import { useLessonEntitiesStore } from '../stores/entities';
import * as lessonRepository from '../repositories/lesson.repository';
import { lessonsToCarouselItems } from '../utils/mappers.utils';

/**
 * Nivel 6: Hook de hidratación para el Home de Lecciones (LearnScreen).
 * Orquesta los repositorios (que inyectan los DTOs en la Entity Store)
 * y mapea los IDs a objetos reactivos listos para los carruseles.
 */
export const useHomeLessons = () => {
  // Suscripción reactiva a la Store de Entidades de Lecciones
  const allLessons = useLessonEntitiesStore((state) => state.lessons);

  // Estados locales para los IDs (la "lista de llaves")
  const [ids, setIds] = useState({
    popular: [] as string[],
    news: [] as string[],
    recommendations: [] as string[],
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchIds = async () => {
      setLoading(true);
      try {
        // El repositorio ya inyecta los DTOs en la Store internamente
        const [popular, news, recommendations] = await Promise.all([
          lessonRepository.getPopular(),
          lessonRepository.getNew(),
          lessonRepository.getRecommended(),
        ]);

        setIds({ popular, news, recommendations });
        setError(null);
      } catch (err) {
        console.error("[useHomeLessons] Error:", err);
        setError("Error al sincronizar lecciones");
      } finally {
        setLoading(false);
      }
    };

    fetchIds();
  }, []);

  // HIDRATACIÓN REACTIVA CON MAPEO:
  // Si una lección cambia en la Store, estos useMemo se disparan automáticamente.

  const populars = useMemo(() => {
    const lessons = ids.popular.map(id => allLessons[id]).filter(Boolean);
    return lessonsToCarouselItems(lessons);
  }, [ids.popular, allLessons]);

  const news = useMemo(() => {
    const lessons = ids.news.map(id => allLessons[id]).filter(Boolean);
    return lessonsToCarouselItems(lessons);
  }, [ids.news, allLessons]);

  const recommendations = useMemo(() => {
    const lessons = ids.recommendations.map(id => allLessons[id]).filter(Boolean);
    return lessonsToCarouselItems(lessons);
  }, [ids.recommendations, allLessons]);

  return {
    populars,
    news,
    recommendations,
    loading,
    error,
  } as const;
};

