import { apiClient } from '../../services/apiClient';
import { API_ENDPOINTS } from '../../config/apiConfig';
import { spotifyLogin, getSpotifyRecommendations } from '../../services/spotify.service';

jest.mock('../../services/apiClient', () => ({
  apiClient: { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() },
}));

const api = apiClient as jest.Mocked<typeof apiClient>;

describe('spotify.service', () => {
  beforeEach(() => jest.clearAllMocks());

  it('spotifyLogin pide la URL de autorización', async () => {
    api.get.mockResolvedValue({ data: { authUrl: 'https://accounts.spotify.com/x' } } as any);
    await expect(spotifyLogin()).resolves.toEqual({ authUrl: 'https://accounts.spotify.com/x' });
    expect(api.get).toHaveBeenCalledWith(API_ENDPOINTS.SPOTIFY.LOGIN);
  });

  it('getSpotifyRecommendations llama al endpoint del usuario de Spotify', async () => {
    api.get.mockResolvedValue({ data: [{ id: 's1' }] } as any);
    await expect(getSpotifyRecommendations('sp1')).resolves.toEqual([{ id: 's1' }]);
    expect(api.get).toHaveBeenCalledWith(API_ENDPOINTS.SPOTIFY.RECOMMENDATIONS('sp1'));
  });

  it('propaga los errores de la API', async () => {
    api.get.mockRejectedValue(new Error('401'));
    await expect(spotifyLogin()).rejects.toThrow('401');
    await expect(getSpotifyRecommendations('x')).rejects.toThrow('401');
  });
});
