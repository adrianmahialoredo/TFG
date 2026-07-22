import { useEffect, useMemo } from 'react';
import { useInstrumentGenreEntitiesStore, useLessonEntitiesStore } from '../stores/entities';
import * as itemRepository from '../repositories/item.repository';

export const useInstrumentGenre = (instrumentGenreId: string) => {
  const instrumentGenre = useInstrumentGenreEntitiesStore(s => s.instrumentGenres[instrumentGenreId]);
  const allLessons = useLessonEntitiesStore(s => s.lessons);

  // Marca el item como accedido para la sección "Recientes" del Home (fire-and-forget)
  useEffect(() => {
    if (instrumentGenreId) itemRepository.markAsAccessed(instrumentGenreId);
  }, [instrumentGenreId]);

  const lessons = useMemo(() => {
    if (!instrumentGenre?.lessonIds) return [];
    return instrumentGenre.lessonIds
        .map(id => allLessons[id])
        .filter(l => !!l);
  }, [instrumentGenre, allLessons]);

  return {
    instrumentGenre,
    lessons
  };
};