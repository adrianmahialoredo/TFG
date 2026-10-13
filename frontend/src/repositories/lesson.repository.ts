import * as lessonService from '../services/lesson.service';
import * as lessonLocal from '../local/lesson.local';
import * as itemRepository from './item.repository';
import * as lessonSectionRepository from './lessonSection.repository';
import { useLessonEntitiesStore, useItemEntitiesStore } from "../stores/entities";
import { LessonDTO, ItemType } from "../types";
import { toItemSummary } from '../utils/mappers.utils';
import { createEntityCache, OnFresh } from '../utils/entityCache.utils';

/** Núcleo de caché de la entidad: RAM -> Disco -> API + Stale-While-Revalidate. */
const cache = createEntityCache<LessonDTO>({
    name: 'LessonRepo',
    getRam: () => useLessonEntitiesStore.getState().lessons,
    // El resumen de la ItemEntitiesStore (tarjetas de carrusel) se mantiene al día con el DTO completo.
    setRam: (entities) => {
        useLessonEntitiesStore.getState().setLessons(entities);
        useItemEntitiesStore.getState().setItems(entities.map((e) => toItemSummary(e, ItemType.LESSON)));
    },
    local: lessonLocal,
    fetchByIds: lessonService.getAllById,
});

export const getById = async (id: string): Promise<LessonDTO | null> => {
    let lesson: LessonDTO | null = null;

    try {
        // 1. Intentar obtener la lección (Local o API)
        lesson = useLessonEntitiesStore.getState().lessons[id] ?? await lessonLocal.getById(id);
        if (lesson) cache.revalidate([id]);
        if (!lesson) {
            lesson = await lessonService.getById(id);
            cache.markFresh([id]);
            if (lesson) await lessonLocal.saveSingle(lesson).catch(e => console.error("Error guardando lección local", e));
        }

        if (!lesson) return null;

        // 2. ACTUALIZAR RAM DE LA LECCIÓN
        useLessonEntitiesStore.getState().setLessons([lesson]);

        // 3. DEPENDENCIAS: secciones de la lección (contenido HTML)
        if (lesson.sectionIds?.length > 0) {
            await lessonSectionRepository.getAllById(lesson.sectionIds)
                .catch(e => console.error("[LessonRepo] Error en secciones:", e));
        }

    } catch (error) {
        console.error(`[LessonRepo] Error crítico en getById(${id}):`, error);
        return null;
    }

    return lesson;
}

export const getAllById = cache.getAllById;

/** Guarda en RAM + Disco DTOs completos recibidos por otra vía (p.ej. un feed mixto de items). */
export const hydrate = (entities: LessonDTO[]): string[] => cache.hydrate(entities);

// --- LISTAS (Cache-First: índice guardado al instante + refresco de red vía `onFresh`) ---

export const getByGenreId = (genreId: string, onFresh?: OnFresh): Promise<string[]> =>
    cache.list(`genre_${genreId}`, () => lessonService.getByGenreId(genreId), onFresh);

export const getRecommendedByGenreId = (genreId: string, onFresh?: OnFresh): Promise<string[]> =>
    cache.list(`recommended_genre_${genreId}`, () => lessonService.getRecommendedByGenreId(genreId), onFresh);

// --- CARRUSELES (el backend devuelve IDs; las tarjetas se pintan con el catálogo de items) ---
// Devuelven IDs de la `ItemEntitiesStore`. El LessonDTO completo solo se pide al abrir el detalle.

export const getPopular = (onFresh?: OnFresh): Promise<string[]> =>
    itemRepository.getFeed('lessons_popular', () => lessonService.getPopularIds(), onFresh);

export const getNew = (onFresh?: OnFresh): Promise<string[]> =>
    itemRepository.getFeed('lessons_new', () => lessonService.getNewIds(), onFresh);

export const getPopularByInstrumentId = (instrumentId: string, onFresh?: OnFresh): Promise<string[]> =>
    itemRepository.getFeed(`lessons_popular_instrument_${instrumentId}`, () => lessonService.getPopularByInstrumentIds(instrumentId), onFresh);

// --- RECOMENDACIONES (el backend devuelve el LessonDTO completo) ---

/** Personalizado por usuario: su índice se borra en el logout. */
export const RECOMMENDED_INDEX_KEY = 'recommended';

export const getRecommended = (userId: string, onFresh?: OnFresh): Promise<string[]> =>
    cache.list(RECOMMENDED_INDEX_KEY, () => lessonService.getRecommendedByUser(userId), onFresh);
