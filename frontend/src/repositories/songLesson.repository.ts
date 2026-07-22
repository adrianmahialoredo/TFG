import * as songLessonService from '../services/songLesson.service';
import * as songLessonLocal from '../local/songLesson.local';
import { useSongLessonEntitiesStore } from "../stores/entities";
import { SongLessonDTO } from '../types';

export const getById = async (id: string): Promise<SongLessonDTO | null> => {
    let songLesson: SongLessonDTO|null = await songLessonLocal.getById(id);

    if (!songLesson) {
        try {
            songLesson = await songLessonService.getById(id);
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

export const getAllById = async (ids: string[]): Promise<SongLessonDTO[]> => {
    const { cached, missing } = await songLessonLocal.getAllById(ids);

    let remoteSongLessons: SongLessonDTO[] = [];

    if (missing.length > 0) {
        try {
            remoteSongLessons = await songLessonService.getAllById(missing);
            if (remoteSongLessons.length > 0) {
                songLessonLocal.saveBatch(remoteSongLessons).catch(console.error);
            }
        } catch (error) {
            console.error("[SongLessonRepo] API Error", error);
        }
    }

    const all = [...cached, ...remoteSongLessons];
    useSongLessonEntitiesStore.getState().setSongLessons(all);

    const map = new Map(all.map(s => [s.id.toString(), s]));
    return ids.map(id => map.get(id)).filter((s): s is SongLessonDTO => !!s);
};
