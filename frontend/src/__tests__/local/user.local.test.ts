import AsyncStorage from '@react-native-async-storage/async-storage';
import * as userLocal from '../../local/user.local';

describe('user.local clearAll', () => {
  beforeEach(async () => {
    await AsyncStorage.clear();
  });

  it('borra usuarios e índices de usuarios y conserva el resto', async () => {
    await userLocal.saveBatch([{ id: 1 } as any, { id: 2 } as any]);
    await userLocal.saveQueryIndex('perfil', ['1']);
    await AsyncStorage.setItem('@genre:1', '{}');

    await userLocal.clearAll();

    expect(await AsyncStorage.getAllKeys()).toEqual(['@genre:1']);
    expect(await userLocal.getById('1')).toBeNull();
  });

  it('no llama a multiRemove si no hay claves de usuario', async () => {
    (AsyncStorage.multiRemove as jest.Mock).mockClear();
    await userLocal.clearAll();
    expect(AsyncStorage.multiRemove).not.toHaveBeenCalled();
  });

  it('registra el error sin propagarlo', async () => {
    jest.spyOn(AsyncStorage, 'getAllKeys').mockRejectedValueOnce(new Error('disco'));
    await expect(userLocal.clearAll()).resolves.toBeUndefined();
    expect(console.error).toHaveBeenCalled();
  });
});
