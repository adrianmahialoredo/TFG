/**
 * Utilidades para SongScreen
 */

/**
 * Parsea la letra con acordes en formato [acorde] texto
 * @param lyrics La letra con acordes entre corchetes
 * @returns Array con objetos de acorde y texto
 */
export const parseLyrics = (lyrics: string): { chord?: string; text: string }[] => {
  const regex = /\[([^\]]+)]|([^[]+)/g;
  const result: { chord?: string; text: string }[] = [];

  let currentChord: string | null = null;
  let match: RegExpExecArray | null;

  while ((match = regex.exec(lyrics)) !== null) {
    if (match[1]) {
      currentChord = match[1];
    } else if (match[2]) {
      result.push({ chord: currentChord ?? undefined, text: match[2] });
      currentChord = null;
    }
  }

  return result;
};

