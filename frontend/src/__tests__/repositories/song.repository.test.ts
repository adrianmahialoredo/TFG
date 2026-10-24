import { AxiosError } from 'axios';

jest.mock('../../services/song.service');
jest.mock('../../services/songLesson.service');
jest.mock('../../services/tabSection.service');
jest.mock('../../services/chord.service');
jest.mock('../../services/user.service');

const flush = async () => {
  for (let i = 0; i < 10; i++) await new Promise((r) => setImmediate(r));
};

const song = (id: string, extra: Record<string, unknown> = {}) =>
  ({ id, title: `S${id}`, imageUrl: 'img', type: 'Song', artist: 'A', lessonIds: [], ...extra }) as any;

const httpError = (status: number) =>
  new AxiosError('http', 'ERR_BAD_REQUEST', undefined, null, { status, data: {} } as any);

let repo: typeof import('../../repositories/song.repository');
let service: jest.Mocked<typeof import('../../services/song.service')>;
let songLessonService: jest.Mocked<typeof import('../../services/songLesson.service')>;
let tabService: jest.Mocked<typeof import('../../services/tabSection.service')>;
let chordService: jest.Mocked<typeof import('../../services/chord.service')>;
let userService: jest.Mocked<typeof import('../../services/user.service')>;
let stores: typeof import('../../stores/entities');
let storage: any;

const readDisk = async (key: string) => JSON.parse((await storage.getItem(key)) ?? 'null');

beforeEach(async () => {
  jest.resetModules();
  storage = require('@react-native-async-storage/async-storage');
  repo = require('../../repositories/song.repository');
  service = require('../../services/song.service');
  songLessonService = require('../../services/songLesson.service');
  tabService = require('../../services/tabSection.service');
  chordService = require('../../services/chord.service');
  userService = require('../../services/user.service');
  stores = require('../../stores/entities');
  await storage.clear();
  service.getAllById.mockResolvedValue([]);
  songLessonService.getAllById.mockResolvedValue([]);
  tabService.getAllById.mockResolvedValue([]);
  chordService.getAllById.mockResolvedValue([]);
});

