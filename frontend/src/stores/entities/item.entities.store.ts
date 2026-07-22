import { create } from 'zustand';
import { AnyItemDTO } from '../../types';

interface ItemEntitiesState {
    items: Record<string, AnyItemDTO>;
    setItems: (newItems: AnyItemDTO[]) => void;
    updateItem: (id: string, data: Partial<AnyItemDTO>) => void;
    clearItems: () => void;
}

export const useItemEntitiesStore = create<ItemEntitiesState>((set) => ({
    items: {},

    setItems: (newItems) =>
        set((state) => {
            // Creamos el nuevo mapa de items
            const newItemsMap = Object.fromEntries(
                newItems.map((item) => [item.id.toString(), item])
            );

            return {
                items: {
                    ...state.items,
                    ...newItemsMap,
                } as Record<string, AnyItemDTO>, // <-- El "as" mágico para evitar el error TS2345
            };
        }),

    updateItem: (id, data) =>
        set((state) => {
            const currentItem = state.items[id];
            if (!currentItem) return state;

            return {
                items: {
                    ...state.items,
                    [id]: { ...currentItem, ...data } as AnyItemDTO, // <-- Otro casteo necesario aquí
                },
            };
        }),

    clearItems: () => set({ items: {} }),
}));