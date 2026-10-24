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
                // Se fusiona con el resumen previo: un resumen derivado de un DTO completo puede
                // no traer todos los metadatos de búsqueda que mandó el backend (y viceversa).
                ...Object.fromEntries(newItems.map((item) => {
                    const id = item.id.toString();
                    return [id, { ...state.items[id], ...item }];
                })),
            },
        })),

    clearItems: () => set({ items: {} }),
}));
