import * as tabSectionService from '../services/tabSection.service';
import * as tabSectionLocal from '../local/tabSection.local';
import { useTabSectionEntitiesStore } from "../stores/entities";
import { TabSectionDTO } from '../types';

export const getById = async (id: string): Promise<TabSectionDTO | null> => {
    let tabSection: TabSectionDTO|null = await tabSectionLocal.getById(id);

    if (!tabSection) {
        try {
            tabSection = await tabSectionService.getById(id);
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

export const getAllById = async (ids: string[]): Promise<TabSectionDTO[]> => {
    const { cached, missing } = await tabSectionLocal.getAllById(ids);

    let remoteTabSections: TabSectionDTO[] = [];

    if (missing.length > 0) {
        try {
            remoteTabSections = await tabSectionService.getAllById(missing);
            if (remoteTabSections.length > 0) {
                tabSectionLocal.saveBatch(remoteTabSections).catch(console.error);
            }
        } catch (error) {
            console.error("[TabSectionRepo] API Error", error);
        }
    }

    const all = [...cached, ...remoteTabSections];
    useTabSectionEntitiesStore.getState().setTabSections(all);

    const map = new Map(all.map(t => [t.id.toString(), t]));
    return ids.map(id => map.get(id)).filter((t): t is TabSectionDTO => !!t);
}

