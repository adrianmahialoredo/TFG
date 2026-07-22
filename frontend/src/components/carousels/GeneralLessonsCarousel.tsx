import { CarouselItemDTO } from '../../types';
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
  return (
    <GenericCarousel
      title={title}
      data={data}
      size="small"
      showArtist={false}
    />
  );
}
