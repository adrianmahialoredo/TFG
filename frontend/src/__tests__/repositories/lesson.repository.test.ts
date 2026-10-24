jest.mock('../../services/lesson.service');
jest.mock('../../services/lessonSection.service');
jest.mock('../../services/item.service');

const flush = async () => {
  for (let i = 0; i < 10; i++) await new Promise((r) => setImmediate(r));
};

const lesson = (id: string, extra: Record<string, unknown> = {}) =>
  ({ id, title: `L${id}`, imageUrl: 'img', type: 'Lesson', sectionIds: [], ...extra }) as any;

let repo: typeof import('../../repositories/lesson.repository');
let service: jest.Mocked<typeof import('../../services/lesson.service')>;
let sectionService: jest.Mocked<typeof import('../../services/lessonSection.service')>;
let itemService: jest.Mocked<typeof import('../../services/item.service')>;
let store: typeof import('../../stores/entities/lesson.entities.store').useLessonEntitiesStore;
let sectionStore: typeof import('../../stores/entities/lessonSection.entities.store').useLessonSectionEntitiesStore;
let itemStore: typeof import('../../stores/entities/item.entities.store').useItemEntitiesStore;
let storage: any;

const readDisk = async (key: string) => JSON.parse((await storage.getItem(key)) ?? 'null');

beforeEach(async () => {
  jest.resetModules();
  storage = require('@react-native-async-storage/async-storage');
  repo = require('../../repositories/lesson.repository');
  service = require('../../services/lesson.service');
  sectionService = require('../../services/lessonSection.service');
  itemService = require('../../services/item.service');
  store = require('../../stores/entities/lesson.entities.store').useLessonEntitiesStore;
  sectionStore = require('../../stores/entities/lessonSection.entities.store').useLessonSectionEntitiesStore;
  itemStore = require('../../stores/entities/item.entities.store').useItemEntitiesStore;
  await storage.clear();
  service.getAllById.mockResolvedValue([]);
  sectionService.getAllById.mockResolvedValue([]);
  itemService.getAllById.mockResolvedValue([]);
});

