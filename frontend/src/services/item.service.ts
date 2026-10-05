import axios from 'axios';
import { AnyItemDTO, ItemSummaryDTO } from '../types';
import { API_ENDPOINTS } from '../config/apiConfig';

/**
 * Recupera el resumen de TODOS los items: el catálogo que la app carga al arrancar.
 */
export const getAll = async (): Promise<ItemSummaryDTO[]> => {
    const response = await axios.get(API_ENDPOINTS.ITEMS.BASE);
    return response.data;
};

/**
 * Recupera un item individual (puede ser Song, Lesson, etc.)
 */
export const getById = async (id: string): Promise<ItemSummaryDTO> => {
    const response = await axios.get(API_ENDPOINTS.ITEMS.BY_ID(id));
    return response.data;
};

/**
 * Recupera un lote de items mezclados por sus IDs.
 * Ideal para cargar el contenido de una ItemList.
 */
export const getAllById = async (ids: string[]): Promise<ItemSummaryDTO[]> => {
    // Siguiendo tu patrón de enviar el array de IDs en el body del POST
    const response = await axios.post(API_ENDPOINTS.ITEMS.BY_IDS, ids);
    return response.data;
};

// --- Feeds mixtos con DTO completo de cada subtipo (decisión de negocio) ---

/**
 * DTOs completos de los items accedidos más recientemente (sección "Recientes" del Home).
 * Cada uno es del DTO de su subtipo y trae `type`.
 */
export const getRecent = async (): Promise<AnyItemDTO[]> => {
    const response = await axios.get(API_ENDPOINTS.ITEMS.RECENT);
    return response.data;
};

/**
 * DTOs completos de los items recomendados ("Para ti") para añadir a una lista.
 * Cada uno es del DTO de su subtipo y trae `type`.
 */
export const getRecommendedByItemList = async (id: string): Promise<AnyItemDTO[]> => {
    const response = await axios.get(API_ENDPOINTS.ITEMS.RECOMMENDED_BY_ITEM_LIST(id));
    return response.data;
};

// --- Feeds por convención: solo IDs (se resuelven contra el catálogo de items) ---

/**
 * IDs de los items recomendados para el Home (sección "Recomendaciones").
 */
export const getRecommendedIds = async (): Promise<string[]> => {
    const response = await axios.get(API_ENDPOINTS.ITEMS.RECOMMENDED_IDS);
    return response.data;
};

/**
 * Marca un item como accedido, actualizando su `lastTimeAccessed` en el backend.
 */
export const markAsAccessed = async (id: string): Promise<void> => {
    await axios.put(API_ENDPOINTS.ITEMS.ACCESS(id));
};
