import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import * as service from '../../services/instrument.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;
type Method = 'get' | 'post' | 'put' | 'delete';

const instrument = { id: '1', name: 'Guitarra' } as any;
const DATA = [{ id: '1' }];

const cases: [string, () => Promise<unknown>, Method, unknown[], boolean][] = [
  ['getAllInstruments', () => service.getAllInstruments(), 'get', [API_ENDPOINTS.INSTRUMENTS.BASE], true],
  ['getInstrumentById', () => service.getInstrumentById('4'), 'get', [API_ENDPOINTS.INSTRUMENTS.BY_ID('4')], true],
  ['getAllById', () => service.getAllById(['1']), 'post', [API_ENDPOINTS.INSTRUMENTS.BY_IDS, ['1']], true],
  ['getByGenreId', () => service.getByGenreId('g1'), 'get', [API_ENDPOINTS.INSTRUMENTS.BY_GENRE('g1')], true],
  ['createInstrument', () => service.createInstrument(instrument), 'post', [API_ENDPOINTS.INSTRUMENTS.BASE, instrument], true],
  [
    'updateInstrument',
    () => service.updateInstrument('1', instrument),
    'put',
    [API_ENDPOINTS.INSTRUMENTS.BY_ID('1'), instrument],
    true,
  ],
  ['deleteInstrument', () => service.deleteInstrument('1'), 'delete', [API_ENDPOINTS.INSTRUMENTS.BY_ID('1')], false],
];

describe('instrument.service', () => {
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
