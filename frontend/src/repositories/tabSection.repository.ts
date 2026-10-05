import * as tabSectionService from '../services/tabSection.service';
import * as tabSectionLocal from '../local/tabSection.local';
import { useTabSectionEntitiesStore } from "../stores/entities";
import { TabSectionDTO } from '../types';
import { createEntityCache } from '../utils/entityCache.utils';

/** Núcleo de caché de la entidad: RAM -> Disco -> API + Stale-While-Revalidate. */
const cache = createEntityCache<TabSectionDTO>({
    name: 'TabSectionRepo',
    getRam: () => useTabSectionEntitiesStore.getState().tabSections,
    setRam: (entities) => useTabSectionEntitiesStore.getState().setTabSections(entities),
    local: tabSectionLocal,
    fetchByIds: tabSectionService.getAllById,
});

export const getById = async (id: string): Promise<TabSectionDTO | null> => {
    let tabSection: TabSectionDTO|null = useTabSectionEntitiesStore.getState().tabSections[id] ?? await tabSectionLocal.getById(id);
    if (tabSection) cache.revalidate([id]);

    if (!tabSection) {
        try {
            tabSection = await tabSectionService.getById(id);
            cache.markFresh([id]);
            if (tabSection) {
                await tabSectionLocal.saveBatch([tabSection]).catch(e => console.error("Local save failed", e));
            }
        } catch (error) {
            console.error(`Error fetching tabSection with ID ${id}:`, error);
            return null;
        }
    }

    if (tabSection) {
        useTabSectionEntitiesStore.getState().setTabSections([tabSection]);
    }

    return tabSection;
}

export const getAllById = cache.getAllById;

