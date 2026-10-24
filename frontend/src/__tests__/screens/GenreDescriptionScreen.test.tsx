import React from 'react';
import { render, screen } from '@testing-library/react-native';
import GenreDescriptionScreen from '../../screens/GenreDescriptionScreen';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

let mockState: any;
const mockUseGenreDescription = jest.fn();
jest.mock('../../hooks/useGenreDescription', () => ({
  useGenreDescription: (id: string) => {
    mockUseGenreDescription(id);
    return mockState;
  },
}));

jest.mock('react-native-youtube-iframe', () => {
  const { Text } = require('react-native');
  return ({ videoId }: any) => <Text>{`YouTube ${videoId}`}</Text>;
});

describe('GenreDescriptionScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      description: 'El rock nació en los años 50.',
      bannerUrl: 'https://img/banner.png',
      welcomeVideoId: 'vid123',
      welcomeSongId: 'song456',
      isValid: true,
    };
  });

  it('muestra la descripción y los dos vídeos', async () => {
    await render(<GenreDescriptionScreen genreId="g1" />);
    expect(mockUseGenreDescription).toHaveBeenCalledWith('g1');
    expect(screen.getByText('El rock nació en los años 50.')).toBeTruthy();
    expect(screen.getByText('Canción Emblemática')).toBeTruthy();
    expect(screen.getByText('Introducción al Estilo')).toBeTruthy();
    expect(screen.getByText('YouTube song456')).toBeTruthy();
    expect(screen.getByText('YouTube vid123')).toBeTruthy();
  });

  it('muestra el banner solo si existe', async () => {
    const { rerender } = await render(<GenreDescriptionScreen genreId="g1" />);
    expect(JSON.stringify(screen.toJSON())).toContain('https://img/banner.png');

    mockState = { ...mockState, bannerUrl: null };
    await rerender(<GenreDescriptionScreen genreId="g1" />);
    expect(JSON.stringify(screen.toJSON())).not.toContain('https://img/banner.png');
  });

  it('muestra un mensaje si el género no tiene datos válidos', async () => {
    mockState.isValid = false;
    await render(<GenreDescriptionScreen genreId="g1" />);
    expect(screen.getByText('La historia de este género se está escribiendo...')).toBeTruthy();
    expect(screen.queryByText(/YouTube/)).toBeNull();
  });
});
