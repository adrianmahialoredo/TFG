import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import * as service from '../../services/itemList.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;
const E = API_ENDPOINTS.ITEM_LISTS;

/** Así llega una lista del backend (IDs numéricos). */
const backendList = { id: 5, userId: 9, name: 'Favs', imageUrl: 'img', description: 'd', itemIds: [1, 2] };
const appList = { id: '5', userId: '9', name: 'Favs', imageUrl: 'img', description: 'd', itemIds: ['1', '2'] };

describe('itemList.service', () => {
  beforeEach(() => jest.clearAllMocks());

  it('getAll convierte cada lista al DTO de la app', async () => {
    api.get.mockResolvedValue({ data: [backendList] } as any);
    await expect(service.getAll()).resolves.toEqual([appList]);
    expect(api.get).toHaveBeenCalledWith(E.BASE);
  });

  it('getById convierte la lista y usa [] si no trae itemIds', async () => {
    api.get.mockResolvedValue({ data: { id: 1, userId: 2, name: 'x' } } as any);
    await expect(service.getById('1')).resolves.toEqual({
      id: '1',
      userId: '2',
      name: 'x',
      imageUrl: undefined,
      description: undefined,
      itemIds: [],
    });
    expect(api.get).toHaveBeenCalledWith(E.BY_ID('1'));
  });

  it('getUserIds convierte los IDs a string', async () => {
    api.get.mockResolvedValue({ data: [1, '2'] } as any);
    await expect(service.getUserIds('9')).resolves.toEqual(['1', '2']);
    expect(api.get).toHaveBeenCalledWith(E.IDS_BY_USER('9'));
  });

  it('getAllById envía los IDs en el body y convierte la respuesta', async () => {
    api.post.mockResolvedValue({ data: [backendList] } as any);
    await expect(service.getAllById(['5'])).resolves.toEqual([appList]);
    expect(api.post).toHaveBeenCalledWith(E.BY_IDS, ['5']);
  });

  it('createItemList envía IDs numéricos al backend', async () => {
    api.post.mockResolvedValue({ data: backendList } as any);
    const result = await service.createItemList({ userId: '9', name: 'Favs', imageUrl: 'img', itemIds: ['1', '2'] } as any);
    expect(api.post).toHaveBeenCalledWith(E.BASE, { userId: 9, name: 'Favs', imageUrl: 'img', itemIds: [1, 2] });
    expect(result).toEqual(appList);
  });

  it('createItemList sin itemIds envía []', async () => {
    api.post.mockResolvedValue({ data: backendList } as any);
    await service.createItemList({ userId: '9', name: 'Favs' } as any);
    expect(api.post).toHaveBeenCalledWith(E.BASE, { userId: 9, name: 'Favs', imageUrl: undefined, itemIds: [] });
  });

  it('updateItemList hace PUT con la lista y convierte la respuesta', async () => {
    api.put.mockResolvedValue({ data: backendList } as any);
    await expect(service.updateItemList('5', appList as any)).resolves.toEqual(appList);
    expect(api.put).toHaveBeenCalledWith(E.BY_ID('5'), appList);
  });

  it('deleteItemList hace DELETE y no devuelve nada', async () => {
    api.delete.mockResolvedValue({ data: null } as any);
    await expect(service.deleteItemList('5')).resolves.toBeUndefined();
    expect(api.delete).toHaveBeenCalledWith(E.BY_ID('5'));
  });

  it('addItemToList hace POST sobre el item de la lista', async () => {
    api.post.mockResolvedValue({ data: backendList } as any);
    await expect(service.addItemToList('5', '2')).resolves.toEqual(appList);
    expect(api.post).toHaveBeenCalledWith(E.ITEM('5', '2'));
  });

  it('removeItemFromList hace DELETE sobre el item de la lista', async () => {
    api.delete.mockResolvedValue({ data: backendList } as any);
    await expect(service.removeItemFromList('5', '2')).resolves.toEqual(appList);
    expect(api.delete).toHaveBeenCalledWith(E.ITEM('5', '2'));
  });

  it.each([
    ['getAll', 'get', () => service.getAll()],
    ['getById', 'get', () => service.getById('1')],
    ['getUserIds', 'get', () => service.getUserIds('1')],
    ['getAllById', 'post', () => service.getAllById(['1'])],
    ['createItemList', 'post', () => service.createItemList({ userId: '1', name: 'x' } as any)],
    ['updateItemList', 'put', () => service.updateItemList('1', appList as any)],
    ['deleteItemList', 'delete', () => service.deleteItemList('1')],
    ['addItemToList', 'post', () => service.addItemToList('1', '2')],
    ['removeItemFromList', 'delete', () => service.removeItemFromList('1', '2')],
  ] as const)('%s propaga los errores de la API', async (_n, method, call) => {
    const error = new Error('fallo');
    api[method].mockRejectedValue(error);
    await expect(call()).rejects.toBe(error);
  });
});
