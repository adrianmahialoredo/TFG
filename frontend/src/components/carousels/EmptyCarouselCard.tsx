import { View, Text } from 'react-native';
import { GOLD_CARD_STYLE } from '../../constants/Theme';

type EmptyCarouselCardProps = {
  message: string;
  /** Alto mínimo: el de las portadas del carrusel, para que la pantalla no salte al llegar datos */
  minHeight: number;
};

/**
 * Card dorada transparente que ocupa el hueco de un carrusel vacío
 * (p.ej. "Recientes" la primera vez que se entra a la app).
 */
export default function EmptyCarouselCard({ message, minHeight }: EmptyCarouselCardProps) {
  return (
    <View
      className="px-6 justify-center"
      style={[GOLD_CARD_STYLE, { minHeight }]}
    >
      <Text className="text-primary text-base text-center leading-snug">{message}</Text>
    </View>
  );
}
