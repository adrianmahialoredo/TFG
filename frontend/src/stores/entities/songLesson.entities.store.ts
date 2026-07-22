import { create } from 'zustand';
import { SongLessonDTO } from '../../types';

interface SongLessonEntitiesState {
  songLessons: Record<string, SongLessonDTO>;

  setSongLessons: (songLessons: SongLessonDTO[]) => void;
  updateSongLesson: (id: string, data: Partial<SongLessonDTO>) => void;
  removeSongLessons: (ids: string[]) => void;
  clearSongLessons: () => void;
}

export const useSongLessonEntitiesStore = create<SongLessonEntitiesState>((set) => ({
  songLessons: {},

  setSongLessons: (newSongLessons) =>
      set((state) => {
        const updatedSongLessons = { ...state.songLessons };

        newSongLessons.forEach((songLesson) => {
          updatedSongLessons[songLesson.id.toString()] = songLesson;
        });

        return { songLessons: updatedSongLessons };
      }),

  updateSongLesson: (id, data) =>
      set((state) => {
        const songLesson = state.songLessons[id];
        if (!songLesson) return state;

        return {
          songLessons: {
            ...state.songLessons,
            [id]: { ...songLesson, ...data },
          },
        };
      }),

  removeSongLessons: (ids) =>
      set((state) => {
        const updatedSongLessons = { ...state.songLessons };
        ids.forEach((id) => delete updatedSongLessons[id]);
        return { songLessons: updatedSongLessons };
      }),

  clearSongLessons: () => set({ songLessons: {} }),
}));

