import { create } from 'zustand';
import { LessonDTO } from '../../types';

interface LessonEntitiesState {
  // Diccionario de lecciones: { "1": LessonDTO, "2": LessonDTO }
  lessons: Record<string, LessonDTO>;

  // Acciones
  setLessons: (lessons: LessonDTO[]) => void;
  updateLesson: (id: string, data: Partial<LessonDTO>) => void;
  removeLessons: (ids: string[]) => void;
  clearLessons: () => void;
}

/**
 * Store de Entidades de Lecciones (La Despensa Global)
 * Mantiene en RAM todos los DTOs de lecciones descargados.
 */
export const useLessonEntitiesStore = create<LessonEntitiesState>((set) => ({
  lessons: {},

  // Agrega o actualiza múltiples lecciones a la vez
  setLessons: (newLessons) =>
      set((state) => {
        const updatedLessons = { ...state.lessons };

        newLessons.forEach((lesson) => {
          // Nos aseguramos de que el ID sea string por seguridad
          updatedLessons[lesson.id.toString()] = lesson;
        });

        return { lessons: updatedLessons };
      }),

  // Actualiza solo una parte de una lección (ej. cambiar título)
  updateLesson: (id, data) =>
      set((state) => {
        const lesson = state.lessons[id];
        if (!lesson) return state; // Si no existe, no hacemos nada

        return {
          lessons: {
            ...state.lessons,
            [id]: { ...lesson, ...data },
          },
        };
      }),

  // Elimina lecciones específicas
  removeLessons: (ids) =>
      set((state) => {
        const updatedLessons = { ...state.lessons };
        ids.forEach((id) => delete updatedLessons[id]);
        return { lessons: updatedLessons };
      }),

  // Limpia toda la despensa (ej. al hacer logout)
  clearLessons: () => set({ lessons: {} }),
}));

