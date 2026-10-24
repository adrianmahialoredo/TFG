/* Mocks globales para los tests (jest-expo ya mockea los módulos nativos de Expo). */
import 'react-native-gesture-handler/jestSetup';

jest.mock('@react-native-async-storage/async-storage', () =>
  require('@react-native-async-storage/async-storage/jest/async-storage-mock')
);

jest.mock('react-native-safe-area-context', () =>
  require('react-native-safe-area-context/jest/mock').default
);

// reanimated 4 carga los worklets nativos: hay que mockearlos antes que reanimated
jest.mock('react-native-worklets', () => require('react-native-worklets/src/mock'));
jest.mock('react-native-reanimated', () => require('react-native-reanimated/mock'));

// El micrófono del afinador es un módulo nativo: se usa el mock que trae la propia librería
jest.mock('react-native-audio-api', () => require('react-native-audio-api/mock'));

// El reproductor de YouTube va sobre un WebView nativo, que no existe en Jest: se sustituye por
// un texto con el ID del vídeo. jsx-runtime en vez de JSX: NativeWind reescribe el JSX de las factorías.
jest.mock('react-native-youtube-iframe', () => {
  const { jsx } = require('react/jsx-runtime');
  const { Text } = require('react-native');
  return ({ videoId }) => jsx(Text, { children: `YouTube ${videoId}` });
});

// Silencia los logs de la app durante los tests (los repositories y services registran mucho)
beforeEach(() => {
  jest.spyOn(console, 'log').mockImplementation(() => {});
  jest.spyOn(console, 'warn').mockImplementation(() => {});
  jest.spyOn(console, 'error').mockImplementation(() => {});
});

afterEach(() => {
  jest.restoreAllMocks();
});