describe('song.repository', () => {
  describe('getById', () => {
    it('devuelve la canción de RAM, la revalida y actualiza el resumen', async () => {
      stores.useSongEntitiesStore.getState().setSongs([song('1', { title: 'Vieja' })]);
      service.getAllById.mockResolvedValue([song('1', { title: 'Nueva' })]);
      await expect(repo.getById('1')).resolves.toMatchObject({ title: 'Vieja' });
      await flush();
      expect(stores.useSongEntitiesStore.getState().songs['1'].title).toBe('Nueva');
      expect(stores.useItemEntitiesStore.getState().items['1']).toMatchObject({ type: 'Song', title: 'Nueva', artist: 'A' });
    });

    it('lee de Disco y la sube a RAM', async () => {
      await storage.setItem('@song:2', JSON.stringify(song('2')));
      await expect(repo.getById('2')).resolves.toEqual(song('2'));
      expect(stores.useSongEntitiesStore.getState().songs['2']).toEqual(song('2'));
      expect(service.getById).not.toHaveBeenCalled();
    });

    it('pide a la API si no hay caché y la guarda en Disco', async () => {
      service.getById.mockResolvedValue(song('3'));
      await expect(repo.getById('3')).resolves.toEqual(song('3'));
      expect(service.getById).toHaveBeenCalledWith('3');
      expect(await readDisk('@song:3')).toEqual(song('3'));
    });

    it('sigue devolviendo la canción aunque falle el guardado en Disco', async () => {
      service.getById.mockResolvedValue(song('3'));
      storage.multiSet.mockRejectedValueOnce(new Error('disco lleno'));
      await expect(repo.getById('3')).resolves.toEqual(song('3'));
    });

    it('devuelve null con un 404 del backend', async () => {
      service.getById.mockRejectedValue(httpError(404));
      await expect(repo.getById('4')).resolves.toBeNull();
      expect(console.warn).toHaveBeenCalled();
    });

    it('devuelve null con otros errores de la API', async () => {
      service.getById.mockRejectedValue(httpError(500));
      await expect(repo.getById('4')).resolves.toBeNull();
      expect(console.error).toHaveBeenCalled();
    });

    it('devuelve null si la API no devuelve nada', async () => {
      service.getById.mockResolvedValue(null as any);
      await expect(repo.getById('4')).resolves.toBeNull();
    });

    it('carga las lecciones de la canción y sus tablaturas y acordes', async () => {
      service.getById.mockResolvedValue(song('5', { lessonIds: ['sl1', 'sl2'] }));
      songLessonService.getAllById.mockResolvedValue([
        { id: 'sl1', tabSectionsIds: ['t1'], chordIds: ['c1'] },
        { id: 'sl2', tabSectionsIds: ['t2'] },
      ] as any);
      tabService.getAllById.mockResolvedValue([{ id: 't1' }, { id: 't2' }] as any);
      chordService.getAllById.mockResolvedValue([{ id: 'c1' }] as any);

      await expect(repo.getById('5')).resolves.toMatchObject({ id: '5' });
      expect(songLessonService.getAllById).toHaveBeenCalledWith(['sl1', 'sl2']);
      expect(tabService.getAllById).toHaveBeenCalledWith(['t1', 't2']);
      expect(chordService.getAllById).toHaveBeenCalledWith(['c1']);
      expect(Object.keys(stores.useTabSectionEntitiesStore.getState().tabSections)).toEqual(['t1', 't2']);
      expect(Object.keys(stores.useChordEntitiesStore.getState().chords)).toEqual(['c1']);
    });

    it('no pide tablaturas ni acordes si las lecciones no tienen', async () => {
      service.getById.mockResolvedValue(song('5', { lessonIds: ['sl1'] }));
      songLessonService.getAllById.mockResolvedValue([{ id: 'sl1' }] as any);
      await repo.getById('5');
      expect(tabService.getAllById).not.toHaveBeenCalled();
      expect(chordService.getAllById).not.toHaveBeenCalled();
    });

    it('si fallan las dependencias devuelve igualmente la canción', async () => {
      stores.useSongEntitiesStore.getState().setSongs([song('1', { lessonIds: ['sl1'] })]);
      storage.multiGet.mockRejectedValueOnce(new Error('disco roto'));
      await expect(repo.getById('1')).resolves.toMatchObject({ id: '1' });
      expect(console.error).toHaveBeenCalled();
    });

    it('no falla si la canción no trae lessonIds', async () => {
      service.getById.mockResolvedValue(song('6', { lessonIds: undefined }));
      await expect(repo.getById('6')).resolves.toMatchObject({ id: '6' });
      expect(songLessonService.getAllById).not.toHaveBeenCalled();
    });
  });

  describe('getAllById / hydrate', () => {
    it('getAllById pide a la API lo que falta', async () => {
      service.getAllById.mockResolvedValueOnce([song('1')]);
      await expect(repo.getAllById(['1'])).resolves.toEqual([song('1')]);
    });

    it('hydrate guarda la canción y su resumen', () => {
      expect(repo.hydrate([song('1')])).toEqual(['1']);
      expect(stores.useSongEntitiesStore.getState().songs['1']).toEqual(song('1'));
      expect(stores.useItemEntitiesStore.getState().items['1']).toMatchObject({ type: 'Song' });
    });
  });

  describe('carruseles con DTO completo', () => {
    it.each([
      ['getPopularByGenreId', () => repo.getPopularByGenreId('g1'), 'getPopularByGenreId', ['g1'], 'popular_genre_g1'],
      ['getClassicByGenreId', () => repo.getClassicByGenreId('g1'), 'getClassicByGenreId', ['g1'], 'classic_genre_g1'],
      ['getNewByGenreId', () => repo.getNewByGenreId('g1'), 'getNewByGenreId', ['g1'], 'new_genre_g1'],
      ['getPopular', () => repo.getPopular(), 'getPopular', [], 'popular'],
      ['getNew', () => repo.getNew(), 'getNew', [], 'new'],
      ['getFromSpotify', () => repo.getFromSpotify('u1'), 'getRecommendedFromSpotify', ['u1', 10], 'spotify'],
      ['getRecommended', () => repo.getRecommended('u1'), 'getRecommendedByUser', ['u1', 10], 'recommended'],
    ] as const)('%s hidrata las canciones y guarda su índice', async (_n, call, serviceFn, args, key) => {
      (service[serviceFn] as jest.Mock).mockResolvedValue([song('1'), song('2')]);
      await expect(call()).resolves.toEqual(['1', '2']);
      expect(service[serviceFn]).toHaveBeenCalledWith(...args);
      expect(stores.useSongEntitiesStore.getState().songs['2']).toEqual(song('2'));
      await flush();
      expect(await readDisk(`@song_index:${key}`)).toEqual(['1', '2']);
    });

    it('exporta las claves de los índices personalizados', () => {
      expect(repo.SPOTIFY_INDEX_KEY).toBe('spotify');
      expect(repo.RECOMMENDED_INDEX_KEY).toBe('recommended');
    });

    it('con índice guardado devuelve la caché y avisa con onFresh', async () => {
      await storage.setItem('@song:1', JSON.stringify(song('1')));
      await storage.setItem('@song_index:popular', JSON.stringify(['1']));
      service.getPopular.mockResolvedValue([song('2')]);
      const onFresh = jest.fn();
      await expect(repo.getPopular(onFresh)).resolves.toEqual(['1']);
      await flush();
      expect(onFresh).toHaveBeenCalledWith(['2']);
    });
  });

  describe('getByFavouriteArtists', () => {
    it('pide los 5 artistas favoritos una sola vez y 12 canciones nuevas y populares suyas', async () => {
      userService.getTopArtists.mockResolvedValue(['Queen', 'Bowie']);
      service.getNewByArtists.mockResolvedValue([song('1'), song('2')]);
      service.getPopularByArtists.mockResolvedValue([song('3')]);

      await expect(repo.getByFavouriteArtists('u1')).resolves.toEqual({ newSongs: ['1', '2'], popularSongs: ['3'] });

      expect(userService.getTopArtists).toHaveBeenCalledTimes(1);
      expect(userService.getTopArtists).toHaveBeenCalledWith('u1', 5);
      expect(service.getNewByArtists).toHaveBeenCalledWith(['Queen', 'Bowie'], 12);
      expect(service.getPopularByArtists).toHaveBeenCalledWith(['Queen', 'Bowie'], 12);
      // Los DTO completos se guardan en la Store (y su resumen en la de items, para las tarjetas)
      expect(stores.useSongEntitiesStore.getState().songs['3']).toBeDefined();
      expect(stores.useItemEntitiesStore.getState().items['1']).toBeDefined();
      await flush();
      expect(await readDisk('@song_index:new_favourite_artists')).toEqual(['1', '2']);
      expect(await readDisk('@song_index:popular_favourite_artists')).toEqual(['3']);
    });

    it('sin artistas favoritos no pide canciones y devuelve listas vacías', async () => {
      userService.getTopArtists.mockResolvedValue([]);

      await expect(repo.getByFavouriteArtists('u1')).resolves.toEqual({ newSongs: [], popularSongs: [] });
      expect(service.getNewByArtists).not.toHaveBeenCalled();
      expect(service.getPopularByArtists).not.toHaveBeenCalled();
    });

    it('con índice guardado devuelve la caché al instante y avisa a cada carrusel con lo fresco', async () => {
      await storage.multiSet([
        ['@song:1', JSON.stringify(song('1'))],
        ['@song:2', JSON.stringify(song('2'))],
        ['@song_index:new_favourite_artists', JSON.stringify(['1'])],
        ['@song_index:popular_favourite_artists', JSON.stringify(['2'])],
      ]);
      userService.getTopArtists.mockResolvedValue(['Queen']);
      service.getNewByArtists.mockResolvedValue([song('4')]);
      service.getPopularByArtists.mockResolvedValue([song('5')]);
      const newSongs = jest.fn();
      const popularSongs = jest.fn();

      await expect(repo.getByFavouriteArtists('u1', { newSongs, popularSongs }))
        .resolves.toEqual({ newSongs: ['1'], popularSongs: ['2'] });
      await flush();
      expect(newSongs).toHaveBeenCalledWith(['4']);
      expect(popularSongs).toHaveBeenCalledWith(['5']);
    });

    it('sin caché y sin red devuelve listas vacías', async () => {
      userService.getTopArtists.mockRejectedValue(new Error('offline'));
      await expect(repo.getByFavouriteArtists('u1')).resolves.toEqual({ newSongs: [], popularSongs: [] });
    });
  });
});
