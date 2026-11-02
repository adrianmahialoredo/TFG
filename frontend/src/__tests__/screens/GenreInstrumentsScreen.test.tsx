import React from 'react';
import { Text } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import GenreInstrumentsScreen from '../../screens/GenreInstrumentsScreen';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

let mockState: any;
const mockUseGenreInstruments = jest.fn();
jest.mock('../../hooks/useGenreInstruments', () => ({
  useGenreInstruments: (id: string) => {
    mockUseGenreInstruments(id);
    return mockState;
  },
}));

jest.mock('../../components/buttons/ToolButton', () => {
  const { Text } = require('react-native');
  return { ToolButton: ({ label, onPress }: any) => <Text onPress={onPress}>{label}</Text> };
});
jest.mock('../../components/InstrumentIcon', () => () => null);
jest.mock('../../components/carousels/GenreLessonsCarousel', () => {
  const { Text } = require('react-native');
  return ({ title, genreId }: any) => <Text>{`Carrusel ${title} ${genreId}`}</Text>;
});
jest.mock('../../screens/InstrumentGenreLessonScreen', () => {
  const { Text } = require('react-native');
  return ({ instrumentGenreId, instrumentName, onBack }: any) => (
    <Text onPress={onBack}>{`Lecciones ${instrumentGenreId} ${instrumentName}`}</Text>
  );
});

describe('GenreInstrumentsScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      instrumentGenres: [
        { id: 'ig1', title: 'Guitarra' },
        { id: 'ig2', title: 'Batería' },
        { id: 'ig3', title: null },
      ],
      selectedInstrumentGenre: null,
      selectInstrument: jest.fn(),
      unselectInstrument: jest.fn(),
    };
  });

  it('muestra un botón por instrumento y el carrusel de recomendaciones', async () => {
    await render(<GenreInstrumentsScreen genreId="g1" />);
    expect(mockUseGenreInstruments).toHaveBeenCalledWith('g1');
    expect(screen.getByText('Guitarra')).toBeTruthy();
    expect(screen.getByText('Batería')).toBeTruthy();
    // Sin título se usa un texto por defecto
    expect(screen.getByText('S/N')).toBeTruthy();
    expect(screen.getByText('Carrusel Recomendaciones g1')).toBeTruthy();
  });

  it('selecciona el instrumento pulsado', async () => {
    await render(<GenreInstrumentsScreen genreId="g1" />);
    await fireEvent.press(screen.getByText('Batería'));
    expect(mockState.selectInstrument).toHaveBeenCalledWith('ig2');
  });

  it('muestra un aviso si el género no tiene instrumentos', async () => {
    mockState.instrumentGenres = [];
    await render(<GenreInstrumentsScreen genreId="g1" />);
    expect(screen.getByText('No hay instrumentos configurados para este género.')).toBeTruthy();
  });

  it('con un instrumento seleccionado muestra sus lecciones y permite volver', async () => {
    mockState.selectedInstrumentGenre = { id: 'ig1', title: 'Guitarra' };
    await render(<GenreInstrumentsScreen genreId="g1" />);
    expect(screen.queryByText('Carrusel Recomendaciones g1')).toBeNull();
    await fireEvent.press(screen.getByText('Lecciones ig1 Guitarra'));
    expect(mockState.unselectInstrument).toHaveBeenCalled();
  });

  it('usa "Instrumento" como nombre si el seleccionado no tiene título', async () => {
    mockState.selectedInstrumentGenre = { id: 'ig3', title: null };
    await render(<GenreInstrumentsScreen genreId="g1" />);
    expect(screen.getByText('Lecciones ig3 Instrumento')).toBeTruthy();
  });

  it('pinta la cabecera dentro del scroll, encima de los instrumentos', async () => {
    await render(<GenreInstrumentsScreen genreId="g1" header={<Text>Rock</Text>} />);
    expect(screen.getByText('Rock')).toBeTruthy();
  });
});
