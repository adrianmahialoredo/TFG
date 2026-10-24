/**
 * Vocabulario de búsqueda de un idioma.
 *
 * El catálogo (nombres de géneros e instrumentos, décadas) se guarda en un único idioma
 * en el backend. Cada vocabulario traduce lo que escribe el usuario en su idioma al término
 * canónico del catálogo, de modo que, p.ej., un holandés que escribe "gitaar" encuentre
 * las canciones de "Guitarra".
 *
 * Todas las claves y valores se escriben ya normalizados (ver `normalizeSearchText`):
 * minúsculas, sin tildes y sin espacios ni signos ("Rock & Roll" -> "rockroll").
 */
export interface SearchVocabulary {
  /** Término del usuario -> nombre canónico de un género o instrumento del catálogo. */
  terms: Record<string, string>;
  /** Palabra del usuario -> década canónica del catálogo (formato "1980s"). */
  decades: Record<string, string>;
}
