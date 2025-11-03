import { SongDTO } from '../../types/SongDTO';
import { storeData, getData } from './storageService';

const POPULAR_SONGS_KEY = 'popularSongs';
const NEW_SONGS_KEY = 'newSongs';
const RECOMMENDED_SONGS_KEY = 'recommendedSongs';

export const storePopularSongs = (songs: SongDTO[]) => storeData(POPULAR_SONGS_KEY, songs);
export const getPopularSongs = () => getData<SongDTO[]>(POPULAR_SONGS_KEY);

export const storeNewSongs = (songs: SongDTO[]) => storeData(NEW_SONGS_KEY, songs);
export const getNewSongs = () => getData<SongDTO[]>(NEW_SONGS_KEY);

export const storeRecommendedSongs = (songs: SongDTO[]) => storeData(RECOMMENDED_SONGS_KEY, songs);
export const getRecommendedSongs = () => getData<SongDTO[]>(RECOMMENDED_SONGS_KEY);
