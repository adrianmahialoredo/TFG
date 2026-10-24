jest.mock('../../services/tabSection.service');

const flush = async () => {
  for (let i = 0; i < 10; i++) await new Promise((r) => setImmediate(r));
};

const entity = (id: string, name = `E${id}`) => ({ id, name }) as any;

let repo: typeof import('../../repositories/tabSection.repository');
let service: jest.Mocked<typeof import('../../services/tabSection.service')>;
let store: typeof import('../../stores/entities/tabSection.entities.store').useTabSectionEntitiesStore;
let storage: any;

const readDisk = async (key: string) => JSON.parse((await storage.getItem(key)) ?? 'null');

beforeEach(async () => {
  jest.resetModules();
  storage = require('@react-native-async-storage/async-storage');
  repo = require('../../repositories/tabSection.repository');
  service = require('../../services/tabSection.service');
  store = require('../../stores/entities/tabSection.entities.store').useTabSectionEntitiesStore;
  await storage.clear();
  service.getAllById.mockResolvedValue([]);
});

describe('tabSection.repository', () => {
  describe('getById', () => {
    it('devuelve la entidad de RAM y lo revalida en segundo plano', async () => {
      store.getState().setTabSections([entity('1', 'Viejo')]);
      service.getAllById.mockResolvedValue([entity('1', 'Nuevo')]);
      await expect(repo.getById('1')).resolves.toEqual(entity('1', 'Viejo'));
      await flush();
      expect(service.getAllById).toHaveBeenCalledWith(['1']);
      expect((store.getState().tabSections['1'] as any).name).toBe('Nuevo');
    });

    it('lee de Disco y lo sube a RAM', async () => {
      await storage.setItem('@tabSection:2', JSON.stringify(entity('2')));
      await expect(repo.getById('2')).resolves.toEqual(entity('2'));
      expect(store.getState().tabSections['2']).toEqual(entity('2'));
      expect(service.getById).not.toHaveBeenCalled();
    });

    it('pide a la API si no hay caché y lo guarda en RAM y Disco', async () => {
      service.getById.mockResolvedValue(entity('3'));
      await expect(repo.getById('3')).resolves.toEqual(entity('3'));
      expect(service.getById).toHaveBeenCalledWith('3');
      expect(store.getState().tabSections['3']).toEqual(entity('3'));
      expect(await readDisk('@tabSection:3')).toEqual(entity('3'));
      // Recién llegado de la API: no se revalida
      await repo.getById('3');
      await flush();
      expect(service.getAllById).not.toHaveBeenCalled();
    });

    it('devuelve null si la API falla', async () => {
      service.getById.mockRejectedValue(new Error('offline'));
      await expect(repo.getById('4')).resolves.toBeNull();
    });

    it('devuelve null si la API no devuelve nada', async () => {
      service.getById.mockResolvedValue(null as any);
      await expect(repo.getById('4')).resolves.toBeNull();
      expect(store.getState().tabSections).toEqual({});
    });
  });

  describe('getAllById', () => {
    it('resuelve contra RAM y pide a la API solo lo que falta', async () => {
      store.getState().setTabSections([entity('1')]);
      service.getAllById.mockResolvedValueOnce([entity('2')]);
      await expect(repo.getAllById(['1', '2'])).resolves.toEqual([entity('1'), entity('2')]);
      expect(service.getAllById).toHaveBeenNthCalledWith(1, ['2']);
    });
  });
});
