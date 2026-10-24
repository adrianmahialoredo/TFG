jest.mock('../../services/instrumentGenre.service');
jest.mock('../../services/lesson.service');
jest.mock('../../services/item.service');

const flush = async () => {
  for (let i = 0; i < 10; i++) await new Promise((r) => setImmediate(r));
};

const ig = (id: string, extra: Record<string, unknown> = {}) =>
  ({ id, title: `IG${id}`, imageUrl: 'img', type: 'InstrumentGenre', genreId: 'g1', instrumentId: 'i1', ...extra }) as any;

let repo: typeof import('../../repositories/instrumentGenre.repository');
let service: jest.Mocked<typeof import('../../services/instrumentGenre.service')>;
let lessonService: jest.Mocked<typeof import('../../services/lesson.service')>;
let itemService: jest.Mocked<typeof import('../../services/item.service')>;
let store: typeof import('../../stores/entities/instrumentGenre.entities.store').useInstrumentGenreEntitiesStore;
let itemStore: typeof import('../../stores/entities/item.entities.store').useItemEntitiesStore;
let storage: any;

const readDisk = async (key: string) => JSON.parse((await storage.getItem(key)) ?? 'null');

beforeEach(async () => {
  jest.resetModules();
  storage = require('@react-native-async-storage/async-storage');
  repo = require('../../repositories/instrumentGenre.repository');
  service = require('../../services/instrumentGenre.service');
  lessonService = require('../../services/lesson.service');
  itemService = require('../../services/item.service');
  store = require('../../stores/entities/instrumentGenre.entities.store').useInstrumentGenreEntitiesStore;
  itemStore = require('../../stores/entities/item.entities.store').useItemEntitiesStore;
  await storage.clear();
  service.getAllById.mockResolvedValue([]);
  lessonService.getAllById.mockResolvedValue([]);
  itemService.getAllById.mockResolvedValue([]);
});

describe('instrumentGenre.repository', () => {
  describe('getById', () => {
    it('devuelve el de RAM y lo revalida', async () => {
      store.getState().setInstrumentGenres([ig('1', { title: 'Viejo' })]);
      service.getAllById.mockResolvedValue([ig('1', { title: 'Nuevo' })]);
      await expect(repo.getById('1')).resolves.toMatchObject({ title: 'Viejo' });
      await flush();
      expect(store.getState().instrumentGenres['1'].title).toBe('Nuevo');
      expect(itemStore.getState().items['1']).toMatchObject({
        type: 'InstrumentGenre',
        genreIds: ['g1'],
        instrumentIds: ['i1'],
      });
    });

    it('lee de Disco y lo sube a RAM', async () => {
      await storage.setItem('@instrumentGenre:2', JSON.stringify(ig('2')));
      await expect(repo.getById('2')).resolves.toEqual(ig('2'));
      expect(store.getState().instrumentGenres['2']).toEqual(ig('2'));
    });

    it('pide a la API si no hay caché y lo guarda en Disco', async () => {
      service.getInstrumentGenreById.mockResolvedValue(ig('3'));
      await expect(repo.getById('3')).resolves.toEqual(ig('3'));
      expect(service.getInstrumentGenreById).toHaveBeenCalledWith('3');
      expect(await readDisk('@instrumentGenre:3')).toEqual(ig('3'));
    });

    it('devuelve null si la API falla', async () => {
      service.getInstrumentGenreById.mockRejectedValue(new Error('offline'));
      await expect(repo.getById('4')).resolves.toBeNull();
    });

    it('devuelve null si la API no devuelve nada', async () => {
      service.getInstrumentGenreById.mockResolvedValue(null as any);
      await expect(repo.getById('4')).resolves.toBeNull();
    });
  });

  describe('getAllById / hydrate', () => {
    it('getAllById pide a la API lo que falta', async () => {
      service.getAllById.mockResolvedValueOnce([ig('5')]);
      await expect(repo.getAllById(['5'])).resolves.toEqual([ig('5')]);
    });

    it('hydrate guarda en RAM y devuelve los IDs', () => {
      expect(repo.hydrate([ig('6')])).toEqual(['6']);
      expect(store.getState().instrumentGenres['6']).toEqual(ig('6'));
    });
  });

  describe('getByGenreId', () => {
    it('devuelve los IDs, guarda el índice y precarga las lecciones', async () => {
      service.getByGenreId.mockResolvedValue([ig('1', { lessonIds: ['l1'] }), ig('2', { lessonIds: ['l2'] }), ig('3')]);
      lessonService.getAllById.mockResolvedValue([{ id: 'l1', title: 'L1', type: 'Lesson' } as any]);

      await expect(repo.getByGenreId('g1')).resolves.toEqual(['1', '2', '3']);
      expect(service.getByGenreId).toHaveBeenCalledWith('g1');
      await flush();
      expect(lessonService.getAllById).toHaveBeenCalledWith(['l1', 'l2']);
      expect(await readDisk('@instrumentGenre_index:genre_g1')).toEqual(['1', '2', '3']);
    });

    it('no precarga lecciones si no hay ninguna', async () => {
      service.getByGenreId.mockResolvedValue([ig('1')]);
      await repo.getByGenreId('g1');
      await flush();
      expect(lessonService.getAllById).not.toHaveBeenCalled();
    });

    it('con índice guardado devuelve la caché y avisa con onFresh', async () => {
      await storage.setItem('@instrumentGenre:1', JSON.stringify(ig('1')));
      await storage.setItem('@instrumentGenre_index:genre_g1', JSON.stringify(['1']));
      service.getByGenreId.mockResolvedValue([ig('1'), ig('2')]);
      const onFresh = jest.fn();
      await expect(repo.getByGenreId('g1', onFresh)).resolves.toEqual(['1']);
      await flush();
      expect(onFresh).toHaveBeenCalledWith(['1', '2']);
    });

    it('sin caché y sin red devuelve []', async () => {
      service.getByGenreId.mockRejectedValue(new Error('offline'));
      await expect(repo.getByGenreId('g1')).resolves.toEqual([]);
    });
  });

  describe('getByInstrumentId', () => {
    it('devuelve los IDs del instrumento y guarda el índice', async () => {
      service.getByInstrumentId.mockResolvedValue([ig('7')]);
      await expect(repo.getByInstrumentId('i1')).resolves.toEqual(['7']);
      expect(service.getByInstrumentId).toHaveBeenCalledWith('i1');
      await flush();
      expect(await readDisk('@instrumentGenre_index:instrument_i1')).toEqual(['7']);
    });
  });

  describe('getFavouriteCombinations', () => {
    it('usa el feed de items con los IDs del backend', async () => {
      service.getFavouriteCombinationIdsByUser.mockResolvedValue([8] as any);
      itemService.getAllById.mockResolvedValue([{ id: '8', title: 'x', type: 'InstrumentGenre' } as any]);
      await expect(repo.getFavouriteCombinations('u1')).resolves.toEqual(['8']);
      expect(service.getFavouriteCombinationIdsByUser).toHaveBeenCalledWith('u1');
      await flush();
      expect(await readDisk('@item_index:instrument_genres_favourite_combinations')).toEqual(['8']);
    });
  });
});
