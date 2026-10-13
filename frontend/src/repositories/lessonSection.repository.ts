import * as lessonSectionService from '../services/lessonSection.service';
import * as lessonSectionLocal from '../local/lessonSection.local';
import { useLessonSectionEntitiesStore } from "../stores/entities";
import { LessonSectionDTO } from '../types';
import { createEntityCache } from '../utils/entityCache.utils';

/** Núcleo de caché de la entidad: RAM -> Disco -> API + Stale-While-Revalidate. */
const cache = createEntityCache<LessonSectionDTO>({
    name: 'LessonSectionRepo',
    getRam: () => useLessonSectionEntitiesStore.getState().lessonSections,
    setRam: (entities) => useLessonSectionEntitiesStore.getState().setLessonSections(entities),
    local: lessonSectionLocal,
    fetchByIds: lessonSectionService.getAllById,
});

export const getById = async (id: string): Promise<LessonSectionDTO | null> => {
    let lessonSection: LessonSectionDTO|null = useLessonSectionEntitiesStore.getState().lessonSections[id] ?? await lessonSectionLocal.getById(id);
    if (lessonSection) cache.revalidate([id]);

    if (!lessonSection) {
        try {
            lessonSection = await lessonSectionService.getById(id);
            cache.markFresh([id]);
            if (lessonSection) {
                await lessonSectionLocal.saveBatch([lessonSection]).catch(e => console.error("Local save failed", e));
            }
        } catch (error) {
            console.error(`Error fetching lessonSection with ID ${id}:`, error);
            return null;
        }
    }

    if (lessonSection) {
        useLessonSectionEntitiesStore.getState().setLessonSections([lessonSection]);
    }

    return lessonSection;
}

export const getAllById = cache.getAllById;

