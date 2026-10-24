import { create } from 'zustand';
import { DifficultyLevel } from '../../types';

interface SongScreenState {
    // Estado de carga y errores
    isLoading: boolean;
    error: string | null;

    // Selección de la lección actual
    // Guardamos el instrumento y la dificultad elegidos; el hook buscará la lección que coincida.
    // null = aún no se ha elegido (el hook aplica la selección por defecto)
    selectedInstrumentId: string | null;
    selectedDifficulty: DifficultyLevel | null;

    // Acciones
    setLoading: (loading: boolean) => void;
    setError: (error: string | null) => void;
    setInstrument: (instrumentId: string) => void;
    setDifficulty: (difficulty: DifficultyLevel) => void;

    // Limpieza al entrar en otra canción
    reset: () => void;
}

export const useSongScreenStore = create<SongScreenState>((set) => ({
    isLoading: true, // Empezamos cargando
    error: null,
    selectedInstrumentId: null,
    selectedDifficulty: null,

    setLoading: (loading) => set({ isLoading: loading }),
    setError: (error) => set({ error }),

    setInstrument: (instrumentId) => set({ selectedInstrumentId: instrumentId }),

    setDifficulty: (difficulty) => set({
        selectedDifficulty: difficulty,
        isLoading: false // Por si queremos disparar un micro-loading al cambiar
    }),

    reset: () => set({
        isLoading: true,
        error: null,
        selectedInstrumentId: null,
        selectedDifficulty: null
    }),
}));
