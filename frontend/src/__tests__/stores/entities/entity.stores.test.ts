/**
 * Comportamiento común de las Entity Stores (diccionario `id -> DTO` con set/update/remove/clear).
 */
import { useChordEntitiesStore } from '../../../stores/entities/chord.entities.store';
import { useGenreEntitiesStore } from '../../../stores/entities/genre.entities.store';
import { useInstrumentEntitiesStore } from '../../../stores/entities/instrument.entities.store';
import { useInstrumentGenreEntitiesStore } from '../../../stores/entities/instrumentGenre.entities.store';
import { useItemListEntitiesStore } from '../../../stores/entities/itemList.entities.store';
import { useLessonEntitiesStore } from '../../../stores/entities/lesson.entities.store';
import { useLessonSectionEntitiesStore } from '../../../stores/entities/lessonSection.entities.store';
import { useSongEntitiesStore } from '../../../stores/entities/song.entities.store';
import { useSongLessonEntitiesStore } from '../../../stores/entities/songLesson.entities.store';
import { useTabSectionEntitiesStore } from '../../../stores/entities/tabSection.entities.store';
import { useTuneEntitiesStore } from '../../../stores/entities/tune.entities.store';
import { useUserEntitiesStore } from '../../../stores/entities/user.entities.store';

type Store = { getState: () => any; setState: (s: any) => void };

/** [nombre, store, clave del diccionario, sufijo de las acciones (plural), singular] */
const stores: [string, Store, string, string, string][] = [
  ['chord', useChordEntitiesStore, 'chords', 'Chords', 'Chord'],
  ['genre', useGenreEntitiesStore, 'genres', 'Genres', 'Genre'],
  ['instrument', useInstrumentEntitiesStore, 'instruments', 'Instruments', 'Instrument'],
  ['instrumentGenre', useInstrumentGenreEntitiesStore, 'instrumentGenres', 'InstrumentGenres', 'InstrumentGenre'],
  ['itemList', useItemListEntitiesStore, 'itemLists', 'ItemLists', 'ItemList'],
  ['lesson', useLessonEntitiesStore, 'lessons', 'Lessons', 'Lesson'],
  ['lessonSection', useLessonSectionEntitiesStore, 'lessonSections', 'LessonSections', 'LessonSection'],
  ['song', useSongEntitiesStore, 'songs', 'Songs', 'Song'],
  ['songLesson', useSongLessonEntitiesStore, 'songLessons', 'SongLessons', 'SongLesson'],
  ['tabSection', useTabSectionEntitiesStore, 'tabSections', 'TabSections', 'TabSection'],
  ['tune', useTuneEntitiesStore, 'tunes', 'Tunes', 'Tune'],
  ['user', useUserEntitiesStore, 'users', 'Users', 'User'],
];

describe.each(stores)('%s.entities.store', (_name, store, key, plural, singular) => {
  const state = () => store.getState();
  const entities = () => state()[key] as Record<string, any>;
  const set = (list: any[]) => state()[`set${plural}`](list);
  const update = (id: string, data: any) => state()[`update${singular}`](id, data);
  const remove = (ids: string[]) => state()[`remove${plural}`](ids);
  const clear = () => state()[`clear${plural}`]();

  beforeEach(() => {
    clear();
  });

  it('empieza vacía', () => {
    expect(entities()).toEqual({});
  });

  it('set guarda las entidades indexadas por su id como string', () => {
    set([{ id: 1, title: 'a' }, { id: '2', title: 'b' }]);
    expect(entities()).toEqual({ '1': { id: 1, title: 'a' }, '2': { id: '2', title: 'b' } });
  });

  it('set sobrescribe las existentes y conserva las demás', () => {
    set([{ id: '1', title: 'a' }, { id: '2', title: 'b' }]);
    set([{ id: '1', title: 'nuevo' }]);
    expect(entities()['1']).toEqual({ id: '1', title: 'nuevo' });
    expect(entities()['2']).toEqual({ id: '2', title: 'b' });
  });

  it('set crea un diccionario nuevo (inmutable)', () => {
    set([{ id: '1' }]);
    const before = entities();
    set([{ id: '2' }]);
    expect(entities()).not.toBe(before);
    expect(before).toEqual({ '1': { id: '1' } });
  });

  it('update fusiona los datos parciales con la entidad existente', () => {
    set([{ id: '1', title: 'a', level: 1 }]);
    update('1', { title: 'b' });
    expect(entities()['1']).toEqual({ id: '1', title: 'b', level: 1 });
  });

  it('update no hace nada si la entidad no existe', () => {
    set([{ id: '1' }]);
    const before = entities();
    update('99', { title: 'x' });
    expect(entities()).toBe(before);
    expect(entities()['99']).toBeUndefined();
  });

  it('remove borra solo los ids indicados e ignora los inexistentes', () => {
    set([{ id: '1' }, { id: '2' }, { id: '3' }]);
    remove(['1', '3', '404']);
    expect(Object.keys(entities())).toEqual(['2']);
  });

  it('clear vacía la store', () => {
    set([{ id: '1' }]);
    clear();
    expect(entities()).toEqual({});
  });
});
