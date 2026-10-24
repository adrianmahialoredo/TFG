import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react-native';
import GenresScreen from '../../screens/GenresScreen';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

const mockNavigation = { navigate: jest.fn() };
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => mockNavigation,
}));

let mockGenresState: any;
const mockUseGenres = jest.fn();
jest.mock('../../hooks/useGenres', () => ({
  useGenres: (q: string) => {
    mockUseGenres(q);
    return mockGenresState;
  },
}));

jest.mock('../../components/ProfileHeader', () => {
  const { Text } = require('react-native');
  return ({ title }: any) => <Text>{title}</Text>;
});

jest.mock('../../components/carousels/GenresCarousel', () => {
  const { View, Text } = require('react-native');
  return ({ data }: any) => (
    <View>
      {data.map((d: any) => (
        <Text key={d.id} onPress={d.onPress}>{`Género ${d.id}`}</Text>
      ))}
    </View>
  );
});

describe('GenresScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockGenresState = {
      genres: [
        { id: 'g1', title: 'Rock', imageUrl: 'a' },
        { id: 'g2', title: 'Jazz', imageUrl: 'b' },
      ],
      loading: false,
    };
  });

  it('muestra los géneros', async () => {
    await render(<GenresScreen />);
    expect(screen.getByText('Géneros')).toBeTruthy();
    expect(screen.getByText('Género g1')).toBeTruthy();
    expect(screen.getByText('Género g2')).toBeTruthy();
  });

  it('navega al género pulsado', async () => {
    await render(<GenresScreen />);
    await fireEvent.press(screen.getByText('Género g2'));
    expect(mockNavigation.navigate).toHaveBeenCalledWith('GenreMainScreen', { genreId: 'g2' });
  });

  it('no muestra los géneros mientras carga', async () => {
    mockGenresState.loading = true;
    await render(<GenresScreen />);
    expect(screen.queryByText(/Género g/)).toBeNull();
  });

  it('pasa la búsqueda al hook', async () => {
    await render(<GenresScreen />);
    expect(mockUseGenres).toHaveBeenLastCalledWith('');
    await fireEvent.changeText(screen.getByPlaceholderText('¿Que quieres tocar?'), 'ro');
    expect(mockUseGenres).toHaveBeenLastCalledWith('ro');
  });
});
