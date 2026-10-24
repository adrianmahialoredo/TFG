import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import * as service from '../../services/user.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;
type Method = 'get' | 'post' | 'put' | 'delete';

const user = { id: 'u1', name: 'Ana' } as any;
const DATA = { id: 'u1' };
const E = API_ENDPOINTS.USERS;

const cases: [string, () => Promise<unknown>, Method, unknown[]][] = [
  ['getById', () => service.getById('u1'), 'get', [E.BY_ID('u1')]],
  ['getAllById', () => service.getAllById(['u1', 'u2']), 'post', [E.BY_IDS, ['u1', 'u2']]],
  ['updateUserProfile', () => service.updateUserProfile(user), 'put', [E.BY_ID('u1'), user]],
  ['addFavoriteGenre', () => service.addFavoriteGenre('u1', 'g1'), 'post', [E.FAVORITE_GENRE('u1', 'g1')]],
  ['removeFavoriteGenre', () => service.removeFavoriteGenre('u1', 'g1'), 'delete', [E.FAVORITE_GENRE('u1', 'g1')]],
  [
    'addFavoriteInstrument',
    () => service.addFavoriteInstrument('u1', 'i1'),
    'post',
    [E.FAVORITE_INSTRUMENT('u1', 'i1')],
  ],
  [
    'removeFavoriteInstrument',
    () => service.removeFavoriteInstrument('u1', 'i1'),
    'delete',
    [E.FAVORITE_INSTRUMENT('u1', 'i1')],
  ],
];

describe('user.service', () => {
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

  it('getAllById con lista vacía devuelve [] sin llamar a la API', async () => {
    await expect(service.getAllById([])).resolves.toEqual([]);
    expect(api.post).not.toHaveBeenCalled();
  });
});

describe('user.service: artistas favoritos', () => {
  beforeEach(() => jest.clearAllMocks());

  it('getTopArtists pide el número de artistas con total_artists', async () => {
    api.get.mockResolvedValue({ data: ['Queen', 'Bowie'] } as any);
    await expect(service.getTopArtists('u1', 5)).resolves.toEqual(['Queen', 'Bowie']);
    expect(api.get).toHaveBeenCalledWith(E.TOP_ARTISTS('u1'), { params: { total_artists: 5 } });
  });
});
