import { CarouselItemDTO } from '../../types';
import GenericCarousel from './GenericCarousel';

type HorizontalItemsCarouselProps = {
  title: string;
  data: CarouselItemDTO[];
  onItemPress?: (itemId: string) => void;
  emptyMessage?: string;
};

/**
 * Carrusel horizontal genérico para items variados
 * Usa GenericCarousel con tamaño grande para mayor visibilidad
 */
export default function HorizontalItemsCarousel({
  title,
  data,
  onItemPress,
  emptyMessage,
}: HorizontalItemsCarouselProps) {
  return (
    <GenericCarousel
      title={title}
      data={data}
      size="large"
      showArtist={false}
      onItemPress={onItemPress}
      borderSquare={false}
      emptyMessage={emptyMessage}
    />
  );
}
