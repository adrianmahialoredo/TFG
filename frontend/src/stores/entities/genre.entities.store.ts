import { create } from 'zustand';
import { GenreDTO } from '../../types';

interface GenreEntitiesState {
  genres: Record<string, GenreDTO>;

  setGenres: (genres: GenreDTO[]) => void;
  updateGenre: (id: string, data: Partial<GenreDTO>) => void;
  removeGenres: (ids: string[]) => void;
  clearGenres: () => void;
}

export const useGenreEntitiesStore = create<GenreEntitiesState>((set) => ({
  genres: {},

  setGenres: (newGenres) =>
      set((state) => {
        const updatedGenres = { ...state.genres };

        newGenres.forEach((genre) => {
          updatedGenres[genre.id.toString()] = genre;
        });

        return { genres: updatedGenres };
      }),

  updateGenre: (id, data) =>
      set((state) => {
        const genre = state.genres[id];
        if (!genre) return state;

        return {
          genres: {
            ...state.genres,
            [id]: { ...genre, ...data },
          },
        };
      }),

  removeGenres: (ids) =>
      set((state) => {
        const updatedGenres = { ...state.genres };
        ids.forEach((id) => delete updatedGenres[id]);
        return { genres: updatedGenres };
      }),

  clearGenres: () => set({ genres: {} }),
}));

