import * as songLessonService from '../services/songLesson.service';
import * as songLessonLocal from '../local/songLesson.local';
import { useSongLessonEntitiesStore } from "../stores/entities";
import { SongLessonDTO } from '../types';
import { createEntityCache } from '../utils/entityCache.utils';

/** Núcleo de caché de la entidad: RAM -> Disco -> API + Stale-While-Revalidate. */
const cache = createEntityCache<SongLessonDTO>({
    name: 'SongLessonRepo',
    getRam: () => useSongLessonEntitiesStore.getState().songLessons,
    setRam: (entities) => useSongLessonEntitiesStore.getState().setSongLessons(entities),
    local: songLessonLocal,
    fetchByIds: songLessonService.getAllById,
});

export const getById = async (id: string): Promise<SongLessonDTO | null> => {
    let songLesson: SongLessonDTO|null = useSongLessonEntitiesStore.getState().songLessons[id] ?? await songLessonLocal.getById(id);
    if (songLesson) cache.revalidate([id]);

    if (!songLesson) {
        try {
            songLesson = await songLessonService.getById(id);
            cache.markFresh([id]);
            if (songLesson) {
                await songLessonLocal.saveBatch([songLesson]).catch(e => console.error("Local save failed", e));
            }
        } catch (error) {
            console.error(`Error fetching songLesson with ID ${id}:`, error);
            return null;
        }
    }

    if (songLesson) {
        useSongLessonEntitiesStore.getState().setSongLessons([songLesson]);
    }

    return songLesson;
}

export const getAllById = cache.getAllById;
