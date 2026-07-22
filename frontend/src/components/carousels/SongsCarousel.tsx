import { CarouselItemDTO } from '../../types';
import GenericCarousel from './GenericCarousel';

type SongsCarouselProps = {
  title: string;
  data: CarouselItemDTO[];
};

/**
 * Carrusel especializado para Canciones
 * Usa GenericCarousel con configuración predefinida
 */
export default function SongsCarousel({ title, data }: SongsCarouselProps) {
  return (
    <GenericCarousel
      title={title}
      data={data}
      size="medium"
      showArtist={true}
    />
  );
}




