/**
 * Utilidades para los vídeos de YouTube (lecciones de canción, secciones de lección).
 */

/** Los IDs de vídeo de YouTube tienen 11 caracteres de [A-Za-z0-9_-]. */
const VIDEO_ID = /^[A-Za-z0-9_-]{11}$/;

/** watch?v=ID, youtu.be/ID, /embed/ID, /shorts/ID y /live/ID (con o sin "www." / "m."). */
const VIDEO_ID_IN_URL = /(?:[?&]v=|youtu\.be\/|\/embed\/|\/shorts\/|\/live\/)([A-Za-z0-9_-]{11})/;

/**
 * Extrae el ID de un vídeo de YouTube a partir de su URL (o lo devuelve tal cual si ya es un ID).
 * El reproductor (`react-native-youtube-iframe`) solo admite el ID, no la URL completa.
 * @returns el ID, o null si no se reconoce como un vídeo de YouTube
 */
export const getYoutubeVideoId = (urlOrId?: string | null): string | null => {
  const value = urlOrId?.trim();
  if (!value) return null;
  if (VIDEO_ID.test(value)) return value;
  return value.match(VIDEO_ID_IN_URL)?.[1] ?? null;
};
