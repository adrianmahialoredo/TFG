jest.mock('../../services/user.service');
jest.mock('../../services/cloudinary.service');

const flush = async () => {
  for (let i = 0; i < 10; i++) await new Promise((r) => setImmediate(r));
};

const user = (id: string, extra: Record<string, unknown> = {}) =>
  ({ id, name: `U${id}`, email: `${id}@x.com`, favouriteGenresIds: ['g1'], favouriteInstrumentsIds: ['i1'], ...extra }) as any;

let repo: typeof import('../../repositories/user.repository');
let service: jest.Mocked<typeof import('../../services/user.service')>;
let cloudinary: jest.Mocked<typeof import('../../services/cloudinary.service')>;
let store: typeof import('../../stores/entities/user.entities.store').useUserEntitiesStore;
let storage: any;

const readDisk = async (key: string) => JSON.parse((await storage.getItem(key)) ?? 'null');

beforeEach(async () => {
  jest.resetModules();
  storage = require('@react-native-async-storage/async-storage');
  repo = require('../../repositories/user.repository');
  service = require('../../services/user.service');
  cloudinary = require('../../services/cloudinary.service');
  store = require('../../stores/entities/user.entities.store').useUserEntitiesStore;
  await storage.clear();
  service.getAllById.mockResolvedValue([]);
});

