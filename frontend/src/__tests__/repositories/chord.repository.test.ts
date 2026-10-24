jest.mock('../../services/chord.service');

const flush = async () => {
  for (let i = 0; i < 10; i++) await new Promise((r) => setImmediate(r));
};

const chord = (id: string, name = `C${id}`) => ({ id, name }) as any;

let repo: typeof import('../../repositories/chord.repository');
let service: jest.Mocked<typeof import('../../services/chord.service')>;
let store: typeof import('../../stores/entities/chord.entities.store').useChordEntitiesStore;
let storage: any;

const readDisk = async (key: string) => JSON.parse((await storage.getItem(key)) ?? 'null');

beforeEach(async () => {
  jest.resetModules();
  storage = require('@react-native-async-storage/async-storage');
  repo = require('../../repositories/chord.repository');
  service = require('../../services/chord.service');
  store = require('../../stores/entities/chord.entities.store').useChordEntitiesStore;
  await storage.clear();
  service.getAllById.mockResolvedValue([]);
});

describe('chord.repository', () => {
  describe('getById', () => {
    it('devuelve el acorde de RAM y lo revalida en segundo plano', async () => {
      store.getState().setChords([chord('1', 'Viejo')]);
      service.getAllById.mockResolvedValue([chord('1', 'Nuevo')]);
      await expect(repo.getById('1')).resolves.toEqual(chord('1', 'Viejo'));
      await flush();
      expect(service.getAllById).toHaveBeenCalledWith(['1']);
      expect(store.getState().chords['1'].name).toBe('Nuevo');
    });

    it('lee de Disco y lo sube a RAM', async () => {
      await storage.setItem('@chord:2', JSON.stringify(chord('2')));
      await expect(repo.getById('2')).resolves.toEqual(chord('2'));
      expect(store.getState().chords['2']).toEqual(chord('2'));
      expect(service.getById).not.toHaveBeenCalled();
    });

    it('pide a la API si no hay caché y lo guarda en RAM y Disco', async () => {
      service.getById.mockResolvedValue(chord('3'));
      await expect(repo.getById('3')).resolves.toEqual(chord('3'));
      expect(service.getById).toHaveBeenCalledWith('3');
      expect(store.getState().chords['3']).toEqual(chord('3'));
      expect(await readDisk('@chord:3')).toEqual(chord('3'));
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
      expect(store.getState().chords).toEqual({});
    });
  });

  describe('getAllById', () => {
    it('resuelve contra RAM y pide a la API solo lo que falta', async () => {
      store.getState().setChords([chord('1')]);
      service.getAllById.mockResolvedValueOnce([chord('2')]);
      await expect(repo.getAllById(['1', '2'])).resolves.toEqual([chord('1'), chord('2')]);
      expect(service.getAllById).toHaveBeenNthCalledWith(1, ['2']);
    });
  });
});
