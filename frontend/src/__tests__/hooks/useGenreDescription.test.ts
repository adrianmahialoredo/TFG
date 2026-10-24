import { renderHook } from '@testing-library/react-native';
import { useGenreDescription } from '../../hooks/useGenreDescription';
import { useGenreEntitiesStore } from '../../stores/entities';

describe('useGenreDescription', () => {
  beforeEach(() => {
    useGenreEntitiesStore.setState({
      genres: {
        g1: {
          id: 'g1',
          description: 'El rock',
          bannerUrl: 'banner.png',
          welcomeIntroVideoId: 'vid1',
          welcomeSongVideoId: 'vid2',
        } as any,
      },
    });
  });

  it('devuelve los datos del género de la Store', async () => {
    const { result } = await renderHook(() => useGenreDescription('g1'));
    expect(result.current).toEqual({
      description: 'El rock',
      bannerUrl: 'banner.png',
      welcomeVideoId: 'vid1',
      welcomeSongId: 'vid2',
      isValid: true,
    });
  });

  it('devuelve valores vacíos si el género no existe', async () => {
    const { result } = await renderHook(() => useGenreDescription('nope'));
    expect(result.current).toEqual({
      description: '',
      bannerUrl: '',
      welcomeVideoId: '',
      welcomeSongId: '',
      isValid: false,
    });
  });
});
