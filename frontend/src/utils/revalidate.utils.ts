/**
 * Stale-While-Revalidate para los Repositories.
 *
 * Cuando una entidad se sirve desde caché (RAM o Disco) se devuelve al instante
 * y se pide al backend en segundo plano. Al llegar, se sobrescribe en RAM + Disco
 * y la UI se actualiza sola por la suscripción a la Entity Store.
 *
 * Cada entidad se revalida como mucho UNA vez por sesión: tras ello (o si acaba
 * de llegar de la API) se considera fresca. Si la petición falla (offline),
 * se olvida para reintentarlo en la siguiente lectura.
 */

// Claves `${entity}:${id}` ya frescas en esta sesión.
const freshKeys = new Set<string>();

export interface Revalidator {
    /** Marca como frescas entidades que acaban de llegar de la API. */
    markFresh: (ids: string[]) => void;
    /** Pide en segundo plano las entidades servidas desde caché que no estén frescas. */
    revalidate: (ids: string[]) => void;
}

export const createRevalidator = <T>(
    entity: string,
    fetchByIds: (ids: string[]) => Promise<T[]>,
    save: (fresh: T[]) => void
): Revalidator => {
    const key = (id: string) => `${entity}:${id}`;

    const markFresh = (ids: string[]) => ids.forEach((id) => freshKeys.add(key(id)));

    const revalidate = (ids: string[]) => {
        const stale = ids.filter((id) => !freshKeys.has(key(id)));
        if (stale.length === 0) return;

        // Se marcan antes de pedir para no lanzar peticiones duplicadas.
        markFresh(stale);

        fetchByIds(stale)
            .then((fresh) => {
                if (fresh.length > 0) save(fresh);
            })
            .catch(() => stale.forEach((id) => freshKeys.delete(key(id))));
    };

    return { markFresh, revalidate };
};
