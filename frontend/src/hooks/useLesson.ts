import { useEffect, useMemo, useState } from 'react';
import {
  useLessonEntitiesStore,
  useLessonSectionEntitiesStore,
  useGenreEntitiesStore,
} from '../stores/entities';
import * as lessonRepository from '../repositories/lesson.repository';
import * as genreRepository from '../repositories/genre.repository';
import * as itemRepository from '../repositories/item.repository';
import { LessonSectionDTO } from '../types';

/**
 * Lección con sus secciones y la navegación entre ellas.
 * El `lessonId` puede cambiar en caliente (p.ej. desde el EdgePanel): la sección vuelve a la primera.
 * @param markAccessed si se registra la lección en "Recientes" (false cuando se muestra dentro de otro item)
 */
export const useLesson = (lessonId: string, { markAccessed = true }: { markAccessed?: boolean } = {}) => {
  // 1. Acceso a las Despensas (RAM)
  const lesson = useLessonEntitiesStore((state) => state.lessons[lessonId]);
  const allSectionsMap = useLessonSectionEntitiesStore((state) => state.lessonSections);
  const genre = useGenreEntitiesStore((state) => (lesson?.genreId ? state.genres[lesson.genreId] : undefined));

  // 2. Estado de Pantalla
  const [loading, setLoading] = useState(true);
  // El índice va ligado a su lección: al cambiar de lección se parte de la primera sección sin
  // pasar por un render intermedio con el índice de la anterior.
  const [sectionState, setSectionState] = useState({ lessonId, index: 0 });
  const sectionIndex = sectionState.lessonId === lessonId ? sectionState.index : 0;
  const setSectionIndex = (index: number) => setSectionState({ lessonId, index });

  // 3. Carga inicial (lección + secciones; el género solo si la lección pertenece a uno)
  useEffect(() => {
    if (!lessonId) {
      setLoading(false);
      return;
    }
    // Si la lección cambia antes de terminar, la carga anterior no debe tocar el estado
    let cancelled = false;

    const loadData = async () => {
      setLoading(true);
      try {
        const loaded = await lessonRepository.getById(lessonId);
        if (loaded?.genreId) await genreRepository.getById(loaded.genreId);
      } catch (error) {
        console.error('[useLesson] Error cargando la lección:', error);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    loadData();
    // Marca el item como accedido para la sección "Recientes" del Home (fire-and-forget)
    if (markAccessed) itemRepository.markAsAccessed(lessonId);

    return () => { cancelled = true; };
  }, [lessonId, markAccessed]);

  // 4. Derivados estables: IDs -> DTOs, en el orden de la lección
  const sections = useMemo<LessonSectionDTO[]>(() => {
    if (!lesson?.sectionIds?.length) return [];

    return lesson.sectionIds
        .map((id) => allSectionsMap[id])
        .filter((section): section is LessonSectionDTO => Boolean(section))
        .sort((a, b) => a.orderIndex - b.orderIndex);
  }, [lesson?.sectionIds, allSectionsMap]);

  const currentSection = sections[sectionIndex];

  // 5. Navegación entre secciones
  const hasPrevious = sectionIndex > 0;
  const hasNext = sectionIndex < sections.length - 1;

  const goToNext = () => hasNext && setSectionIndex(sectionIndex + 1);
  const goToPrevious = () => hasPrevious && setSectionIndex(sectionIndex - 1);

  return {
    lesson,
    genre,
    loading,
    sections,
    currentSection,
    sectionIndex,
    hasPrevious,
    hasNext,
    goToNext,
    goToPrevious,
  };
};
