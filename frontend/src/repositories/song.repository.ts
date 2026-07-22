import * as songService from '../services/song.service';
import * as songLocal from '../local/song.local';
import * as songLessonRepository from './songLesson.repository';
import * as tabSectionRepository from './tabSection.repository';
import * as chordRepository from './chord.repository';
import { useSongEntitiesStore } from "../stores/entities";
import { SongDTO } from "../types";
import axios from 'axios';

/**
 * Helper para orquestar la hidratación de RAM, Local y retornar IDs
 * Maneja el guardado de la "Foto" (Índice) para persistencia offline
 */
const hydrateAndGetIds = async (songs: SongDTO[], cacheKey?: string): Promise<string[]> => {
    if (songs.length === 0) return [];

    const ids = songs.map(s => s.id.toString());

    // 1. Hidratar RAM (Despensa)
    useSongEntitiesStore.getState().setSongs(songs);

    // 2. Hidratar Local (Baúl de entidades)
    songLocal.saveBatch(songs).catch(console.error);

    // 3. Si hay clave de caché, guardar la "Foto" (Índice/Orden)
    if (cacheKey) {
        songLocal.saveQueryIndex(cacheKey, ids).catch(console.error);
    }

    return ids;
};

export const getById = async (id: string): Promise<SongDTO | null> => {
    let song: SongDTO | null = null;

    try {
        song = await songLocal.getById(id);
        if (!song) {
            try {
                song = await songService.getById(id);
                if (song) await songLocal.saveSingle(song).catch(e => console.error("Error guardando local", e));
            } catch (serviceError) {
                if (axios.isAxiosError(serviceError) && serviceError.response?.status === 404) {
                    console.warn(`[SongRepo] getById(${id}) no encontrado en backend`);
                    return null;
                }

                throw serviceError;
            }
        }

        if (!song) return null;

        useSongEntitiesStore.getState().setSongs([song]);

        if (song.lessonIds?.length > 0) {
            try {
                await songLessonRepository.getAllById(song.lessonIds).then(async (lessons) => {
                    const tabIds = lessons.flatMap(l => l.tabSectionIds || []);
                    const chordIds = lessons.flatMap(l => l.chordIds || []);
                    await Promise.allSettled([
                        tabIds.length > 0 ? tabSectionRepository.getAllById(tabIds) : Promise.resolve(),
                        chordIds.length > 0 ? chordRepository.getAllById(chordIds) : Promise.resolve()
                    ]);
                });
            } catch (childError) {
                console.error("[SongRepo] Error en dependencias:", childError);
            }
        }
    } catch (error) {
        console.error(`[SongRepo] Error en getById(${id}):`, error);
        return null;
    }

    return song;
};

export const getAllById = async (ids: string[]): Promise<SongDTO[]> => {
    const { cached, missing } = await songLocal.getAllById(ids);
    let remoteSongs: SongDTO[] = [];

    if (missing.length > 0) {
        try {
            remoteSongs = await songService.getAllById(missing);
            if (remoteSongs.length > 0) {
                songLocal.saveBatch(remoteSongs).catch(console.error);
            }
        } catch (error) {
            console.error("[SongRepo] API Error", error);
        }
    }

    const all = [...cached, ...remoteSongs];
    useSongEntitiesStore.getState().setSongs(all);

    const map = new Map(all.map(s => [s.id.toString(), s]));
    return ids.map(id => map.get(id)).filter((s): s is SongDTO => !!s);
};

// --- MÉTODOS POR GÉNERO CON FALLBACK OFFLINE ---

export const getPopularByGenreId = async (genreId: string): Promise<string[]> => {
    const key = `popular_genre_${genreId}`;
    try {
        const songs = await songService.getPopularByGenreId(genreId);
        return await hydrateAndGetIds(songs, key);
    } catch (error) {
        return (await songLocal.getQueryIndex(key)) || [];
    }
};

export const getClassicByGenreId = async (genreId: string): Promise<string[]> => {
    const key = `classic_genre_${genreId}`;
    try {
        const songs = await songService.getClassicByGenreId(genreId);
        return await hydrateAndGetIds(songs, key);
    } catch (error) {
        return (await songLocal.getQueryIndex(key)) || [];
    }
};

export const getNewByGenreId = async (genreId: string): Promise<string[]> => {
    const key = `new_genre_${genreId}`;
    try {
        const songs = await songService.getNewByGenreId(genreId);
        return await hydrateAndGetIds(songs, key);
    } catch (error) {
        return (await songLocal.getQueryIndex(key)) || [];
    }
};

/**
 * Versión optimizada para GenreSongs (Devuelve objetos para evitar parpadeos en carruseles)
 */
export const getGenreSongCategories = async (genreId: string) => {
    const keyBase = `categories_genre_${genreId}`;
    try {
        const [populars, classics, news] = await Promise.all([
            songService.getPopularByGenreId(genreId),
            songService.getClassicByGenreId(genreId),
            songService.getNewByGenreId(genreId)
        ]);

        // Hidratamos todo de golpe
        await hydrateAndGetIds([...populars, ...classics, ...news]);

        // Guardamos los índices individuales para fallback
        songLocal.saveQueryIndex(`${keyBase}_pop`, populars.map(s => s.id.toString()));
        songLocal.saveQueryIndex(`${keyBase}_cla`, classics.map(s => s.id.toString()));
        songLocal.saveQueryIndex(`${keyBase}_new`, news.map(s => s.id.toString()));

        return { populars, classics, news };
    } catch (error) {
        console.error("[SongRepo] Error en categorías, intentando local...");
        // Fallback: obtenemos los IDs y luego los objetos de la RAM/Local
        const [pIds, cIds, nIds] = await Promise.all([
            songLocal.getQueryIndex(`${keyBase}_pop`),
            songLocal.getQueryIndex(`${keyBase}_cla`),
            songLocal.getQueryIndex(`${keyBase}_new`)
        ]);

        const [populars, classics, news] = await Promise.all([
            getAllById(pIds || []),
            getAllById(cIds || []),
            getAllById(nIds || [])
        ]);

        return { populars, classics, news };
    }
};

// --- MÉTODOS HOME CON FALLBACK OFFLINE ---

export const getPopular = async (): Promise<string[]> => {
    const key = 'home_popular';
    try {
        const songs = await songService.getPopular();
        return await hydrateAndGetIds(songs, key);
    } catch (error) {
        return (await songLocal.getQueryIndex(key)) || [];
    }
};

export const getNew = async (): Promise<string[]> => {
    const key = 'home_new';
    try {
        const songs = await songService.getNew();
        return await hydrateAndGetIds(songs, key);
    } catch (error) {
        return (await songLocal.getQueryIndex(key)) || [];
    }
};

export const getRecommended = async (): Promise<string[]> => {
    const key = 'home_recommended';
    try {
        const songs = await songService.getRecommended();
        return await hydrateAndGetIds(songs, key);
    } catch (error) {
        return (await songLocal.getQueryIndex(key)) || [];
    }
};

export const getFromSpotify = async (): Promise<string[]> => {
    const key = 'home_spotify';
    try {
        const songs = await songService.getFromSpotify();
        return await hydrateAndGetIds(songs, key);
    } catch (error) {
        return (await songLocal.getQueryIndex(key)) || [];
    }
};