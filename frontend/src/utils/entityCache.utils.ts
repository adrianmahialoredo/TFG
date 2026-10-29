import { isAxiosError } from 'axios';
import { createRevalidator } from './revalidate.utils';

/**
 * 401 de una petición que iba con token: el interceptor de apiClient ya cerró la sesión y
 * lleva al login, no es un fallo que avisar. Un 401 SIN token (p.ej. en el registro) sí se
 * avisa: significa que el endpoint no es público y la pantalla se quedará vacía.
 */
const isUnauthorized = (error: unknown) =>
    isAxiosError(error) && error.response?.status === 401 && !!error.config?.headers?.Authorization;

type Identified = { id: string | number };

/** Callback que recibe los IDs frescos cuando la red responde tras haber servido la caché. */
export type OnFresh = (ids: string[]) => void;

interface EntityLocal<T> {
    getAllById: (ids: string[]) => Promise<{ cached: T[]; missing: string[] }>;
    saveBatch: (entities: T[]) => Promise<void>;
    saveQueryIndex?: (key: string, ids: string[]) => Promise<void>;
    getQueryIndex?: (key: string) => Promise<string[] | null>;
}

interface EntityCacheConfig<T> {
    /** Nombre para logs y para las claves de revalidación. */
    name: string;
    getRam: () => Record<string, T>;
    setRam: (entities: T[]) => void;
    local: EntityLocal<T>;
    fetchByIds: (ids: string[]) => Promise<T[]>;
}

/**
 * Núcleo común de los Repositories (Nivel 3) para una entidad:
 *  - `getAllById`: RAM -> Disco -> API, con Stale-While-Revalidate.
 *  - `hydrate`: guarda en RAM + Disco (+ índice) lo que llega de la API y devuelve los IDs.
 *  - `list` / `idList` / `feed`: lecturas de listas Cache-First (índice guardado al instante +
 *    refresco de red), para endpoints que devuelven objetos, solo IDs, o una carga a medida.
 */
