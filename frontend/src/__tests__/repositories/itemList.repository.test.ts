jest.mock('../../services/itemList.service');
jest.mock('../../services/item.service');
jest.mock('../../services/cloudinary.service');

const flush = async () => {
  for (let i = 0; i < 10; i++) await new Promise((r) => setImmediate(r));
};

const list = (id: string, extra: Record<string, unknown> = {}) =>
  ({ id, userId: 'u1', name: `L${id}`, itemIds: ['a', 'b'], ...extra }) as any;

let repo: typeof import('../../repositories/itemList.repository');
let service: jest.Mocked<typeof import('../../services/itemList.service')>;
let itemService: jest.Mocked<typeof import('../../services/item.service')>;
let cloudinary: jest.Mocked<typeof import('../../services/cloudinary.service')>;
let store: typeof import('../../stores/entities/itemList.entities.store').useItemListEntitiesStore;
let storage: any;

const readDisk = async (key: string) => JSON.parse((await storage.getItem(key)) ?? 'null');
const seedDisk = async (lists: any[], userId = 'u1') => {
  await storage.multiSet(lists.map((l) => [`@itemList:${l.id}`, JSON.stringify(l)]));
  await storage.setItem(`@itemList_user:${userId}`, JSON.stringify(lists.map((l) => l.id)));
};

beforeEach(async () => {
  jest.resetModules();
  storage = require('@react-native-async-storage/async-storage');
  repo = require('../../repositories/itemList.repository');
  service = require('../../services/itemList.service');
  itemService = require('../../services/item.service');
  cloudinary = require('../../services/cloudinary.service');
  store = require('../../stores/entities/itemList.entities.store').useItemListEntitiesStore;
  await storage.clear();
  service.getAllById.mockResolvedValue([]);
  itemService.getAllById.mockResolvedValue([]);
});

