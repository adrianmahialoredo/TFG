import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useAddItemScreen, VISIBLE_RECOMMENDATIONS } from '../../hooks/useAddItemScreen';
import { useItemEntitiesStore, useItemListEntitiesStore } from '../../stores/entities';
import * as itemRepository from '../../repositories/item.repository';
import * as itemFeedRepository from '../../repositories/itemFeed.repository';
import * as itemListRepository from '../../repositories/itemList.repository';
import { ItemType } from '../../types';

jest.mock('../../repositories/item.repository', () => ({ getCatalog: jest.fn() }));
jest.mock('../../repositories/itemFeed.repository', () => ({ getRecommendedByItemList: jest.fn() }));
jest.mock('../../repositories/itemList.repository', () => ({ addItemToList: jest.fn() }));

const mockedItem = itemRepository as jest.Mocked<typeof itemRepository>;
const mockedFeed = itemFeedRepository as jest.Mocked<typeof itemFeedRepository>;
const mockedList = itemListRepository as jest.Mocked<typeof itemListRepository>;

const item = (id: string, title: string, artist?: string) =>
  ({ id, title, artist, type: ItemType.SONG, imageUrl: '' }) as any;

const items = [
  item('1', 'Canción del mar', 'Artista A'),
  item('2', 'Otra canción', 'Artista B'),
  item('3', 'Blues', 'Cancionero'),
  item('4', 'Rock'),
];

