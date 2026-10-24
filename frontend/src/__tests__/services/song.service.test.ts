import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import * as service from '../../services/song.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;
type Method = 'get' | 'post' | 'put' | 'delete';

const song = { id: '1', title: 'Song' } as any;
const DATA = [{ id: '1' }];
const E = API_ENDPOINTS.SONGS;

const cases: [string, () => Promise<unknown>, Method, unknown[], boolean][] = [
  ['getAll', () => service.getAll(), 'get', [E.BASE], true],
  ['getById', () => service.getById('2'), 'get', [E.BY_ID('2')], true],
  ['getAllById', () => service.getAllById(['1', '2']), 'post', [E.BY_IDS, ['1', '2']], true],
  ['getByDecade', () => service.getByDecade('80s'), 'get', [E.BY_DECADE('80s')], true],
  ['createSong', () => service.createSong(song), 'post', [E.BASE, song], true],
  ['updateSong', () => service.updateSong('1', song), 'put', [E.BY_ID('1'), song], true],
  ['deleteSong', () => service.deleteSong('1'), 'delete', [E.BY_ID('1')], false],
  ['getPopular sin límite', () => service.getPopular(), 'get', [E.POPULAR], true],
  ['getPopular con límite', () => service.getPopular(5), 'get', [`${E.POPULAR}?limit=5`], true],
  ['getNew sin límite', () => service.getNew(), 'get', [E.NEW], true],
  ['getNew con límite', () => service.getNew(3), 'get', [`${E.NEW}?limit=3`], true],
  ['getNew con límite 0 (se ignora)', () => service.getNew(0), 'get', [E.NEW], true],
  [
    'getRecommendedFromSpotify',
    () => service.getRecommendedFromSpotify('u', 10),
    'get',
    [`${E.SPOTIFY('u')}?limit=10`],
    true,
  ],
  ['getPopularByGenreId', () => service.getPopularByGenreId('g'), 'get', [E.POPULAR_BY_GENRE('g')], true],
  ['getClassicByGenreId', () => service.getClassicByGenreId('g'), 'get', [E.CLASSIC_BY_GENRE('g')], true],
  ['getNewByGenreId', () => service.getNewByGenreId('g'), 'get', [E.NEW_BY_GENRE('g')], true],
  [
    'getRecommendedByUser',
    () => service.getRecommendedByUser('u', 10),
    'get',
    [`${E.RECOMMENDED_BY_USER('u')}?limit=10`],
    true,
  ],
];

describe('song.service', () => {
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

describe('song.service: canciones por artistas', () => {
  beforeEach(() => jest.clearAllMocks());

  it('toArtistParams numera los artistas en orden y añade el límite', () => {
    expect(service.toArtistParams(['Queen', 'Bowie'], 12)).toEqual({ limit: 12, artist1: 'Queen', artist2: 'Bowie' });
  });

  it.each([
    ['getNewByArtists', service.getNewByArtists, E.NEW_BY_ARTISTS],
    ['getPopularByArtists', service.getPopularByArtists, E.POPULAR_BY_ARTISTS],
  ])('%s pide su endpoint con los artistas numerados', async (_n, call, url) => {
    api.get.mockResolvedValue({ data: DATA } as any);
    await expect(call(['Queen', 'Bowie'], 12)).resolves.toEqual(DATA);
    expect(api.get).toHaveBeenCalledWith(url, { params: { limit: 12, artist1: 'Queen', artist2: 'Bowie' } });
  });
});
