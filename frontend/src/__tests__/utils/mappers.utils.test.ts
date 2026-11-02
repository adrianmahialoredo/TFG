import {
  genreToCarouselItem,
  genresToCarouselItems,
  getItemSubtitle,
  instrumentToCarouselItem,
  instrumentsToCarouselItems,
  itemToCarouselItem,
  itemToListItem,
  itemsToCarouselItems,
  toItemSummary,
} from '../../utils/mappers.utils';
import { ItemSummaryDTO, ItemType } from '../../types';

const summary = (overrides: Partial<ItemSummaryDTO> = {}): ItemSummaryDTO => ({
  id: '1',
  title: 'Título',
  imageUrl: 'img.png',
  type: ItemType.SONG,
  ...overrides,
});

const base = { id: '1', title: 'Título', imageUrl: 'img.png', level: 3 };

describe('toItemSummary', () => {
  it('mapea una canción con artista, década y metadatos como strings', () => {
    const song = { ...base, type: ItemType.SONG, artist: 'Queen', decade: '1970s', genreIds: [1, 2], instrumentIds: [3] };
    expect(toItemSummary(song as any)).toEqual({
      id: '1',
      title: 'Título',
      imageUrl: 'img.png',
      type: ItemType.SONG,
      level: 3,
      artist: 'Queen',
      genreIds: ['1', '2'],
      instrumentIds: ['3'],
      decade: '1970s',
    });
  });

  it('en una canción sin géneros, instrumentos ni década deja listas vacías y no añade la década', () => {
    const result = toItemSummary({ ...base, type: ItemType.SONG, artist: 'X' } as any);
    expect(result.genreIds).toEqual([]);
    expect(result.instrumentIds).toEqual([]);
    expect(result).not.toHaveProperty('decade');
  });

  it('conserva una década null en canciones', () => {
    const result = toItemSummary({ ...base, type: ItemType.SONG, decade: null } as any);
    expect(result.decade).toBeNull();
  });

  it('filtra IDs nulos', () => {
    const result = toItemSummary({ ...base, type: ItemType.SONG, genreIds: [1, null, undefined, 2] } as any);
    expect(result.genreIds).toEqual(['1', '2']);
  });

  it('mapea una lección con género e instrumento y artista null', () => {
    const lesson = { ...base, type: ItemType.LESSON, genreId: 4, instrumentId: 5 };
    const result = toItemSummary(lesson as any);
    expect(result.artist).toBeNull();
    expect(result.genreIds).toEqual(['4']);
    expect(result.instrumentIds).toEqual(['5']);
  });

  it('en una lección sin género ni instrumento no incluye esos campos', () => {
    const result = toItemSummary({ ...base, type: ItemType.LESSON, genreId: null } as any);
    expect(result).not.toHaveProperty('genreIds');
    expect(result).not.toHaveProperty('instrumentIds');
  });

  it('mapea un género-instrumento con su género e instrumento', () => {
    const ig = { ...base, type: ItemType.INSTRUMENT_GENRE, genreId: 7, instrumentId: 8 };
    const result = toItemSummary(ig as any);
    expect(result.genreIds).toEqual(['7']);
    expect(result.instrumentIds).toEqual(['8']);
  });

  it('un género no añade metadatos de búsqueda', () => {
    const result = toItemSummary({ ...base, type: ItemType.GENRE } as any);
    expect(result).toEqual({ ...base, type: ItemType.GENRE, artist: null });
  });

  it('usa el tipo explícito en lugar del tipo del DTO', () => {
    const result = toItemSummary({ ...base, artist: 'Y' } as any, ItemType.SONG);
    expect(result.type).toBe(ItemType.SONG);
    expect(result.artist).toBe('Y');
  });
});

