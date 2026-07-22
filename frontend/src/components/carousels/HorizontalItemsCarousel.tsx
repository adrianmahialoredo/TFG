import { CarouselItemDTO } from '../../types';
import GenericCarousel from './GenericCarousel';

type HorizontalItemsCarouselProps = {
  title: string;
  data: CarouselItemDTO[];
  onItemPress?: (itemId: string) => void;
};

/**
 * Carrusel horizontal genérico para items variados
 * Usa GenericCarousel con tamaño grande para mayor visibilidad
 */
export default function HorizontalItemsCarousel({
  title,
  data,
  onItemPress,
}: HorizontalItemsCarouselProps) {
  return (
    <GenericCarousel
      title={title}
      data={data}
      size="large"
      showArtist={false}
      onItemPress={onItemPress}
    />
  );
}
