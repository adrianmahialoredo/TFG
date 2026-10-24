import { create } from 'zustand';
import { TuneDTO } from '../../types';

interface TuneEntitiesState {
  tunes: Record<string, TuneDTO>;

  setTunes: (tunes: TuneDTO[]) => void;
  updateTune: (id: string, data: Partial<TuneDTO>) => void;
  removeTunes: (ids: string[]) => void;
  clearTunes: () => void;
}

export const useTuneEntitiesStore = create<TuneEntitiesState>((set) => ({
  tunes: {},

  setTunes: (newTunes) =>
    set((state) => {
      const updatedTunes = { ...state.tunes };

      newTunes.forEach((tune) => {
        updatedTunes[tune.id.toString()] = tune;
      });

      return { tunes: updatedTunes };
    }),

  updateTune: (id, data) =>
    set((state) => {
      const tune = state.tunes[id];
      if (!tune) return state;

      return {
        tunes: {
          ...state.tunes,
          [id]: { ...tune, ...data },
        },
      };
    }),

  removeTunes: (ids) =>
    set((state) => {
      const updatedTunes = { ...state.tunes };
      ids.forEach((id) => delete updatedTunes[id]);
      return { tunes: updatedTunes };
    }),

  clearTunes: () => set({ tunes: {} }),
}));

