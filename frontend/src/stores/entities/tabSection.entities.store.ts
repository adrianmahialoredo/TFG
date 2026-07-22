import { create } from 'zustand';
import { TabSectionDTO } from '../../types';

interface TabSectionEntitiesState {
  tabSections: Record<string, TabSectionDTO>;

  setTabSections: (tabSections: TabSectionDTO[]) => void;
  updateTabSection: (id: string, data: Partial<TabSectionDTO>) => void;
  removeTabSections: (ids: string[]) => void;
  clearTabSections: () => void;
}

export const useTabSectionEntitiesStore = create<TabSectionEntitiesState>((set) => ({
  tabSections: {},

  setTabSections: (newTabSections) =>
      set((state) => {
        const updatedTabSections = { ...state.tabSections };

        newTabSections.forEach((tabSection) => {
          updatedTabSections[tabSection.id.toString()] = tabSection;
        });

        return { tabSections: updatedTabSections };
      }),

  updateTabSection: (id, data) =>
      set((state) => {
        const tabSection = state.tabSections[id];
        if (!tabSection) return state;

        return {
          tabSections: {
            ...state.tabSections,
            [id]: { ...tabSection, ...data },
          },
        };
      }),

  removeTabSections: (ids) =>
      set((state) => {
        const updatedTabSections = { ...state.tabSections };
        ids.forEach((id) => delete updatedTabSections[id]);
        return { tabSections: updatedTabSections };
      }),

  clearTabSections: () => set({ tabSections: {} }),
}));

