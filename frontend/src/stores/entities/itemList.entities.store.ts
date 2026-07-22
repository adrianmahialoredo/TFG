import { create } from 'zustand';
import { ItemListDTO } from '../../types';

interface ItemListEntitiesState {
  itemLists: Record<string, ItemListDTO>;

  setItemLists: (itemLists: ItemListDTO[]) => void;
  updateItemList: (id: string, data: Partial<ItemListDTO>) => void;
  removeItemLists: (ids: string[]) => void;
  clearItemLists: () => void;
}

export const useItemListEntitiesStore = create<ItemListEntitiesState>((set) => ({
  itemLists: {},

  setItemLists: (newItemLists) =>
      set((state) => {
        const updatedItemLists = { ...state.itemLists };

        newItemLists.forEach((itemList) => {
          updatedItemLists[itemList.id.toString()] = itemList;
        });

        return { itemLists: updatedItemLists };
      }),

  updateItemList: (id, data) =>
      set((state) => {
        const itemList = state.itemLists[id];
        if (!itemList) return state;

        return {
          itemLists: {
            ...state.itemLists,
            [id]: { ...itemList, ...data },
          },
        };
      }),

  removeItemLists: (ids) =>
      set((state) => {
        const updatedItemLists = { ...state.itemLists };
        ids.forEach((id) => delete updatedItemLists[id]);
        return { itemLists: updatedItemLists };
      }),

  clearItemLists: () => set({ itemLists: {} }),
}));

