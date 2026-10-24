import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useItemLists } from '../../hooks/useItemLists';
import { useItemListEntitiesStore } from '../../stores/entities';
import * as itemListRepository from '../../repositories/itemList.repository';

jest.mock('../../repositories/itemList.repository', () => ({ getByUserId: jest.fn(), createItemList: jest.fn() }));
const mocked = itemListRepository as jest.Mocked<typeof itemListRepository>;

describe('useItemLists', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useItemListEntitiesStore.setState({
      itemLists: {
        L1: { id: 'L1', userId: '5' } as any,
        L2: { id: 'L2', userId: '6' } as any,
      },
    });
    mocked.getByUserId.mockResolvedValue([] as any);
    mocked.createItemList.mockResolvedValue({ id: 'L3' } as any);
  });

  it('carga las listas del usuario y filtra las suyas', async () => {
    let resolve: (v: any) => void = () => {};
    mocked.getByUserId.mockReturnValue(new Promise((r) => { resolve = r; }));
    const { result } = await renderHook(() => useItemLists(5));
    expect(result.current.loading).toBe(true);
    expect(mocked.getByUserId).toHaveBeenCalledWith('5');

    await act(async () => resolve([]));
    expect(result.current.loading).toBe(false);
    expect(result.current.lists.map((l) => l.id)).toEqual(['L1']);
  });

  it('si el repositorio falla deja de cargar y registra el error', async () => {
    mocked.getByUserId.mockRejectedValue(new Error('red caída'));
    const { result } = await renderHook(() => useItemLists('5'));
    await waitFor(() => expect(mocked.getByUserId).toHaveBeenCalled());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(console.error).toHaveBeenCalledWith('[useItemLists] Error cargando listas:', expect.any(Error));
  });

  it('sin usuario no carga y devuelve listas vacías', async () => {
    const { result } = await renderHook(() => useItemLists(undefined));
    expect(result.current.lists).toEqual([]);
    expect(mocked.getByUserId).not.toHaveBeenCalled();
    await expect(result.current.createList({ title: 'x' } as any)).resolves.toBeNull();
    expect(mocked.createItemList).not.toHaveBeenCalled();
  });

  it('createList crea la lista con el userId y gestiona creating', async () => {
    let resolve: (v: any) => void = () => {};
    mocked.createItemList.mockReturnValue(new Promise((r) => { resolve = r; }));
    const { result } = await renderHook(() => useItemLists('5'));
    await waitFor(() => expect(result.current.loading).toBe(false));

    let promise: Promise<any>;
    await act(async () => {
      promise = result.current.createList({ title: 'Nueva' } as any);
    });
    expect(result.current.creating).toBe(true);
    expect(mocked.createItemList).toHaveBeenCalledWith({ title: 'Nueva', userId: '5' });

    await act(async () => {
      resolve({ id: 'L3' });
      await promise;
    });
    await expect(promise!).resolves.toEqual({ id: 'L3' });
    expect(result.current.creating).toBe(false);
  });

  it('createList quita creating aunque falle', async () => {
    mocked.createItemList.mockRejectedValue(new Error('x'));
    const { result } = await renderHook(() => useItemLists('5'));
    await act(async () => {
      await expect(result.current.createList({ title: 'x' } as any)).rejects.toThrow('x');
    });
    expect(result.current.creating).toBe(false);
  });
});