describe('lesson.repository', () => {
  describe('getById', () => {
    it('devuelve la lección de RAM, la revalida y actualiza el resumen', async () => {
      store.getState().setLessons([lesson('1', { title: 'Vieja' })]);
      service.getAllById.mockResolvedValue([lesson('1', { title: 'Nueva', genreId: 3 })]);
      await expect(repo.getById('1')).resolves.toMatchObject({ title: 'Vieja' });
      await flush();
      expect(store.getState().lessons['1'].title).toBe('Nueva');
      expect(itemStore.getState().items['1']).toMatchObject({ type: 'Lesson', title: 'Nueva', genreIds: ['3'] });
    });

    it('lee de Disco y la sube a RAM', async () => {
      await storage.setItem('@lesson:2', JSON.stringify(lesson('2')));
      await expect(repo.getById('2')).resolves.toEqual(lesson('2'));
      expect(store.getState().lessons['2']).toEqual(lesson('2'));
      expect(service.getById).not.toHaveBeenCalled();
    });

    it('pide a la API si no hay caché y la guarda en Disco', async () => {
      service.getById.mockResolvedValue(lesson('3'));
      await expect(repo.getById('3')).resolves.toEqual(lesson('3'));
      expect(service.getById).toHaveBeenCalledWith('3');
      expect(await readDisk('@lesson:3')).toEqual(lesson('3'));
    });

    it('carga también las secciones de la lección', async () => {
      service.getById.mockResolvedValue(lesson('3', { sectionIds: ['s1', 's2'] }));
      sectionService.getAllById.mockResolvedValue([{ id: 's1' }, { id: 's2' }] as any);
      await repo.getById('3');
      expect(sectionService.getAllById).toHaveBeenCalledWith(['s1', 's2']);
      expect(Object.keys(sectionStore.getState().lessonSections)).toEqual(['s1', 's2']);
    });

    it('si fallan las secciones devuelve igualmente la lección', async () => {
      store.getState().setLessons([lesson('1', { sectionIds: ['s1'] })]);
      storage.multiGet.mockRejectedValueOnce(new Error('disco roto'));
      await expect(repo.getById('1')).resolves.toMatchObject({ id: '1' });
      expect(console.error).toHaveBeenCalled();
    });

    it('no falla si la lección no trae sectionIds', async () => {
      service.getById.mockResolvedValue(lesson('3', { sectionIds: undefined }));
      await expect(repo.getById('3')).resolves.toMatchObject({ id: '3' });
      expect(sectionService.getAllById).not.toHaveBeenCalled();
    });

    it('devuelve null si la API falla', async () => {
      service.getById.mockRejectedValue(new Error('offline'));
      await expect(repo.getById('4')).resolves.toBeNull();
    });

    it('devuelve null si la API no devuelve nada', async () => {
      service.getById.mockResolvedValue(null as any);
      await expect(repo.getById('4')).resolves.toBeNull();
    });

    it('sigue devolviendo la lección aunque falle el guardado en Disco', async () => {
      service.getById.mockResolvedValue(lesson('5'));
      storage.multiSet.mockRejectedValueOnce(new Error('disco lleno'));
      await expect(repo.getById('5')).resolves.toEqual(lesson('5'));
    });
  });

  describe('getAllById / hydrate', () => {
    it('getAllById pide a la API lo que falta', async () => {
      service.getAllById.mockResolvedValueOnce([lesson('1')]);
      await expect(repo.getAllById(['1'])).resolves.toEqual([lesson('1')]);
    });

    it('hydrate guarda en RAM y devuelve los IDs', () => {
      expect(repo.hydrate([lesson('1')])).toEqual(['1']);
      expect(store.getState().lessons['1']).toEqual(lesson('1'));
    });
  });

  describe('listas con DTO completo', () => {
    it.each([
      ['getByGenreId', 'getByGenreId', '@lesson_index:genre_g1'],
      ['getRecommendedByGenreId', 'getRecommendedByGenreId', '@lesson_index:recommended_genre_g1'],
    ] as const)('%s hidrata las lecciones y guarda el índice', async (fn, serviceFn, indexKey) => {
      service[serviceFn].mockResolvedValue([lesson('1'), lesson('2')]);
      await expect(repo[fn]('g1')).resolves.toEqual(['1', '2']);
      expect(service[serviceFn]).toHaveBeenCalledWith('g1');
      expect(store.getState().lessons['2']).toEqual(lesson('2'));
      await flush();
      expect(await readDisk(indexKey)).toEqual(['1', '2']);
    });

    it('getRecommended usa el índice personalizado del usuario', async () => {
      service.getRecommendedByUser.mockResolvedValue([lesson('1')]);
      await expect(repo.getRecommended('u1')).resolves.toEqual(['1']);
      expect(service.getRecommendedByUser).toHaveBeenCalledWith('u1');
      await flush();
      expect(await readDisk(`@lesson_index:${repo.RECOMMENDED_INDEX_KEY}`)).toEqual(['1']);
    });
  });

  describe('carruseles por IDs', () => {
    it.each([
      ['getPopular', () => repo.getPopular(), 'getPopularIds', '@item_index:lessons_popular'],
      ['getNew', () => repo.getNew(), 'getNewIds', '@item_index:lessons_new'],
      [
        'getPopularByInstrumentId',
        () => repo.getPopularByInstrumentId('i1'),
        'getPopularByInstrumentIds',
        '@item_index:lessons_popular_instrument_i1',
      ],
    ] as const)('%s resuelve los IDs contra el catálogo', async (_n, call, serviceFn, indexKey) => {
      (service[serviceFn] as jest.Mock).mockResolvedValue([1, 2]);
      itemStore.getState().setItems([{ id: '1', title: 'x', type: 'Lesson' } as any]);
      itemService.getAllById.mockResolvedValue([{ id: '2', title: 'y', type: 'Lesson' } as any]);

      await expect(call()).resolves.toEqual(['1', '2']);
      expect(itemService.getAllById).toHaveBeenCalledWith(['2']);
      await flush();
      expect(await readDisk(indexKey)).toEqual(['1', '2']);
    });

    it('getPopularByInstrumentId pasa el instrumento al service', async () => {
      service.getPopularByInstrumentIds.mockResolvedValue([]);
      await repo.getPopularByInstrumentId('i9');
      expect(service.getPopularByInstrumentIds).toHaveBeenCalledWith('i9');
    });
  });
});
