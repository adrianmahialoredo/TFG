import { useNavigation, NavigationProp } from '@react-navigation/native';
import { CarouselItemDTO, RootStackParamList } from '../../types';
import GenericCarousel from './GenericCarousel';

type LessonsCarouselProps = {
  title: string;
  data: CarouselItemDTO[];
};

/**
 * Carrusel especializado para Lecciones Generales
 * Usa GenericCarousel con tamaño pequeño y sin artista
 */
export default function GeneralLessonsCarousel({ title, data }: LessonsCarouselProps) {
  const navigation = useNavigation<NavigationProp<RootStackParamList>>();

  return (
    <GenericCarousel
      title={title}
      data={data}
      size="small"
      showArtist={false}
      onItemPress={(lessonId) => navigation.navigate('LessonScreen', { lessonId })}
    />
  );
}
