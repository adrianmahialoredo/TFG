jest.mock('../../services/item.service');

const flush = async () => {
  for (let i = 0; i < 10; i++) await new Promise((r) => setImmediate(r));
};

const item = (id: string, title = `T${id}`) => ({ id, title, imageUrl: 'img', type: 'Song' }) as any;

let repo: typeof import('../../repositories/item.repository');
let service: jest.Mocked<typeof import('../../services/item.service')>;
let itemStore: typeof import('../../stores/entities/item.entities.store').useItemEntitiesStore;
let authStore: typeof import('../../stores/entities/auth.entities.store').useAuthStore;
let recentStore: typeof import('../../stores/entities/recentItems.entities.store').useRecentItemsStore;
let storage: any;

const readDisk = async (key: string) => JSON.parse((await storage.getItem(key)) ?? 'null');

beforeEach(async () => {
  jest.resetModules();
  storage = require('@react-native-async-storage/async-storage');
  repo = require('../../repositories/item.repository');
  service = require('../../services/item.service');
  itemStore = require('../../stores/entities/item.entities.store').useItemEntitiesStore;
  authStore = require('../../stores/entities/auth.entities.store').useAuthStore;
  recentStore = require('../../stores/entities/recentItems.entities.store').useRecentItemsStore;
  await storage.clear();
  service.getAllById.mockResolvedValue([]);
});

describe('item.repository', () => {
  describe('getAllById', () => {
    it('combina Disco y API y guarda lo nuevo', async () => {
      await storage.setItem('@item:1', JSON.stringify(item('1')));
      service.getAllById.mockResolvedValueOnce([item('2')]);
      await expect(repo.getAllById(['1', '2'])).resolves.toEqual([item('1'), item('2')]);
      expect(service.getAllById).toHaveBeenNthCalledWith(1, ['2']);
      expect(itemStore.getState().items['1']).toEqual(item('1'));
      await flush();
      expect(await readDisk('@item:2')).toEqual(item('2'));
    });
  });

  describe('hydrateSummaries', () => {
    it('guarda los resúmenes en RAM y Disco', async () => {
      expect(repo.hydrateSummaries([item('1'), item('2')])).toEqual(['1', '2']);
      expect(Object.keys(itemStore.getState().items)).toEqual(['1', '2']);
      await flush();
      expect(await readDisk('@item:1')).toEqual(item('1'));
    });
  });

  describe('getCustomFeed', () => {
    it('ejecuta la carga y guarda el índice', async () => {
      const load = jest.fn().mockResolvedValue(['a', 'b']);
      await expect(repo.getCustomFeed('mi_feed', load)).resolves.toEqual(['a', 'b']);
      await flush();
      expect(await readDisk('@item_index:mi_feed')).toEqual(['a', 'b']);
    });

    it('si la carga falla sin caché devuelve []', async () => {
      await expect(repo.getCustomFeed('mi_feed', () => Promise.reject(new Error('x')))).resolves.toEqual([]);
    });
  });

  describe('getCatalog', () => {
    it('sin caché carga el catálogo de la API y después lo lee del índice sin red', async () => {
      service.getAll.mockResolvedValue([item('1'), item('2')]);
      await expect(repo.getCatalog()).resolves.toEqual(['1', '2']);
      await flush();
      expect(await readDisk('@item_index:items_catalog')).toEqual(['1', '2']);

      await expect(repo.getCatalog()).resolves.toEqual(['1', '2']);
      expect(service.getAll).toHaveBeenCalledTimes(1);
    });

    it('con catálogo guardado lo devuelve al instante y refresca una sola vez por sesión', async () => {
      await storage.setItem('@item:1', JSON.stringify(item('1')));
      await storage.setItem('@item_index:items_catalog', JSON.stringify(['1']));
      service.getAll.mockResolvedValue([item('1'), item('3')]);
      const onFresh = jest.fn();

      await expect(repo.getCatalog(onFresh)).resolves.toEqual(['1']);
      await flush();
      expect(onFresh).toHaveBeenCalledWith(['1', '3']);

      await expect(repo.getCatalog()).resolves.toEqual(['1', '3']);
      await flush();
      expect(service.getAll).toHaveBeenCalledTimes(1);
    });

    it('si el refresco falla, se vuelve a intentar en la siguiente llamada', async () => {
      await storage.setItem('@item:1', JSON.stringify(item('1')));
      await storage.setItem('@item_index:items_catalog', JSON.stringify(['1']));
      service.getAll.mockRejectedValueOnce(new Error('offline')).mockResolvedValue([item('1')]);

      await repo.getCatalog();
      await flush();
      await repo.getCatalog();
      await flush();
      expect(service.getAll).toHaveBeenCalledTimes(2);
    });

    it('sincronizado pero con catálogo vacío vuelve a la API', async () => {
      service.getAll.mockResolvedValue([]);
      await expect(repo.getCatalog()).resolves.toEqual([]);
      await flush();
      await expect(repo.getCatalog()).resolves.toEqual([]);
      expect(service.getAll).toHaveBeenCalledTimes(2);
    });

    it('sin caché ni red devuelve []', async () => {
      service.getAll.mockRejectedValue(new Error('offline'));
      await expect(repo.getCatalog()).resolves.toEqual([]);
    });
  });

  describe('getFeed', () => {
    it('convierte los IDs a string y pide solo los items que faltan', async () => {
      itemStore.getState().setItems([item('1')]);
      service.getAllById.mockResolvedValueOnce([item('2')]);
      await expect(repo.getFeed('popular', async () => [1, 2])).resolves.toEqual(['1', '2']);
      expect(service.getAllById).toHaveBeenCalledWith(['2']);
      expect(itemStore.getState().items['2']).toEqual(item('2'));
    });

    it('no llama a by-ids si todos están en caché', async () => {
      itemStore.getState().setItems([item('1')]);
      await repo.getFeed('popular', async () => ['1']);
      expect(service.getAllById).not.toHaveBeenCalled();
    });
  });

  describe('markAsAccessed', () => {
    it('añade el item a los recientes del usuario de la sesión', () => {
      authStore.setState({ currentUserId: 'u1' });
      repo.markAsAccessed('5');
      repo.markAsAccessed('6');
      expect(recentStore.getState().byUser.u1).toEqual(['6', '5']);
    });

    it('sin sesión no hace nada', () => {
      authStore.setState({ currentUserId: null });
      repo.markAsAccessed('5');
      expect(recentStore.getState().byUser).toEqual({});
    });

    it('con un id vacío no hace nada', () => {
      authStore.setState({ currentUserId: 'u1' });
      repo.markAsAccessed('');
      expect(recentStore.getState().byUser).toEqual({});
    });
  });
});
