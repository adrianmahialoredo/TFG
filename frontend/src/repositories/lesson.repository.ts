import * as lessonService from '../services/lesson.service';
import * as lessonLocal from '../local/lesson.local';
import { useLessonEntitiesStore } from "../stores/entities";
import { LessonDTO } from "../types";

/**
 * Helper para orquestar la hidratación de RAM, Local y retornar IDs.
 * Maneja el guardado de la "Foto" (Índice) para persistencia offline.
 */
const hydrateAndGetIds = async (lessons: LessonDTO[], cacheKey?: string): Promise<string[]> => {
    if (lessons.length === 0) return [];

    const ids = lessons.map(l => l.id.toString());

    // 1. Hidratar RAM (Despensa)
    useLessonEntitiesStore.getState().setLessons(lessons);

    // 2. Hidratar Local (Baúl de entidades)
    lessonLocal.saveBatch(lessons).catch(console.error);

    // 3. Si hay clave de caché, guardar la "Foto" (Índice/Orden)
    if (cacheKey) {
        lessonLocal.saveQueryIndex(cacheKey, ids).catch(console.error);
    }

    return ids;
};

export const getById = async (id: string): Promise<LessonDTO | null> => {
    let lesson: LessonDTO | null = null;

    try {
        // 1. Intentar obtener la lección (Local o API)
        lesson = await lessonLocal.getById(id);
        if (!lesson) {
            lesson = await lessonService.getById(id);
            if (lesson) await lessonLocal.saveSingle(lesson).catch(e => console.error("Error guardando lección local", e));
        }

        if (!lesson) return null;

        // 2. ACTUALIZAR RAM DE LA LECCIÓN
        useLessonEntitiesStore.getState().setLessons([lesson]);

    } catch (error) {
        console.error(`[LessonRepo] Error crítico en getById(${id}):`, error);
        return null;
    }

    return lesson;
}

export const getAllById = async (ids: string[]): Promise<LessonDTO[]> => {
    // 1. Preguntar al local
    const { cached, missing } = await lessonLocal.getAllById(ids);

    let remoteLessons: LessonDTO[] = [];

    // 2. Si falta algo, vamos a la API directamente con la lista que nos dio el local
    if (missing.length > 0) {
        try {
            remoteLessons = await lessonService.getAllById(missing);
            if (remoteLessons.length > 0) {
                lessonLocal.saveBatch(remoteLessons).catch(console.error);
            }
        } catch (error) {
            console.error("[LessonRepo] API Error", error);
        }
    }

    // 3. Unificamos y actualizamos la RAM (Despensa)
    const all = [...cached, ...remoteLessons];
    useLessonEntitiesStore.getState().setLessons(all);

    // 4. Reordenar según el array de 'ids' original (Fundamental para la UI)
    const map = new Map(all.map(s => [s.id.toString(), s]));
    return ids.map(id => map.get(id)).filter((s): s is LessonDTO => !!s);
}

/**
 * Obtiene lecciones de un género específico
 * Retorna IDs para mantener la arquitectura (Entidades pesadas se recuperan por ID)
 */
export const getByGenreId = async (genreId: string): Promise<string[]> => {
    try {
        const lessons = await lessonService.getByGenreId(genreId);
        if (lessons.length > 0) {
            useLessonEntitiesStore.getState().setLessons(lessons);
            await lessonLocal.saveBatch(lessons).catch(console.error);
        }
        return lessons.map(s => s.id.toString());
    } catch (error) {
        console.error("[LessonRepo] Error en getByGenreId:", error);
        return [];
    }
}

// --- MÉTODOS HOME CON FALLBACK OFFLINE ---

export const getPopular = async (): Promise<string[]> => {
    const key = 'home_popular';
    try {
        const lessons = await lessonService.getPopular();
        return await hydrateAndGetIds(lessons, key);
    } catch (error) {
        return (await lessonLocal.getQueryIndex(key)) || [];
    }
}

export const getNew = async (): Promise<string[]> => {
    const key = 'home_new';
    try {
        const lessons = await lessonService.getNew();
        return await hydrateAndGetIds(lessons, key);
    } catch (error) {
        return (await lessonLocal.getQueryIndex(key)) || [];
    }
}

export const getRecommended = async (): Promise<string[]> => {
    const key = 'home_recommended';
    try {
        const lessons = await lessonService.getRecommended();
        return await hydrateAndGetIds(lessons, key);
    } catch (error) {
        return (await lessonLocal.getQueryIndex(key)) || [];
    }
}

