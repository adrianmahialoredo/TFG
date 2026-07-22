import { CarouselItemDTO } from '../../types';
import GenericCarousel from './GenericCarousel';

type ListCarouselProps = {
  data: CarouselItemDTO[];
  onItemPress?: (itemId: string) => void;
};

/**
 * Carrusel especializado para Listas
 * Usa GenericCarousel con tamaño medio y sin mostrar artista
 */
export default function ListCarousel({ data, onItemPress }: ListCarouselProps) {
  return (
    <GenericCarousel
      title=""
      data={data}
      size="medium"
      showArtist={false}
      onItemPress={onItemPress}
    />
  );
}