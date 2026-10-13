import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';

/** Items que guarda la sección "Recientes" del Home, por usuario. */
export const MAX_RECENT_ITEMS = 12;

/**
 * Items abiertos más recientemente por cada usuario (sección "Recientes" del Home).
 *
 * Vive solo en el front: el backend no sabe qué ha abierto cada usuario. Se guardan solo los
 * IDs, del más reciente al más antiguo; las tarjetas se pintan con el catálogo de items
 * (ItemEntitiesStore), que ya está en RAM y en disco desde el arranque.
 *
 * Va por usuario (`byUser[userId]`) para que, en un mismo dispositivo, nadie vea los recientes
 * de otro, y para que cada uno los conserve al volver a iniciar sesión.
 */
interface RecentItemsState {
    byUser: Record<string, string[]>;

    /** Pone el item el primero de los recientes del usuario (sin repetir y hasta MAX_RECENT_ITEMS). */
    markAsAccessed: (userId: string, itemId: string) => void;
    /** Quita items que ya no existen (p. ej. borrados del catálogo). */
    removeItems: (itemIds: string[]) => void;
}

export const useRecentItemsStore = create<RecentItemsState>()(
    persist(
        (set) => ({
            byUser: {},

            markAsAccessed: (userId, itemId) =>
                set((state) => {
                    const current = state.byUser[userId] ?? [];
                    // Ya era el último abierto: no se reescribe (ni se re-renderiza el Home)
                    if (current[0] === itemId) return state;
                    const next = [itemId, ...current.filter((id) => id !== itemId)].slice(0, MAX_RECENT_ITEMS);
                    return { byUser: { ...state.byUser, [userId]: next } };
                }),

            removeItems: (itemIds) =>
                set((state) => {
                    const toRemove = new Set(itemIds);
                    const byUser = Object.fromEntries(
                        Object.entries(state.byUser).map(([userId, ids]) => [userId, ids.filter((id) => !toRemove.has(id))])
                    );
                    return { byUser };
                }),
        }),
        {
            name: 'recent-items-store',
            storage: createJSONStorage(() => AsyncStorage),
        }
    )
);
