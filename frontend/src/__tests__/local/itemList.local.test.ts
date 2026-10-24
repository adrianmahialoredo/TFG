import AsyncStorage from '@react-native-async-storage/async-storage';
import * as itemListLocal from '../../local/itemList.local';

const list = (id: string | number, userId: string) => ({ id, userId, name: `lista ${id}`, itemIds: [] }) as any;

describe('itemList.local', () => {
  beforeEach(async () => {
    await AsyncStorage.clear();
  });

  describe('saveBatchWithUserIndex / getByUserId', () => {
    it('guarda las listas y las agrupa en el índice de cada usuario', async () => {
      await itemListLocal.saveBatchWithUserIndex([list(1, 'u1'), list(2, 'u2'), list(3, 'u1')]);

      expect(await AsyncStorage.getItem('@itemList_user:u1')).toBe('["1","3"]');
      expect(await AsyncStorage.getItem('@itemList_user:u2')).toBe('["2"]');
      expect(await itemListLocal.getByUserId('u1')).toEqual([list(1, 'u1'), list(3, 'u1')]);
    });

    it('combina con el índice existente sin duplicar', async () => {
      await itemListLocal.saveBatchWithUserIndex([list(1, 'u1')]);
      await itemListLocal.saveBatchWithUserIndex([list(1, 'u1'), list(2, 'u1')]);
      expect(await AsyncStorage.getItem('@itemList_user:u1')).toBe('["1","2"]');
    });

    it('con lista vacía no escribe nada', async () => {
      await itemListLocal.saveBatchWithUserIndex([]);
      expect(await AsyncStorage.getAllKeys()).toEqual([]);
    });

    it('registra el error si falla la actualización de índices', async () => {
      jest.spyOn(AsyncStorage, 'getItem').mockRejectedValueOnce(new Error('disco'));
      await expect(itemListLocal.saveBatchWithUserIndex([list(1, 'u1')])).resolves.toBeUndefined();
      expect(console.error).toHaveBeenCalled();
    });

    it('getByUserId devuelve [] si el usuario no tiene índice o está vacío', async () => {
      expect(await itemListLocal.getByUserId('nadie')).toEqual([]);
      await AsyncStorage.setItem('@itemList_user:vacio', '[]');
      expect(await itemListLocal.getByUserId('vacio')).toEqual([]);
    });

    it('getByUserId omite las listas del índice que ya no están en Disco', async () => {
      await AsyncStorage.setItem('@itemList_user:u1', '["1","2"]');
      await itemListLocal.saveSingle(list(2, 'u1'));
      expect(await itemListLocal.getByUserId('u1')).toEqual([list(2, 'u1')]);
    });

    it('getByUserId devuelve [] y registra el error si el índice está corrupto', async () => {
      await AsyncStorage.setItem('@itemList_user:u1', '{');
      expect(await itemListLocal.getByUserId('u1')).toEqual([]);
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('removeById', () => {
    it('borra la lista y la quita del índice de su usuario', async () => {
      await itemListLocal.saveBatchWithUserIndex([list(1, 'u1'), list(2, 'u1')]);
      await itemListLocal.removeById('1', 'u1');

      expect(await itemListLocal.getById('1')).toBeNull();
      expect(await AsyncStorage.getItem('@itemList_user:u1')).toBe('["2"]');
    });

    it('no crea índice si el usuario no lo tenía', async () => {
      await itemListLocal.saveSingle(list(1, 'u1'));
      await itemListLocal.removeById('1', 'u1');
      expect(await AsyncStorage.getItem('@itemList_user:u1')).toBeNull();
    });

    it('registra el error sin propagarlo', async () => {
      jest.spyOn(AsyncStorage, 'removeItem').mockRejectedValueOnce(new Error('disco'));
      await expect(itemListLocal.removeById('1', 'u1')).resolves.toBeUndefined();
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('clearAll', () => {
    it('borra listas, índices e índices por usuario y conserva lo demás', async () => {
      await itemListLocal.saveBatchWithUserIndex([list(1, 'u1')]);
      await itemListLocal.saveQueryIndex('mias', ['1']);
      await AsyncStorage.setItem('@song:1', '{}');

      await itemListLocal.clearAll();

      expect(await AsyncStorage.getAllKeys()).toEqual(['@song:1']);
    });

    it('no llama a multiRemove si no hay claves', async () => {
      (AsyncStorage.multiRemove as jest.Mock).mockClear();
      await itemListLocal.clearAll();
      expect(AsyncStorage.multiRemove).not.toHaveBeenCalled();
    });

    it('registra el error sin propagarlo', async () => {
      jest.spyOn(AsyncStorage, 'getAllKeys').mockRejectedValueOnce(new Error('disco'));
      await expect(itemListLocal.clearAll()).resolves.toBeUndefined();
      expect(console.error).toHaveBeenCalled();
    });
  });
});
