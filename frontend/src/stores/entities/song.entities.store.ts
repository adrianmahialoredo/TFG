import { create } from 'zustand';
import { SongDTO } from '../../types';

interface SongEntitiesState {
  // Diccionario de canciones: { "1": SongDTO, "2": SongDTO }
  songs: Record<string, SongDTO>;

  // Acciones
  setSongs: (songs: SongDTO[]) => void;
  updateSong: (id: string, data: Partial<SongDTO>) => void;
  removeSongs: (ids: string[]) => void;
  clearSongs: () => void;
}

/**
 * Store de Entidades de Canciones (La Despensa Global)
 * Mantiene en RAM todos los DTOs de canciones descargados.
 */
export const useSongEntitiesStore = create<SongEntitiesState>((set) => ({
  songs: {},

  // Agrega o actualiza múltiples canciones a la vez
  setSongs: (newSongs) =>
      set((state) => {
        const updatedSongs = { ...state.songs };

        newSongs.forEach((song) => {
          // Nos aseguramos de que el ID sea string por seguridad
          updatedSongs[song.id.toString()] = song;
        });

        return { songs: updatedSongs };
      }),

  // Actualiza solo una parte de una canción (ej. marcar como favorita)
  updateSong: (id, data) =>
      set((state) => {
        const song = state.songs[id];
        if (!song) return state; // Si no existe, no hacemos nada

        return {
          songs: {
            ...state.songs,
            [id]: { ...song, ...data },
          },
        };
      }),

  // Elimina canciones específicas
  removeSongs: (ids) =>
      set((state) => {
        const updatedSongs = { ...state.songs };
        ids.forEach((id) => delete updatedSongs[id]);
        return { songs: updatedSongs };
      }),

  // Limpia toda la despensa (ej. al hacer logout)
  clearSongs: () => set({ songs: {} }),
}));