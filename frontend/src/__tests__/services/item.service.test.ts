import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import * as service from '../../services/item.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;
type Method = 'get' | 'post' | 'put' | 'delete';

const DATA = [{ id: '1', type: 'Song' }];
const E = API_ENDPOINTS.ITEMS;

const cases: [string, () => Promise<unknown>, Method, unknown[]][] = [
  ['getAll', () => service.getAll(), 'get', [E.BASE]],
  ['getById', () => service.getById('9'), 'get', [E.BY_ID('9')]],
  ['getAllById', () => service.getAllById(['1', '2']), 'post', [E.BY_IDS, ['1', '2']]],
  ['getRecommendedByItemList', () => service.getRecommendedByItemList('l1'), 'get', [E.RECOMMENDED_BY_ITEM_LIST('l1')]],
  ['getRecommendedByUser', () => service.getRecommendedByUser('u1'), 'get', [E.RECOMMENDED_BY_USER('u1')]],
];

describe('item.service', () => {
  beforeEach(() => jest.clearAllMocks());

  it.each(cases)('%s llama al endpoint correcto y devuelve los datos', async (_n, call, method, args) => {
    api[method].mockResolvedValue({ data: DATA } as any);
    await expect(call()).resolves.toEqual(DATA);
    expect(api[method]).toHaveBeenCalledWith(...args);
  });

  it.each(cases)('%s propaga los errores de la API', async (_n, call, method) => {
    const error = new Error('fallo');
    api[method].mockRejectedValue(error);
    await expect(call()).rejects.toBe(error);
  });
});
