import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import * as service from '../../services/instrumentGenre.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;
type Method = 'get' | 'post' | 'put' | 'delete';

const ig = { id: '1', title: 'Rock con guitarra' } as any;
const DATA = [{ id: '1' }];
const E = API_ENDPOINTS.INSTRUMENT_GENRES;

const cases: [string, () => Promise<unknown>, Method, unknown[], boolean][] = [
  ['getAllInstrumentGenres', () => service.getAllInstrumentGenres(), 'get', [E.BASE], true],
  ['getInstrumentGenreById', () => service.getInstrumentGenreById('5'), 'get', [E.BY_ID('5')], true],
  ['getAllById', () => service.getAllById(['1', '2']), 'post', [E.BY_IDS, ['1', '2']], true],
  ['getByGenreId', () => service.getByGenreId('g'), 'get', [E.BY_GENRE('g')], true],
  ['getByInstrumentId', () => service.getByInstrumentId('i'), 'get', [E.BY_INSTRUMENT('i')], true],
  [
    'getFavouriteCombinationIdsByUser',
    () => service.getFavouriteCombinationIdsByUser('u'),
    'get',
    [E.FAVOURITE_COMBINATION_IDS_BY_USER('u')],
    true,
  ],
  ['createInstrumentGenre', () => service.createInstrumentGenre(ig), 'post', [E.BASE, ig], true],
  ['updateInstrumentGenre', () => service.updateInstrumentGenre('1', ig), 'put', [E.BY_ID('1'), ig], true],
  ['deleteInstrumentGenre', () => service.deleteInstrumentGenre('1'), 'delete', [E.BY_ID('1')], false],
];

describe('instrumentGenre.service', () => {
  beforeEach(() => jest.clearAllMocks());

  it.each(cases)('%s llama al endpoint correcto y devuelve los datos', async (_n, call, method, args, returnsData) => {
    api[method].mockResolvedValue({ data: DATA } as any);
    const result = await call();
    expect(api[method]).toHaveBeenCalledWith(...args);
    expect(result).toEqual(returnsData ? DATA : undefined);
  });

  it.each(cases)('%s propaga los errores de la API', async (_n, call, method) => {
    const error = new Error('fallo');
    api[method].mockRejectedValue(error);
    await expect(call()).rejects.toBe(error);
  });
});
