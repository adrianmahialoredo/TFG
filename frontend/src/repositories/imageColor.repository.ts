import { getColorSample } from '../services/cloudinary.service';
import { pickDominantColor, rgbToHex } from '../utils/imageColor.utils';

/**
 * Color principal de las imágenes (degradado de ListScreen).
 *
 * Es un dato derivado de la imagen, no una entidad: no va a ninguna Entity Store ni al disco.
 * Se guarda en RAM por URL (una URL de Cloudinary no cambia de contenido; al cambiar la foto
 * de una lista cambia la URL), así que cada imagen se pide una sola vez por sesión.
 */
const colorsByUrl = new Map<string, Promise<string | null>>();

/** `#rrggbb` del color principal de la imagen, o null si no se puede calcular. */
export const getDominantColor = (imageUrl: string): Promise<string | null> => {
    const cached = colorsByUrl.get(imageUrl);
    if (cached) return cached;

    const request = getColorSample(imageUrl)
        .then((pixels) => {
            const color = pickDominantColor(pixels);
            return color ? rgbToHex(color) : null;
        })
        .catch((error) => {
            console.error('[ImageColorRepo] Error calculando el color de la imagen:', error);
            // Sin red puede salir bien más adelante: no se guarda el fallo
            colorsByUrl.delete(imageUrl);
            return null;
        });

    colorsByUrl.set(imageUrl, request);
    return request;
};
