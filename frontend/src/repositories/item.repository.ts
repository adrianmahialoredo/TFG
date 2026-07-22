import {AnyItemDTO} from "../types";
import * as itemLocal from "../local/item.local";
import * as itemService from "../services/item.service";
import {useItemEntitiesStore} from "../stores/entities/item.entities.store";


/**
 * Trae una lista de items mezclados por sus IDs.
 * Sigue la regla: RAM (opcional) -> DISCO -> API
 */
export const getAllById = async (ids: string[]): Promise<AnyItemDTO[]> => {
    if (!ids || ids.length === 0) return [];

    // 1. Intentamos recuperar lo que haya en DISCO (AsyncStorage)
    const { cached, missing } = await itemLocal.getAllById(ids);

    let remoteItems: AnyItemDTO[] = [];

    // 2. Si faltan IDs, vamos a la API a por ellos
    if (missing.length > 0) {
        try {
            // El backend debe devolver objetos que incluyan el campo "type"
            remoteItems = await itemService.getAllById(missing);

            if (remoteItems.length > 0) {
                // Guardamos en disco para la próxima
                await itemLocal.saveBatch(remoteItems).catch(console.error);
            }
        } catch (error) {
            console.error("[ItemRepo] Error fetching remote items:", error);
        }
    }

    const allFound = [...cached, ...remoteItems];

    // 3. Actualizamos la RAM para que la UI reaccione
    useItemEntitiesStore.getState().setItems(allFound);

    // Devolvemos los items en el mismo orden que se pidieron los IDs
    const map = new Map(allFound.map(i => [i.id.toString(), i]));
    return ids.map(id => map.get(id)).filter((i): i is AnyItemDTO => !!i);
};

/**
 * Obtiene los items recomendados ("Para ti").
 * Retorna IDs (hidratando la RAM y el Local) con fallback offline al índice.
 */
export const getRecommended = async (): Promise<string[]> => {
    const key = 'items_recommended';
    try {
        const items = await itemService.getRecommended();
        if (items.length > 0) {
            useItemEntitiesStore.getState().setItems(items);
            itemLocal.saveBatch(items).catch(console.error);
            itemLocal.saveQueryIndex(key, items.map(i => i.id.toString())).catch(console.error);
        }
        return items.map(i => i.id.toString());
    } catch (error) {
        console.error("[ItemRepo] Error en getRecommended, usando índice local:", error);
        return (await itemLocal.getQueryIndex(key)) || [];
    }
};

/**
 * Obtiene los items accedidos más recientemente (Home "Recientes").
 * Retorna IDs (hidratando la RAM y el Local) con fallback offline al índice.
 */
export const getRecent = async (): Promise<string[]> => {
    const key = 'items_recent';
    try {
        const items = await itemService.getRecent();
        if (items.length > 0) {
            useItemEntitiesStore.getState().setItems(items);
            itemLocal.saveBatch(items).catch(console.error);
            itemLocal.saveQueryIndex(key, items.map(i => i.id.toString())).catch(console.error);
        }
        return items.map(i => i.id.toString());
    } catch (error) {
        console.error("[ItemRepo] Error en getRecent, usando índice local:", error);
        return (await itemLocal.getQueryIndex(key)) || [];
    }
};

/**
 * Obtiene los items recomendados para el Home (Home "Recomendaciones").
 * Retorna IDs (hidratando la RAM y el Local) con fallback offline al índice.
 */
export const getRecommendations = async (): Promise<string[]> => {
    const key = 'items_home_recommendations';
    try {
        const items = await itemService.getRecommendations();
        if (items.length > 0) {
            useItemEntitiesStore.getState().setItems(items);
            itemLocal.saveBatch(items).catch(console.error);
            itemLocal.saveQueryIndex(key, items.map(i => i.id.toString())).catch(console.error);
        }
        return items.map(i => i.id.toString());
    } catch (error) {
        console.error("[ItemRepo] Error en getRecommendations, usando índice local:", error);
        return (await itemLocal.getQueryIndex(key)) || [];
    }
};

/**
 * Marca un item como accedido (actualiza `lasttimeaccess` en el backend).
 * Fire-and-forget: actualiza la RAM de forma optimista y no propaga errores
 * para no interrumpir la navegación del usuario.
 */
export const markAsAccessed = async (id: string): Promise<void> => {
    if (!id) return;

    // Actualización optimista de la RAM (si el item ya vive en la Store).
    useItemEntitiesStore.getState().updateItem(id, {
        lasttimeaccess: new Date().toISOString(),
    } as Partial<AnyItemDTO>);

    try {
        await itemService.markAsAccessed(id);
    } catch (error) {
        console.error(`[ItemRepo] Error en markAsAccessed(${id}):`, error);
    }
};

/**
 * Busca items por título. Retorna IDs e hidrata la RAM/Local.
 * Los resultados son transitorios: no se persiste índice.
 */
export const search = async (query: string): Promise<string[]> => {
    if (!query.trim()) return [];
    try {
        const items = await itemService.searchItems(query);
        if (items.length > 0) {
            useItemEntitiesStore.getState().setItems(items);
            itemLocal.saveBatch(items).catch(console.error);
        }
        return items.map(i => i.id.toString());
    } catch (error) {
        console.error("[ItemRepo] Error en search:", error);
        return [];
    }
};
