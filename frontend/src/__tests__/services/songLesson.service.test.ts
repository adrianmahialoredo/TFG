import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import * as service from '../../services/songLesson.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;
type Method = 'get' | 'post' | 'put' | 'delete';

const sl = { id: '1' } as any;
const DATA = [{ id: '1' }];
const E = API_ENDPOINTS.SONG_LESSONS;

const cases: [string, () => Promise<unknown>, Method, unknown[], boolean][] = [
  ['getAll', () => service.getAll(), 'get', [E.BASE], true],
  ['getById', () => service.getById('2'), 'get', [E.BY_ID('2')], true],
  ['getBySong', () => service.getBySong('s'), 'get', [E.BY_SONG('s')], true],
  ['getAllById', () => service.getAllById(['1']), 'post', [E.BY_IDS, ['1']], true],
  ['createSongLesson', () => service.createSongLesson(sl), 'post', [E.BASE, sl], true],
  ['updateSongLesson', () => service.updateSongLesson('1', sl), 'put', [E.BY_ID('1'), sl], true],
  ['deleteSongLesson', () => service.deleteSongLesson('1'), 'delete', [E.BY_ID('1')], false],
];

describe('songLesson.service', () => {
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
