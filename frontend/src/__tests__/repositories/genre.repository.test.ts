import { AxiosError } from 'axios';

jest.mock('../../services/genre.service');
jest.mock('../../services/item.service');

/** Deja correr las promesas encadenadas en segundo plano (revalidaciones, refrescos). */
const flush = async () => {
  for (let i = 0; i < 10; i++) await new Promise((r) => setImmediate(r));
};

const genre = (id: string, title = `G${id}`) => ({ id, title, imageUrl: `img${id}`, type: 'Genre', description: 'd' }) as any;

let repo: typeof import('../../repositories/genre.repository');
let service: jest.Mocked<typeof import('../../services/genre.service')>;
let itemService: jest.Mocked<typeof import('../../services/item.service')>;
let genreStore: typeof import('../../stores/entities/genre.entities.store').useGenreEntitiesStore;
let itemStore: typeof import('../../stores/entities/item.entities.store').useItemEntitiesStore;
let storage: any;

const seedDisk = async (entities: any[]) =>
  storage.multiSet(entities.map((e) => [`@genre:${e.id}`, JSON.stringify(e)]));
const readDisk = async (key: string) => JSON.parse((await storage.getItem(key)) ?? 'null');

beforeEach(async () => {
  // Módulos nuevos en cada test: el estado de revalidación (por sesión) y las Stores empiezan limpios.
  jest.resetModules();
  storage = require('@react-native-async-storage/async-storage');
  repo = require('../../repositories/genre.repository');
  service = require('../../services/genre.service');
  itemService = require('../../services/item.service');
  genreStore = require('../../stores/entities/genre.entities.store').useGenreEntitiesStore;
  itemStore = require('../../stores/entities/item.entities.store').useItemEntitiesStore;
  await storage.clear();
  service.getAllById.mockResolvedValue([]);
  itemService.getAllById.mockResolvedValue([]);
});

