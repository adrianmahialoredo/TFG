import { useEffect, useMemo, useState } from 'react';
import { useInstrumentGenreEntitiesStore, useLessonEntitiesStore } from '../stores/entities';
import * as itemRepository from '../repositories/item.repository';
import * as instrumentGenreRepository from '../repositories/instrumentGenre.repository';
import * as lessonRepository from '../repositories/lesson.repository';

export const useInstrumentGenre = (instrumentGenreId: string) => {
  const instrumentGenre = useInstrumentGenreEntitiesStore(s => s.instrumentGenres[instrumentGenreId]);
  const allLessons = useLessonEntitiesStore(s => s.lessons);
  const [loading, setLoading] = useState(!instrumentGenre);

  // Marca el item como accedido para la sección "Recientes" del Home (fire-and-forget)
  useEffect(() => {
    if (instrumentGenreId) itemRepository.markAsAccessed(instrumentGenreId);
  }, [instrumentGenreId]);

  // Trae el género-instrumento si no está en la Store (p.ej. al llegar desde una card del Home,
  // que solo tiene su resumen) y, con él, sus lecciones (RAM -> Disco -> API)
  useEffect(() => {
    if (!instrumentGenreId) return;
    let cancelled = false;

    const load = async () => {
      setLoading(!useInstrumentGenreEntitiesStore.getState().instrumentGenres[instrumentGenreId]);
      try {
        const loaded = await instrumentGenreRepository.getById(instrumentGenreId);
        if (loaded?.lessonIds?.length) await lessonRepository.getAllById(loaded.lessonIds);
      } catch (err) {
        console.error('[useInstrumentGenre] Error cargando el género-instrumento:', err);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    load();

    return () => {
      cancelled = true;
    };
  }, [instrumentGenreId]);

  const lessons = useMemo(() => {
    if (!instrumentGenre?.lessonIds) return [];
    return instrumentGenre.lessonIds
        .map(id => allLessons[id])
        .filter(l => !!l);
  }, [instrumentGenre, allLessons]);

  return {
    instrumentGenre,
    lessons,
    loading,
  };
};
