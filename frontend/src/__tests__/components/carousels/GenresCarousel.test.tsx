import { render, screen, fireEvent } from '@testing-library/react-native';
import GenresCarousel from '@/components/carousels/GenresCarousel';

jest.setTimeout(30000);

describe('GenresCarousel', () => {
  it('muestra una imagen por género y llama a su onPress', async () => {
    const onRock = jest.fn();
    const onJazz = jest.fn();
    await render(
      <GenresCarousel
        data={[
          { id: 'rock', imageUrl: 'http://img/rock.png', onPress: onRock },
          { id: 'jazz', imageUrl: 'http://img/jazz.png', onPress: onJazz },
        ]}
      />
    );
    const images = screen.container.queryAll((n) => n.type === 'Image');
    expect(images.map((i) => i.props.source.uri)).toEqual(['http://img/rock.png', 'http://img/jazz.png']);
    await fireEvent.press(images[1]);
    expect(onJazz).toHaveBeenCalledTimes(1);
    expect(onRock).not.toHaveBeenCalled();
  });

  it('con la lista vacía no muestra imágenes', async () => {
    await render(<GenresCarousel data={[]} />);
    expect(screen.container.queryAll((n) => n.type === 'Image')).toHaveLength(0);
  });
});
