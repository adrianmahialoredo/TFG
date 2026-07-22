import { create } from 'zustand';
import { InstrumentDTO } from '../../types';

interface InstrumentEntitiesState {
  instruments: Record<string, InstrumentDTO>;

  setInstruments: (instruments: InstrumentDTO[]) => void;
  updateInstrument: (id: string, data: Partial<InstrumentDTO>) => void;
  removeInstruments: (ids: string[]) => void;
  clearInstruments: () => void;
}

export const useInstrumentEntitiesStore = create<InstrumentEntitiesState>((set) => ({
  instruments: {},

  setInstruments: (newInstruments) =>
    set((state) => {
      const updatedInstruments = { ...state.instruments };

      newInstruments.forEach((instrument) => {
        updatedInstruments[instrument.id.toString()] = instrument;
      });

      return { instruments: updatedInstruments };
    }),

  updateInstrument: (id, data) =>
    set((state) => {
      const instrument = state.instruments[id];
      if (!instrument) return state;

      return {
        instruments: {
          ...state.instruments,
          [id]: { ...instrument, ...data },
        },
      };
    }),

  removeInstruments: (ids) =>
    set((state) => {
      const updatedInstruments = { ...state.instruments };
      ids.forEach((id) => delete updatedInstruments[id]);
      return { instruments: updatedInstruments };
    }),

  clearInstruments: () => set({ instruments: {} }),
}));

