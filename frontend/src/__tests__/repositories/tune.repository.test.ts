jest.mock('../../services/tune.service');

const flush = async () => {
  for (let i = 0; i < 10; i++) await new Promise((r) => setImmediate(r));
};

const tune = (id: string, title = `T${id}`, instrumentId = '1') =>
  ({ id, title, instrumentId, notes: ['E2', 'A2'] }) as any;

let repo: typeof import('../../repositories/tune.repository');
let service: jest.Mocked<typeof import('../../services/tune.service')>;
let store: typeof import('../../stores/entities/tune.entities.store').useTuneEntitiesStore;
let storage: any;

const readDisk = async (key: string) => JSON.parse((await storage.getItem(key)) ?? 'null');

beforeEach(async () => {
  jest.resetModules();
  storage = require('@react-native-async-storage/async-storage');
  repo = require('../../repositories/tune.repository');
  service = require('../../services/tune.service');
  store = require('../../stores/entities/tune.entities.store').useTuneEntitiesStore;
  await storage.clear();
  service.getAllById.mockResolvedValue([]);
});

describe('tune.repository', () => {
  describe('getById', () => {
    it('devuelve la afinación de RAM', async () => {
      store.getState().setTunes([tune('1', 'Estándar')]);
      await expect(repo.getById('1')).resolves.toEqual(tune('1', 'Estándar'));
    });

    it('lee de Disco y la sube a RAM', async () => {
      await storage.setItem('@tune:2', JSON.stringify(tune('2')));
      await expect(repo.getById('2')).resolves.toEqual(tune('2'));
      expect(store.getState().tunes['2']).toEqual(tune('2'));
    });

    it('pide a la API si no hay caché y la guarda en RAM y Disco', async () => {
      service.getAllById.mockResolvedValue([tune('3')]);
      await expect(repo.getById('3')).resolves.toEqual(tune('3'));
      expect(service.getAllById).toHaveBeenCalledWith(['3']);
      expect(store.getState().tunes['3']).toEqual(tune('3'));
      expect(await readDisk('@tune:3')).toEqual(tune('3'));
    });

    it('devuelve null si no existe', async () => {
      await expect(repo.getById('4')).resolves.toBeNull();
    });
  });

  describe('getAll', () => {
    it('sin caché devuelve los IDs de la API, hidrata la Store y guarda el índice', async () => {
      service.getAll.mockResolvedValue([tune('1'), tune('2')]);
      await expect(repo.getAll()).resolves.toEqual(['1', '2']);
      expect(store.getState().tunes['2']).toEqual(tune('2'));
      await flush();
      expect(await readDisk('@tune_index:all')).toEqual(['1', '2']);
    });

    it('con índice devuelve la caché al instante y avisa con los IDs frescos', async () => {
      await storage.setItem('@tune:1', JSON.stringify(tune('1', 'Caché')));
      await storage.setItem('@tune_index:all', JSON.stringify(['1']));
      let resolveFetch!: (v: any) => void;
      service.getAll.mockReturnValue(new Promise((r) => (resolveFetch = r)));
      const onFresh = jest.fn();

      await expect(repo.getAll(onFresh)).resolves.toEqual(['1']);
      expect(store.getState().tunes['1'].title).toBe('Caché');

      resolveFetch([tune('1', 'Red'), tune('5')]);
      await flush();
      expect(store.getState().tunes['1'].title).toBe('Red');
      expect(onFresh).toHaveBeenCalledWith(['1', '5']);
    });

    it('sin caché y sin red devuelve []', async () => {
      service.getAll.mockRejectedValue(new Error('offline'));
      await expect(repo.getAll()).resolves.toEqual([]);
    });
  });

  describe('getByInstrumentId', () => {
    it('guarda un índice por instrumento', async () => {
      service.getByInstrumentId.mockResolvedValue([tune('6', 'Estándar', '3')]);
      await expect(repo.getByInstrumentId('3')).resolves.toEqual(['6']);
      expect(service.getByInstrumentId).toHaveBeenCalledWith('3');
      await flush();
      expect(await readDisk('@tune_index:by_instrument:3')).toEqual(['6']);
    });
  });
});
