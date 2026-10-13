import { NavigationProp } from '@react-navigation/native';
import { ItemSummaryDTO, ItemType, RootStackParamList } from '../types';
/**
 * Navega a la pantalla de detalle adecuada segun el tipo de item polimorfico.
 *
 * Centraliza el "switch" de navegacion para reutilizarlo desde cualquier
 * pantalla que liste items mezclados (Home, Listas, etc.) y evitar duplicar
 * la logica de mapeo `type -> ruta`.
 *
 * @param navigation instancia de navegacion tipada con RootStackParamList.
 * @param item item polimorfico (Song, Lesson, Genre, InstrumentGenre).
 */
export const navigateToItem = (
  navigation: NavigationProp<RootStackParamList>,
  item: ItemSummaryDTO
): void => {
  switch (item.type) {
    case ItemType.SONG:
      navigation.navigate('SongScreen', { songId: item.id });
      break;
    case ItemType.GENRE:
      navigation.navigate('GenreMainScreen', { genreId: item.id });
      break;
    case ItemType.INSTRUMENT_GENRE:
      navigation.navigate('InstrumentGenreScreen', {
        instrumentGenreId: item.id,
        instrumentName: item.title,
      });
      break;
    case ItemType.LESSON:
      navigation.navigate('LessonScreen', { lessonId: item.id });
      break;
  }
};
