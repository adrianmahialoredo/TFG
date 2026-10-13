import { create } from 'zustand';
import { LessonSectionDTO } from '../../types';

interface LessonSectionEntitiesState {
  lessonSections: Record<string, LessonSectionDTO>;

  setLessonSections: (lessonSections: LessonSectionDTO[]) => void;
  updateLessonSection: (id: string, data: Partial<LessonSectionDTO>) => void;
  removeLessonSections: (ids: string[]) => void;
  clearLessonSections: () => void;
}

export const useLessonSectionEntitiesStore = create<LessonSectionEntitiesState>((set) => ({
  lessonSections: {},

  setLessonSections: (newLessonSections) =>
      set((state) => {
        const updatedLessonSections = { ...state.lessonSections };

        newLessonSections.forEach((lessonSection) => {
          updatedLessonSections[lessonSection.id.toString()] = lessonSection;
        });

        return { lessonSections: updatedLessonSections };
      }),

  updateLessonSection: (id, data) =>
      set((state) => {
        const lessonSection = state.lessonSections[id];
        if (!lessonSection) return state;

        return {
          lessonSections: {
            ...state.lessonSections,
            [id]: { ...lessonSection, ...data },
          },
        };
      }),

  removeLessonSections: (ids) =>
      set((state) => {
        const updatedLessonSections = { ...state.lessonSections };
        ids.forEach((id) => delete updatedLessonSections[id]);
        return { lessonSections: updatedLessonSections };
      }),

  clearLessonSections: () => set({ lessonSections: {} }),
}));

