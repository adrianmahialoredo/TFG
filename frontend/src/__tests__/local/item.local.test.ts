import AsyncStorage from '@react-native-async-storage/async-storage';
import * as itemLocal from '../../local/item.local';

const item = (id: string) => ({ id, title: `t${id}`, imageUrl: '', type: 'Song' }) as any;

describe('item.local', () => {
  beforeEach(async () => {
    await AsyncStorage.clear();
  });

  it('saveBatch guarda los resúmenes y getAllById los recupera', async () => {
    await itemLocal.saveBatch([item('1'), item('2')]);
    expect(await AsyncStorage.getItem('@item:1')).toBe(JSON.stringify(item('1')));
    expect(await itemLocal.getAllById(['2', '3'])).toEqual({ cached: [item('2')], missing: ['3'] });
  });

  it('saveBatch propaga los errores de Disco (a diferencia del resto de locales)', async () => {
    jest.spyOn(AsyncStorage, 'multiSet').mockRejectedValueOnce(new Error('disco'));
    await expect(itemLocal.saveBatch([item('1')])).rejects.toThrow('disco');
  });

  it('getAllById marca como faltantes las entradas corruptas', async () => {
    await AsyncStorage.setItem('@item:9', 'x{');
    expect(await itemLocal.getAllById(['9'])).toEqual({ cached: [], missing: ['9'] });
  });

  it('getAllById con lista vacía devuelve listas vacías', async () => {
    expect(await itemLocal.getAllById([])).toEqual({ cached: [], missing: [] });
  });

  it('guarda y lee índices', async () => {
    await itemLocal.saveQueryIndex('recientes', ['1']);
    expect(await AsyncStorage.getItem('@item_index:recientes')).toBe('["1"]');
    expect(await itemLocal.getQueryIndex('recientes')).toEqual(['1']);
    expect(await itemLocal.getQueryIndex('otro')).toBeNull();
  });

  it('registra los errores de los índices sin propagarlos', async () => {
    jest.spyOn(AsyncStorage, 'setItem').mockRejectedValueOnce(new Error('disco'));
    await itemLocal.saveQueryIndex('k', []);
    await AsyncStorage.setItem('@item_index:roto', '{');
    expect(await itemLocal.getQueryIndex('roto')).toBeNull();
    expect(console.error).toHaveBeenCalledTimes(2);
  });

  describe('clearQueryIndexes', () => {
    beforeEach(async () => {
      await itemLocal.saveBatch([item('1')]);
      await itemLocal.saveQueryIndex('catalogo', ['1']);
      await itemLocal.saveQueryIndex('recientes', ['1']);
      await itemLocal.saveQueryIndex('recomendados', ['1']);
      await AsyncStorage.setItem('@song_index:x', '[]');
    });

    it('borra todos los índices de items y conserva las entidades y otras claves', async () => {
      await itemLocal.clearQueryIndexes();
      const keys = await AsyncStorage.getAllKeys();
      expect([...keys].sort()).toEqual(['@item:1', '@song_index:x']);
    });

    it('conserva los índices indicados en keep', async () => {
      await itemLocal.clearQueryIndexes(['catalogo']);
      expect(await itemLocal.getQueryIndex('catalogo')).toEqual(['1']);
      expect(await itemLocal.getQueryIndex('recientes')).toBeNull();
    });

    it('no llama a multiRemove si no hay nada que borrar', async () => {
      await AsyncStorage.clear();
      (AsyncStorage.multiRemove as jest.Mock).mockClear();
      await itemLocal.clearQueryIndexes();
      expect(AsyncStorage.multiRemove).not.toHaveBeenCalled();
    });

    it('registra el error sin propagarlo', async () => {
      jest.spyOn(AsyncStorage, 'getAllKeys').mockRejectedValueOnce(new Error('disco'));
      await expect(itemLocal.clearQueryIndexes()).resolves.toBeUndefined();
      expect(console.error).toHaveBeenCalled();
    });
  });
});
