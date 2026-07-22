import { create } from 'zustand';
import { DifficultyLevel } from '../../types';

interface SongScreenState {
    // Estado de carga y errores
    isLoading: boolean;
    error: string | null;

    // Selección de la lección actual
    // Guardamos la dificultad preferida; el hook buscará la lección que coincida
    selectedDifficulty: DifficultyLevel | null;

    // Acciones
    setLoading: (loading: boolean) => void;
    setError: (error: string | null) => void;
    setDifficulty: (difficulty: DifficultyLevel) => void;

    // Limpieza al salir de la pantalla
    reset: () => void;
}

export const useSongScreenStore = create<SongScreenState>((set) => ({
    isLoading: true, // Empezamos cargando
    error: null,
    selectedDifficulty: null,

    setLoading: (loading) => set({ isLoading: loading }),
    setError: (error) => set({ error }),

    setDifficulty: (difficulty) => set({
        selectedDifficulty: difficulty,
        isLoading: false // Por si queremos disparar un micro-loading al cambiar
    }),

    reset: () => set({
        isLoading: true,
        error: null,
        selectedDifficulty: null
    }),
}));