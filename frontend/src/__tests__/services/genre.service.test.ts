import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import * as service from '../../services/genre.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;
type Method = 'get' | 'post' | 'put' | 'delete';

const genre = { id: '1', title: 'Rock' } as any;
const DATA = [{ id: '1' }];

const cases: [string, () => Promise<unknown>, Method, unknown[], boolean][] = [
  ['getAll', () => service.getAll(), 'get', [API_ENDPOINTS.GENRES.BASE], true],
  ['getById', () => service.getById('7'), 'get', [API_ENDPOINTS.GENRES.BY_ID('7')], true],
  ['getAllById', () => service.getAllById(['1', '2']), 'post', [API_ENDPOINTS.GENRES.BY_IDS, ['1', '2']], true],
  ['createGenre', () => service.createGenre(genre), 'post', [API_ENDPOINTS.GENRES.BASE, genre], true],
  ['updateGenre', () => service.updateGenre('1', genre), 'put', [API_ENDPOINTS.GENRES.BY_ID('1'), genre], true],
  ['deleteGenre', () => service.deleteGenre('1'), 'delete', [API_ENDPOINTS.GENRES.BY_ID('1')], false],
  [
    'getMostUsedInItemListsIdsByUser',
    () => service.getMostUsedInItemListsIdsByUser('u1'),
    'get',
    [API_ENDPOINTS.GENRES.MOST_USED_IN_ITEM_LISTS_IDS_BY_USER('u1')],
    true,
  ],
];

describe('genre.service', () => {
  beforeEach(() => jest.clearAllMocks());

  it.each(cases)('%s llama al endpoint correcto y devuelve los datos', async (_n, call, method, args, returnsData) => {
    api[method].mockResolvedValue({ data: DATA } as any);
    const result = await call();
    expect(api[method]).toHaveBeenCalledTimes(1);
    expect(api[method]).toHaveBeenCalledWith(...args);
    expect(result).toEqual(returnsData ? DATA : undefined);
  });

  it.each(cases)('%s propaga los errores de la API', async (_n, call, method) => {
    const error = new Error('fallo');
    api[method].mockRejectedValue(error);
    await expect(call()).rejects.toBe(error);
  });

  it('la URL de recomendaciones apunta al recurso de recomendaciones', () => {
    expect(API_ENDPOINTS.GENRES.MOST_USED_IN_ITEM_LISTS_IDS_BY_USER('u1')).toMatch(
      /\/api\/recommendations\/genres\/most-used-in-item-lists\/by-user\/u1\/ids$/
    );
  });
});
