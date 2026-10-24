import { SearchVocabulary } from './types';

/**
 * Vocabulario de búsqueda en español (idioma del catálogo).
 * Solo hacen falta sinónimos y variantes: los nombres del catálogo ya se encuentran tal cual.
 */
export const es: SearchVocabulary = {
  terms: {
    // Instrumentos
    ukulele: 'ukelele',
    teclado: 'piano',
    percusion: 'bateria',
    // Géneros
    rocanrol: 'rockroll',
    rockandroll: 'rockroll',
    rocknroll: 'rockroll',
  },
  decades: {
    cincuenta: '1950s',
    sesenta: '1960s',
    setenta: '1970s',
    ochenta: '1980s',
    noventa: '1990s',
    dosmil: '2000s',
  },
};
