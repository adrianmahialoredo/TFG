jest.mock('../../services/instrument.service');

const flush = async () => {
  for (let i = 0; i < 10; i++) await new Promise((r) => setImmediate(r));
};

const instrument = (id: string, name = `I${id}`) => ({ id, name, imageUrl: 'img' }) as any;

let repo: typeof import('../../repositories/instrument.repository');
let service: jest.Mocked<typeof import('../../services/instrument.service')>;
let store: typeof import('../../stores/entities/instrument.entities.store').useInstrumentEntitiesStore;
let storage: any;

const readDisk = async (key: string) => JSON.parse((await storage.getItem(key)) ?? 'null');

beforeEach(async () => {
  jest.resetModules();
  storage = require('@react-native-async-storage/async-storage');
  repo = require('../../repositories/instrument.repository');
  service = require('../../services/instrument.service');
  store = require('../../stores/entities/instrument.entities.store').useInstrumentEntitiesStore;
  await storage.clear();
  service.getAllById.mockResolvedValue([]);
});

describe('instrument.repository', () => {
  describe('getById', () => {
    it('devuelve el instrumento de RAM y lo revalida en segundo plano', async () => {
      store.getState().setInstruments([instrument('1', 'Viejo')]);
      service.getAllById.mockResolvedValue([instrument('1', 'Nuevo')]);
      await expect(repo.getById('1')).resolves.toEqual(instrument('1', 'Viejo'));
      await flush();
      expect(store.getState().instruments['1'].name).toBe('Nuevo');
    });

    it('lee de Disco y lo sube a RAM', async () => {
      await storage.setItem('@instrument:2', JSON.stringify(instrument('2')));
      await expect(repo.getById('2')).resolves.toEqual(instrument('2'));
      expect(store.getState().instruments['2']).toEqual(instrument('2'));
      expect(service.getInstrumentById).not.toHaveBeenCalled();
    });

    it('pide a la API si no hay caché y lo guarda en RAM y Disco', async () => {
      service.getInstrumentById.mockResolvedValue(instrument('3'));
      await expect(repo.getById('3')).resolves.toEqual(instrument('3'));
      expect(service.getInstrumentById).toHaveBeenCalledWith('3');
      expect(store.getState().instruments['3']).toEqual(instrument('3'));
      expect(await readDisk('@instrument:3')).toEqual(instrument('3'));
    });

    it('devuelve null si la API falla', async () => {
      service.getInstrumentById.mockRejectedValue(new Error('offline'));
      await expect(repo.getById('4')).resolves.toBeNull();
    });

    it('devuelve null si la API no devuelve nada', async () => {
      service.getInstrumentById.mockResolvedValue(null as any);
      await expect(repo.getById('4')).resolves.toBeNull();
    });
  });

  describe('getAllById', () => {
    it('pide a la API solo los que no están en caché', async () => {
      store.getState().setInstruments([instrument('1')]);
      service.getAllById.mockResolvedValueOnce([instrument('2')]);
      await expect(repo.getAllById(['2', '1'])).resolves.toEqual([instrument('2'), instrument('1')]);
      expect(service.getAllById).toHaveBeenNthCalledWith(1, ['2']);
    });
  });

  describe('getAll', () => {
    it('sin caché devuelve los instrumentos de la API y guarda el índice', async () => {
      service.getAllInstruments.mockResolvedValue([instrument('1'), instrument('2')]);
      await expect(repo.getAll()).resolves.toEqual([instrument('1'), instrument('2')]);
      await flush();
      expect(await readDisk('@instrument_index:all')).toEqual(['1', '2']);
    });

    it('con índice devuelve la caché al instante y refresca la Store', async () => {
      await storage.setItem('@instrument:1', JSON.stringify(instrument('1', 'Cache')));
      await storage.setItem('@instrument_index:all', JSON.stringify(['1']));
      let resolveFetch!: (v: any) => void;
      service.getAllInstruments.mockReturnValue(new Promise((r) => (resolveFetch = r)));
      await expect(repo.getAll()).resolves.toEqual([instrument('1', 'Cache')]);
      resolveFetch([instrument('1', 'Red')]);
      await flush();
      expect(store.getState().instruments['1'].name).toBe('Red');
    });

    it('sin caché y sin red devuelve []', async () => {
      service.getAllInstruments.mockRejectedValue(new Error('offline'));
      await expect(repo.getAll()).resolves.toEqual([]);
    });
  });
});