describe('useAddItemScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useItemEntitiesStore.setState({ items: Object.fromEntries(items.map((i) => [i.id, i])) });
    useItemListEntitiesStore.setState({ itemLists: { L1: { id: 'L1', itemIds: ['4'] } as any } });
    mockedFeed.getRecommendedByItemList.mockResolvedValue(['1', '2', '4', 'inexistente']);
    mockedItem.getCatalog.mockResolvedValue(['1', '2', '3', '4', 'inexistente']);
    mockedList.addItemToList.mockResolvedValue({ id: 'L1' } as any);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('carga las recomendaciones y oculta las que ya están en la lista', async () => {
    const { result } = await renderHook(() => useAddItemScreen('L1'));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mockedFeed.getRecommendedByItemList).toHaveBeenCalledWith('L1', expect.any(Function));
    expect(result.current.items.map((i) => i.id)).toEqual(['1', '2']);
    expect(result.current.error).toBeNull();
    expect(result.current.isAdded('4')).toBe(true);
    expect(result.current.isAdded('1')).toBe(false);
  });

  it('aplica los IDs frescos que llegan por onFresh', async () => {
    let onFresh: (ids: string[]) => void = () => {};
    mockedFeed.getRecommendedByItemList.mockImplementation(async (_id, cb) => {
      onFresh = cb as any;
      return ['1'];
    });
    const { result } = await renderHook(() => useAddItemScreen('L1'));
    await waitFor(() => expect(result.current.items.map((i) => i.id)).toEqual(['1']));

    await act(async () => onFresh(['3']));
    expect(result.current.items.map((i) => i.id)).toEqual(['3']);
  });

  it('muestra un error si fallan las recomendaciones', async () => {
    mockedFeed.getRecommendedByItemList.mockRejectedValue(new Error('red'));
    const { result } = await renderHook(() => useAddItemScreen('L1'));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.error).toBe('Error al cargar recomendaciones');
  });

  it('no pide recomendaciones sin itemListId y no se queda cargando', async () => {
    const { result } = await renderHook(() => useAddItemScreen(''));
    expect(mockedFeed.getRecommendedByItemList).not.toHaveBeenCalled();
    await waitFor(() => expect(result.current.loading).toBe(false));
  });

  it('busca en local por título y artista, sin tildes y priorizando los que empiezan por la búsqueda', async () => {
    const { result } = await renderHook(() => useAddItemScreen('L1'));
    await waitFor(() => expect(result.current.loading).toBe(false));

    await act(async () => result.current.setQuery('cancion'));
    expect(result.current.query).toBe('cancion');
    // "Canción del mar" empieza por la búsqueda; luego por orden alfabético
    expect(result.current.items.map((i) => i.id)).toEqual(['1', '3', '2']);
    expect(result.current.searching).toBe(false);
  });

  it('una búsqueda de solo espacios muestra las recomendaciones', async () => {
    const { result } = await renderHook(() => useAddItemScreen('L1'));
    await waitFor(() => expect(result.current.loading).toBe(false));

    await act(async () => result.current.setQuery('   '));
    expect(result.current.items.map((i) => i.id)).toEqual(['1', '2']);
  });

  it('indica que busca mientras el catálogo aún no ha llegado', async () => {
    mockedItem.getCatalog.mockReturnValue(new Promise(() => {}));
    const { result } = await renderHook(() => useAddItemScreen('L1'));

    await act(async () => result.current.setQuery('rock'));
    expect(result.current.searching).toBe(true);
    expect(result.current.items).toEqual([]);
  });

  it('registra el error si falla el catálogo y deja de buscar', async () => {
    mockedItem.getCatalog.mockRejectedValue(new Error('catálogo'));
    const { result } = await renderHook(() => useAddItemScreen('L1'));
    await act(async () => result.current.setQuery('rock'));

    await waitFor(() => expect(result.current.searching).toBe(false));
    expect(console.error).toHaveBeenCalledWith('[useAddItemScreen] Error cargando el catálogo:', expect.any(Error));
  });

  it('addItem añade el item, lo marca y lo quita de la pantalla tras el tic', async () => {
    jest.useFakeTimers();
    const { result } = await renderHook(() => useAddItemScreen('L1'));
    await act(async () => {
      await jest.runOnlyPendingTimersAsync();
    });
    await waitFor(() => expect(result.current.loading).toBe(false));

    await act(async () => {
      await result.current.addItem('1');
    });
    expect(mockedList.addItemToList).toHaveBeenCalledWith('L1', '1');
    expect(result.current.isAdded('1')).toBe(true);
    // Mientras se ve el tic sigue en la lista
    expect(result.current.items.map((i) => i.id)).toEqual(['1', '2']);

    await act(async () => {
      jest.advanceTimersByTime(600);
    });
    expect(result.current.items.map((i) => i.id)).toEqual(['2']);
  });

  it('muestra solo VISIBLE_RECOMMENDATIONS recomendaciones y al añadir una entra la siguiente', async () => {
    jest.useFakeTimers();
    const many = Array.from({ length: 8 }, (_, i) => item(`r${i}`, `Rec ${i}`));
    useItemEntitiesStore.setState({ items: Object.fromEntries(many.map((i) => [i.id, i])) });
    mockedFeed.getRecommendedByItemList.mockResolvedValue(many.map((i) => i.id));
    const { result } = await renderHook(() => useAddItemScreen('L1'));
    await act(async () => {
      await jest.runOnlyPendingTimersAsync();
    });
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(VISIBLE_RECOMMENDATIONS).toBe(5);
    expect(result.current.items.map((i) => i.id)).toEqual(['r0', 'r1', 'r2', 'r3', 'r4']);

    await act(async () => {
      await result.current.addItem('r1');
    });
    // Mientras se ve el tic, el añadido conserva su hueco
    expect(result.current.items.map((i) => i.id)).toEqual(['r0', 'r1', 'r2', 'r3', 'r4']);

    await act(async () => {
      jest.advanceTimersByTime(600);
    });
    // Sale el añadido y entra la siguiente en espera
    expect(result.current.items.map((i) => i.id)).toEqual(['r0', 'r2', 'r3', 'r4', 'r5']);
  });

  it('la búsqueda no se limita a VISIBLE_RECOMMENDATIONS', async () => {
    const many = Array.from({ length: 8 }, (_, i) => item(`r${i}`, `Rec ${i}`));
    useItemEntitiesStore.setState({ items: Object.fromEntries(many.map((i) => [i.id, i])) });
    mockedItem.getCatalog.mockResolvedValue(many.map((i) => i.id));
    const { result } = await renderHook(() => useAddItemScreen('L1'));
    await waitFor(() => expect(result.current.loading).toBe(false));

    await act(async () => result.current.setQuery('rec'));
    expect(result.current.items).toHaveLength(8);
  });

  it('addItem no hace nada si el item ya está en la lista', async () => {
    const { result } = await renderHook(() => useAddItemScreen('L1'));
    await waitFor(() => expect(result.current.loading).toBe(false));

    await act(async () => {
      await result.current.addItem('4');
    });
    expect(mockedList.addItemToList).not.toHaveBeenCalled();
  });

  it('addItem revierte el feedback y muestra error si falla', async () => {
    mockedList.addItemToList.mockResolvedValue(null as any);
    const { result } = await renderHook(() => useAddItemScreen('L1'));
    await waitFor(() => expect(result.current.loading).toBe(false));

    await act(async () => {
      await result.current.addItem('2');
    });
    expect(result.current.isAdded('2')).toBe(false);
    expect(result.current.error).toBe('No se pudo añadir el item a la lista');
    expect(result.current.items.map((i) => i.id)).toEqual(['1', '2']);
  });

  it('limpia los temporizadores pendientes al desmontar', async () => {
    jest.useFakeTimers();
    const clearSpy = jest.spyOn(global, 'clearTimeout');
    const { result, unmount } = await renderHook(() => useAddItemScreen('L1'));
    await act(async () => {
      await jest.runOnlyPendingTimersAsync();
    });

    await act(async () => {
      await result.current.addItem('1');
    });
    await unmount();
    expect(clearSpy).toHaveBeenCalled();
  });

  it('ignora resultados que llegan tras desmontar', async () => {
    let resolveRecs: (v: string[]) => void = () => {};
    let resolveCatalog: (v: string[]) => void = () => {};
    mockedFeed.getRecommendedByItemList.mockReturnValue(new Promise((r) => { resolveRecs = r; }));
    mockedItem.getCatalog.mockReturnValue(new Promise((r) => { resolveCatalog = r; }));

    const { unmount } = await renderHook(() => useAddItemScreen('L1'));
    await unmount();
    await act(async () => {
      resolveRecs(['1']);
      resolveCatalog(['1']);
    });
    expect(console.error).not.toHaveBeenCalled();
  });
});
