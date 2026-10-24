import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import * as service from '../../services/tune.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;
type Method = 'get' | 'post';

// El backend devuelve los IDs como números
const BACKEND_TUNE = { id: 1, title: 'Estándar', instrumentId: 3, notes: ['E1', 'A1', 'D2', 'G2'] };
const APP_TUNE = { id: '1', title: 'Estándar', instrumentId: '3', notes: ['E1', 'A1', 'D2', 'G2'] };

const cases: [string, () => Promise<unknown>, Method, unknown[], boolean][] = [
  ['getAll', () => service.getAll(), 'get', [API_ENDPOINTS.TUNES.BASE], true],
  ['getAllById', () => service.getAllById(['1']), 'post', [API_ENDPOINTS.TUNES.BY_IDS, ['1']], true],
  ['getByInstrumentId', () => service.getByInstrumentId('3'), 'get', [API_ENDPOINTS.TUNES.BY_INSTRUMENT('3')], true],
  ['getById', () => service.getById('1'), 'get', [API_ENDPOINTS.TUNES.BY_ID('1')], false],
];

describe('tune.service', () => {
  beforeEach(() => jest.clearAllMocks());

  it.each(cases)('%s llama al endpoint correcto y pasa los IDs a string', async (_n, call, method, args, isList) => {
    api[method].mockResolvedValue({ data: isList ? [BACKEND_TUNE] : BACKEND_TUNE } as any);
    const result = await call();
    expect(api[method]).toHaveBeenCalledWith(...args);
    expect(result).toEqual(isList ? [APP_TUNE] : APP_TUNE);
  });

  it.each(cases)('%s propaga los errores de la API', async (_n, call, method) => {
    const error = new Error('fallo');
    api[method].mockRejectedValue(error);
    await expect(call()).rejects.toBe(error);
  });

  it('toAppTuneDTO conserva el orden de las cuerdas', () => {
    expect(service.toAppTuneDTO({ ...BACKEND_TUNE, notes: ['G4', 'C4', 'E4', 'A4'] }).notes)
      .toEqual(['G4', 'C4', 'E4', 'A4']);
  });
});