describe('genre.repository', () => {
  describe('getById', () => {
    it('devuelve el género de RAM y lo revalida en segundo plano una sola vez', async () => {
      genreStore.getState().setGenres([genre('1', 'Viejo')]);
      service.getAllById.mockResolvedValue([genre('1', 'Nuevo')]);

      const result = await repo.getById('1');
      expect(result?.title).toBe('Viejo');
      expect(service.getById).not.toHaveBeenCalled();

      await flush();
      expect(service.getAllById).toHaveBeenCalledWith(['1']);
      expect(genreStore.getState().genres['1'].title).toBe('Nuevo');
      // El resumen de la tarjeta se mantiene al día
      expect(itemStore.getState().items['1']).toMatchObject({ title: 'Nuevo', type: 'Genre' });
      expect((await readDisk('@genre:1')).title).toBe('Nuevo');

      await repo.getById('1');
      await flush();
      expect(service.getAllById).toHaveBeenCalledTimes(1);
    });

    it('lee de Disco si no está en RAM y lo sube a RAM', async () => {
      await seedDisk([genre('2')]);
      const result = await repo.getById('2');
      expect(result).toEqual(genre('2'));
      expect(genreStore.getState().genres['2']).toEqual(genre('2'));
      expect(service.getById).not.toHaveBeenCalled();
      await flush();
      expect(service.getAllById).toHaveBeenCalledWith(['2']);
    });

    it('pide a la API si no hay caché, guarda en RAM y Disco y no revalida después', async () => {
      service.getById.mockResolvedValue(genre('3'));
      const result = await repo.getById('3');
      expect(result).toEqual(genre('3'));
      expect(service.getById).toHaveBeenCalledWith('3');
      expect(genreStore.getState().genres['3']).toEqual(genre('3'));
      expect(await readDisk('@genre:3')).toEqual(genre('3'));

      await repo.getById('3');
      await flush();
      expect(service.getAllById).not.toHaveBeenCalled();
    });

    it('devuelve null si la API falla', async () => {
      service.getById.mockRejectedValue(new Error('offline'));
      await expect(repo.getById('4')).resolves.toBeNull();
      expect(genreStore.getState().genres['4']).toBeUndefined();
    });

    it('devuelve null si la API no devuelve nada', async () => {
      service.getById.mockResolvedValue(null as any);
      await expect(repo.getById('4')).resolves.toBeNull();
    });

    it('sigue devolviendo el género aunque falle el guardado en Disco', async () => {
      service.getById.mockResolvedValue(genre('5'));
      storage.multiSet.mockRejectedValueOnce(new Error('disco lleno'));
      await expect(repo.getById('5')).resolves.toEqual(genre('5'));
    });

    it('si la revalidación falla, se reintenta en la siguiente lectura', async () => {
      genreStore.getState().setGenres([genre('1')]);
      service.getAllById.mockRejectedValueOnce(new Error('offline'));
      await repo.getById('1');
      await flush();
      await repo.getById('1');
      await flush();
      expect(service.getAllById).toHaveBeenCalledTimes(2);
    });

    it('una revalidación que llega tarde no pisa un dato escrito mientras tanto', async () => {
      genreStore.getState().setGenres([genre('1', 'Viejo')]);
      let resolveFetch!: (v: any) => void;
      service.getAllById.mockReturnValue(new Promise((r) => (resolveFetch = r)));

      await repo.getById('1');
      repo.hydrate([genre('1', 'Escrito')]);
      resolveFetch([genre('1', 'Respuesta tardía')]);
      await flush();

      expect(genreStore.getState().genres['1'].title).toBe('Escrito');
    });
  });

  describe('getAllById', () => {
    it('devuelve [] para una lista vacía sin tocar la API', async () => {
      await expect(repo.getAllById([])).resolves.toEqual([]);
      expect(service.getAllById).not.toHaveBeenCalled();
    });

    it('combina RAM, Disco y API respetando el orden pedido', async () => {
      genreStore.getState().setGenres([genre('1')]);
      await seedDisk([genre('2')]);
      service.getAllById.mockImplementation(async (ids: string[]) => ids.map((id) => genre(id)));

      const result = await repo.getAllById(['3', '1', '2']);
      expect(result.map((g) => g.id)).toEqual(['3', '1', '2']);
      // Solo lo que falta va a la API; lo cacheado se revalida aparte
      expect(service.getAllById).toHaveBeenNthCalledWith(1, ['3']);
      await flush();
      expect(service.getAllById).toHaveBeenNthCalledWith(2, ['1', '2']);
      expect(Object.keys(genreStore.getState().genres).sort()).toEqual(['1', '2', '3']);
      expect(await readDisk('@genre:3')).toEqual(genre('3'));
    });

    it('si la API falla devuelve solo lo cacheado', async () => {
      genreStore.getState().setGenres([genre('1')]);
      service.getAllById.mockRejectedValueOnce(new Error('offline'));
      const result = await repo.getAllById(['1', '2']);
      expect(result).toEqual([genre('1')]);
    });

    it('descarta los IDs que la API no devuelve', async () => {
      service.getAllById.mockResolvedValueOnce([genre('1')]);
      await expect(repo.getAllById(['1', '99'])).resolves.toEqual([genre('1')]);
    });

    it('trata como ausente una entrada corrupta en Disco', async () => {
      await storage.setItem('@genre:7', '{no-json');
      service.getAllById.mockResolvedValueOnce([genre('7')]);
      await expect(repo.getAllById(['7'])).resolves.toEqual([genre('7')]);
      expect(service.getAllById).toHaveBeenCalledWith(['7']);
    });
  });

  describe('getAll', () => {
    it('sin índice guardado espera a la API y guarda el índice', async () => {
      service.getAll.mockResolvedValue([genre('1'), genre('2')]);
      const result = await repo.getAll();
      expect(result).toEqual([genre('1'), genre('2')]);
      await flush();
      expect(await readDisk('@genre_index:all')).toEqual(['1', '2']);
      expect(await readDisk('@genre:2')).toEqual(genre('2'));
    });

    it('con índice devuelve la caché al instante y refresca en segundo plano', async () => {
      await seedDisk([genre('1', 'Cache')]);
      await storage.setItem('@genre_index:all', JSON.stringify(['1']));
      let resolveFetch!: (v: any) => void;
      service.getAll.mockReturnValue(new Promise((r) => (resolveFetch = r)));

      const result = await repo.getAll();
      expect(result).toEqual([genre('1', 'Cache')]);

      resolveFetch([genre('1', 'Red'), genre('2')]);
      await flush();
      expect(genreStore.getState().genres['1'].title).toBe('Red');
      expect(await readDisk('@genre_index:all')).toEqual(['1', '2']);
    });

    it('con índice y refresco fallido mantiene la caché y avisa', async () => {
      await seedDisk([genre('1')]);
      await storage.setItem('@genre_index:all', JSON.stringify(['1']));
      service.getAll.mockRejectedValue(new Error('offline'));
      await expect(repo.getAll()).resolves.toEqual([genre('1')]);
      await flush();
      expect(console.warn).toHaveBeenCalled();
    });

    it('un 401 con token en el refresco no se avisa (ya lo gestiona apiClient)', async () => {
      await seedDisk([genre('1')]);
      await storage.setItem('@genre_index:all', JSON.stringify(['1']));
      const err = new AxiosError('401', 'ERR_BAD_REQUEST', { headers: { Authorization: 'Bearer t' } } as any, null, {
        status: 401,
      } as any);
      service.getAll.mockRejectedValue(err);
      await repo.getAll();
      await flush();
      expect(console.warn).not.toHaveBeenCalled();
    });

    it('sin índice y sin red devuelve []', async () => {
      service.getAll.mockRejectedValue(new Error('offline'));
      await expect(repo.getAll()).resolves.toEqual([]);
      expect(console.error).toHaveBeenCalled();
    });

    it('un 401 con token sin caché devuelve [] sin registrar error', async () => {
      const err = new AxiosError('401', 'ERR_BAD_REQUEST', { headers: { Authorization: 'Bearer t' } } as any, null, {
        status: 401,
      } as any);
      service.getAll.mockRejectedValue(err);
      await expect(repo.getAll()).resolves.toEqual([]);
      expect(console.error).not.toHaveBeenCalled();
    });

    it('un índice que apunta a entidades borradas del Disco espera a la API', async () => {
      await storage.setItem('@genre_index:all', JSON.stringify(['1']));
      service.getAll.mockResolvedValue([genre('2')]);
      await expect(repo.getAll()).resolves.toEqual([genre('2')]);
    });

    it('guarda el índice vacío si la API no devuelve géneros', async () => {
      service.getAll.mockResolvedValue([]);
      await expect(repo.getAll()).resolves.toEqual([]);
      await flush();
      expect(await readDisk('@genre_index:all')).toEqual([]);
    });
  });

  describe('hydrate', () => {
    it('guarda en RAM, Disco y en la ItemEntitiesStore y devuelve los IDs', async () => {
      const ids = repo.hydrate([genre('1'), genre('2')]);
      expect(ids).toEqual(['1', '2']);
      expect(genreStore.getState().genres['2']).toEqual(genre('2'));
      expect(itemStore.getState().items['1']).toMatchObject({ id: '1', title: 'G1', type: 'Genre' });
      await flush();
      expect(await readDisk('@genre:1')).toEqual(genre('1'));
    });

    it('no hace nada con una lista vacía', () => {
      expect(repo.hydrate([])).toEqual([]);
      expect(genreStore.getState().genres).toEqual({});
    });
  });

  describe('getMostUsedInItemLists', () => {
    it('pide los IDs al backend y resuelve los que faltan en el catálogo', async () => {
      service.getMostUsedInItemListsIdsByUser.mockResolvedValue([1, 2] as any);
      itemService.getAllById.mockResolvedValue([{ id: '2', title: 'G2', type: 'Genre' }] as any);
      itemStore.getState().setItems([{ id: '1', title: 'G1', type: 'Genre' } as any]);

      await expect(repo.getMostUsedInItemLists('u1')).resolves.toEqual(['1', '2']);
      expect(service.getMostUsedInItemListsIdsByUser).toHaveBeenCalledWith('u1');
      expect(itemService.getAllById).toHaveBeenCalledWith(['2']);
      await flush();
      expect(await readDisk('@item_index:genres_most_used_in_item_lists')).toEqual(['1', '2']);
    });

    it('con índice guardado devuelve la caché y avisa con onFresh', async () => {
      await storage.setItem('@item:1', JSON.stringify({ id: '1', title: 'G1', type: 'Genre' }));
      await storage.setItem('@item_index:genres_most_used_in_item_lists', JSON.stringify(['1']));
      service.getMostUsedInItemListsIdsByUser.mockResolvedValue(['1', '3'] as any);
      itemService.getAllById.mockResolvedValue([{ id: '3', title: 'G3', type: 'Genre' }] as any);
      const onFresh = jest.fn();

      await expect(repo.getMostUsedInItemLists('u1', onFresh)).resolves.toEqual(['1']);
      await flush();
      expect(onFresh).toHaveBeenCalledWith(['1', '3']);
    });

    it('si falla resolver los items que faltan, devuelve igualmente los IDs', async () => {
      service.getMostUsedInItemListsIdsByUser.mockResolvedValue(['9'] as any);
      itemService.getAllById.mockRejectedValue(new Error('offline'));
      await expect(repo.getMostUsedInItemLists('u1')).resolves.toEqual(['9']);
    });
  });
});
