import { navigateToItem } from '../../utils/navigation.utils';
import { ItemSummaryDTO, ItemType } from '../../types';

const item = (type: ItemType): ItemSummaryDTO => ({ id: '42', title: 'Rock Guitarra', imageUrl: '', type });

describe('navigateToItem', () => {
  const setup = () => ({ navigate: jest.fn() }) as any;

  it('lleva las canciones a SongScreen', () => {
    const navigation = setup();
    navigateToItem(navigation, item(ItemType.SONG));
    expect(navigation.navigate).toHaveBeenCalledWith('SongScreen', { songId: '42' });
  });

  it('lleva los géneros a GenreMainScreen', () => {
    const navigation = setup();
    navigateToItem(navigation, item(ItemType.GENRE));
    expect(navigation.navigate).toHaveBeenCalledWith('GenreMainScreen', { genreId: '42' });
  });

  it('lleva los géneros-instrumento a su vista de lecciones con el título como nombre', () => {
    const navigation = setup();
    navigateToItem(navigation, item(ItemType.INSTRUMENT_GENRE));
    expect(navigation.navigate).toHaveBeenCalledWith('InstrumentGenreLesson', {
      instrumentGenreId: '42',
      instrumentName: 'Rock Guitarra',
    });
  });

  it('lleva las lecciones a LessonScreen', () => {
    const navigation = setup();
    navigateToItem(navigation, item(ItemType.LESSON));
    expect(navigation.navigate).toHaveBeenCalledWith('LessonScreen', { lessonId: '42' });
  });

  it('no navega con un tipo desconocido', () => {
    const navigation = setup();
    navigateToItem(navigation, item('Otro' as ItemType));
    expect(navigation.navigate).not.toHaveBeenCalled();
  });
});