describe('itemList.repository', () => {
  describe('getById', () => {
    it('devuelve la lista de RAM y la revalida', async () => {
      store.getState().setItemLists([list('1', { name: 'Vieja' })]);
      service.getAllById.mockResolvedValue([list('1', { name: 'Nueva' })]);
      await expect(repo.getById('1')).resolves.toMatchObject({ name: 'Vieja' });
      expect(service.getById).not.toHaveBeenCalled();
      await flush();
      expect(service.getAllById).toHaveBeenCalledWith(['1']);
      expect(store.getState().itemLists['1'].name).toBe('Nueva');
    });

    it('lee de Disco y la sube a RAM', async () => {
      await seedDisk([list('2')]);
      await expect(repo.getById('2')).resolves.toEqual(list('2'));
      expect(store.getState().itemLists['2']).toEqual(list('2'));
      await flush();
      expect(service.getAllById).toHaveBeenCalledWith(['2']);
    });

    it('pide a la API si no hay caché y la guarda con el índice del usuario', async () => {
      service.getById.mockResolvedValue(list('3'));
      await expect(repo.getById('3')).resolves.toEqual(list('3'));
      expect(store.getState().itemLists['3']).toEqual(list('3'));
      expect(await readDisk('@itemList:3')).toEqual(list('3'));
      expect(await readDisk('@itemList_user:u1')).toEqual(['3']);
    });

    it('devuelve null si la API falla', async () => {
      service.getById.mockRejectedValue(new Error('404'));
      await expect(repo.getById('4')).resolves.toBeNull();
    });

    it('devuelve null si la API no devuelve nada', async () => {
      service.getById.mockResolvedValue(null as any);
      await expect(repo.getById('4')).resolves.toBeNull();
      expect(store.getState().itemLists).toEqual({});
    });
  });

  describe('getAllById', () => {
    it('pide a la API las listas que faltan', async () => {
      service.getAllById.mockResolvedValueOnce([list('5')]);
      await expect(repo.getAllById(['5'])).resolves.toEqual([list('5')]);
    });
  });

  describe('getItemListWithItems', () => {
    it('carga la lista y sus items y devuelve los IDs en orden', async () => {
      store.getState().setItemLists([list('1', { itemIds: ['b', 'a'] })]);
      itemService.getAllById.mockResolvedValueOnce([
        { id: 'a', title: 'A', type: 'Song' },
        { id: 'b', title: 'B', type: 'Song' },
      ] as any);
      await expect(repo.getItemListWithItems('1')).resolves.toEqual(['b', 'a']);
      expect(itemService.getAllById).toHaveBeenCalledWith(['b', 'a']);
    });

    it('si la lista no existe devuelve []', async () => {
      service.getById.mockRejectedValue(new Error('404'));
      await expect(repo.getItemListWithItems('x')).resolves.toEqual([]);
    });

    it('una lista sin itemIds devuelve []', async () => {
      store.getState().setItemLists([list('1', { itemIds: undefined })]);
      await expect(repo.getItemListWithItems('1')).resolves.toEqual([]);
    });

    it('ante un error inesperado devuelve []', async () => {
      storage.getItem.mockRejectedValueOnce(new Error('disco roto'));
      await expect(repo.getItemListWithItems('1')).resolves.toEqual([]);
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('getByUserId', () => {
    it('con listas en Disco las devuelve al instante y sincroniza en segundo plano', async () => {
      await seedDisk([list('1')]);
      let resolveIds!: (v: string[]) => void;
      service.getUserIds.mockReturnValue(new Promise((r) => (resolveIds = r)));
      service.getAllById.mockResolvedValue([list('1', { name: 'Remota' }), list('2')]);

      await expect(repo.getByUserId('u1')).resolves.toEqual(['1']);
      expect(store.getState().itemLists['1']).toEqual(list('1'));

      resolveIds(['1', '2']);
      await flush();
      expect(service.getAllById).toHaveBeenCalledWith(['1', '2']);
      expect(store.getState().itemLists['1'].name).toBe('Remota');
      expect(await readDisk('@itemList_user:u1')).toEqual(['1', '2']);
    });

    it('la sincronización de fondo quita de RAM y Disco las listas borradas en el backend', async () => {
      await seedDisk([list('1'), list('2')]);
      service.getUserIds.mockResolvedValue(['2']);
      service.getAllById.mockResolvedValue([list('2')]);

      await expect(repo.getByUserId('u1')).resolves.toEqual(['1', '2']);
      await flush();

      expect(store.getState().itemLists['1']).toBeUndefined();
      expect(store.getState().itemLists['2']).toEqual(list('2'));
      expect(await readDisk('@itemList:1')).toBeNull();
      expect(await readDisk('@itemList_user:u1')).toEqual(['2']);
    });

    it('si el backend ya no tiene listas, la sincronización de fondo vacía las del usuario', async () => {
      await seedDisk([list('1')]);
      service.getUserIds.mockResolvedValue([]);
      await repo.getByUserId('u1');
      await flush();
      expect(service.getAllById).not.toHaveBeenCalled();
      expect(store.getState().itemLists).toEqual({});
      expect(await readDisk('@itemList_user:u1')).toEqual([]);
    });

    it('un fallo en la sincronización de fondo se ignora', async () => {
      await seedDisk([list('1')]);
      service.getUserIds.mockRejectedValue(new Error('offline'));
      await expect(repo.getByUserId('u1')).resolves.toEqual(['1']);
      await flush();
    });

    it('sin Disco pide los IDs y las listas a la API', async () => {
      service.getUserIds.mockResolvedValue(['7', '8']);
      service.getAllById.mockResolvedValue([list('7'), list('8')]);
      await expect(repo.getByUserId('u1')).resolves.toEqual(['7', '8']);
      expect(service.getUserIds).toHaveBeenCalledWith('u1');
      expect(Object.keys(store.getState().itemLists)).toEqual(['7', '8']);
      expect(await readDisk('@itemList_user:u1')).toEqual(['7', '8']);
    });

    it('sin Disco y sin listas en el backend devuelve []', async () => {
      service.getUserIds.mockResolvedValue([]);
      await expect(repo.getByUserId('u1')).resolves.toEqual([]);
      expect(service.getAllById).not.toHaveBeenCalled();
    });

    it('sin Disco y sin red devuelve []', async () => {
      service.getUserIds.mockRejectedValue(new Error('offline'));
      await expect(repo.getByUserId('u1')).resolves.toEqual([]);
    });
  });

  describe.each([
    ['addItemToList', 'addItemToList'],
    ['removeItemFromList', 'removeItemFromList'],
  ] as const)('%s', (fn, serviceFn) => {
    it('sincroniza la lista actualizada en RAM y Disco', async () => {
      const updated = list('1', { itemIds: ['z'] });
      service[serviceFn].mockResolvedValue(updated);
      await expect(repo[fn]('1', 'z')).resolves.toEqual(updated);
      expect(service[serviceFn]).toHaveBeenCalledWith('1', 'z');
      expect(store.getState().itemLists['1']).toEqual(updated);
      expect(await readDisk('@itemList:1')).toEqual(updated);
    });

    it('si el backend no devuelve la lista, no toca la caché', async () => {
      service[serviceFn].mockResolvedValue(null as any);
      await expect(repo[fn]('1', 'z')).resolves.toBeNull();
      expect(store.getState().itemLists).toEqual({});
    });

    it('devuelve null si la API falla', async () => {
      service[serviceFn].mockRejectedValue(new Error('500'));
      await expect(repo[fn]('1', 'z')).resolves.toBeNull();
    });
  });

  describe('createItemList', () => {
    it('crea la lista y la guarda en RAM, Disco e índice del usuario', async () => {
      service.createItemList.mockResolvedValue(list('9'));
      const payload = { userId: 'u1', name: 'L9' } as any;
      await expect(repo.createItemList(payload)).resolves.toEqual(list('9'));
      expect(service.createItemList).toHaveBeenCalledWith(payload);
      expect(store.getState().itemLists['9']).toEqual(list('9'));
      expect(await readDisk('@itemList_user:u1')).toEqual(['9']);
    });

    it('devuelve null si la API falla', async () => {
      service.createItemList.mockRejectedValue(new Error('500'));
      await expect(repo.createItemList({} as any)).resolves.toBeNull();
    });
  });

  describe('updateItemList', () => {
    it('combina los cambios con la lista actual sin cambiar id ni userId', async () => {
      store.getState().setItemLists([list('1')]);
      service.updateItemList.mockImplementation(async (_id, payload) => payload);

      const result = await repo.updateItemList('1', { name: 'Nuevo', id: 'otro', userId: 'otro' } as any);
      expect(service.updateItemList).toHaveBeenCalledWith('1', {
        id: '1',
        userId: 'u1',
        name: 'Nuevo',
        itemIds: ['a', 'b'],
      });
      expect(result?.name).toBe('Nuevo');
      expect(store.getState().itemLists['1'].name).toBe('Nuevo');
    });

    it('usa los itemIds nuevos si vienen en los cambios', async () => {
      store.getState().setItemLists([list('1')]);
      service.updateItemList.mockImplementation(async (_id, payload) => payload);
      await repo.updateItemList('1', { itemIds: ['x'] });
      expect(service.updateItemList.mock.calls[0][1].itemIds).toEqual(['x']);
    });

    it('usa [] si ni la lista ni los cambios traen itemIds', async () => {
      store.getState().setItemLists([list('1', { itemIds: undefined })]);
      service.updateItemList.mockImplementation(async (_id, payload) => payload);
      await repo.updateItemList('1', { name: 'x' });
      expect(service.updateItemList.mock.calls[0][1].itemIds).toEqual([]);
    });

    it('devuelve null si la lista no existe', async () => {
      service.getById.mockRejectedValue(new Error('404'));
      await expect(repo.updateItemList('1', { name: 'x' })).resolves.toBeNull();
      expect(service.updateItemList).not.toHaveBeenCalled();
    });

    it('si el backend no devuelve la lista, devuelve null sin tocar la caché', async () => {
      store.getState().setItemLists([list('1')]);
      service.updateItemList.mockResolvedValue(null as any);
      await expect(repo.updateItemList('1', { name: 'x' })).resolves.toBeNull();
      expect(store.getState().itemLists['1'].name).toBe('L1');
    });

    it('devuelve null si la API falla', async () => {
      store.getState().setItemLists([list('1')]);
      service.updateItemList.mockRejectedValue(new Error('500'));
      await expect(repo.updateItemList('1', { name: 'x' })).resolves.toBeNull();
    });
  });

  describe('deleteItemList', () => {
    it('borra en el backend, en RAM y en Disco (con el userId de RAM)', async () => {
      await seedDisk([list('1'), list('2')]);
      store.getState().setItemLists([list('1')]);
      service.deleteItemList.mockResolvedValue(undefined);

      await expect(repo.deleteItemList('1')).resolves.toBe(true);
      expect(service.deleteItemList).toHaveBeenCalledWith('1');
      expect(store.getState().itemLists['1']).toBeUndefined();
      expect(await storage.getItem('@itemList:1')).toBeNull();
      expect(await readDisk('@itemList_user:u1')).toEqual(['2']);
    });

    it('obtiene el userId del Disco si no está en RAM', async () => {
      await seedDisk([list('1')]);
      service.deleteItemList.mockResolvedValue(undefined);
      await expect(repo.deleteItemList('1')).resolves.toBe(true);
      expect(await storage.getItem('@itemList:1')).toBeNull();
      expect(await readDisk('@itemList_user:u1')).toEqual([]);
    });

    it('sin userId conocido borra en el backend y en RAM igualmente', async () => {
      service.deleteItemList.mockResolvedValue(undefined);
      await expect(repo.deleteItemList('1')).resolves.toBe(true);
      expect(service.deleteItemList).toHaveBeenCalledWith('1');
    });

    it('si el backend falla devuelve false y conserva la lista', async () => {
      store.getState().setItemLists([list('1')]);
      service.deleteItemList.mockRejectedValue(new Error('500'));
      await expect(repo.deleteItemList('1')).resolves.toBe(false);
      expect(store.getState().itemLists['1']).toBeDefined();
    });
  });

  describe('updateItemListImage', () => {
    it('sube la imagen y guarda su URL en la lista', async () => {
      store.getState().setItemLists([list('1')]);
      cloudinary.uploadProfileImageToCloudinary.mockResolvedValue('https://img');
      service.updateItemList.mockImplementation(async (_id, payload) => payload);

      await expect(repo.updateItemListImage('1', 'file://a.png')).resolves.toMatchObject({ imageUrl: 'https://img' });
      expect(cloudinary.uploadProfileImageToCloudinary).toHaveBeenCalledWith('file://a.png');
    });

    it('propaga el error si falla la subida', async () => {
      cloudinary.uploadProfileImageToCloudinary.mockRejectedValue(new Error('cloudinary'));
      await expect(repo.updateItemListImage('1', 'x')).rejects.toThrow('cloudinary');
      expect(service.updateItemList).not.toHaveBeenCalled();
    });
  });
});
