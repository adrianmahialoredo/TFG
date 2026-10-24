jest.mock('../../services/spotify.service');
jest.mock('../../services/user.service');

let repo: typeof import('../../repositories/spotify.repository');
let service: jest.Mocked<typeof import('../../services/spotify.service')>;
let userService: jest.Mocked<typeof import('../../services/user.service')>;
let userStore: typeof import('../../stores/entities/user.entities.store').useUserEntitiesStore;

beforeEach(() => {
  jest.resetModules();
  repo = require('../../repositories/spotify.repository');
  service = require('../../services/spotify.service');
  userService = require('../../services/user.service');
  userStore = require('../../stores/entities/user.entities.store').useUserEntitiesStore;
});

describe('spotify.repository', () => {
  describe('getAuthUrl', () => {
    it('devuelve la URL de autorización', async () => {
      service.spotifyLogin.mockResolvedValue({ authUrl: 'https://accounts.spotify.com/auth' });
      await expect(repo.getAuthUrl()).resolves.toBe('https://accounts.spotify.com/auth');
    });

    it('lanza un error si el backend no devuelve URL', async () => {
      service.spotifyLogin.mockResolvedValue({ authUrl: '' });
      await expect(repo.getAuthUrl()).rejects.toThrow('Spotify auth URL no disponible');
    });

    it('propaga los errores del service', async () => {
      service.spotifyLogin.mockRejectedValue(new Error('401'));
      await expect(repo.getAuthUrl()).rejects.toThrow('401');
    });
  });

  describe('refreshLinkedUser', () => {
    it('recarga el usuario desde la API y lo guarda en la Store', async () => {
      userService.getById.mockResolvedValue({ id: 'u1', spotifyUserId: 'sp' } as any);
      await expect(repo.refreshLinkedUser('u1')).resolves.toMatchObject({ spotifyUserId: 'sp' });
      expect(userService.getById).toHaveBeenCalledWith('u1');
      expect(userStore.getState().users.u1).toMatchObject({ spotifyUserId: 'sp' });
    });
  });
});
