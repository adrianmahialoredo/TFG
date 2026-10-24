/**
 * Utilidades del buscador de items (canciones y lecciones).
 *
 * El usuario escribe una única palabra y se busca en varios campos del item a la vez
 * (título, artista, género, instrumento, década). El orden de los campos es su prioridad:
 * cada item se agrupa con el primer campo por el que coincide.
 */

import { SearchVocabulary } from '../constants/search';

/** Campos de un item por los que se puede buscar. */
export type SearchField = 'title' | 'artist' | 'genre' | 'instrument' | 'decade';

/** Datos de un item que entran en la búsqueda, ya resueltos a texto. */
export interface SearchableItem {
  title: string;
  artist?: string | null;
  genres: string[];
  instruments: string[];
  decade?: string | null;
}

/** Longitud mínima para traducir una palabra a medio escribir con el vocabulario ("ochen" -> "1980s"). */
const MIN_PREFIX_LENGTH = 3;

/**
 * Normaliza un texto para compararlo: minúsculas, sin tildes y sin espacios ni signos.
 * Así "Pop-Rock", "pop rock" y "poprock" son lo mismo, y "bateria" encuentra "Batería".
 */
export const normalizeSearchText = (text: string): string =>
  text
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]/g, '');

/** Valores del diccionario cuya clave es la palabra o empieza por ella (si es lo bastante larga). */
const lookup = (word: string, dictionary: Record<string, string>): string[] =>
  Object.entries(dictionary)
    .filter(([key]) => key === word || (word.length >= MIN_PREFIX_LENGTH && key.startsWith(word)))
    .map(([, value]) => value);

/**
 * Década a la que se refiere un número: "80", "80s", "85", "1980", "1985" -> "1980s".
 * Con dos cifras, de 30 en adelante se entiende el siglo XX ("90" -> "1990s") y si no el XXI ("10" -> "2010s").
 */
const numberToDecade = (word: string): string | null => {
  const match = /^(\d{2}|\d{4})s?$/.exec(word);
  if (!match) return null;
  const digits = match[1];
  const year = digits.length === 4 ? Number(digits) : Number(digits) + (Number(digits) >= 30 ? 1900 : 2000);
  return `${Math.floor(year / 10) * 10}s`;
};

/** Coincidencia de un item: el campo de más prioridad con el que coincide y cómo. */
export interface SearchMatch {
  field: SearchField;
  /** 2 si el campo empieza por la búsqueda; 1 si solo la contiene. */
  strength: number;
}

/**
 * Crea la función que busca en un item.
 * @param query Lo que ha escrito el usuario
 * @param fields Campos por los que buscar, en orden de prioridad
 * @param vocabularies Vocabularios activos (traducen géneros, instrumentos y décadas del idioma del usuario)
 * @returns `null` si la búsqueda está vacía; si no, una función que devuelve la coincidencia de un item (o `null`)
 */
export const createSearchMatcher = (
  query: string,
  fields: SearchField[],
  vocabularies: SearchVocabulary[],
): ((item: SearchableItem) => SearchMatch | null) | null => {
  const word = normalizeSearchText(query);
  if (!word) return null;

  // Lo escrito más su traducción a términos del catálogo
  const terms = [word, ...vocabularies.flatMap((v) => lookup(word, v.terms))];
  const decades = [numberToDecade(word), ...vocabularies.flatMap((v) => lookup(word, v.decades))]
    .filter((decade): decade is string => Boolean(decade))
    .map(normalizeSearchText);

  /** 0 si no coincide; 1 si contiene la búsqueda; 2 si empieza por ella. */
  const matchText = (text: string | null | undefined, candidates: string[]): number => {
    const normalized = normalizeSearchText(text ?? '');
    if (!normalized) return 0;
    if (candidates.some((c) => normalized.startsWith(c))) return 2;
    return candidates.some((c) => normalized.includes(c)) ? 1 : 0;
  };
  const matchList = (texts: string[], candidates: string[]): number =>
    Math.max(0, ...texts.map((text) => matchText(text, candidates)));

  const matchField = (item: SearchableItem, field: SearchField): number => {
    switch (field) {
      case 'title':
        return matchText(item.title, [word]);
      case 'artist':
        return matchText(item.artist, [word]);
      case 'genre':
        return matchList(item.genres, terms);
      case 'instrument':
        return matchList(item.instruments, terms);
      case 'decade':
        // Con una sola cifra coincidirían casi todas las décadas
        if (word.length < 2) return 0;
        return matchText(item.decade, [word, ...decades]);
    }
  };

  // El item cuenta solo para el primer campo (el de más prioridad) con el que coincide
  return (item) => {
    for (const field of fields) {
      const strength = matchField(item, field);
      if (strength > 0) return { field, strength };
    }
    return null;
  };
};
