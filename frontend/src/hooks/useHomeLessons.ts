import { useState, useEffect, useMemo } from 'react';
import { useAuthStore, useItemEntitiesStore, useUserEntitiesStore } from '../stores/entities';
import * as lessonRepository from '../repositories/lesson.repository';
import { itemsToCarouselItems } from '../utils/mappers.utils';
import { useUserTasteKey } from './useUserTasteKey';
import { useInstrumentsByIds } from './useInstruments';

/**
 * Nivel 6: Hook de hidratación para el Home de Lecciones (LearnScreen).
 * Orquesta los repositorios (que inyectan los DTOs en la Entity Store)
 * y mapea los IDs a objetos reactivos listos para los carruseles.
 */
export const useHomeLessons = () => {
  // Suscripción reactiva a la Store de Entidades de Lecciones
  const allItems = useItemEntitiesStore((state) => state.items);
  const currentUserId = useAuthStore((state) => state.currentUserId);

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

  // Las recomendaciones son por usuario (sin sesión no se piden) y se vuelven a pedir cuando
  // cambian sus listas, sus favoritos o su nivel, sin vaciar el carrusel mientras llegan.
  const tasteKey = useUserTasteKey();

  useEffect(() => {
    setIds((prev) => ({ ...prev, recommendations: [] }));
  }, [currentUserId]);

  useEffect(() => {
    if (!currentUserId) return;

    let cancelled = false;
    const apply = (sectionIds: string[]) => {
      if (cancelled) return;
      setIds((prev) => ({ ...prev, recommendations: sectionIds }));
      if (sectionIds.length > 0) setLoading(false);
    };
    lessonRepository.getRecommended(currentUserId, apply)
      .then(apply)
      .catch((err) => console.error('[useHomeLessons] Error en las recomendaciones:', err));

    return () => {
      cancelled = true;
    };
  }, [currentUserId, tasteKey]);

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

  // --- Carrusel "Para <instrumento favorito>" ---
  // Se elige uno de los instrumentos favoritos del usuario al azar, estable mientras no cambien.
  const favouriteInstrumentIds = useUserEntitiesStore((state) =>
    currentUserId ? state.users[currentUserId]?.favouriteInstrumentsIds : undefined
  );
  const favouriteKey = (favouriteInstrumentIds ?? []).map(String).join(',');

  const favouriteInstrumentId = useMemo(() => {
    const candidates = favouriteKey ? favouriteKey.split(',') : [];
    return candidates.length > 0 ? candidates[Math.floor(Math.random() * candidates.length)] : null;
  }, [favouriteKey]);

  // Trae el instrumento (para su nombre) si aún no está en la Store
  const [favouriteInstrumentEntity] = useInstrumentsByIds(favouriteInstrumentId ? [favouriteInstrumentId] : []);

  const [favouriteInstrumentLessonIds, setFavouriteInstrumentLessonIds] = useState<string[]>([]);

  useEffect(() => {
    setFavouriteInstrumentLessonIds([]);
    if (!favouriteInstrumentId) return;

    let cancelled = false;
    const apply = (sectionIds: string[]) => {
      if (!cancelled) setFavouriteInstrumentLessonIds(sectionIds);
    };
    lessonRepository.getPopularByInstrumentId(favouriteInstrumentId, apply)
      .then(apply)
      .catch((err) => console.error('[useHomeLessons] Error en el instrumento favorito:', err));

    return () => {
      cancelled = true;
    };
  }, [favouriteInstrumentId]);

  const favouriteInstrument = useMemo(() => {
    if (!favouriteInstrumentId) return null;
    const lessons = favouriteInstrumentLessonIds.map(id => allItems[id]).filter(Boolean);
    return {
      title: favouriteInstrumentEntity?.name ?? '',
      data: itemsToCarouselItems(lessons),
    };
  }, [favouriteInstrumentId, favouriteInstrumentLessonIds, allItems, favouriteInstrumentEntity]);

  return {
    populars,
    news,
    recommendations,
    favouriteInstrument,
    loading,
    error,
  } as const;
};

