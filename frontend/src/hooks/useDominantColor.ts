import { useEffect, useState } from 'react';
import * as imageColorRepository from '../repositories/imageColor.repository';

/**
 * Nivel 6: color principal (`#rrggbb`) de una imagen.
 *
 * Devuelve null mientras se calcula (para que la UI no enseñe un color y luego otro) y
 * `fallback` si no hay imagen o no se puede calcular (p. ej. una imagen que no es de Cloudinary).
 */
export const useDominantColor = (imageUrl: string | null | undefined, fallback: string): string | null => {
  const [result, setResult] = useState<{ url: string; color: string | null } | null>(null);

  useEffect(() => {
    if (!imageUrl) return;

    let cancelled = false;
    imageColorRepository.getDominantColor(imageUrl).then((color) => {
      if (!cancelled) setResult({ url: imageUrl, color });
    });
    return () => {
      cancelled = true;
    };
  }, [imageUrl]);

  if (!imageUrl) return fallback;
  // Resultado de otra imagen (la URL acaba de cambiar): aún se está calculando
  if (result?.url !== imageUrl) return null;
  return result.color ?? fallback;
};
