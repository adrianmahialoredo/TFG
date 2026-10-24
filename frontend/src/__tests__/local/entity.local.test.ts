/**
 * Comportamiento común de los módulos *.local.ts de entidades (todos siguen el mismo patrón:
 * saveSingle / getById / saveBatch / getAllById / saveQueryIndex / getQueryIndex).
 * Las funciones propias de cada módulo se prueban en su fichero (song, lesson, itemList, user...).
 */
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as chordLocal from '../../local/chord.local';
import * as genreLocal from '../../local/genre.local';
import * as instrumentLocal from '../../local/instrument.local';
import * as instrumentGenreLocal from '../../local/instrumentGenre.local';
import * as itemListLocal from '../../local/itemList.local';
import * as lessonLocal from '../../local/lesson.local';
import * as lessonSectionLocal from '../../local/lessonSection.local';
import * as songLocal from '../../local/song.local';
import * as songLessonLocal from '../../local/songLesson.local';
import * as tabSectionLocal from '../../local/tabSection.local';
import * as tuneLocal from '../../local/tune.local';
import * as userLocal from '../../local/user.local';

type EntityLocal = {
  saveSingle: (e: any) => Promise<void>;
  getById: (id: string) => Promise<any>;
  saveBatch: (e: any[]) => Promise<void>;
  getAllById: (ids: string[]) => Promise<{ cached: any[]; missing: string[] }>;
  saveQueryIndex: (key: string, ids: string[]) => Promise<void>;
  getQueryIndex: (key: string) => Promise<string[] | null>;
};

const modules: [string, string, EntityLocal][] = [
  ['chord', 'chord', chordLocal],
  ['genre', 'genre', genreLocal],
  ['instrument', 'instrument', instrumentLocal],
  ['instrumentGenre', 'instrumentGenre', instrumentGenreLocal],
  ['itemList', 'itemList', itemListLocal],
  ['lesson', 'lesson', lessonLocal],
  ['lessonSection', 'lessonSection', lessonSectionLocal],
  ['song', 'song', songLocal],
  ['songLesson', 'songLesson', songLessonLocal],
  ['tabSection', 'tabSection', tabSectionLocal],
  ['tune', 'tune', tuneLocal],
  ['user', 'user', userLocal],
];

describe.each(modules)('%s.local', (_name, prefix, local) => {
  beforeEach(async () => {
    await AsyncStorage.clear();
  });

  it(`saveSingle guarda bajo la clave @${prefix}:<id> y getById la recupera`, async () => {
    await local.saveSingle({ id: 1, title: 'uno' });
    expect(await AsyncStorage.getItem(`@${prefix}:1`)).toBe('{"id":1,"title":"uno"}');
    expect(await local.getById('1')).toEqual({ id: 1, title: 'uno' });
  });

  it('getById devuelve null si no existe', async () => {
    expect(await local.getById('404')).toBeNull();
  });

  it('saveBatch guarda todas las entidades de golpe', async () => {
    await local.saveBatch([{ id: 1 }, { id: 2 }]);
    expect(AsyncStorage.multiSet).toHaveBeenLastCalledWith([
      [`@${prefix}:1`, '{"id":1}'],
      [`@${prefix}:2`, '{"id":2}'],
    ]);
    expect(await local.getById('2')).toEqual({ id: 2 });
  });

  it('saveBatch con lista vacía no escribe nada', async () => {
    (AsyncStorage.multiSet as jest.Mock).mockClear();
    await local.saveBatch([]);
    expect(AsyncStorage.multiSet).not.toHaveBeenCalled();
  });

  it('saveBatch registra el error sin propagarlo', async () => {
    jest.spyOn(AsyncStorage, 'multiSet').mockRejectedValueOnce(new Error('disco lleno'));
    await expect(local.saveBatch([{ id: 1 }])).resolves.toBeUndefined();
    expect(console.error).toHaveBeenCalled();
  });

  it('getAllById separa las entidades en caché de las que faltan', async () => {
    await local.saveBatch([{ id: 1 }, { id: 3 }]);
    expect(await local.getAllById(['1', '2', '3'])).toEqual({
      cached: [{ id: 1 }, { id: 3 }],
      missing: ['2'],
    });
  });

  it('getAllById trata como faltantes las entradas con JSON corrupto', async () => {
    await AsyncStorage.setItem(`@${prefix}:5`, '{roto');
    expect(await local.getAllById(['5'])).toEqual({ cached: [], missing: ['5'] });
  });

  it('getAllById con lista vacía no lee el Disco', async () => {
    (AsyncStorage.multiGet as jest.Mock).mockClear();
    expect(await local.getAllById([])).toEqual({ cached: [], missing: [] });
    expect(AsyncStorage.multiGet).not.toHaveBeenCalled();
  });

  it(`saveQueryIndex y getQueryIndex usan el prefijo @${prefix}_index:`, async () => {
    await local.saveQueryIndex('populares', ['1', '2']);
    expect(await AsyncStorage.getItem(`@${prefix}_index:populares`)).toBe('["1","2"]');
    expect(await local.getQueryIndex('populares')).toEqual(['1', '2']);
  });

  it('getQueryIndex devuelve null si no hay índice', async () => {
    expect(await local.getQueryIndex('nada')).toBeNull();
  });

  it('getQueryIndex devuelve null y registra el error si el índice está corrupto', async () => {
    await AsyncStorage.setItem(`@${prefix}_index:roto`, '[1,');
    expect(await local.getQueryIndex('roto')).toBeNull();
    expect(console.error).toHaveBeenCalled();
  });

  it('saveQueryIndex registra el error sin propagarlo', async () => {
    jest.spyOn(AsyncStorage, 'setItem').mockRejectedValueOnce(new Error('disco'));
    await expect(local.saveQueryIndex('k', ['1'])).resolves.toBeUndefined();
    expect(console.error).toHaveBeenCalled();
  });
});
