jest.mock('../../services/auth.service');

const user = { id: 7, email: 'a@b.c', username: 'ana', name: 'Ana' } as any;

let repo: typeof import('../../repositories/auth.repository');
let service: jest.Mocked<typeof import('../../services/auth.service')>;
let stores: typeof import('../../stores/entities');
let storage: any;

const readDisk = async (key: string) => JSON.parse((await storage.getItem(key)) ?? 'null');

beforeEach(async () => {
  jest.resetModules();
  storage = require('@react-native-async-storage/async-storage');
  repo = require('../../repositories/auth.repository');
  service = require('../../services/auth.service');
  stores = require('../../stores/entities');
  await storage.clear();
});

describe('auth.repository', () => {
  describe('login', () => {
    it('guarda el usuario en RAM y Disco y abre la sesión solo con su ID', async () => {
      service.login.mockResolvedValue({ token: 'tok', user });
      await expect(repo.login('a@b.c', 'pw')).resolves.toEqual({ token: 'tok', user });
      expect(service.login).toHaveBeenCalledWith('a@b.c', 'pw');
      expect(stores.useUserEntitiesStore.getState().users['7']).toEqual(user);
      expect(await readDisk('@user:7')).toEqual(user);
      expect(stores.useAuthStore.getState()).toMatchObject({ token: 'tok', currentUserId: '7', isAuthenticated: true });
    });

    it('abre la sesión aunque falle el guardado en Disco', async () => {
      service.login.mockResolvedValue({ token: 'tok', user });
      storage.multiSet.mockRejectedValueOnce(new Error('disco lleno'));
      await repo.login('a', 'b');
      expect(stores.useAuthStore.getState().token).toBe('tok');
    });

    it('propaga el error del backend sin abrir sesión', async () => {
      const error = new Error('Email o contrasena incorrectos');
      service.login.mockRejectedValue(error);
      await expect(repo.login('a', 'b')).rejects.toBe(error);
      expect(stores.useAuthStore.getState().isAuthenticated).toBe(false);
      expect(console.warn).toHaveBeenCalled();
    });
  });

  describe('requestVerificationCode', () => {
    it('delega en el service', async () => {
      service.requestVerificationCode.mockResolvedValue(undefined);
      await repo.requestVerificationCode('a@b.c');
      expect(service.requestVerificationCode).toHaveBeenCalledWith('a@b.c');
    });

    it('propaga los errores', async () => {
      service.requestVerificationCode.mockRejectedValue(new Error('429'));
      await expect(repo.requestVerificationCode('a')).rejects.toThrow('429');
    });
  });

  describe('register', () => {
    it('quita confirmPassword, guarda el usuario y abre sesión si hay token', async () => {
      service.register.mockResolvedValue({ token: 'tok', user });
      const form = { email: 'a@b.c', password: 'P', confirmPassword: 'P', name: 'Ana' };
      await expect(repo.register(form, '123')).resolves.toEqual({ token: 'tok', user });
      expect(service.register).toHaveBeenCalledWith({ email: 'a@b.c', password: 'P', name: 'Ana' }, '123');
      expect(stores.useUserEntitiesStore.getState().users['7']).toEqual(user);
      expect(await readDisk('@user:7')).toEqual(user);
      expect(stores.useAuthStore.getState()).toMatchObject({ token: 'tok', currentUserId: '7' });
    });

    it('sin token no abre sesión y devuelve token vacío', async () => {
      service.register.mockResolvedValue({ token: '', user });
      await expect(repo.register({ email: 'a' }, '1')).resolves.toEqual({ token: '', user });
      expect(stores.useAuthStore.getState().isAuthenticated).toBe(false);
      expect(stores.useUserEntitiesStore.getState().users['7']).toEqual(user);
    });

    it('propaga los errores del service', async () => {
      service.register.mockRejectedValue(new Error('El código no es correcto'));
      await expect(repo.register({}, '1')).rejects.toThrow('El código no es correcto');
      expect(stores.useUserEntitiesStore.getState().users).toEqual({});
    });
  });

  describe('logout', () => {
    it('limpia la sesión y los datos privados de RAM y Disco, conservando el contenido público', async () => {
      stores.useAuthStore.setState({ token: 't', currentUserId: '7', isAuthenticated: true });
      stores.useUserEntitiesStore.getState().setUsers([user]);
      stores.useItemListEntitiesStore.getState().setItemLists([{ id: '1', userId: '7', name: 'L' } as any]);
      stores.useSongEntitiesStore.getState().setSongs([{ id: 's1', title: 'S' } as any]);

      await storage.multiSet([
        ['@user:7', '{}'],
        ['@user_index:x', '[]'],
        ['@itemList:1', '{}'],
        ['@itemList_index:x', '[]'],
        ['@itemList_user:7', '["1"]'],
        ['@item:1', '{}'],
        ['@item_index:items_catalog', '["1"]'],
        ['@item_index:items_recommended_user', '["1"]'],
        ['@song:s1', '{}'],
        ['@song_index:spotify', '[]'],
        ['@song_index:recommended', '[]'],
        ['@song_index:new_favourite_artists', '[]'],
        ['@song_index:popular_favourite_artists', '[]'],
        ['@song_index:popular', '[]'],
        ['@lesson_index:recommended', '[]'],
        ['@lesson_index:genre_g1', '[]'],
        ['@genre:1', '{}'],
      ]);

      await repo.logout();

      expect(stores.useAuthStore.getState()).toMatchObject({ token: null, currentUserId: null, isAuthenticated: false });
      expect(stores.useUserEntitiesStore.getState().users).toEqual({});
      expect(stores.useItemListEntitiesStore.getState().itemLists).toEqual({});
      // El contenido público sigue en RAM
      expect(stores.useSongEntitiesStore.getState().songs.s1).toBeDefined();

      // Las claves de las Stores persistidas (auth-store…) no son de los módulos local
      const keys = (await storage.getAllKeys()).filter((k: string) => k.startsWith('@')).sort();
      expect(keys).toEqual(
        [
          '@genre:1',
          '@item:1',
          '@item_index:items_catalog',
          '@lesson_index:genre_g1',
          '@song:s1',
          '@song_index:popular',
        ].sort()
      );
    });

    it('no propaga los errores', async () => {
      stores.useAuthStore.setState({
        clearAuth: () => {
          throw new Error('boom');
        },
      });
      await expect(repo.logout()).resolves.toBeUndefined();
      expect(console.error).toHaveBeenCalled();
    });
  });
});
