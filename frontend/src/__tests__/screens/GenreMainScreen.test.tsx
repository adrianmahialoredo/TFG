import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react-native';
import GenreMainScreen from '../../screens/GenreMainScreen';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useRoute: () => ({ params: { genreId: 'g1' } }),
}));

let mockGenreMainState: any;
const mockUseGenreMain = jest.fn();
jest.mock('../../hooks/useGenreMain', () => ({
  useGenreMain: (id: string) => {
    mockUseGenreMain(id);
    return mockGenreMainState;
  },
}));

jest.mock('../../components/ProfileHeader', () => {
  const { Text } = require('react-native');
  return () => <Text>Cabecera</Text>;
});
jest.mock('../../screens/GenreInstrumentsScreen', () => {
  const { Text } = require('react-native');
  // Pinta la cabecera que recibe (el título del género va dentro de su scroll)
  return ({ genreId, header }: any) => <>{header}<Text>{`Vista instrumentos ${genreId}`}</Text></>;
});
jest.mock('../../screens/GenreSongsScreen', () => {
  const { Text } = require('react-native');
  // Pinta la cabecera que recibe (el título del género va dentro de su scroll)
  return ({ genreId, header }: any) => <>{header}<Text>{`Vista canciones ${genreId}`}</Text></>;
});
jest.mock('../../screens/GenreDescriptionScreen', () => {
  const { Text } = require('react-native');
  // Pinta la cabecera que recibe (el título del género va dentro de su scroll)
  return ({ genreId, header }: any) => <>{header}<Text>{`Vista descripción ${genreId}`}</Text></>;
});

describe('GenreMainScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockGenreMainState = {
      genre: { id: 'g1', title: 'Rock' },
      loading: false,
      error: null,
      selectedTab: 'Instrumentos',
      changeTab: jest.fn(),
    };
  });

  it('pide el género de la ruta y muestra su título y las pestañas', async () => {
    await render(<GenreMainScreen />);
    expect(mockUseGenreMain).toHaveBeenCalledWith('g1');
    expect(screen.getByText('Rock')).toBeTruthy();
    ['Instrumentos', 'Canciones', 'Género'].forEach((t) => expect(screen.getByText(t)).toBeTruthy());
    expect(screen.getByText('Vista instrumentos g1')).toBeTruthy();
  });

  it('muestra la vista de la pestaña seleccionada', async () => {
    mockGenreMainState.selectedTab = 'Canciones';
    const { rerender } = await render(<GenreMainScreen />);
    expect(screen.getByText('Vista canciones g1')).toBeTruthy();
    expect(screen.queryByText(/Vista instrumentos/)).toBeNull();

    mockGenreMainState = { ...mockGenreMainState, selectedTab: 'Género' };
    await rerender(<GenreMainScreen />);
    expect(screen.getByText('Vista descripción g1')).toBeTruthy();
  });

  it('cambia de pestaña al pulsar un botón', async () => {
    await render(<GenreMainScreen />);
    await fireEvent.press(screen.getByText('Canciones'));
    expect(mockGenreMainState.changeTab).toHaveBeenCalledWith('Canciones');
  });

  it('mientras carga sin género no muestra contenido', async () => {
    mockGenreMainState = { ...mockGenreMainState, genre: null, loading: true };
    await render(<GenreMainScreen />);
    expect(screen.queryByText('Instrumentos')).toBeNull();
  });

  it('muestra el error si no hay género', async () => {
    mockGenreMainState = { ...mockGenreMainState, genre: null, error: 'Género no encontrado' };
    await render(<GenreMainScreen />);
    expect(screen.getByText('Género no encontrado')).toBeTruthy();
  });

  it('con género cargado no muestra el error', async () => {
    mockGenreMainState.error = 'Error de sincronización';
    await render(<GenreMainScreen />);
    expect(screen.queryByText('Error de sincronización')).toBeNull();
    expect(screen.getByText('Rock')).toBeTruthy();
  });
});
