import * as songService from '../services/song.service';
import * as songLocal from '../local/song.local';
import * as songLessonRepository from './songLesson.repository';
import * as tabSectionRepository from './tabSection.repository';
import * as chordRepository from './chord.repository';
import * as itemRepository from './item.repository';
import { useSongEntitiesStore, useItemEntitiesStore } from "../stores/entities";
import { SongDTO, ItemType } from "../types";
import axios from 'axios';
import { toItemSummary } from '../utils/mappers.utils';
import { createEntityCache, OnFresh } from '../utils/entityCache.utils';

/** Núcleo de caché de la entidad: RAM -> Disco -> API + Stale-While-Revalidate. */
const cache = createEntityCache<SongDTO>({
    name: 'SongRepo',
    getRam: () => useSongEntitiesStore.getState().songs,
    // El resumen de la ItemEntitiesStore (tarjetas de carrusel) se mantiene al día con el DTO completo.
    setRam: (entities) => {
        useSongEntitiesStore.getState().setSongs(entities);
        useItemEntitiesStore.getState().setItems(entities.map((e) => toItemSummary(e, ItemType.SONG)));
    },
    local: songLocal,
    fetchByIds: songService.getAllById,
});

export const getById = async (id: string): Promise<SongDTO | null> => {
    let song: SongDTO | null = null;

    try {
        song = useSongEntitiesStore.getState().songs[id] ?? await songLocal.getById(id);
        if (song) cache.revalidate([id]);
        if (!song) {
            try {
                song = await songService.getById(id);
                cache.markFresh([id]);
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

export const getAllById = cache.getAllById;

/** Guarda en RAM + Disco DTOs completos recibidos por otra vía (p.ej. un feed mixto de items). */
export const hydrate = (entities: SongDTO[]): string[] => cache.hydrate(entities);

// --- CARRUSELES CON DTO COMPLETO (decisión de negocio) ---
// El usuario suele abrir lo que ve en estos carruseles: el backend devuelve el SongDTO
// completo, así el detalle abre al instante. La tarjeta se pinta con el resumen, que la
// propia caché mantiene en la ItemEntitiesStore.

export const getPopularByGenreId = (genreId: string, onFresh?: OnFresh): Promise<string[]> =>
    cache.list(`popular_genre_${genreId}`, () => songService.getPopularByGenreId(genreId), onFresh);

export const getClassicByGenreId = (genreId: string, onFresh?: OnFresh): Promise<string[]> =>
    cache.list(`classic_genre_${genreId}`, () => songService.getClassicByGenreId(genreId), onFresh);

export const getNewByGenreId = (genreId: string, onFresh?: OnFresh): Promise<string[]> =>
    cache.list(`new_genre_${genreId}`, () => songService.getNewByGenreId(genreId), onFresh);

export const getPopular = (onFresh?: OnFresh): Promise<string[]> =>
    cache.list('popular', () => songService.getPopular(), onFresh);

export const getNew = (onFresh?: OnFresh): Promise<string[]> =>
    cache.list('new', () => songService.getNew(), onFresh);

/** Personalizado por usuario: su índice se borra en el logout. */
export const SPOTIFY_INDEX_KEY = 'spotify';

export const getFromSpotify = (onFresh?: OnFresh): Promise<string[]> =>
    cache.list(SPOTIFY_INDEX_KEY, () => songService.getFromSpotify(), onFresh);

// --- CARRUSELES POR CONVENCIÓN (el backend devuelve IDs; se pintan con el catálogo de items) ---

export const getRecommended = (onFresh?: OnFresh): Promise<string[]> =>
    itemRepository.getFeed('songs_recommended', () => songService.getRecommendedIds(), onFresh);
