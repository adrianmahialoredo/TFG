import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import * as service from '../../services/lesson.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;
type Method = 'get' | 'post' | 'put' | 'delete';

const lesson = { id: '1', title: 'Escalas' } as any;
const DATA = [{ id: '1' }];
const E = API_ENDPOINTS.LESSONS;

const cases: [string, () => Promise<unknown>, Method, unknown[], boolean][] = [
  ['getAll', () => service.getAll(), 'get', [E.BASE], true],
  ['getById', () => service.getById('2'), 'get', [E.BY_ID('2')], true],
  ['getAllById', () => service.getAllById(['1']), 'post', [E.BY_IDS, ['1']], true],
  ['getByGenreId', () => service.getByGenreId('g'), 'get', [E.BY_GENRE('g')], true],
  ['getRecommendedByGenreId', () => service.getRecommendedByGenreId('g'), 'get', [E.RECOMMENDED_BY_GENRE('g')], true],
  ['createLesson', () => service.createLesson(lesson), 'post', [E.BASE, lesson], true],
  ['updateLesson', () => service.updateLesson('1', lesson), 'put', [E.BY_ID('1'), lesson], true],
  ['deleteLesson', () => service.deleteLesson('1'), 'delete', [E.BY_ID('1')], false],
  ['getPopularIds', () => service.getPopularIds(), 'get', [E.POPULAR_IDS], true],
  ['getNewIds', () => service.getNewIds(), 'get', [E.NEW_IDS], true],
  [
    'getPopularByInstrumentIds',
    () => service.getPopularByInstrumentIds('i'),
    'get',
    [E.POPULAR_BY_INSTRUMENT_IDS('i')],
    true,
  ],
  ['getRecommendedByUser', () => service.getRecommendedByUser('u'), 'get', [E.RECOMMENDED_BY_USER('u')], true],
];

describe('lesson.service', () => {
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
