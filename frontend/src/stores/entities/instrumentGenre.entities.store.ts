import { create } from 'zustand';
import { InstrumentGenreDTO } from '../../types';

interface InstrumentGenreEntitiesState {
  instrumentGenres: Record<string, InstrumentGenreDTO>;

  setInstrumentGenres: (instrumentGenres: InstrumentGenreDTO[]) => void;
  updateInstrumentGenre: (id: string, data: Partial<InstrumentGenreDTO>) => void;
  removeInstrumentGenres: (ids: string[]) => void;
  clearInstrumentGenres: () => void;
}

export const useInstrumentGenreEntitiesStore = create<InstrumentGenreEntitiesState>((set) => ({
  instrumentGenres: {},

  setInstrumentGenres: (newInstrumentGenres) =>
    set((state) => {
      const updatedInstrumentGenres = { ...state.instrumentGenres };

      newInstrumentGenres.forEach((ig) => {
        updatedInstrumentGenres[ig.id.toString()] = ig;
      });

      return { instrumentGenres: updatedInstrumentGenres };
    }),

  updateInstrumentGenre: (id, data) =>
    set((state) => {
      const ig = state.instrumentGenres[id];
      if (!ig) return state;

      return {
        instrumentGenres: {
          ...state.instrumentGenres,
          [id]: { ...ig, ...data },
        },
      };
    }),

  removeInstrumentGenres: (ids) =>
    set((state) => {
      const updatedInstrumentGenres = { ...state.instrumentGenres };
      ids.forEach((id) => delete updatedInstrumentGenres[id]);
      return { instrumentGenres: updatedInstrumentGenres };
    }),

  clearInstrumentGenres: () => set({ instrumentGenres: {} }),
}));

