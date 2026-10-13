/**
 * Utilidades para el JWT del backend.
 *
 * Solo se lee el payload para saber si el token ha caducado (claim `exp`); la firma
 * la valida el backend. Sirve para no lanzar peticiones condenadas al 401 al arrancar
 * con un token guardado que ya expiró.
 */

/** Margen para no dar por bueno un token que caduca en mitad de las primeras peticiones. */
const EXPIRY_MARGIN_MS = 30_000;

/** Devuelve el `exp` del token en milisegundos, o null si no se puede leer. */
const getExpiration = (token: string): number | null => {
  try {
    const payload = token.split('.')[1];
    if (!payload) return null;
    // base64url -> base64 con relleno, que es lo que entiende atob
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/').padEnd(Math.ceil(payload.length / 4) * 4, '=');
    const { exp } = JSON.parse(atob(base64));
    return typeof exp === 'number' ? exp * 1000 : null;
  } catch {
    return null;
  }
};

/** true si el token ha caducado o no se puede leer (se trata como inválido). */
export const isTokenExpired = (token: string): boolean => {
  const expiration = getExpiration(token);
  return expiration === null || expiration - EXPIRY_MARGIN_MS <= Date.now();
};
