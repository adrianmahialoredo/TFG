import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useCatalogSearch } from '../../hooks/useCatalogSearch';
import { useGenreEntitiesStore, useInstrumentEntitiesStore, useItemEntitiesStore } from '../../stores/entities';
import * as itemRepository from '../../repositories/item.repository';
import * as genreRepository from '../../repositories/genre.repository';
import * as instrumentRepository from '../../repositories/instrument.repository';
import { ItemType } from '../../types';
import { SearchField } from '../../utils/search.utils';

jest.mock('../../repositories/item.repository', () => ({ getCatalog: jest.fn() }));
jest.mock('../../repositories/genre.repository', () => ({ getAll: jest.fn() }));
jest.mock('../../repositories/instrument.repository', () => ({ getAll: jest.fn() }));

const mockedItem = itemRepository as jest.Mocked<typeof itemRepository>;
const mockedGenre = genreRepository as jest.Mocked<typeof genreRepository>;
const mockedInstrument = instrumentRepository as jest.Mocked<typeof instrumentRepository>;

const FIELDS: SearchField[] = ['title', 'artist', 'genre', 'instrument', 'decade'];

describe('useCatalogSearch', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockedItem.getCatalog.mockResolvedValue([] as any);
    mockedGenre.getAll.mockResolvedValue([] as any);
    mockedInstrument.getAll.mockResolvedValue([] as any);
    useGenreEntitiesStore.setState({ genres: { g1: { id: 'g1', title: 'Rock' } as any } });
    useInstrumentEntitiesStore.setState({ instruments: { i1: { id: 'i1', name: 'Guitarra' } as any } });
    useItemEntitiesStore.setState({
      items: {
        s1: { id: 's1', title: 'Rocket', type: ItemType.SONG, artist: 'Elton', genreIds: ['g1'], decade: '1970s' } as any,
        s2: { id: 's2', title: 'Amor', type: ItemType.SONG, artist: 'Rockeros', genreIds: [], instrumentIds: ['i1'] } as any,
        s3: { id: 's3', title: 'Balada', type: ItemType.SONG, artist: 'X', genreIds: ['g1', 'gx'], instrumentIds: ['ix'] } as any,
        s4: { id: 's4', title: 'Mi rock', type: ItemType.SONG, artist: null } as any,
        l1: { id: 'l1', title: 'Rock básico', type: ItemType.LESSON } as any,
      },
    });
  });

  it('carga catálogo, géneros e instrumentos al montar', async () => {
    await renderHook(() => useCatalogSearch('', ItemType.SONG, FIELDS));
    expect(mockedItem.getCatalog).toHaveBeenCalled();
    expect(mockedGenre.getAll).toHaveBeenCalled();
    expect(mockedInstrument.getAll).toHaveBeenCalled();
  });

  it('registra los errores de carga sin romper', async () => {
    mockedItem.getCatalog.mockRejectedValue(new Error('a'));
    mockedGenre.getAll.mockRejectedValue(new Error('b'));
    mockedInstrument.getAll.mockRejectedValue(new Error('c'));
    await renderHook(() => useCatalogSearch('', ItemType.SONG, FIELDS));

    await waitFor(() => expect(console.error).toHaveBeenCalledTimes(3));
    expect(console.error).toHaveBeenCalledWith('[useCatalogSearch] Error cargando el catálogo:', expect.any(Error));
    expect(console.error).toHaveBeenCalledWith('[useCatalogSearch] Error cargando géneros:', expect.any(Error));
    expect(console.error).toHaveBeenCalledWith('[useCatalogSearch] Error cargando instrumentos:', expect.any(Error));
  });

  it('sin búsqueda devuelve una lista vacía', async () => {
    const { result } = await renderHook(() => useCatalogSearch('  ', ItemType.SONG, FIELDS));
    expect(result.current).toEqual([]);
  });

  it('agrupa por el campo de más prioridad, solo del tipo indicado y ordena por fuerza y título', async () => {
    const { result } = await renderHook(() => useCatalogSearch('rock', ItemType.SONG, FIELDS));

    expect(result.current.map((s) => s.field)).toEqual(['title', 'artist', 'genre']);
    // "Rocket" empieza por la búsqueda (fuerza 2), "Mi rock" solo la contiene
    expect(result.current[0].items.map((i) => i.id)).toEqual(['s1', 's4']);
    expect(result.current[1].items.map((i) => i.id)).toEqual(['s2']);
    expect(result.current[2].items.map((i) => i.id)).toEqual(['s3']);
  });

  it('busca por instrumento con el nombre de la Store', async () => {
    const { result } = await renderHook(() => useCatalogSearch('guitarra', ItemType.SONG, FIELDS));
    expect(result.current).toEqual([{ field: 'instrument', items: [expect.objectContaining({ id: 's2' })] }]);
  });

  it('busca por década', async () => {
    const { result } = await renderHook(() => useCatalogSearch('70', ItemType.SONG, ['decade']));
    expect(result.current).toEqual([{ field: 'decade', items: [expect.objectContaining({ id: 's1' })] }]);
  });

  it('filtra por tipo de item', async () => {
    const { result } = await renderHook(() => useCatalogSearch('rock', ItemType.LESSON, FIELDS));
    expect(result.current).toEqual([{ field: 'title', items: [expect.objectContaining({ id: 'l1' })] }]);
  });

  it('recalcula al cambiar la Store de items', async () => {
    const { result } = await renderHook(() => useCatalogSearch('zeta', ItemType.SONG, ['title']));
    expect(result.current).toEqual([]);

    await act(async () => {
      useItemEntitiesStore.setState({ items: { z: { id: 'z', title: 'Zeta', type: ItemType.SONG } as any } });
    });
    expect(result.current[0].items.map((i) => i.id)).toEqual(['z']);
  });
});
