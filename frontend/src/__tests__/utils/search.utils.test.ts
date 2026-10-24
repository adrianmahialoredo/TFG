import { createSearchMatcher, normalizeSearchText, SearchableItem } from '../../utils/search.utils';
import { es } from '../../constants/search/es';

const item = (overrides: Partial<SearchableItem> = {}): SearchableItem => ({
  title: 'Bohemian Rhapsody',
  artist: 'Queen',
  genres: ['Rock'],
  instruments: ['Guitarra'],
  decade: '1970s',
  ...overrides,
});

const ALL_FIELDS = ['title', 'artist', 'genre', 'instrument', 'decade'] as const;

const match = (query: string, target: SearchableItem, fields = [...ALL_FIELDS], vocabularies = [es]) =>
  createSearchMatcher(query, fields, vocabularies)!(target);

describe('normalizeSearchText', () => {
  it('pasa a minúsculas, quita tildes, espacios y signos', () => {
    expect(normalizeSearchText('Batería')).toBe('bateria');
    expect(normalizeSearchText('Pop-Rock')).toBe('poprock');
    expect(normalizeSearchText('pop rock')).toBe('poprock');
    expect(normalizeSearchText('Rock & Roll!')).toBe('rockroll');
    expect(normalizeSearchText('Canción Ñ 80s')).toBe('cancionn80s');
  });

  it('devuelve cadena vacía si solo hay signos', () => {
    expect(normalizeSearchText('  -¿?- ')).toBe('');
  });
});

describe('createSearchMatcher', () => {
  it('devuelve null si la búsqueda está vacía tras normalizar', () => {
    expect(createSearchMatcher('', ['title'], [es])).toBeNull();
    expect(createSearchMatcher('  !! ', ['title'], [es])).toBeNull();
  });

  it('fuerza 2 si el título empieza por la búsqueda y 1 si solo la contiene', () => {
    expect(match('bohem', item())).toEqual({ field: 'title', strength: 2 });
    expect(match('rhaps', item())).toEqual({ field: 'title', strength: 1 });
  });

  it('ignora tildes y mayúsculas', () => {
    expect(match('BOHÉMIAN', item())).toEqual({ field: 'title', strength: 2 });
  });

  it('devuelve el primer campo de la lista de prioridad que coincide', () => {
    const target = item({ title: 'Queen of hearts' });
    expect(match('queen', target, ['title', 'artist'])).toEqual({ field: 'title', strength: 2 });
    expect(match('queen', target, ['artist', 'title'])).toEqual({ field: 'artist', strength: 2 });
  });

  it('devuelve null si no coincide ningún campo', () => {
    expect(match('zzz', item())).toBeNull();
  });

  it('solo busca en los campos indicados', () => {
    expect(match('queen', item(), ['title'])).toBeNull();
  });

  it('un artista nulo no coincide', () => {
    expect(match('queen', item({ artist: null }), ['artist'])).toBeNull();
  });

  it('busca en géneros e instrumentos (máximo de la lista)', () => {
    expect(match('rock', item({ genres: ['Pop', 'Hard Rock'] }), ['genre'])).toEqual({ field: 'genre', strength: 1 });
    expect(match('rock', item({ genres: ['Hard Rock', 'Rock'] }), ['genre'])).toEqual({ field: 'genre', strength: 2 });
    expect(match('guit', item(), ['instrument'])).toEqual({ field: 'instrument', strength: 2 });
    expect(match('rock', item({ genres: [] }), ['genre'])).toBeNull();
  });

  it('traduce términos del vocabulario a nombres del catálogo', () => {
    expect(match('teclado', item({ instruments: ['Piano'] }), ['instrument'])).toEqual({ field: 'instrument', strength: 2 });
    expect(match('rock and roll', item({ genres: ['Rock & Roll'] }), ['genre'])).toEqual({ field: 'genre', strength: 2 });
  });

  it('traduce prefijos del vocabulario de al menos 3 letras', () => {
    expect(match('percu', item({ instruments: ['Batería'] }), ['instrument'])).toEqual({ field: 'instrument', strength: 2 });
    // Con 2 letras no se traduce: "pe" no está en "bateria"
    expect(match('pe', item({ instruments: ['Batería'] }), ['instrument'])).toBeNull();
  });

  it('sin vocabularios no traduce', () => {
    expect(match('teclado', item({ instruments: ['Piano'] }), ['instrument'], [])).toBeNull();
  });

  describe('décadas', () => {
    it.each([
      ['70', '1970s'],
      ['70s', '1970s'],
      ['75', '1970s'],
      ['1975', '1970s'],
      ['1970s', '1970s'],
      ['10', '2010s'],
      ['29', '2020s'],
      ['30', '1930s'],
      ['2000', '2000s'],
    ])('"%s" encuentra la década %s', (query, decade) => {
      expect(match(query, item({ decade }), ['decade'])).toEqual({ field: 'decade', strength: 2 });
    });

    it('traduce palabras del vocabulario y sus prefijos', () => {
      expect(match('setenta', item(), ['decade'])).toEqual({ field: 'decade', strength: 2 });
      expect(match('ochen', item({ decade: '1980s' }), ['decade'])).toEqual({ field: 'decade', strength: 2 });
    });

    it('con una sola cifra no coincide con ninguna década', () => {
      expect(match('7', item(), ['decade'])).toBeNull();
    });

    it('no coincide si la década es distinta o nula', () => {
      expect(match('80', item(), ['decade'])).toBeNull();
      expect(match('70', item({ decade: null }), ['decade'])).toBeNull();
    });

    it('los números de 3 cifras no se interpretan como década', () => {
      expect(match('197', item({ decade: '1980s' }), ['decade'])).toBeNull();
      // pero sí coinciden como texto si la década lo contiene
      expect(match('197', item(), ['decade'])).toEqual({ field: 'decade', strength: 2 });
    });

    it('un texto contenido en la década cuenta como coincidencia débil', () => {
      expect(match('0s', item(), ['decade'])).toEqual({ field: 'decade', strength: 1 });
    });
  });
});
