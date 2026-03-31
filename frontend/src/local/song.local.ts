import { SongDTO } from '../types';
import { storeData, getData } from './storage.local';

const POPULAR_SONGS_KEY = 'popularSongs';
const NEW_SONGS_KEY = 'newSongs';
const RECOMMENDED_SONGS_KEY = 'recommendedSongs';
const SONG_CACHE_PREFIX = 'song_';

export const storePopularSongs = async (songs: SongDTO[]): Promise<void> => {
  return storeData(POPULAR_SONGS_KEY, songs);
};
export const getPopularSongs = async (): Promise<SongDTO[] | null> => {
  return getData<SongDTO[]>(POPULAR_SONGS_KEY);
};

export const storeNewSongs = async (songs: SongDTO[]): Promise<void> => {
  return storeData(NEW_SONGS_KEY, songs);
};
export const getNewSongs = async (): Promise<SongDTO[] | null> => {
  return getData<SongDTO[]>(NEW_SONGS_KEY);
};

export const storeRecommendedSongs = async (songs: SongDTO[]): Promise<void> => {
  return storeData(RECOMMENDED_SONGS_KEY, songs);
};
export const getRecommendedSongs = async (): Promise<SongDTO[] | null> => {
  return getData<SongDTO[]>(RECOMMENDED_SONGS_KEY);
};

/**
 * Almacena una canción individual en la cache
 * @param song La canción a guardar
 */
export const cacheSong = async (song: SongDTO): Promise<void> => {
  const key = `${SONG_CACHE_PREFIX}${song.id}`;
  return storeData(key, song);
};

/**
 * Obtiene una canción individual de la cache
 * @param songId El ID de la canción
 */
export const getCachedSong = async (songId: number): Promise<SongDTO | null> => {
  const key = `${SONG_CACHE_PREFIX}${songId}`;
  return getData<SongDTO>(key);
};

/**
 * Almacena múltiples canciones en la cache
 * @param songs Las canciones a guardar
 */
export const cacheSongs = async (songs: SongDTO[]): Promise<void> => {
  for (const song of songs) {
    await cacheSong(song);
  }
};

/**
 * Obtiene múltiples canciones de la cache
 * @param songIds Los IDs de las canciones
 */
export const getCachedSongs = async (songIds: number[]): Promise<{
  cached: SongDTO[];
  missing: number[];
}> => {
  const cached: SongDTO[] = [];
  const missing: number[] = [];

  for (const id of songIds) {
    const song = await getCachedSong(id);
    if (song) {
      cached.push(song);
    } else {
      missing.push(id);
    }
  }

  return { cached, missing };
};

