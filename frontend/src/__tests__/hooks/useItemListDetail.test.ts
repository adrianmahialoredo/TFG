import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useItemListDetail } from '../../hooks/useItemListDetail';
import { useItemEntitiesStore, useItemListEntitiesStore } from '../../stores/entities';
import * as itemListRepository from '../../repositories/itemList.repository';
import * as itemRepository from '../../repositories/item.repository';

jest.mock('../../repositories/itemList.repository', () => ({
  getItemListWithItems: jest.fn(),
  updateItemList: jest.fn(),
  updateItemListImage: jest.fn(),
  deleteItemList: jest.fn(),
  removeItemFromList: jest.fn(),
}));
jest.mock('../../repositories/item.repository', () => ({ getAllById: jest.fn() }));

const mockedList = itemListRepository as jest.Mocked<typeof itemListRepository>;
const mockedItem = itemRepository as jest.Mocked<typeof itemRepository>;

describe('useItemListDetail', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useItemListEntitiesStore.setState({ itemLists: { L1: { id: 'L1', title: 'Mi lista', itemIds: ['a', 'b'] } as any } });
    useItemEntitiesStore.setState({ items: { a: { id: 'a', title: 'A' } as any } });
    mockedList.getItemListWithItems.mockResolvedValue({} as any);
    mockedItem.getAllById.mockResolvedValue([] as any);
  });

  it('carga el detalle y resuelve los items desde la Store', async () => {
    const { result } = await renderHook(() => useItemListDetail('L1'));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mockedList.getItemListWithItems).toHaveBeenCalledWith('L1');
    expect(result.current.list).toEqual(expect.objectContaining({ id: 'L1' }));
    expect(result.current.items.map((i) => i.id)).toEqual(['a']);
    expect(result.current.error).toBeNull();
    // Trae los items que faltan en RAM
    expect(mockedItem.getAllById).toHaveBeenCalledWith(['b']);
  });

  it('muestra un error si falla la carga', async () => {
    mockedList.getItemListWithItems.mockRejectedValue(new Error('red'));
    const { result } = await renderHook(() => useItemListDetail('L1'));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.error).toBe('Error al cargar el detalle');
  });

  it('refresh vuelve a cargar y limpia el error', async () => {
    mockedList.getItemListWithItems.mockRejectedValueOnce(new Error('red'));
    const { result } = await renderHook(() => useItemListDetail('L1'));
    await waitFor(() => expect(result.current.error).not.toBeNull());

    await act(async () => {
      await result.current.refresh();
    });
    expect(result.current.error).toBeNull();
    expect(mockedList.getItemListWithItems).toHaveBeenCalledTimes(2);
  });

  it('sin lista devuelve null y no pide items', async () => {
    const { result } = await renderHook(() => useItemListDetail(''));
    expect(result.current.list).toBeNull();
    expect(result.current.items).toEqual([]);
    expect(mockedList.getItemListWithItems).not.toHaveBeenCalled();
    expect(mockedItem.getAllById).not.toHaveBeenCalled();
  });

  it('no pide items si están todos en la Store', async () => {
    useItemEntitiesStore.setState({ items: { a: { id: 'a' } as any, b: { id: 'b' } as any } });
    await renderHook(() => useItemListDetail('L1'));
    expect(mockedItem.getAllById).not.toHaveBeenCalled();
  });

  it('registra el error si fallan los items que faltan', async () => {
    mockedItem.getAllById.mockRejectedValue(new Error('x'));
    await renderHook(() => useItemListDetail('L1'));
    await waitFor(() => expect(console.error).toHaveBeenCalledWith(expect.any(Error)));
  });

  it('updateList devuelve la lista actualizada', async () => {
    mockedList.updateItemList.mockResolvedValue({ id: 'L1', name: 'Nuevo' } as any);
    const { result } = await renderHook(() => useItemListDetail('L1'));
    await expect(result.current.updateList({ name: 'Nuevo' })).resolves.toEqual({ id: 'L1', name: 'Nuevo' });
    expect(mockedList.updateItemList).toHaveBeenCalledWith('L1', { name: 'Nuevo' });
  });

  it('updateList lanza si no se pudo actualizar', async () => {
    mockedList.updateItemList.mockResolvedValue(null as any);
    const { result } = await renderHook(() => useItemListDetail('L1'));
    await expect(result.current.updateList({})).rejects.toThrow('No se pudo actualizar la lista');
  });

  it('updateImage devuelve la lista actualizada o lanza si falla', async () => {
    const { result } = await renderHook(() => useItemListDetail('L1'));
    mockedList.updateItemListImage.mockResolvedValueOnce({ id: 'L1' } as any);
    await expect(result.current.updateImage('file://x')).resolves.toEqual({ id: 'L1' });
    expect(mockedList.updateItemListImage).toHaveBeenCalledWith('L1', 'file://x');

    mockedList.updateItemListImage.mockResolvedValueOnce(null as any);
    await expect(result.current.updateImage('file://y')).rejects.toThrow('No se pudo actualizar la foto de la lista');
  });

  it('removeItem quita el item y devuelve la lista actualizada', async () => {
    mockedList.removeItemFromList.mockResolvedValue({ id: 'L1', itemIds: ['b'] } as any);
    const { result } = await renderHook(() => useItemListDetail('L1'));
    await expect(result.current.removeItem('a')).resolves.toEqual({ id: 'L1', itemIds: ['b'] });
    expect(mockedList.removeItemFromList).toHaveBeenCalledWith('L1', 'a');
  });

  it('removeItem lanza si no se pudo quitar', async () => {
    mockedList.removeItemFromList.mockResolvedValue(null);
    const { result } = await renderHook(() => useItemListDetail('L1'));
    await expect(result.current.removeItem('a')).rejects.toThrow('No se pudo quitar el item de la lista');
  });

  it('deleteList delega en el repositorio', async () => {
    mockedList.deleteItemList.mockResolvedValue(true as any);
    const { result } = await renderHook(() => useItemListDetail('L1'));
    await expect(result.current.deleteList()).resolves.toBe(true);
    expect(mockedList.deleteItemList).toHaveBeenCalledWith('L1');
  });
});
