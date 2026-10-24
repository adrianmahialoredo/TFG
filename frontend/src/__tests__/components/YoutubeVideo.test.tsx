import { render, screen } from '@testing-library/react-native';
import YoutubeVideo from '@/components/YoutubeVideo';

jest.setTimeout(30000);

describe('YoutubeVideo', () => {
  it('pasa al reproductor el ID extraído de la URL', async () => {
    await render(<YoutubeVideo url="https://www.youtube.com/watch?v=dQw4w9WgXcQ" />);
    expect(screen.getByText('YouTube dQw4w9WgXcQ')).toBeTruthy();
  });

  it('acepta directamente el ID del vídeo', async () => {
    await render(<YoutubeVideo url="dQw4w9WgXcQ" height={120} />);
    expect(screen.getByText('YouTube dQw4w9WgXcQ')).toBeTruthy();
  });

  it('no pinta nada sin URL o con una URL que no es de YouTube', async () => {
    const { rerender } = await render(<YoutubeVideo url={undefined} />);
    expect(screen.toJSON()).toBeNull();

    await rerender(<YoutubeVideo url="https://video/1.mp4" />);
    expect(screen.toJSON()).toBeNull();
  });
});