export const createEntityCache = <T extends Identified>(config: EntityCacheConfig<T>) => {
    const { name, getRam, setRam, local, fetchByIds } = config;
    const toId = (entity: T) => entity.id.toString();

    const revalidator = createRevalidator<T>(name, fetchByIds, (fresh) => {
        setRam(fresh);
        local.saveBatch(fresh).catch(console.error);
    }, toId);

    /** RAM -> Disco, sin red. Sube a RAM lo que se lee de Disco. */
    const getCached = async (ids: string[]): Promise<{ found: T[]; missing: string[] }> => {
        const ram = getRam();
        const inRam = ids.filter((id) => ram[id]).map((id) => ram[id]);
        const notInRam = ids.filter((id) => !ram[id]);
        if (notInRam.length === 0) return { found: inRam, missing: [] };

        const { cached, missing } = await local.getAllById(notInRam);
        if (cached.length > 0) setRam(cached);
        return { found: [...inRam, ...cached], missing };
    };

    /** Guarda en RAM + Disco (+ índice si hay clave) entidades recién llegadas de la API. */
    const hydrate = (entities: T[], cacheKey?: string): string[] => {
        const ids = entities.map(toId);
        if (entities.length > 0) {
            revalidator.markFresh(ids);
            setRam(entities);
            local.saveBatch(entities).catch(console.error);
        }
        // El índice se guarda aunque venga vacío, para no resucitar resultados viejos.
        if (cacheKey && local.saveQueryIndex) {
            local.saveQueryIndex(cacheKey, ids).catch(console.error);
        }
        return ids;
    };

    /** RAM -> Disco -> API. Devuelve en el orden de `ids` pedido. */
    const getAllById = async (requestedIds: string[]): Promise<T[]> => {
        if (!requestedIds || requestedIds.length === 0) return [];
        // Los DTOs del backend traen los IDs como números aunque el tipo diga string:
        // sin normalizar, el Map de abajo (claves string) no encontraría nada.
        const ids = requestedIds.map(String);

        const { found, missing } = await getCached(ids);

        let remote: T[] = [];
        if (missing.length > 0) {
            try {
                remote = await fetchByIds(missing);
                hydrate(remote);
            } catch (error) {
                console.error(`[${name}] API Error`, error);
            }
        }

        revalidator.revalidate(found.map(toId));

        const map = new Map([...found, ...remote].map((e) => [toId(e), e]));
        return ids.map((id) => map.get(id)).filter((e): e is T => !!e);
    };

    /** Lee un índice guardado y sube sus entidades a RAM (sin red). Devuelve los IDs disponibles. */
    const readIndex = async (cacheKey: string): Promise<string[]> => {
        if (!local.getQueryIndex) return [];
        const ids = (await local.getQueryIndex(cacheKey)) || [];
        if (ids.length === 0) return [];
        const { found } = await getCached(ids);
        const available = new Set(found.map(toId));
        return ids.filter((id) => available.has(id));
    };

    /**
     * Esqueleto común de las listas Cache-First:
     *  - Si hay índice guardado: devuelve sus IDs AL INSTANTE y refresca desde la red
     *    en segundo plano, avisando con `onFresh(ids)` cuando llegan.
     *  - Si no hay índice: espera a la red.
     *  - Sin red y sin índice: devuelve [].
     */
    const cacheFirst = async (
        cacheKey: string,
        fetchFresh: () => Promise<string[]>,
        onFresh?: OnFresh
    ): Promise<string[]> => {
        const cached = await readIndex(cacheKey).catch(() => [] as string[]);
        if (cached.length > 0) {
            fetchFresh()
                .then((ids) => onFresh?.(ids))
                .catch((error) => {
                    if (!isUnauthorized(error)) {
                        console.warn(`[${name}] Refresco de '${cacheKey}' fallido, se mantiene la caché:`, error);
                    }
                });
            return cached;
        }

        try {
            return await fetchFresh();
        } catch (error) {
            if (!isUnauthorized(error)) {
                console.error(`[${name}] Error en '${cacheKey}' sin caché disponible:`, error);
            }
            return [];
        }
    };

    /** Lista cuyo endpoint devuelve OBJETOS: se hidratan y se guarda el índice. */
    const list = (cacheKey: string, fetchList: () => Promise<T[]>, onFresh?: OnFresh): Promise<string[]> =>
        cacheFirst(cacheKey, async () => hydrate(await fetchList(), cacheKey), onFresh);

    /**
     * Lista genérica: `load` hace la petición y la hidratación que haga falta (p.ej. repartir
     * DTOs de varios tipos en sus Stores) y devuelve los IDs. Aquí se guarda el índice y se
     * aplica el Cache-First sobre esta entidad.
     */
    const feed = (cacheKey: string, load: () => Promise<string[]>, onFresh?: OnFresh): Promise<string[]> =>
        cacheFirst(cacheKey, async () => {
            const ids = await load();
            local.saveQueryIndex?.(cacheKey, ids).catch(console.error);
            return ids;
        }, onFresh);

    /**
     * Lista cuyo endpoint devuelve solo IDs (las entidades ya suelen estar en caché,
     * p.ej. por el catálogo). Se resuelven contra RAM -> Disco y solo se piden a la
     * API (`by-ids`) las que falten. No se revalida: lo hace quien carga el catálogo.
     */
    const idList = (cacheKey: string, fetchIds: () => Promise<(string | number)[]>, onFresh?: OnFresh): Promise<string[]> =>
        feed(cacheKey, async () => {
            const ids = (await fetchIds()).map(String);
            const { missing } = await getCached(ids);
            if (missing.length > 0) {
                try {
                    hydrate(await fetchByIds(missing));
                } catch (error) {
                    console.error(`[${name}] API Error resolviendo '${cacheKey}'`, error);
                }
            }
            return ids;
        }, onFresh);

    return {
        getAllById,
        hydrate,
        list,
        idList,
        feed,
        readIndex,
        markFresh: revalidator.markFresh,
        revalidate: revalidator.revalidate,
    };
};
