import AsyncStorage from '@react-native-async-storage/async-storage';
import * as songLocal from '../../local/song.local';
import * as lessonLocal from '../../local/lesson.local';

// song.local y lesson.local comparten removeQueryIndexes (borrado de índices personalizados en el logout)
describe.each([
  ['song.local', 'song', songLocal],
  ['lesson.local', 'lesson', lessonLocal],
])('%s removeQueryIndexes', (_name, prefix, local) => {
  beforeEach(async () => {
    await AsyncStorage.clear();
  });

  it('borra solo los índices indicados', async () => {
    await local.saveQueryIndex('recomendadas-u1', ['1']);
    await local.saveQueryIndex('populares', ['2']);
    await local.saveBatch([{ id: 1 } as any]);

    await local.removeQueryIndexes(['recomendadas-u1']);

    expect(await local.getQueryIndex('recomendadas-u1')).toBeNull();
    expect(await local.getQueryIndex('populares')).toEqual(['2']);
    expect(await AsyncStorage.getItem(`@${prefix}:1`)).not.toBeNull();
  });

  it('registra el error sin propagarlo', async () => {
    jest.spyOn(AsyncStorage, 'multiRemove').mockRejectedValueOnce(new Error('disco'));
    await expect(local.removeQueryIndexes(['x'])).resolves.toBeUndefined();
    expect(console.error).toHaveBeenCalled();
  });
});
