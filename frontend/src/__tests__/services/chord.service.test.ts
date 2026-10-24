import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import * as service from '../../services/chord.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;
type Method = 'get' | 'post' | 'put' | 'delete';

const chord = { id: '1', name: 'Am' } as any;
const DATA = [{ id: '1' }];

const cases: [string, () => Promise<unknown>, Method, unknown[], boolean][] = [
  ['getAll', () => service.getAll(), 'get', [API_ENDPOINTS.CHORDS.BASE], true],
  ['getById', () => service.getById('3'), 'get', [API_ENDPOINTS.CHORDS.BY_ID('3')], true],
  ['getAllById', () => service.getAllById(['1', '2']), 'post', [API_ENDPOINTS.CHORDS.BY_IDS, ['1', '2']], true],
  ['createChord', () => service.createChord(chord), 'post', [API_ENDPOINTS.CHORDS.BASE, chord], true],
  ['updateChord', () => service.updateChord('1', chord), 'put', [API_ENDPOINTS.CHORDS.BY_ID('1'), chord], true],
  ['deleteChord', () => service.deleteChord('1'), 'delete', [API_ENDPOINTS.CHORDS.BY_ID('1')], false],
];

describe('chord.service', () => {
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

  describe('getIdsBySongLesson', () => {
    it('convierte los IDs numéricos a string', async () => {
      api.get.mockResolvedValue({ data: [1, '2', 30] } as any);
      await expect(service.getIdsBySongLesson('9')).resolves.toEqual(['1', '2', '30']);
      expect(api.get).toHaveBeenCalledWith(API_ENDPOINTS.CHORDS.IDS_BY_SONG_LESSON('9'));
    });

    it('devuelve [] si no hay acordes', async () => {
      api.get.mockResolvedValue({ data: [] } as any);
      await expect(service.getIdsBySongLesson('9')).resolves.toEqual([]);
    });

    it('propaga los errores', async () => {
      api.get.mockRejectedValue(new Error('x'));
      await expect(service.getIdsBySongLesson('9')).rejects.toThrow('x');
    });
  });
});