describe('user.repository', () => {
  describe('getById', () => {
    it('devuelve el usuario de RAM y lo revalida', async () => {
      store.getState().setUsers([user('1', { name: 'Viejo' })]);
      service.getAllById.mockResolvedValue([user('1', { name: 'Nuevo' })]);
      await expect(repo.getById('1')).resolves.toMatchObject({ name: 'Viejo' });
      expect(service.getById).not.toHaveBeenCalled();
      await flush();
      expect(store.getState().users['1'].name).toBe('Nuevo');
      expect((await readDisk('@user:1')).name).toBe('Nuevo');
    });

    it('lee de Disco y lo sube a RAM', async () => {
      await storage.setItem('@user:2', JSON.stringify(user('2')));
      await expect(repo.getById('2')).resolves.toEqual(user('2'));
      expect(store.getState().users['2']).toEqual(user('2'));
      await flush();
      expect(service.getAllById).toHaveBeenCalledWith(['2']);
    });

    it('pide a la API si no hay caché y lo guarda en RAM y Disco', async () => {
      service.getById.mockResolvedValue(user('3'));
      await expect(repo.getById('3')).resolves.toEqual(user('3'));
      expect(store.getState().users['3']).toEqual(user('3'));
      expect(await readDisk('@user:3')).toEqual(user('3'));
    });

    it('devuelve null si la API falla', async () => {
      service.getById.mockRejectedValue(new Error('offline'));
      await expect(repo.getById('4')).resolves.toBeNull();
    });

    it('devuelve null si la API no devuelve nada', async () => {
      service.getById.mockResolvedValue(null as any);
      await expect(repo.getById('4')).resolves.toBeNull();
      expect(store.getState().users).toEqual({});
    });
  });

  describe('getAllById', () => {
    it('pide a la API los usuarios que faltan', async () => {
      service.getAllById.mockResolvedValueOnce([user('5')]);
      await expect(repo.getAllById(['5'])).resolves.toEqual([user('5')]);
    });
  });

  describe('refresh', () => {
    it('pide el usuario saltándose la caché y lo hidrata', async () => {
      store.getState().setUsers([user('1', { spotifyConnected: false })]);
      service.getById.mockResolvedValue(user('1', { spotifyConnected: true }));
      await expect(repo.refresh('1')).resolves.toMatchObject({ spotifyConnected: true });
      expect(store.getState().users['1'].spotifyConnected).toBe(true);
      expect((await readDisk('@user:1')).spotifyConnected).toBe(true);
    });

    it('propaga los errores', async () => {
      service.getById.mockRejectedValue(new Error('offline'));
      await expect(repo.refresh('1')).rejects.toThrow('offline');
    });
  });

  describe('updateProfile', () => {
    it('devuelve null si el usuario no está en RAM', async () => {
      await expect(repo.updateProfile('1', { name: 'x' })).resolves.toBeNull();
      expect(service.updateUserProfile).not.toHaveBeenCalled();
    });

    it('aplica el cambio al instante y guarda la respuesta de la API', async () => {
      store.getState().setUsers([user('1')]);
      let resolveApi!: (v: any) => void;
      service.updateUserProfile.mockReturnValue(new Promise((r) => (resolveApi = r)));

      const pending = repo.updateProfile('1', { name: 'Optimista' });
      expect(store.getState().users['1'].name).toBe('Optimista');
      expect(service.updateUserProfile).toHaveBeenCalledWith({ ...user('1'), name: 'Optimista' });

      resolveApi(user('1', { name: 'Servidor' }));
      await expect(pending).resolves.toMatchObject({ name: 'Servidor' });
      expect(store.getState().users['1'].name).toBe('Servidor');
      expect((await readDisk('@user:1')).name).toBe('Servidor');
    });

    it('si la API falla deshace el cambio y propaga el error', async () => {
      store.getState().setUsers([user('1')]);
      service.updateUserProfile.mockRejectedValue(new Error('500'));
      await expect(repo.updateProfile('1', { name: 'x' })).rejects.toThrow('500');
      expect(store.getState().users['1']).toEqual(user('1'));
    });

    it('una revalidación en vuelo no pisa la modificación', async () => {
      store.getState().setUsers([user('1')]);
      let resolveRevalidation!: (v: any) => void;
      service.getAllById.mockReturnValue(new Promise((r) => (resolveRevalidation = r)));
      await repo.getById('1'); // lanza la revalidación

      service.updateUserProfile.mockResolvedValue(user('1', { name: 'Editado' }));
      await repo.updateProfile('1', { name: 'Editado' });
      resolveRevalidation([user('1', { name: 'Viejo del servidor' })]);
      await flush();
      expect(store.getState().users['1'].name).toBe('Editado');
    });
  });

  describe('updateFavorites', () => {
    it('devuelve null si el usuario no está en RAM', async () => {
      await expect(repo.updateFavorites('1', 'genres', ['g2'])).resolves.toBeNull();
    });

    it('sin cambios devuelve el usuario sin llamar a la API', async () => {
      store.getState().setUsers([user('1')]);
      await expect(repo.updateFavorites('1', 'genres', ['g1'])).resolves.toEqual(user('1'));
      expect(service.addFavoriteGenre).not.toHaveBeenCalled();
      expect(service.removeFavoriteGenre).not.toHaveBeenCalled();
    });

    it('añade y quita géneros en serie y guarda la última respuesta', async () => {
      store.getState().setUsers([user('1', { favouriteGenresIds: [1, 'g2'] })]);
      service.addFavoriteGenre.mockImplementation(async (_u, g) => user('1', { favouriteGenresIds: ['tras-add-' + g] }));
      service.removeFavoriteGenre.mockResolvedValue(user('1', { favouriteGenresIds: ['g3', 'g4'] }));

      let pending = repo.updateFavorites('1', 'genres', ['g3', 'g4']);
      // Cambio optimista inmediato
      expect(store.getState().users['1'].favouriteGenresIds).toEqual(['g3', 'g4']);
      const result = await pending;

      expect(service.addFavoriteGenre.mock.calls).toEqual([
        ['1', 'g3'],
        ['1', 'g4'],
      ]);
      // Los IDs actuales se comparan como string (1 → '1')
      expect(service.removeFavoriteGenre.mock.calls).toEqual([
        ['1', '1'],
        ['1', 'g2'],
      ]);
      expect(result?.favouriteGenresIds).toEqual(['g3', 'g4']);
      expect(store.getState().users['1'].favouriteGenresIds).toEqual(['g3', 'g4']);
      expect((await readDisk('@user:1')).favouriteGenresIds).toEqual(['g3', 'g4']);
    });

    it('usa los endpoints de instrumentos para kind "instruments"', async () => {
      store.getState().setUsers([user('1', { favouriteInstrumentsIds: undefined })]);
      service.addFavoriteInstrument.mockResolvedValue(user('1', { favouriteInstrumentsIds: ['i9'] }));
      await expect(repo.updateFavorites('1', 'instruments', ['i9'])).resolves.toMatchObject({ favouriteInstrumentsIds: ['i9'] });
      expect(service.addFavoriteInstrument).toHaveBeenCalledWith('1', 'i9');
      expect(service.removeFavoriteInstrument).not.toHaveBeenCalled();
      expect(service.addFavoriteGenre).not.toHaveBeenCalled();
    });

    it('si falla a mitad deshace, recarga el estado real del backend y propaga el error', async () => {
      store.getState().setUsers([user('1')]);
      service.addFavoriteGenre.mockRejectedValue(new Error('500'));
      let resolveReload!: (v: any) => void;
      service.getById.mockReturnValue(new Promise((r) => (resolveReload = r)));

      await expect(repo.updateFavorites('1', 'genres', ['g2'])).rejects.toThrow('500');
      expect(store.getState().users['1']).toEqual(user('1'));
      resolveReload(user('1', { favouriteGenresIds: ['real'] }));
      await flush();
      expect(service.getById).toHaveBeenCalledWith('1');
      expect(store.getState().users['1'].favouriteGenresIds).toEqual(['real']);
    });

    it('si también falla la recarga, mantiene la copia de seguridad', async () => {
      store.getState().setUsers([user('1')]);
      service.removeFavoriteGenre.mockRejectedValue(new Error('500'));
      service.getById.mockRejectedValue(new Error('offline'));
      await expect(repo.updateFavorites('1', 'genres', [])).rejects.toThrow('500');
      await flush();
      expect(store.getState().users['1']).toEqual(user('1'));
    });
  });

  describe('updateProfileImage', () => {
    it('sube la imagen y guarda la URL en el perfil', async () => {
      store.getState().setUsers([user('1')]);
      cloudinary.uploadProfileImageToCloudinary.mockResolvedValue('https://img');
      service.updateUserProfile.mockImplementation(async (u) => u);
      await expect(repo.updateProfileImage('1', 'file://a.jpg')).resolves.toMatchObject({ profileImageUrl: 'https://img' });
      expect(cloudinary.uploadProfileImageToCloudinary).toHaveBeenCalledWith('file://a.jpg');
    });

    it('propaga el error de la subida sin tocar el perfil', async () => {
      store.getState().setUsers([user('1')]);
      cloudinary.uploadProfileImageToCloudinary.mockRejectedValue(new Error('cloudinary'));
      await expect(repo.updateProfileImage('1', 'x')).rejects.toThrow('cloudinary');
      expect(service.updateUserProfile).not.toHaveBeenCalled();
    });
  });
});
