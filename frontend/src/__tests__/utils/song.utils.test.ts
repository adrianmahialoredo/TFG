import { parseLyrics } from '../../utils/song.utils';

describe('parseLyrics', () => {
  it('asigna cada acorde a la palabra que le sigue y trocea por palabras', () => {
    expect(parseLyrics("[C]Say it ain't [G]so")).toEqual([
      [
        { chord: 'C', text: 'Say ' },
        { chord: undefined, text: 'it ' },
        { chord: undefined, text: "ain't " },
        { chord: 'G', text: 'so' },
      ],
    ]);
  });

  it('parte una palabra cuando el acorde cae en medio', () => {
    expect(parseLyrics('Loui[D]siana')).toEqual([
      [
        { chord: undefined, text: 'Loui' },
        { chord: 'D', text: 'siana' },
      ],
    ]);
  });

  it('dos acordes seguidos: el primero ocupa su propio hueco vacío', () => {
    expect(parseLyrics('[Am][G]hola')).toEqual([
      [
        { chord: 'Am', text: '' },
        { chord: 'G', text: 'hola' },
      ],
    ]);
  });

  it('un acorde al final de la línea queda con texto vacío', () => {
    expect(parseLyrics('fin [E7]')).toEqual([
      [
        { chord: undefined, text: 'fin ' },
        { chord: 'E7', text: '' },
      ],
    ]);
  });

  it('separa líneas con \\n y \\r\\n', () => {
    const lines = parseLyrics('[C]uno\r\ndos\ntres');
    expect(lines).toHaveLength(3);
    expect(lines[1]).toEqual([{ chord: undefined, text: 'dos' }]);
    expect(lines[2]).toEqual([{ chord: undefined, text: 'tres' }]);
  });

  it('una línea vacía produce una línea sin fragmentos', () => {
    expect(parseLyrics('a\n\nb')[1]).toEqual([]);
    expect(parseLyrics('')).toEqual([[]]);
  });

  it('conserva los espacios iniciales como fragmento propio', () => {
    expect(parseLyrics('  [C]hola')).toEqual([
      [
        { chord: undefined, text: '  ' },
        { chord: 'C', text: 'hola' },
      ],
    ]);
  });

  it('una línea solo con acordes', () => {
    expect(parseLyrics('[C][G][Am]')).toEqual([
      [
        { chord: 'C', text: '' },
        { chord: 'G', text: '' },
        { chord: 'Am', text: '' },
      ],
    ]);
  });
});
