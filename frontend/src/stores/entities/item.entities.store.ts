import { create } from 'zustand';
import { ItemSummaryDTO } from '../../types';

/**
 * Store de los resúmenes de items (catálogo + feeds mezclados).
 * Nunca guarda DTOs completos: esos viven en la Store de su entidad.
 */
interface ItemEntitiesState {
    items: Record<string, ItemSummaryDTO>;
    setItems: (newItems: ItemSummaryDTO[]) => void;
    clearItems: () => void;
}

export const useItemEntitiesStore = create<ItemEntitiesState>((set) => ({
    items: {},

    setItems: (newItems) =>
        set((state) => ({
            items: {
                ...state.items,
                ...Object.fromEntries(newItems.map((item) => [item.id.toString(), item])),
            },
        })),

    clearItems: () => set({ items: {} }),
}));
