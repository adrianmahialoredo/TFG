import { getYoutubeVideoId } from '../../utils/youtube.utils';

const ID = 'dQw4w9WgXcQ';

describe('getYoutubeVideoId', () => {
  it.each([
    [`https://www.youtube.com/watch?v=${ID}`],
    [`https://youtube.com/watch?v=${ID}&t=42s`],
    [`https://m.youtube.com/watch?feature=share&v=${ID}`],
    [`https://youtu.be/${ID}`],
    [`https://youtu.be/${ID}?si=abc`],
    [`https://www.youtube.com/embed/${ID}`],
    [`https://www.youtube.com/shorts/${ID}`],
    [`https://www.youtube.com/live/${ID}`],
  ])('extrae el ID de %s', (url) => {
    expect(getYoutubeVideoId(url)).toBe(ID);
  });

  it('devuelve el ID tal cual si ya es un ID (con espacios alrededor)', () => {
    expect(getYoutubeVideoId(ID)).toBe(ID);
    expect(getYoutubeVideoId(`  ${ID}  `)).toBe(ID);
  });

  it.each([[undefined], [null], [''], ['   ']])('devuelve null sin valor (%p)', (value) => {
    expect(getYoutubeVideoId(value)).toBeNull();
  });

  it('devuelve null si no es un vídeo de YouTube', () => {
    expect(getYoutubeVideoId('https://video/1.mp4')).toBeNull();
    expect(getYoutubeVideoId('https://www.youtube.com/watch?v=corto')).toBeNull();
    expect(getYoutubeVideoId('no es un vídeo')).toBeNull();
  });
});
