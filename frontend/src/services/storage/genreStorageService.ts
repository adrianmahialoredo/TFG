import AsyncStorage from "@react-native-async-storage/async-storage";
import { GenreDTO } from "../../types/GenreDTO";
import { getData, storeData } from "./storageService";

const GENRES_KEY = 'genres';

/* Guarda todos los géneros */
export const storeGenres = (genres: GenreDTO[]) => storeData(GENRES_KEY, genres);

/* Obtiene todos los géneros */
export const getStoredGenres = () => getData<GenreDTO[]>(GENRES_KEY);

/* Obtiene un género por su ID */
export const getStoredGenresById = async (id: string): Promise<GenreDTO | null> => {
  const genres = await getStoredGenres();
  return genres?.find((genre) => genre.id === id) || null;
};

/* Obtiene varios géneros a partir de una lista de IDs */
export const getStoredGenresByIdList = async (ids: string[]): Promise<GenreDTO[]> => {
  const genres = await getStoredGenres();
  return genres?.filter((genre) => ids.includes(genre.id)) || [];
};