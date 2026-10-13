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
 *
 * Cada escritura (`markFresh`) sube la versión de la entidad. Una revalidación solo
 * guarda su respuesta si la versión no ha cambiado desde que se pidió: así un GET
 * lanzado antes de una modificación no pisa el dato nuevo cuando llega tarde.
 */

// Claves `${entity}:${id}` ya frescas en esta sesión.
const freshKeys = new Set<string>();

// Versión de escritura de cada clave: sube cada vez que se guarda un dato nuevo.
const versions = new Map<string, number>();
const versionOf = (k: string) => versions.get(k) ?? 0;

export interface Revalidator {
    /**
     * Marca como frescas entidades que acaban de llegar de la API o que se acaban de
     * modificar. Llamarlo en TODA escritura: invalida las revalidaciones en vuelo.
     */
    markFresh: (ids: string[]) => void;
    /** Pide en segundo plano las entidades servidas desde caché que no estén frescas. */
    revalidate: (ids: string[]) => void;
}

export const createRevalidator = <T>(
    entity: string,
    fetchByIds: (ids: string[]) => Promise<T[]>,
    save: (fresh: T[]) => void,
    getId: (entity: T) => string
): Revalidator => {
    const key = (id: string) => `${entity}:${id}`;

    const markFresh = (ids: string[]) => ids.forEach((id) => {
        freshKeys.add(key(id));
        versions.set(key(id), versionOf(key(id)) + 1);
    });

    const revalidate = (ids: string[]) => {
        const stale = ids.filter((id) => !freshKeys.has(key(id)));
        if (stale.length === 0) return;

        // Se marcan antes de pedir para no lanzar peticiones duplicadas (sin subir versión).
        stale.forEach((id) => freshKeys.add(key(id)));
        const requestedVersions = new Map(stale.map((id) => [id, versionOf(key(id))]));
        const unchanged = (id: string) => versionOf(key(id)) === requestedVersions.get(id);

        fetchByIds(stale)
            .then((fresh) => {
                // Lo modificado mientras tanto ya tiene un dato más nuevo que esta respuesta.
                const stillValid = fresh.filter((e) => unchanged(getId(e)));
                if (stillValid.length > 0) save(stillValid);
            })
            .catch(() => stale.filter(unchanged).forEach((id) => freshKeys.delete(key(id))));
    };

    return { markFresh, revalidate };
};
