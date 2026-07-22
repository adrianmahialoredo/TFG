import axios from 'axios';
import { AnyItemDTO } from '../types';
import { API_ENDPOINTS } from '../config/apiConfig';

/**
 * Recupera un item individual (puede ser Song, Lesson, etc.)
 */
export const getById = async (id: string): Promise<AnyItemDTO> => {
    const response = await axios.get(API_ENDPOINTS.ITEMS.BY_ID(id));
    return response.data;
};

/**
 * Recupera un lote de items mezclados por sus IDs.
 * Ideal para cargar el contenido de una ItemList.
 */
export const getAllById = async (ids: string[]): Promise<AnyItemDTO[]> => {
    // Siguiendo tu patrón de enviar el array de IDs en el body del POST
    const response = await axios.post(API_ENDPOINTS.ITEMS.BY_IDS, ids);
    return response.data;
};

/**
 * Ejemplo de búsqueda global de items
 */
export const searchItems = async (query: string): Promise<AnyItemDTO[]> => {
    const response = await axios.get(API_ENDPOINTS.ITEMS.SEARCH(query));
    return response.data;
};

/**
 * Recupera los items recomendados ("Para ti") para añadir a una lista.
 */
export const getRecommended = async (): Promise<AnyItemDTO[]> => {
    const response = await axios.get(API_ENDPOINTS.ITEMS.RECOMMENDED);
    return response.data;
};

/**
 * Recupera los items accedidos más recientemente (sección "Recientes" del Home).
 */
export const getRecent = async (): Promise<AnyItemDTO[]> => {
    const response = await axios.get(API_ENDPOINTS.ITEMS.RECENT);
    return response.data;
};

/**
 * Recupera los items recomendados para el Home (sección "Recomendaciones").
 */
export const getRecommendations = async (): Promise<AnyItemDTO[]> => {
    const response = await axios.get(API_ENDPOINTS.ITEMS.RECOMMENDATIONS);
    return response.data;
};

/**
 * Marca un item como accedido, actualizando su `lasttimeaccess` en el backend.
 */
export const markAsAccessed = async (id: string): Promise<void> => {
    await axios.put(API_ENDPOINTS.ITEMS.ACCESS(id));
};

