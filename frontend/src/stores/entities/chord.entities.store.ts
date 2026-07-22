import { create } from 'zustand';
import { ChordDTO } from '../../types';

interface ChordEntitiesState {
  chords: Record<string, ChordDTO>;

  setChords: (chords: ChordDTO[]) => void;
  updateChord: (id: string, data: Partial<ChordDTO>) => void;
  removeChords: (ids: string[]) => void;
  clearChords: () => void;
}

export const useChordEntitiesStore = create<ChordEntitiesState>((set) => ({
  chords: {},

  setChords: (newChords) =>
      set((state) => {
        const updatedChords = { ...state.chords };

        newChords.forEach((chord) => {
          updatedChords[chord.id.toString()] = chord;
        });

        return { chords: updatedChords };
      }),

  updateChord: (id, data) =>
      set((state) => {
        const chord = state.chords[id];
        if (!chord) return state;

        return {
          chords: {
            ...state.chords,
            [id]: { ...chord, ...data },
          },
        };
      }),

  removeChords: (ids) =>
      set((state) => {
        const updatedChords = { ...state.chords };
        ids.forEach((id) => delete updatedChords[id]);
        return { chords: updatedChords };
      }),

  clearChords: () => set({ chords: {} }),
}));

