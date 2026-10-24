import { SearchVocabulary } from './types';
import { es } from './es';

export type { SearchVocabulary } from './types';

/** Idioma en el que está el catálogo: su vocabulario está siempre activo. */
const CATALOG_LANGUAGE = 'es';

/**
 * Vocabularios disponibles, por código de idioma ISO 639-1.
 * Para soportar un idioma nuevo basta con crear su fichero (p.ej. `nl.ts`) y registrarlo aquí.
 */
const VOCABULARIES: Record<string, SearchVocabulary> = {
  es,
};

/** Idioma del dispositivo ("nl-NL" -> "nl"), o null si no se puede averiguar. */
const getDeviceLanguage = (): string | null => {
  try {
    return Intl.DateTimeFormat().resolvedOptions().locale.split('-')[0].toLowerCase();
  } catch {
    return null;
  }
};

/**
 * Vocabularios con los que se interpreta la búsqueda: el del idioma del catálogo
 * más el del idioma del dispositivo, si está disponible.
 */
export const getActiveSearchVocabularies = (): SearchVocabulary[] => {
  const languages = new Set([CATALOG_LANGUAGE, getDeviceLanguage()]);
  return [...languages]
    .map((language) => (language ? VOCABULARIES[language] : undefined))
    .filter((vocabulary): vocabulary is SearchVocabulary => Boolean(vocabulary));
};
