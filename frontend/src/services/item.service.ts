import { apiClient } from './apiClient';
import { AnyItemDTO, ItemSummaryDTO } from '../types';
import { API_ENDPOINTS } from '../config/apiConfig';

/**
 * Recupera el resumen de TODOS los items: el catálogo que la app carga al arrancar.
 */
export const getAll = async (): Promise<ItemSummaryDTO[]> => {
    const response = await apiClient.get(API_ENDPOINTS.ITEMS.BASE);
    return response.data;
};

/**
 * Recupera un item individual (puede ser Song, Lesson, etc.)
 */
export const getById = async (id: string): Promise<ItemSummaryDTO> => {
    const response = await apiClient.get(API_ENDPOINTS.ITEMS.BY_ID(id));
    return response.data;
};

/**
 * Recupera un lote de items mezclados por sus IDs.
 * Ideal para cargar el contenido de una ItemList.
 */
export const getAllById = async (ids: string[]): Promise<ItemSummaryDTO[]> => {
    // Siguiendo tu patrón de enviar el array de IDs en el body del POST
    const response = await apiClient.post(API_ENDPOINTS.ITEMS.BY_IDS, ids);
    return response.data;
};

// --- Feeds mixtos con DTO completo de cada subtipo (decisión de negocio) ---

/**
 * DTOs completos de los items recomendados ("Para ti") para añadir a una lista.
 * Cada uno es del DTO de su subtipo y trae `type`.
 */
export const getRecommendedByItemList = async (id: string): Promise<AnyItemDTO[]> => {
    const response = await apiClient.get(API_ENDPOINTS.ITEMS.RECOMMENDED_BY_ITEM_LIST(id));
    return response.data;
};

/**
 * DTOs completos de los items recomendados a un usuario (Home "Recomendaciones").
 * Cada uno es del DTO de su subtipo y trae `type`.
 */
export const getRecommendedByUser = async (userId: string): Promise<AnyItemDTO[]> => {
    const response = await apiClient.get(API_ENDPOINTS.ITEMS.RECOMMENDED_BY_USER(userId));
    return response.data;
};

/**
 * DTOs completos de los items recomendados a un usuario a partir de su Spotify (Home "Desde tu Spotify").
 * Cada uno es del DTO de su subtipo y trae `type`. Requiere la cuenta de Spotify vinculada.
 */
export const getRecommendedFromSpotify = async (userId: string): Promise<AnyItemDTO[]> => {
    const response = await apiClient.get(API_ENDPOINTS.ITEMS.SPOTIFY_BY_USER(userId));
    return response.data;
};
