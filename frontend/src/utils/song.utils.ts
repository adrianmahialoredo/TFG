/**
 * Utilidades para SongScreen
 */

/** Fragmento de letra con el acorde (opcional) que suena justo al inicio del fragmento */
export type LyricSegment = { chord?: string; text: string };

/** Una línea de la canción: fragmentos que se pintan en fila, con el acorde encima de cada uno */
export type LyricLine = LyricSegment[];

/**
 * Parsea la letra con acordes en formato ChordPro simplificado ("[C]Say it ain't [G]so").
 *
 * Cada línea (separada por saltos de línea) se divide en fragmentos de una palabra para que,
 * al pintarlos en una fila con wrap, la línea pueda partirse por palabras sin que el acorde
 * pierda la posición. El acorde se asigna a la palabra (o trozo de palabra) que le sigue,
 * así "Loui[D]siana" produce "Loui" + "siana" con el D encima de "siana".
 *
 * @param lyrics La letra con acordes entre corchetes
 * @returns Array de líneas, cada una con sus fragmentos
 */
export const parseLyrics = (lyrics: string): LyricLine[] =>
  lyrics.split(/\r?\n/).map(parseLine);

const parseLine = (line: string): LyricLine => {
  const regex = /\[([^\]]+)]|([^[]+)/g;
  const segments: LyricLine = [];

  let pendingChord: string | undefined;
  let match: RegExpExecArray | null;

  while ((match = regex.exec(line)) !== null) {
    if (match[1]) {
      // Dos acordes seguidos sin letra entre medias: el primero ocupa su propio hueco
      if (pendingChord) segments.push({ chord: pendingChord, text: '' });
      pendingChord = match[1];
    } else if (match[2]) {
      // Se trocea por palabras conservando el espacio final de cada una
      const words = match[2].match(/\S+\s*|\s+/g) ?? [];
      words.forEach((word, i) => {
        segments.push({ chord: i === 0 ? pendingChord : undefined, text: word });
      });
      pendingChord = undefined;
    }
  }

  // Acorde al final de la línea sin letra detrás
  if (pendingChord) segments.push({ chord: pendingChord, text: '' });

  return segments;
};
