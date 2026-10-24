import AsyncStorage from '@react-native-async-storage/async-storage';
import { getData, removeData, storeData } from '../../local/storage.local';

describe('storage.local', () => {
  beforeEach(async () => {
    await AsyncStorage.clear();
  });

  it('storeData guarda el valor serializado en JSON', async () => {
    await storeData('clave', { a: 1, b: ['x'] });
    expect(await AsyncStorage.getItem('clave')).toBe('{"a":1,"b":["x"]}');
  });

  it('getData devuelve el valor deserializado', async () => {
    await storeData('clave', [1, 2, 3]);
    expect(await getData<number[]>('clave')).toEqual([1, 2, 3]);
  });

  it('getData devuelve null si la clave no existe', async () => {
    expect(await getData('no-existe')).toBeNull();
  });

  it('getData devuelve null si lo guardado es una cadena vacía', async () => {
    await AsyncStorage.setItem('vacio', '');
    expect(await getData('vacio')).toBeNull();
  });

  it('getData propaga el error si el JSON está corrupto', async () => {
    await AsyncStorage.setItem('roto', '{no-json');
    await expect(getData('roto')).rejects.toThrow(SyntaxError);
  });

  it('removeData borra la clave', async () => {
    await storeData('clave', 'valor');
    await removeData('clave');
    expect(await getData('clave')).toBeNull();
  });
});
