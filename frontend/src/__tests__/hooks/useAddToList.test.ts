import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useAddToList } from '../../hooks/useAddToList';
import { useAuthStore, useItemListEntitiesStore } from '../../stores/entities';
import * as itemListRepository from '../../repositories/itemList.repository';

jest.mock('../../repositories/itemList.repository', () => ({ getByUserId: jest.fn(), addItemToList: jest.fn() }));
const mocked = itemListRepository as jest.Mocked<typeof itemListRepository>;

describe('useAddToList', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useAuthStore.setState({ currentUserId: '5' } as any);
    useItemListEntitiesStore.setState({
      itemLists: {
        L1: { id: 'L1', userId: '5', name: 'Rock', itemIds: ['i1'] } as any,
        L2: { id: 'L2', userId: '5', name: 'Blues', itemIds: [] } as any,
        L3: { id: 'L3', userId: '6', name: 'Ajena', itemIds: [] } as any,
      },
    });
    mocked.getByUserId.mockResolvedValue([] as any);
  });

  it('devuelve las listas del usuario actual ordenadas por nombre', async () => {
    const { result } = await renderHook(() => useAddToList('i1'));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.lists.map((l) => l.name)).toEqual(['Blues', 'Rock']);
  });

  it('contains indica si la lista ya tiene el item', async () => {
    const { result } = await renderHook(() => useAddToList('i1'));
    expect(result.current.contains('L1')).toBe(true);
    expect(result.current.contains('L2')).toBe(false);
  });

  it('addToList añade el item y marca la lista mientras se añade', async () => {
    let resolve: (v: any) => void = () => {};
    mocked.addItemToList.mockReturnValue(new Promise((r) => { resolve = r; }));
    const { result } = await renderHook(() => useAddToList('i1'));

    let promise: Promise<boolean>;
    await act(async () => {
      promise = result.current.addToList('L2');
    });
    expect(mocked.addItemToList).toHaveBeenCalledWith('L2', 'i1');
    expect(result.current.addingListId).toBe('L2');

    await act(async () => resolve({ id: 'L2' }));
    await expect(promise!).resolves.toBe(true);
    expect(result.current.addingListId).toBeNull();
    expect(result.current.error).toBeNull();
  });

  it('si la lista ya tiene el item no llama al repositorio', async () => {
    const { result } = await renderHook(() => useAddToList('i1'));
    let added = false;
    await act(async () => {
      added = await result.current.addToList('L1');
    });
    expect(added).toBe(true);
    expect(mocked.addItemToList).not.toHaveBeenCalled();
  });

  it('si falla devuelve false y expone el error, que se limpia al cambiar de item', async () => {
    mocked.addItemToList.mockResolvedValue(null);
    const { result, rerender } = await renderHook(
      ({ id }: { id: string }) => useAddToList(id),
      { initialProps: { id: 'i1' } }
    );
    let added = true;
    await act(async () => {
      added = await result.current.addToList('L2');
    });
    expect(added).toBe(false);
    expect(result.current.error).toBe('No se pudo añadir a la lista');

    await rerender({ id: 'i2' });
    expect(result.current.error).toBeNull();
  });

  it('sin item no añade nada', async () => {
    const { result } = await renderHook(() => useAddToList(null));
    let added = true;
    await act(async () => {
      added = await result.current.addToList('L2');
    });
    expect(added).toBe(false);
    expect(result.current.contains('L1')).toBe(false);
    expect(mocked.addItemToList).not.toHaveBeenCalled();
  });
});
