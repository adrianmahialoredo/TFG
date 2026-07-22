import * as instrumentGenreService from '../services/instrumentGenre.service';
import * as instrumentGenreLocal from '../local/instrumentGenre.local';
import * as songLessonRepository from './songLesson.repository';
import { useInstrumentGenreEntitiesStore } from "../stores/entities";
import { InstrumentGenreDTO } from '../types';

/**
 * Repositorio de InstrumentGenreScreen
 * Maneja la lógica de cache + backend fallback para relaciones instrumento-género
 */

/**
 * Obtiene un InstrumentGenreScreen por su ID
 * Cache first, backend fallback
 * @param id El ID del InstrumentGenreScreen
 * @returns InstrumentGenreDTO o null si no se encuentra
 */
export const getById = async (id: string): Promise<InstrumentGenreDTO | null> => {
    let instrumentGenre: InstrumentGenreDTO | null = await instrumentGenreLocal.getById(id);

    if (!instrumentGenre) {
        try {
            instrumentGenre = await instrumentGenreService.getInstrumentGenreById(id);
            if (instrumentGenre) {
                await instrumentGenreLocal.saveBatch([instrumentGenre]).catch(e => console.error("Local save failed", e));
            }
        } catch (error) {
            console.error(`Error fetching InstrumentGenre with ID ${id}:`, error);
            return null;
        }
    }

    if (instrumentGenre) {
        useInstrumentGenreEntitiesStore.getState().setInstrumentGenres([instrumentGenre]);
    }

    return instrumentGenre;
}

/**
 * Obtiene múltiples InstrumentGenres por sus IDs
 * @param ids Array de IDs
 * @returns Array de InstrumentGenreDTO
 */
export const getAllById = async (ids: string[]): Promise<InstrumentGenreDTO[]> => {
    const { cached, missing } = await instrumentGenreLocal.getAllById(ids);

    let remoteInstrumentGenres: InstrumentGenreDTO[] = [];

    if (missing.length > 0) {
        try {
            remoteInstrumentGenres = await instrumentGenreService.getAllById(missing);
            if (remoteInstrumentGenres.length > 0) {
                instrumentGenreLocal.saveBatch(remoteInstrumentGenres).catch(console.error);
            }
        } catch (error) {
            console.error("[InstrumentGenreRepo] API Error", error);
        }
    }

    const all = [...cached, ...remoteInstrumentGenres];
    useInstrumentGenreEntitiesStore.getState().setInstrumentGenres(all);

    const map = new Map(all.map(ig => [ig.id.toString(), ig]));
    return ids.map(id => map.get(id)).filter((ig): ig is InstrumentGenreDTO => !!ig);
}

/**
 * Obtiene todos los InstrumentGenres de un género específico
 * @param genreId El ID del género
 * @returns Array de InstrumentGenreDTO
 */
export const getByGenreId = async (genreId: string): Promise<InstrumentGenreDTO[]> => {
    try {
        // @TODO: Esta llamada requiere un endpoint en el backend
        // GET /instrument-genres/by-genre/{genreId}
        // Que devuelva todos los InstrumentGenres para ese género
        const response = await instrumentGenreService.getByGenreId(genreId);
        
        if (response.length > 0) {
            useInstrumentGenreEntitiesStore.getState().setInstrumentGenres(response);
            instrumentGenreLocal.saveBatch(response).catch(console.error);

            // Cargar las lecciones asociadas en segundo plano
            const allLessonIds = response.flatMap(ig => ig.lessonIds || []);
            if (allLessonIds.length > 0) {
                songLessonRepository.getAllById(allLessonIds).catch(console.error);
            }
        }

        return response;
    } catch (error) {
        console.error("[InstrumentGenreRepo] Error en getByGenreId:", error);
        return [];
    }
}

/**
 * Obtiene todos los InstrumentGenres de un instrumento específico
 * @param instrumentId El ID del instrumento
 * @returns Array de InstrumentGenreDTO
 */
export const getByInstrumentId = async (instrumentId: string): Promise<InstrumentGenreDTO[]> => {
    try {
        // @TODO: Esta llamada requiere un endpoint en el backend
        // GET /instrument-genres/by-instrument/{instrumentId}
        const response = await instrumentGenreService.getByInstrumentId(instrumentId);
        
        if (response.length > 0) {
            useInstrumentGenreEntitiesStore.getState().setInstrumentGenres(response);
            instrumentGenreLocal.saveBatch(response).catch(console.error);
        }

        return response;
    } catch (error) {
        console.error("[InstrumentGenreRepo] Error en getByInstrumentId:", error);
        return [];
    }
}

