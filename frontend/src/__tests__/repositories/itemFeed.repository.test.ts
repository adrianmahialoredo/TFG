jest.mock('../../services/item.service');

const flush = async () => {
  for (let i = 0; i < 10; i++) await new Promise((r) => setImmediate(r));
};

const mixed = [
  { id: 1, title: 'Song', imageUrl: 'a', type: 'Song', artist: 'Queen', genreIds: [3], instrumentIds: [], decade: '80s' },
  { id: 2, title: 'Lesson', imageUrl: 'b', type: 'Lesson', genreId: 3, instrumentId: 4, sectionIds: [] },
  { id: 3, title: 'Genre', imageUrl: 'c', type: 'Genre' },
  { id: 4, title: 'IG', imageUrl: 'd', type: 'InstrumentGenre', genreId: 3, instrumentId: 4 },
] as any[];

let repo: typeof import('../../repositories/itemFeed.repository');
let service: jest.Mocked<typeof import('../../services/item.service')>;
let stores: typeof import('../../stores/entities');
let storage: any;

const readDisk = async (key: string) => JSON.parse((await storage.getItem(key)) ?? 'null');

beforeEach(async () => {
  jest.resetModules();
  storage = require('@react-native-async-storage/async-storage');
  repo = require('../../repositories/itemFeed.repository');
  service = require('../../services/item.service');
  stores = require('../../stores/entities');
  await storage.clear();
});

describe('itemFeed.repository', () => {
  describe('getRecommendedForUser', () => {
    it('reparte cada DTO en la Store de su subtipo y guarda los resúmenes', async () => {
      service.getRecommendedByUser.mockResolvedValue(mixed);

      await expect(repo.getRecommendedForUser('u1')).resolves.toEqual(['1', '2', '3', '4']);
      expect(service.getRecommendedByUser).toHaveBeenCalledWith('u1');

      expect(stores.useSongEntitiesStore.getState().songs['1'].title).toBe('Song');
      expect(stores.useLessonEntitiesStore.getState().lessons['2'].title).toBe('Lesson');
      expect(stores.useGenreEntitiesStore.getState().genres['3'].title).toBe('Genre');
      expect(stores.useInstrumentGenreEntitiesStore.getState().instrumentGenres['4'].title).toBe('IG');

      const items = stores.useItemEntitiesStore.getState().items;
      expect(Object.keys(items).sort()).toEqual(['1', '2', '3', '4']);
      expect(items['1']).toMatchObject({ type: 'Song', artist: 'Queen', genreIds: ['3'], decade: '80s' });
      expect(items['2']).toMatchObject({ type: 'Lesson', genreIds: ['3'], instrumentIds: ['4'], artist: null });

      await flush();
      expect(await readDisk('@item_index:items_recommended_user')).toEqual(['1', '2', '3', '4']);
      expect(await readDisk('@song:1')).toMatchObject({ title: 'Song' });
      expect(await readDisk('@item:4')).toMatchObject({ type: 'InstrumentGenre' });
    });

    it('con índice guardado devuelve la caché y avisa con los IDs frescos', async () => {
      await storage.setItem('@item:9', JSON.stringify({ id: '9', title: 'x', type: 'Song' }));
      await storage.setItem('@item_index:items_recommended_user', JSON.stringify(['9']));
      service.getRecommendedByUser.mockResolvedValue([mixed[2]]);
      const onFresh = jest.fn();

      await expect(repo.getRecommendedForUser('u1', onFresh)).resolves.toEqual(['9']);
      await flush();
      expect(onFresh).toHaveBeenCalledWith(['3']);
    });

    it('sin caché y sin red devuelve []', async () => {
      service.getRecommendedByUser.mockRejectedValue(new Error('offline'));
      await expect(repo.getRecommendedForUser('u1')).resolves.toEqual([]);
    });
  });

  describe('getRecommendedByItemList', () => {
    it('usa un índice por lista', async () => {
      service.getRecommendedByItemList.mockResolvedValue([mixed[0]]);
      await expect(repo.getRecommendedByItemList('L7')).resolves.toEqual(['1']);
      expect(service.getRecommendedByItemList).toHaveBeenCalledWith('L7');
      await flush();
      expect(await readDisk('@item_index:items_recommended_list_L7')).toEqual(['1']);
    });

    it('una respuesta vacía guarda un índice vacío', async () => {
      service.getRecommendedByItemList.mockResolvedValue([]);
      await expect(repo.getRecommendedByItemList('L7')).resolves.toEqual([]);
      await flush();
      expect(await readDisk('@item_index:items_recommended_list_L7')).toEqual([]);
    });
  });
});