describe('itemToCarouselItem', () => {
  it('en canciones usa el artista como artista y descripción', () => {
    expect(itemToCarouselItem(summary({ artist: 'Queen' }))).toEqual({
      id: '1', title: 'Título', imgUrl: 'img.png', artist: 'Queen', description: 'Queen',
    });
  });

  it('en canciones sin artista deja artista y descripción sin definir', () => {
    const result = itemToCarouselItem(summary({ artist: null }));
    expect(result.artist).toBeUndefined();
    expect(result.description).toBeUndefined();
  });

  it('en lecciones usa el título como descripción', () => {
    expect(itemToCarouselItem(summary({ type: ItemType.LESSON }))).toEqual({
      id: '1', title: 'Título', imgUrl: 'img.png', description: 'Título',
    });
  });

  it('en otros tipos no añade artista ni descripción', () => {
    expect(itemToCarouselItem(summary({ type: ItemType.GENRE }))).toEqual({ id: '1', title: 'Título', imgUrl: 'img.png' });
  });

  it('itemsToCarouselItems mapea cada elemento', () => {
    expect(itemsToCarouselItems([summary({ id: 'a' }), summary({ id: 'b' })]).map((i) => i.id)).toEqual(['a', 'b']);
    expect(itemsToCarouselItems([])).toEqual([]);
  });
});

describe('géneros e instrumentos a carrusel', () => {
  it('genreToCarouselItem copia la descripción', () => {
    const genre = { id: 'g', title: 'Rock', imageUrl: 'r.png', description: 'Desc' };
    expect(genreToCarouselItem(genre as any)).toEqual({ id: 'g', title: 'Rock', imgUrl: 'r.png', description: 'Desc' });
    expect(genresToCarouselItems([genre as any])).toHaveLength(1);
  });

  it('instrumentToCarouselItem usa el nombre como título', () => {
    const instrument = { id: 'i', name: 'Guitarra', imageUrl: 'g.png' };
    expect(instrumentToCarouselItem(instrument as any)).toEqual({ id: 'i', title: 'Guitarra', imgUrl: 'g.png' });
    expect(instrumentsToCarouselItems([instrument as any, instrument as any])).toHaveLength(2);
  });
});

describe('getItemSubtitle', () => {
  const names = {
    genres: { '7': { id: '7', title: 'Rock' } },
    instruments: { '3': { id: '3', name: 'Guitarra' } },
  } as any;

  it.each([
    [ItemType.LESSON, 'Lección'],
    [ItemType.GENRE, 'Género'],
    [ItemType.INSTRUMENT_GENRE, 'Instrumento'],
  ])('para %s sin contexto devuelve "%s"', (type, label) => {
    expect(getItemSubtitle(summary({ type, artist: 'Ignorado' }))).toBe(label);
  });

  it('en canciones antepone el artista', () => {
    expect(getItemSubtitle(summary({ artist: 'Queen' }))).toBe('Queen · Canción');
  });

  it('en canciones sin artista solo muestra el tipo', () => {
    expect(getItemSubtitle(summary({ artist: null }))).toBe('Canción');
  });

  it('en lecciones antepone el instrumento', () => {
    const lesson = summary({ type: ItemType.LESSON, genreIds: ['7'], instrumentIds: ['3'] });
    expect(getItemSubtitle(lesson, names)).toBe('Guitarra · Lección');
  });

  it('en lecciones sin instrumento antepone el género', () => {
    const lesson = summary({ type: ItemType.LESSON, genreIds: ['7'], instrumentIds: [] });
    expect(getItemSubtitle(lesson, names)).toBe('Rock · Lección');
  });

  it('en lecciones con ids que no están en las Stores solo muestra el tipo', () => {
    const lesson = summary({ type: ItemType.LESSON, genreIds: ['99'], instrumentIds: ['98'] });
    expect(getItemSubtitle(lesson, names)).toBe('Lección');
  });

  it('con un tipo desconocido devuelve cadena vacía', () => {
    expect(getItemSubtitle(summary({ type: 'Otro' as ItemType }))).toBe('');
  });
});

describe('itemToListItem', () => {
  it('construye descripción e info con título y subtítulo', () => {
    expect(itemToListItem(summary({ artist: 'Queen' }))).toEqual({
      id: '1',
      title: 'Título',
      description: 'Queen · Canción',
      imageUrl: 'img.png',
      info: 'Título\nQueen · Canción',
    });
  });

  it('en géneros pone título y tipo en una sola línea', () => {
    expect(itemToListItem(summary({ type: ItemType.GENRE, title: 'Rock' })).info).toBe('Rock · Género');
  });
});
